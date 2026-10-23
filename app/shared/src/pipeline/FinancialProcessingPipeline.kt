package com.anix.android.anixlifetracker.pipeline

import com.anix.android.anixlifetracker.database.AppDatabase
import com.anix.android.anixlifetracker.database.entity.AccountEntity
import com.anix.android.anixlifetracker.database.entity.TransactionEntity
import com.anix.android.anixlifetracker.database.entity.TransactionSourceRecordEntity
import com.anix.android.anixlifetracker.database.entity.UnifiedReviewItemEntity
import com.anix.android.anixlifetracker.model.*
import kotlinx.coroutines.flow.firstOrNull
import kotlin.time.Clock

class FinancialProcessingPipeline(
    private val db: AppDatabase,
    private val categorizationEngine: CategorizationEngine = CategorizationEngine()
) {

    suspend fun processCandidate(
        candidate: TransactionCandidate,
        sourceType: TransactionSourceType,
        sourceIdentifier: String,
        autoSaveHighConfidence: Boolean = true
    ): IngestionResult {
        // Step 1: Source Idempotency Check
        val existingSource = db.transactionSourceRecordDao().getByFingerprint(candidate.sourceFingerprint)
        if (existingSource != null) {
            return IngestionResult.DuplicateSkipped(candidate.sourceFingerprint)
        }

        // Step 2: Cross-Source Payment Reference Matching (e.g. SMS + Notification link)
        val refNo = candidate.transactionReference
        if (!refNo.isNullOrBlank()) {
            val allTransactions = db.transactionDao().observeAllTransactions().firstOrNull() ?: emptyList()
            val existingTx = allTransactions.find { it.transactionReference == refNo }
            if (existingTx != null) {
                val nowMs = Clock.System.now().toEpochMilliseconds()
                val nowIso = Clock.System.now().toString()
                val sourceRecord = TransactionSourceRecordEntity(
                    id = "src_linked_$nowMs",
                    transactionId = existingTx.id,
                    sourceType = sourceType,
                    sourceIdentifier = sourceIdentifier,
                    sourceFingerprint = candidate.sourceFingerprint,
                    rawContentSnippet = candidate.rawContentSnippet,
                    receivedAt = candidate.occurredAt.ifBlank { nowIso },
                    processingStatus = "CONFIRMED_LINKED",
                    createdAt = nowIso
                )
                db.transactionSourceRecordDao().insertSourceRecord(sourceRecord)
                return IngestionResult.Confirmed(existingTx.id)
            }
        }

        // Step 3: Account Association
        val activeAccounts = db.accountDao().observeActiveAccounts().firstOrNull() ?: emptyList()
        val matchedAccount = findMatchingAccount(candidate, activeAccounts)
        val targetAccountId = matchedAccount?.id ?: candidate.suggestedAccountId

        // Step 4: Categorization
        val activeRules = db.categorizationRuleDao().getActiveRules()
        val categoryResult = categorizationEngine.determineCategory(candidate, activeRules)

        val nowMs = Clock.System.now().toEpochMilliseconds()
        val nowIso = Clock.System.now().toString()

        // Step 5: Auto-posting Decision
        if (candidate.parseStatus == ParseConfidence.HIGH_CONFIDENCE &&
            autoSaveHighConfidence &&
            targetAccountId != null
        ) {
            val txId = "tx_pipe_$nowMs"

            val transactionEntity = TransactionEntity(
                id = txId,
                ownerId = "local_user",
                accountId = targetAccountId,
                amountMinor = candidate.amount.amountInCents,
                currencyCode = candidate.amount.currencyCode,
                direction = candidate.direction,
                transactionType = candidate.transactionType,
                categoryId = categoryResult.categoryId,
                merchantName = candidate.merchantName,
                description = candidate.description ?: "Processed Financial Transaction",
                transactionReference = candidate.transactionReference,
                occurredAt = candidate.occurredAt.ifBlank { nowIso },
                localTransactionDate = candidate.localTransactionDate.ifBlank { nowIso.substringBefore("T") },
                status = TransactionState.POSTED,
                transferGroupId = null,
                refundParentTransactionId = null,
                isUserOverridden = false,
                userOverriddenFieldsJson = null,
                processingStatus = "CONFIRMED",
                sourceType = sourceType,
                createdAt = nowIso,
                updatedAt = nowIso,
                deletedAt = null,
                syncStatus = SyncStatus.LOCAL_ONLY
            )

            db.transactionDao().insertTransaction(transactionEntity)

            // Save source provenance
            val sourceRecord = TransactionSourceRecordEntity(
                id = "src_$nowMs",
                transactionId = txId,
                sourceType = sourceType,
                sourceIdentifier = sourceIdentifier,
                sourceFingerprint = candidate.sourceFingerprint,
                rawContentSnippet = candidate.rawContentSnippet,
                receivedAt = candidate.occurredAt.ifBlank { nowIso },
                processingStatus = "CONFIRMED",
                createdAt = nowIso
            )

            db.transactionSourceRecordDao().insertSourceRecord(sourceRecord)

            // Update account balance snapshot if available
            if (candidate.availableBalance != null && matchedAccount != null) {
                db.accountDao().updateAccount(
                    matchedAccount.copy(
                        balanceSnapshotMinor = candidate.availableBalance.amountInCents,
                        balanceSnapshotTimestamp = nowIso,
                        isObservedBalance = true,
                        lastObservationTimestamp = nowIso,
                        updatedAt = nowIso
                    )
                )
            } else if (matchedAccount != null) {
                val newSnapshot = if (candidate.direction == TransactionDirection.CREDIT) {
                    matchedAccount.balanceSnapshotMinor + candidate.amount.amountInCents
                } else {
                    matchedAccount.balanceSnapshotMinor - candidate.amount.amountInCents
                }
                db.accountDao().updateAccount(
                    matchedAccount.copy(
                        balanceSnapshotMinor = newSnapshot,
                        balanceSnapshotTimestamp = nowIso,
                        updatedAt = nowIso
                    )
                )
            }

            return IngestionResult.Confirmed(txId)
        } else {
            // Queue into Unified Review Items
            val reviewItem = UnifiedReviewItemEntity(
                id = "rev_$nowMs",
                ownerId = "local_user",
                candidateId = candidate.id,
                sourceType = sourceType,
                title = candidate.merchantName ?: candidate.description ?: "Financial Transaction",
                subtitle = "${candidate.amount.formattedString()} • ${candidate.parseStatus.name}",
                candidateJson = buildCandidateJson(candidate, targetAccountId, categoryResult.categoryId),
                matchType = if (targetAccountId != null) "ACCOUNT_MATCHED" else "UNMATCHED_ACCOUNT",
                suggestedAction = "CONFIRM_OR_EDIT",
                confidenceScore = candidate.confidenceScore,
                status = "PENDING",
                createdAt = nowIso,
                updatedAt = nowIso
            )

            db.unifiedReviewItemDao().insert(reviewItem)
            return IngestionResult.QueuedForReview(reviewItem.id)
        }
    }

    private fun findMatchingAccount(
        candidate: TransactionCandidate,
        activeAccounts: List<AccountEntity>
    ): AccountEntity? {
        val candidateRef = candidate.accountRefMasked?.takeLast(4) ?: ""
        val candidateInst = candidate.institutionName?.lowercase() ?: ""

        if (candidateRef.isNotBlank()) {
            val refMatch = activeAccounts.find { acc ->
                acc.accountReferenceMasked.contains(candidateRef)
            }
            if (refMatch != null) return refMatch
        }

        if (candidateInst.isNotBlank()) {
            val instMatch = activeAccounts.find { acc ->
                acc.institutionName.lowercase().contains(candidateInst) || candidateInst.contains(acc.institutionName.lowercase())
            }
            if (instMatch != null) return instMatch
        }

        return null
    }

    private fun buildCandidateJson(
        candidate: TransactionCandidate,
        accountId: String?,
        categoryId: String?
    ): String {
        return """{"amount":${candidate.amount.amountInCents},"currency":"${candidate.amount.currencyCode}","direction":"${candidate.direction.name}","type":"${candidate.transactionType.name}","merchant":"${candidate.merchantName ?: ""}","accountRef":"${candidate.accountRefMasked ?: ""}","accountId":"${accountId ?: ""}","categoryId":"${categoryId ?: ""}"}"""
    }
}
