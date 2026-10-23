package com.anix.android.anixlifetracker.notification.engine

import com.anix.android.anixlifetracker.model.*
import com.anix.android.anixlifetracker.usecase.BudgetStatusModel
import com.anix.android.anixlifetracker.usecase.SpendingAnomaly
import kotlin.time.Clock

object FinancialReminderGenerator {

    /**
     * Generates a stable reminder definition and schedule from a confirmed UpcomingObligation.
     */
    fun generateFromObligation(
        obligation: UpcomingObligationModel,
        ownerId: String = "local_user"
    ): Pair<ReminderDefinitionModel, ReminderScheduleModel> {
        val stableId = "rem_obl_${obligation.id}"
        val definition = ReminderDefinitionModel(
            id = stableId,
            ownerId = ownerId,
            title = obligation.title,
            description = "Obligation: ${obligation.obligationType.name}",
            category = ReminderCategory.PAYMENT,
            priority = ReminderPriority.HIGH,
            isEnabled = obligation.status == ObligationStatus.PENDING,
            sourceType = "OBLIGATION",
            sourceEntityId = obligation.id,
            createdAt = obligation.createdAt,
            updatedAt = obligation.updatedAt
        )

        val schedule = ReminderScheduleModel(
            id = "sched_${stableId}",
            reminderId = stableId,
            isRecurring = false,
            localDueDateOrTime = obligation.expectedDate,
            cadence = CadenceType.ONE_OFF,
            dateOnlyPolicy = DateOnlyPolicy.LOCAL_DAY_END,
            leadTimesDays = 2,
            updatedAt = obligation.updatedAt
        )

        return Pair(definition, schedule)
    }

    /**
     * Generates a stable reminder definition and schedule from a RecurringPayment.
     */
    fun generateFromRecurringPayment(
        recurring: RecurringPaymentModel,
        ownerId: String = "local_user"
    ): Pair<ReminderDefinitionModel, ReminderScheduleModel> {
        val stableId = "rem_rec_${recurring.id}"
        val cadence = when (recurring.frequency) {
            RecurrenceFrequency.DAILY -> CadenceType.DAILY
            RecurrenceFrequency.WEEKLY -> CadenceType.WEEKLY
            RecurrenceFrequency.MONTHLY -> CadenceType.MONTHLY
            RecurrenceFrequency.QUARTERLY -> CadenceType.MONTHLY
            RecurrenceFrequency.YEARLY -> CadenceType.YEARLY
            RecurrenceFrequency.CUSTOM -> CadenceType.MONTHLY
        }

        val anchor = recurring.nextDueDate.takeLast(2).toIntOrNull() ?: 1

        val definition = ReminderDefinitionModel(
            id = stableId,
            ownerId = ownerId,
            title = recurring.name,
            description = "Recurring Payment",
            category = ReminderCategory.PAYMENT,
            priority = ReminderPriority.MEDIUM,
            isEnabled = recurring.isActive,
            sourceType = "RECURRING_PAYMENT",
            sourceEntityId = recurring.id,
            createdAt = recurring.createdAt,
            updatedAt = recurring.updatedAt
        )

        val schedule = ReminderScheduleModel(
            id = "sched_${stableId}",
            reminderId = stableId,
            isRecurring = true,
            localDueDateOrTime = recurring.nextDueDate,
            cadence = cadence,
            anchorDay = anchor,
            dateOnlyPolicy = DateOnlyPolicy.LOCAL_DAY_END,
            leadTimesDays = 3,
            updatedAt = recurring.updatedAt
        )

        return Pair(definition, schedule)
    }

    /**
     * Generates an InAppInboxItem for budget threshold crossings.
     */
    fun generateBudgetInsight(
        budgetStatus: BudgetStatusModel,
        ownerId: String = "local_user",
        nowISO: String = Clock.System.now().toString()
    ): InAppInboxItemModel? {
        val pct = budgetStatus.percentageUsed
        if (pct < 80.0) return null

        val (severity, reason) = if (budgetStatus.isOverbudget) {
            Pair(InboxSeverity.CRITICAL, "Budget limit exceeded by ${budgetStatus.remainingAmount.formattedString()}. Spent ${pct.toInt()}% of cap.")
        } else {
            Pair(InboxSeverity.WARNING, "Budget reached ${pct.toInt()}% threshold. ${budgetStatus.remainingAmount.formattedString()} remaining.")
        }

        val stableId = "inbox_bud_${budgetStatus.budget.id}_${nowISO.take(7)}" // Cooldown per month

        return InAppInboxItemModel(
            id = stableId,
            ownerId = ownerId,
            linkedEntityType = "BUDGET",
            linkedEntityId = budgetStatus.budget.id,
            title = "Budget Alert: ${budgetStatus.budget.name}",
            subtitle = reason,
            severity = severity,
            reason = reason,
            freshnessISO = nowISO,
            createdAt = nowISO,
            updatedAt = nowISO
        )
    }

    /**
     * Generates an InAppInboxItem for spending anomalies.
     */
    fun generateAnomalyInsight(
        anomaly: SpendingAnomaly,
        ownerId: String = "local_user",
        nowISO: String = Clock.System.now().toString()
    ): InAppInboxItemModel {
        val stableId = "inbox_${anomaly.id}"
        return InAppInboxItemModel(
            id = stableId,
            ownerId = ownerId,
            linkedEntityType = "TRANSACTION_ANOMALY",
            linkedEntityId = anomaly.id,
            title = "Unusual Spending: ${anomaly.merchantName}",
            subtitle = anomaly.anomalyDescription,
            severity = InboxSeverity.WARNING,
            reason = "Amount ${anomaly.actualAmount.formattedString()} is baseline deviation.",
            freshnessISO = anomaly.detectedAtISO,
            createdAt = nowISO,
            updatedAt = nowISO
        )
    }
}
