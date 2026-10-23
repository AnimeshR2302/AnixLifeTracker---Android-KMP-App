package com.anix.android.anixlifetracker.repository

import com.anix.android.anixlifetracker.database.AppDatabase
import com.anix.android.anixlifetracker.database.entity.toEntity
import com.anix.android.anixlifetracker.database.entity.toModel
import com.anix.android.anixlifetracker.model.*
import com.anix.android.anixlifetracker.notification.collector.NotificationCollector
import com.anix.android.anixlifetracker.pipeline.FinancialProcessingPipeline
import com.anix.android.anixlifetracker.sms.parser.DeterministicFinancialMessageParser
import com.anix.android.anixlifetracker.sms.parser.FinancialMessageParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

class NotificationRepositoryImpl(
    private val db: AppDatabase,
    private val parser: FinancialMessageParser = DeterministicFinancialMessageParser(),
    private val collector: NotificationCollector = NotificationCollector(),
    private val pipeline: FinancialProcessingPipeline = FinancialProcessingPipeline(db)
) : NotificationRepository {

    override fun observeSettings(): Flow<NotificationSettingsModel> {
        val settingsFlow = db.notificationSettingsDao().observeSettings()
        val appConfigsFlow = db.notificationAppConfigDao().observeAppConfigs()

        return combine(settingsFlow, appConfigsFlow) { settingsEntity, configs ->
            val enabledPkgs = configs.filter { it.isEnabled }.map { it.packageName }.toSet()
            settingsEntity?.toModel(enabledPkgs) ?: NotificationSettingsModel(enabledPackageNames = enabledPkgs)
        }
    }

    override suspend fun getSettings(): NotificationSettingsModel {
        val settingsEntity = db.notificationSettingsDao().getSettings()
        val configs = db.notificationAppConfigDao().getAllAppConfigs()
        val enabledPkgs = configs.filter { it.isEnabled }.map { it.packageName }.toSet()
        return settingsEntity?.toModel(enabledPkgs) ?: NotificationSettingsModel(enabledPackageNames = enabledPkgs)
    }

    override suspend fun updateSettings(settings: NotificationSettingsModel) {
        db.notificationSettingsDao().insertOrUpdate(settings.toEntity())
    }

    override fun observeSupportedApps(): Flow<List<FinancialAppProfile>> {
        return db.notificationAppConfigDao().observeAppConfigs().map { configs ->
            val installedApps = collector.getInstalledSupportedApps()
            val configMap = configs.associateBy { it.packageName }

            installedApps.map { installedApp ->
                val cfg = configMap[installedApp.packageName]
                if (cfg != null) {
                    installedApp.copy(
                        isEnabled = cfg.isEnabled,
                        lastProcessedAt = cfg.lastProcessedAt
                    )
                } else {
                    installedApp
                }
            }
        }
    }

    override suspend fun getSupportedApps(): List<FinancialAppProfile> {
        val configs = db.notificationAppConfigDao().getAllAppConfigs().associateBy { it.packageName }
        val installedApps = collector.getInstalledSupportedApps()

        return installedApps.map { app ->
            val cfg = configs[app.packageName]
            if (cfg != null) {
                app.copy(isEnabled = cfg.isEnabled, lastProcessedAt = cfg.lastProcessedAt)
            } else {
                app
            }
        }
    }

    override suspend fun setAppEnabledStatus(packageName: String, isEnabled: Boolean) {
        val existing = db.notificationAppConfigDao().getByPackageName(packageName)
        val supported = collector.getInstalledSupportedApps().find { it.packageName == packageName }
        val displayName = existing?.displayName ?: supported?.displayName ?: packageName
        val parserProfileId = existing?.parserProfileId ?: supported?.parserProfileId ?: "default_upi_bank"

        val updatedConfig = com.anix.android.anixlifetracker.database.entity.NotificationAppConfigEntity(
            packageName = packageName,
            displayName = displayName,
            isEnabled = isEnabled,
            parserProfileId = parserProfileId,
            lastProcessedAt = existing?.lastProcessedAt,
            updatedAt = Clock.System.now().toString()
        )
        db.notificationAppConfigDao().insertOrUpdate(updatedConfig)
    }

    override fun observePendingReviewCandidates(): Flow<List<NotificationReviewCandidateModel>> {
        return db.notificationReviewCandidateDao().observePendingCandidates().map { list ->
            list.map { it.toModel() }
        }
    }

    override suspend fun getPendingReviewCandidates(): List<NotificationReviewCandidateModel> {
        val pending = db.notificationReviewCandidateDao().observePendingCandidates().firstOrNull() ?: emptyList()
        return pending.map { it.toModel() }
    }

    override suspend fun ingestNotification(notification: FinancialNotification): IngestionResult {
        val settings = getSettings()
        if (!settings.isNotificationFeatureEnabled || !settings.isConsentGiven || !settings.liveMonitoringEnabled) {
            return IngestionResult.Rejected("Notification tracking feature disabled or consent not granted")
        }

        // Allowlist check
        val appConfig = db.notificationAppConfigDao().getByPackageName(notification.sourcePackage)
        if (appConfig == null || !appConfig.isEnabled) {
            return IngestionResult.Rejected("Notification package '${notification.sourcePackage}' is not enabled in allowlist")
        }

        val rawMessage = FinancialMessage(
            id = notification.notificationIdentifier ?: "notif_${notification.postedAt}",
            sender = notification.sourcePackage,
            body = "${notification.title ?: ""} ${notification.content ?: ""}".trim(),
            timestampMillis = notification.postedAt
        )

        val parseResult = parser.parse(rawMessage)
        if (parseResult.status == ParseConfidence.REJECTED || parseResult.status == ParseConfidence.UNRECOGNIZED) {
            return IngestionResult.Rejected(parseResult.rejectionReason ?: "Notification content not recognized as financial transaction")
        }

        val candidate = parseResult.candidate
            ?: return IngestionResult.Failed("Parser produced null candidate")

        return ingestCandidate(candidate, notification.sourcePackage)
    }

    override suspend fun ingestCandidate(candidate: TransactionCandidate, sourcePackage: String): IngestionResult {
        val settings = getSettings()
        val result = pipeline.processCandidate(
            candidate = candidate,
            sourceType = TransactionSourceType.NOTIFICATION,
            sourceIdentifier = sourcePackage,
            autoSaveHighConfidence = settings.autoSaveHighConfidence
        )
        if (result is IngestionResult.Confirmed) {
            updateSettings(settings.copy(totalImportedCount = settings.totalImportedCount + 1))
            val appConfig = db.notificationAppConfigDao().getByPackageName(sourcePackage)
            if (appConfig != null) {
                db.notificationAppConfigDao().insertOrUpdate(
                    appConfig.copy(lastProcessedAt = Clock.System.now().toString())
                )
            }
        }
        return result
    }

    override suspend fun confirmReviewCandidate(
        candidateId: String,
        correctedCandidate: TransactionCandidate
    ): IngestionResult {
        val reviewCandidate = db.notificationReviewCandidateDao().getCandidateById(candidateId)
            ?: return IngestionResult.Failed("Notification review candidate not found")

        val result = ingestCandidate(correctedCandidate, reviewCandidate.sourcePackage)
        db.notificationReviewCandidateDao().deleteCandidateById(candidateId)
        return result
    }

    override suspend fun rejectReviewCandidate(candidateId: String) {
        db.notificationReviewCandidateDao().deleteCandidateById(candidateId)
    }

    override suspend fun clearPendingReviewQueue() {
        db.notificationReviewCandidateDao().clearPendingCandidates()
    }
}
