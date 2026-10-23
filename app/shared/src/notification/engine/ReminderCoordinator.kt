package com.anix.android.anixlifetracker.notification.engine

import com.anix.android.anixlifetracker.database.AppDatabase
import com.anix.android.anixlifetracker.database.entity.toDefinitionModel
import com.anix.android.anixlifetracker.database.entity.toEntity
import com.anix.android.anixlifetracker.database.entity.toModel
import com.anix.android.anixlifetracker.model.OccurrenceState
import com.anix.android.anixlifetracker.repository.InAppInboxRepository
import com.anix.android.anixlifetracker.repository.InAppInboxRepositoryImpl
import com.anix.android.anixlifetracker.repository.ReminderRepository
import com.anix.android.anixlifetracker.repository.ReminderRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlin.time.Clock

interface ReminderCoordinator {
    suspend fun reconcileReminders(ownerId: String = "local_user")
    suspend fun handleOccurrenceAction(
        occurrenceId: String,
        actionType: com.anix.android.anixlifetracker.model.ReminderActionType,
        ownerId: String = "local_user"
    )
}

class DefaultReminderCoordinator(
    private val db: AppDatabase,
    private val reminderRepository: ReminderRepository = ReminderRepositoryImpl(db),
    private val inboxRepository: InAppInboxRepository = InAppInboxRepositoryImpl(db),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : ReminderCoordinator {

    override suspend fun reconcileReminders(ownerId: String) {
        val nowIso = Clock.System.now().toString()

        // 1. Process confirmed Obligations -> Reminder definitions & occurrences
        val obligations = db.upcomingObligationDao().observePendingObligations().firstOrNull() ?: emptyList()
        obligations.forEach { oblEntity ->
            val oblModel = oblEntity.toModel()
            val (def, sched) = FinancialReminderGenerator.generateFromObligation(oblModel, ownerId)

            val existingDef = db.reminderDao().getReminderById(def.id)
            if (existingDef == null) {
                db.reminderDao().insertReminder(def.toEntity(dueDate = oblModel.expectedDate))
                db.reminderScheduleDao().insertSchedule(sched.toEntity())

                val occurrences = ReminderScheduleCalculator.calculateOccurrences(def, sched, referenceInstantISO = nowIso)
                occurrences.forEach { occ ->
                    db.reminderOccurrenceDao().insertOccurrence(occ.toEntity())
                }
            }
        }

        // 2. Process Recurring Payments -> Reminder definitions & occurrences
        val recurring = db.recurringPaymentDao().observeActiveRecurringPayments().firstOrNull() ?: emptyList()
        recurring.forEach { recEntity ->
            val recModel = recEntity.toModel()
            val (def, sched) = FinancialReminderGenerator.generateFromRecurringPayment(recModel, ownerId)

            val existingDef = db.reminderDao().getReminderById(def.id)
            if (existingDef == null) {
                db.reminderDao().insertReminder(def.toEntity(dueDate = recModel.nextDueDate))
                db.reminderScheduleDao().insertSchedule(sched.toEntity())

                val occurrences = ReminderScheduleCalculator.calculateOccurrences(def, sched, referenceInstantISO = nowIso)
                occurrences.forEach { occ ->
                    db.reminderOccurrenceDao().insertOccurrence(occ.toEntity())
                }
            }
        }

        // 3. Re-evaluate existing custom definitions & schedules
        val definitions = db.reminderDao().observeAllReminders().firstOrNull() ?: emptyList()
        definitions.forEach { defEntity ->
            val def = defEntity.toDefinitionModel()
            val schedEntity = db.reminderScheduleDao().getScheduleForReminder(def.id)
            if (schedEntity != null) {
                val sched = schedEntity.toModel()
                val occurrences = ReminderScheduleCalculator.calculateOccurrences(def, sched, referenceInstantISO = nowIso)
                occurrences.forEach { occ ->
                    val existingOcc = db.reminderOccurrenceDao().getOccurrenceById(occ.id)
                    if (existingOcc == null) {
                        db.reminderOccurrenceDao().insertOccurrence(occ.toEntity())
                    }
                }
            }
        }
    }

    override suspend fun handleOccurrenceAction(
        occurrenceId: String,
        actionType: com.anix.android.anixlifetracker.model.ReminderActionType,
        ownerId: String
    ) {
        val nowIso = Clock.System.now().toString()
        val occEntity = db.reminderOccurrenceDao().getOccurrenceById(occurrenceId) ?: return

        when (actionType) {
            com.anix.android.anixlifetracker.model.ReminderActionType.COMPLETE -> {
                db.reminderOccurrenceDao().updateOccurrenceState(occurrenceId, OccurrenceState.COMPLETED, nowIso)
            }
            com.anix.android.anixlifetracker.model.ReminderActionType.SKIP -> {
                db.reminderOccurrenceDao().updateOccurrenceState(occurrenceId, OccurrenceState.SKIPPED, nowIso)
            }
            com.anix.android.anixlifetracker.model.ReminderActionType.SNOOZE -> {
                val updated = occEntity.copy(
                    snoozeUntilISO = nowIso,
                    updatedAt = nowIso
                )
                db.reminderOccurrenceDao().updateOccurrence(updated)
            }
            com.anix.android.anixlifetracker.model.ReminderActionType.DISMISS -> {
                db.reminderOccurrenceDao().updateOccurrenceState(occurrenceId, OccurrenceState.SKIPPED, nowIso)
            }
            com.anix.android.anixlifetracker.model.ReminderActionType.UNDO -> {
                db.reminderOccurrenceDao().updateOccurrenceState(occurrenceId, OccurrenceState.DUE, nowIso)
            }
        }

        // Record action for sync outbox tracking
        val actionEntity = com.anix.android.anixlifetracker.database.entity.ReminderActionEntity(
            id = "act_${occurrenceId}_${Clock.System.now().toEpochMilliseconds()}",
            occurrenceId = occurrenceId,
            reminderId = occEntity.reminderId,
            ownerId = ownerId,
            actionType = actionType,
            effectiveTimeISO = nowIso,
            baseRevision = occEntity.scheduleVersion,
            syncStatus = com.anix.android.anixlifetracker.model.SyncStatus.PENDING_UPLOAD,
            conflictState = null
        )
        db.reminderActionDao().insertAction(actionEntity)
    }
}
