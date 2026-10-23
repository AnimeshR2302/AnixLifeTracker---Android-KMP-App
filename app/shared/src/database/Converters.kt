package com.anix.android.anixlifetracker.database

import androidx.room.TypeConverter
import com.anix.android.anixlifetracker.model.*

class Converters {

    @TypeConverter
    fun fromAccountCategoryType(value: AccountCategoryType): String = value.name

    @TypeConverter
    fun toAccountCategoryType(value: String): AccountCategoryType = enumValueOfOrDefault(value, AccountCategoryType.OTHER)

    @TypeConverter
    fun fromTransactionDirection(value: TransactionDirection): String = value.name

    @TypeConverter
    fun toTransactionDirection(value: String): TransactionDirection = enumValueOfOrDefault(value, TransactionDirection.DEBIT)

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = enumValueOfOrDefault(value, TransactionType.EXPENSE)

    @TypeConverter
    fun fromTransactionState(value: TransactionState): String = value.name

    @TypeConverter
    fun toTransactionState(value: String): TransactionState = enumValueOfOrDefault(value, TransactionState.POSTED)

    @TypeConverter
    fun fromTransactionSourceType(value: TransactionSourceType): String = value.name

    @TypeConverter
    fun toTransactionSourceType(value: String): TransactionSourceType = enumValueOfOrDefault(value, TransactionSourceType.MANUAL)

    @TypeConverter
    fun fromCategoryKind(value: CategoryKind): String = value.name

    @TypeConverter
    fun toCategoryKind(value: String): CategoryKind = enumValueOfOrDefault(value, CategoryKind.EXPENSE)

    @TypeConverter
    fun fromBudgetPeriodType(value: BudgetPeriodType): String = value.name

    @TypeConverter
    fun toBudgetPeriodType(value: String): BudgetPeriodType = enumValueOfOrDefault(value, BudgetPeriodType.MONTHLY)

    @TypeConverter
    fun fromRecurrenceFrequency(value: RecurrenceFrequency): String = value.name

    @TypeConverter
    fun toRecurrenceFrequency(value: String): RecurrenceFrequency = enumValueOfOrDefault(value, RecurrenceFrequency.MONTHLY)

    @TypeConverter
    fun fromObligationType(value: ObligationType): String = value.name

    @TypeConverter
    fun toObligationType(value: String): ObligationType = enumValueOfOrDefault(value, ObligationType.SCHEDULED_EXPENSE)

    @TypeConverter
    fun fromObligationStatus(value: ObligationStatus): String = value.name

    @TypeConverter
    fun toObligationStatus(value: String): ObligationStatus = enumValueOfOrDefault(value, ObligationStatus.PENDING)

    @TypeConverter
    fun fromInvestmentAccountType(value: InvestmentAccountType): String = value.name

    @TypeConverter
    fun toInvestmentAccountType(value: String): InvestmentAccountType = enumValueOfOrDefault(value, InvestmentAccountType.BROKER)

    @TypeConverter
    fun fromInstrumentType(value: InstrumentType): String = value.name

    @TypeConverter
    fun toInstrumentType(value: String): InstrumentType = enumValueOfOrDefault(value, InstrumentType.MUTUAL_FUND)

    @TypeConverter
    fun fromInvestmentTransactionType(value: InvestmentTransactionType): String = value.name

    @TypeConverter
    fun toInvestmentTransactionType(value: String): InvestmentTransactionType = enumValueOfOrDefault(value, InvestmentTransactionType.BUY)

    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String = value.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = enumValueOfOrDefault(value, SyncStatus.LOCAL_ONLY)

    @TypeConverter
    fun fromReminderCategory(value: ReminderCategory): String = value.name

    @TypeConverter
    fun toReminderCategory(value: String): ReminderCategory = enumValueOfOrDefault(value, ReminderCategory.PERSONAL)

    @TypeConverter
    fun fromReminderPriority(value: ReminderPriority): String = value.name

    @TypeConverter
    fun toReminderPriority(value: String): ReminderPriority = enumValueOfOrDefault(value, ReminderPriority.MEDIUM)

    @TypeConverter
    fun fromParseConfidence(value: ParseConfidence): String = value.name

    @TypeConverter
    fun toParseConfidence(value: String): ParseConfidence = enumValueOfOrDefault(value, ParseConfidence.NEEDS_REVIEW)

    @TypeConverter
    fun fromDateOnlyPolicy(value: DateOnlyPolicy): String = value.name

    @TypeConverter
    fun toDateOnlyPolicy(value: String): DateOnlyPolicy = enumValueOfOrDefault(value, DateOnlyPolicy.LOCAL_DAY_END)

    @TypeConverter
    fun fromCadenceType(value: CadenceType): String = value.name

    @TypeConverter
    fun toCadenceType(value: String): CadenceType = enumValueOfOrDefault(value, CadenceType.ONE_OFF)

    @TypeConverter
    fun fromMissedDatePolicy(value: MissedDatePolicy): String = value.name

    @TypeConverter
    fun toMissedDatePolicy(value: String): MissedDatePolicy = enumValueOfOrDefault(value, MissedDatePolicy.GENERATE_LATEST)

    @TypeConverter
    fun fromOccurrenceState(value: OccurrenceState): String = value.name

    @TypeConverter
    fun toOccurrenceState(value: String): OccurrenceState = enumValueOfOrDefault(value, OccurrenceState.UPCOMING)

    @TypeConverter
    fun fromReminderActionType(value: ReminderActionType): String = value.name

    @TypeConverter
    fun toReminderActionType(value: String): ReminderActionType = enumValueOfOrDefault(value, ReminderActionType.COMPLETE)

    @TypeConverter
    fun fromInboxSeverity(value: InboxSeverity): String = value.name

    @TypeConverter
    fun toInboxSeverity(value: String): InboxSeverity = enumValueOfOrDefault(value, InboxSeverity.INFO)

    private inline fun <reified T : Enum<T>> enumValueOfOrDefault(value: String, default: T): T {
        return try {
            enumValueOf<T>(value)
        } catch (_: Exception) {
            default
        }
    }
}
