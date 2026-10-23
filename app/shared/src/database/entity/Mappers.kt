package com.anix.android.anixlifetracker.database.entity

import com.anix.android.anixlifetracker.model.*

fun AccountEntity.toModel(): AccountModel = AccountModel(
    id = id,
    ownerId = ownerId,
    accountName = accountName,
    accountType = accountType,
    institutionName = institutionName,
    accountReferenceMasked = accountReferenceMasked,
    currencyCode = currencyCode,
    openingBalance = Money(openingBalanceMinor, currencyCode),
    balanceSnapshot = Money(balanceSnapshotMinor, currencyCode),
    balanceSnapshotTimestamp = balanceSnapshotTimestamp,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus
)

fun AccountModel.toEntity(): AccountEntity = AccountEntity(
    id = id,
    ownerId = ownerId,
    accountName = accountName,
    accountType = accountType,
    institutionName = institutionName,
    accountReferenceMasked = accountReferenceMasked,
    currencyCode = currencyCode,
    openingBalanceMinor = openingBalance.amountInCents,
    balanceSnapshotMinor = balanceSnapshot.amountInCents,
    balanceSnapshotTimestamp = balanceSnapshotTimestamp,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus
)

fun TransactionEntity.toModel(categoryName: String? = null): TransactionModel = TransactionModel(
    id = id,
    ownerId = ownerId,
    accountId = accountId,
    amount = Money(amountMinor, currencyCode),
    direction = direction,
    transactionType = transactionType,
    categoryId = categoryId,
    categoryName = categoryName,
    merchantName = merchantName,
    description = description,
    transactionReference = transactionReference,
    occurredAt = occurredAt,
    localTransactionDate = localTransactionDate,
    status = status,
    transferGroupId = transferGroupId,
    sourceType = sourceType,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    syncStatus = syncStatus
)

fun TransactionModel.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    ownerId = ownerId,
    accountId = accountId,
    amountMinor = amount.amountInCents,
    currencyCode = amount.currencyCode,
    direction = direction,
    transactionType = transactionType,
    categoryId = categoryId,
    merchantName = merchantName,
    description = description,
    transactionReference = transactionReference,
    occurredAt = occurredAt,
    localTransactionDate = localTransactionDate,
    status = status,
    transferGroupId = transferGroupId,
    sourceType = sourceType,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    syncStatus = syncStatus
)

fun CategoryEntity.toModel(): TransactionCategoryModel = TransactionCategoryModel(
    id = id,
    ownerId = ownerId,
    name = name,
    parentCategoryId = parentCategoryId,
    categoryType = categoryType,
    iconIdentifier = iconIdentifier,
    isSystemCategory = isSystemCategory,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun TransactionCategoryModel.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    ownerId = ownerId,
    name = name,
    parentCategoryId = parentCategoryId,
    categoryType = categoryType,
    iconIdentifier = iconIdentifier,
    isSystemCategory = isSystemCategory,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun BudgetEntity.toModel(): BudgetModel = BudgetModel(
    id = id,
    ownerId = ownerId,
    name = name,
    categoryId = categoryId,
    limitAmount = Money(limitAmountMinor, currencyCode),
    periodType = periodType,
    startDate = startDate,
    endDate = endDate,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun BudgetModel.toEntity(): BudgetEntity = BudgetEntity(
    id = id,
    ownerId = ownerId,
    name = name,
    categoryId = categoryId,
    limitAmountMinor = limitAmount.amountInCents,
    currencyCode = limitAmount.currencyCode,
    periodType = periodType,
    startDate = startDate,
    endDate = endDate,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun RecurringPaymentEntity.toModel(): RecurringPaymentModel = RecurringPaymentModel(
    id = id,
    ownerId = ownerId,
    accountId = accountId,
    name = name,
    categoryId = categoryId,
    expectedAmount = Money(expectedAmountMinor, currencyCode),
    frequency = frequency,
    startDate = startDate,
    nextDueDate = nextDueDate,
    endDate = endDate,
    isVariableAmount = isVariableAmount,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun RecurringPaymentModel.toEntity(): RecurringPaymentEntity = RecurringPaymentEntity(
    id = id,
    ownerId = ownerId,
    accountId = accountId,
    name = name,
    categoryId = categoryId,
    expectedAmountMinor = expectedAmount.amountInCents,
    currencyCode = expectedAmount.currencyCode,
    frequency = frequency,
    startDate = startDate,
    nextDueDate = nextDueDate,
    endDate = endDate,
    isVariableAmount = isVariableAmount,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun UpcomingObligationEntity.toModel(): UpcomingObligationModel = UpcomingObligationModel(
    id = id,
    ownerId = ownerId,
    accountId = accountId,
    recurringPaymentId = recurringPaymentId,
    title = title,
    expectedAmount = Money(expectedAmountMinor, currencyCode),
    expectedDate = expectedDate,
    obligationType = obligationType,
    status = status,
    linkedTransactionId = linkedTransactionId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun UpcomingObligationModel.toEntity(): UpcomingObligationEntity = UpcomingObligationEntity(
    id = id,
    ownerId = ownerId,
    accountId = accountId,
    recurringPaymentId = recurringPaymentId,
    title = title,
    expectedAmountMinor = expectedAmount.amountInCents,
    currencyCode = expectedAmount.currencyCode,
    expectedDate = expectedDate,
    obligationType = obligationType,
    status = status,
    linkedTransactionId = linkedTransactionId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun InvestmentAccountEntity.toModel(): InvestmentAccountModel = InvestmentAccountModel(
    id = id,
    ownerId = ownerId,
    name = name,
    provider = provider,
    accountType = accountType,
    currencyCode = currencyCode,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun InvestmentAccountModel.toEntity(): InvestmentAccountEntity = InvestmentAccountEntity(
    id = id,
    ownerId = ownerId,
    name = name,
    provider = provider,
    accountType = accountType,
    currencyCode = currencyCode,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun InvestmentInstrumentEntity.toModel(): InvestmentInstrumentModel = InvestmentInstrumentModel(
    id = id,
    instrumentType = instrumentType,
    name = name,
    symbol = symbol,
    isin = isin,
    amfiCode = amfiCode,
    exchange = exchange,
    currencyCode = currencyCode,
    isActive = isActive
)

fun InvestmentInstrumentModel.toEntity(): InvestmentInstrumentEntity = InvestmentInstrumentEntity(
    id = id,
    instrumentType = instrumentType,
    name = name,
    symbol = symbol,
    isin = isin,
    amfiCode = amfiCode,
    exchange = exchange,
    currencyCode = currencyCode,
    isActive = isActive
)

fun InvestmentTransactionEntity.toModel(): InvestmentTransactionModel = InvestmentTransactionModel(
    id = id,
    ownerId = ownerId,
    investmentAccountId = investmentAccountId,
    instrumentId = instrumentId,
    transactionType = transactionType,
    quantityFixedPoint = quantityFixedPoint,
    price = Money(priceMinor, currencyCode),
    grossAmount = Money(grossAmountMinor, currencyCode),
    fees = Money(feesMinor, currencyCode),
    taxes = Money(taxesMinor, currencyCode),
    transactionDate = transactionDate,
    linkedFinancialTransactionId = linkedFinancialTransactionId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun InvestmentTransactionModel.toEntity(): InvestmentTransactionEntity = InvestmentTransactionEntity(
    id = id,
    ownerId = ownerId,
    investmentAccountId = investmentAccountId,
    instrumentId = instrumentId,
    transactionType = transactionType,
    quantityFixedPoint = quantityFixedPoint,
    priceMinor = price.amountInCents,
    currencyCode = price.currencyCode,
    grossAmountMinor = grossAmount.amountInCents,
    feesMinor = fees.amountInCents,
    taxesMinor = taxes.amountInCents,
    transactionDate = transactionDate,
    linkedFinancialTransactionId = linkedFinancialTransactionId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun InvestmentHoldingEntity.toModel(): InvestmentHoldingDetailModel = InvestmentHoldingDetailModel(
    id = id,
    ownerId = ownerId,
    investmentAccountId = investmentAccountId,
    instrumentId = instrumentId,
    totalUnitsFixedPoint = totalUnitsFixedPoint,
    averageAcquisitionPrice = Money(averageAcquisitionPriceMinor, currencyCode),
    currentValue = Money(currentValueMinor, currencyCode),
    unrealizedGain = Money(unrealizedGainMinor, currencyCode),
    realizedGain = Money(realizedGainMinor, currencyCode),
    lastCalculatedAt = lastCalculatedAt
)

fun InvestmentHoldingDetailModel.toEntity(): InvestmentHoldingEntity = InvestmentHoldingEntity(
    id = id,
    ownerId = ownerId,
    investmentAccountId = investmentAccountId,
    instrumentId = instrumentId,
    totalUnitsFixedPoint = totalUnitsFixedPoint,
    averageAcquisitionPriceMinor = averageAcquisitionPrice.amountInCents,
    currentValueMinor = currentValue.amountInCents,
    currencyCode = currentValue.currencyCode,
    unrealizedGainMinor = unrealizedGain.amountInCents,
    realizedGainMinor = realizedGain.amountInCents,
    lastCalculatedAt = lastCalculatedAt
)

fun SIPScheduleEntity.toModel(): SIPScheduleModel = SIPScheduleModel(
    id = id,
    ownerId = ownerId,
    investmentAccountId = investmentAccountId,
    instrumentId = instrumentId,
    contributionAmount = Money(contributionAmountMinor, currencyCode),
    frequency = frequency,
    startDate = startDate,
    nextDueDate = nextDueDate,
    endDate = endDate,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun SIPScheduleModel.toEntity(): SIPScheduleEntity = SIPScheduleEntity(
    id = id,
    ownerId = ownerId,
    investmentAccountId = investmentAccountId,
    instrumentId = instrumentId,
    contributionAmountMinor = contributionAmount.amountInCents,
    currencyCode = contributionAmount.currencyCode,
    frequency = frequency,
    startDate = startDate,
    nextDueDate = nextDueDate,
    endDate = endDate,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun MarketPriceEntity.toModel(): MarketPriceModel = MarketPriceModel(
    instrumentId = instrumentId,
    priceDate = priceDate,
    price = Money(priceMinor, currencyCode),
    source = source,
    fetchedAt = fetchedAt
)

fun MarketPriceModel.toEntity(): MarketPriceEntity = MarketPriceEntity(
    instrumentId = instrumentId,
    priceDate = priceDate,
    priceMinor = price.amountInCents,
    currencyCode = price.currencyCode,
    source = source,
    fetchedAt = fetchedAt
)

fun FinancialGoalEntity.toModel(): FinancialGoalModel = FinancialGoalModel(
    id = id,
    ownerId = ownerId,
    name = name,
    targetAmount = Money(targetAmountMinor, currencyCode),
    targetDate = targetDate,
    associatedAccountIds = if (associatedAccountIdsJson.isBlank()) emptyList() else associatedAccountIdsJson.split(","),
    currentProgress = Money(currentProgressMinor, currencyCode),
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun FinancialGoalModel.toEntity(): FinancialGoalEntity = FinancialGoalEntity(
    id = id,
    ownerId = ownerId,
    name = name,
    targetAmountMinor = targetAmount.amountInCents,
    currencyCode = targetAmount.currencyCode,
    targetDate = targetDate,
    associatedAccountIdsJson = associatedAccountIds.joinToString(","),
    currentProgressMinor = currentProgress.amountInCents,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ReminderEntity.toModel(): ReminderRecordModel = ReminderRecordModel(
    id = id,
    ownerId = ownerId,
    title = title,
    description = description,
    dueDate = dueDate,
    category = category,
    priority = priority,
    isCompleted = isCompleted,
    linkedEntityType = linkedEntityType,
    linkedEntityId = linkedEntityId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ReminderRecordModel.toEntity(): ReminderEntity = ReminderEntity(
    id = id,
    ownerId = ownerId,
    title = title,
    description = description,
    dueDate = dueDate,
    category = category,
    priority = priority,
    isCompleted = isCompleted,
    linkedEntityType = linkedEntityType,
    linkedEntityId = linkedEntityId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun TransactionSourceRecordEntity.toModel(): TransactionSourceRecordModel = TransactionSourceRecordModel(
    id = id,
    transactionId = transactionId,
    sourceType = sourceType,
    sourceIdentifier = sourceIdentifier,
    sourceFingerprint = sourceFingerprint,
    rawContentSnippet = rawContentSnippet,
    receivedAt = receivedAt,
    processingStatus = processingStatus,
    createdAt = createdAt
)

fun TransactionSourceRecordModel.toEntity(): TransactionSourceRecordEntity = TransactionSourceRecordEntity(
    id = id,
    transactionId = transactionId,
    sourceType = sourceType,
    sourceIdentifier = sourceIdentifier,
    sourceFingerprint = sourceFingerprint,
    rawContentSnippet = rawContentSnippet,
    receivedAt = receivedAt,
    processingStatus = processingStatus,
    createdAt = createdAt
)

fun SmsReviewCandidateEntity.toModel(): SmsReviewCandidateModel = SmsReviewCandidateModel(
    id = id,
    candidate = TransactionCandidate(
        id = id,
        amount = Money(amountMinor, currencyCode),
        direction = direction,
        transactionType = transactionType,
        accountRefMasked = accountRefMasked,
        institutionName = institutionName,
        merchantName = merchantName,
        description = description,
        transactionReference = transactionReference,
        occurredAt = occurredAt,
        localTransactionDate = localTransactionDate,
        availableBalance = availableBalanceMinor?.let { Money(it, currencyCode) },
        confidenceScore = confidenceScore,
        parseStatus = parseStatus,
        rejectionReason = rejectionReason,
        rawContentSnippet = rawContentSnippet,
        sourceFingerprint = sourceFingerprint,
        sender = sender,
        suggestedAccountId = suggestedAccountId
    ),
    createdAt = createdAt,
    status = status
)

fun SmsReviewCandidateModel.toEntity(): SmsReviewCandidateEntity = SmsReviewCandidateEntity(
    id = id,
    amountMinor = candidate.amount.amountInCents,
    currencyCode = candidate.amount.currencyCode,
    direction = candidate.direction,
    transactionType = candidate.transactionType,
    accountRefMasked = candidate.accountRefMasked,
    institutionName = candidate.institutionName,
    merchantName = candidate.merchantName,
    description = candidate.description,
    transactionReference = candidate.transactionReference,
    occurredAt = candidate.occurredAt,
    localTransactionDate = candidate.localTransactionDate,
    availableBalanceMinor = candidate.availableBalance?.amountInCents,
    confidenceScore = candidate.confidenceScore,
    parseStatus = candidate.parseStatus,
    rejectionReason = candidate.rejectionReason,
    rawContentSnippet = candidate.rawContentSnippet,
    sourceFingerprint = candidate.sourceFingerprint,
    sender = candidate.sender,
    suggestedAccountId = candidate.suggestedAccountId,
    status = status,
    createdAt = createdAt
)

fun SmsSettingsEntity.toModel(): SmsSettingsModel = SmsSettingsModel(
    isSmsFeatureEnabled = isSmsFeatureEnabled,
    isConsentGiven = isConsentGiven,
    liveMonitoringEnabled = liveMonitoringEnabled,
    autoSaveHighConfidence = autoSaveHighConfidence,
    importDateRangeDays = importDateRangeDays,
    lastImportTimestamp = lastImportTimestamp,
    totalImportedCount = totalImportedCount
)

fun SmsSettingsModel.toEntity(): SmsSettingsEntity = SmsSettingsEntity(
    id = "sms_settings_root",
    isSmsFeatureEnabled = isSmsFeatureEnabled,
    isConsentGiven = isConsentGiven,
    liveMonitoringEnabled = liveMonitoringEnabled,
    autoSaveHighConfidence = autoSaveHighConfidence,
    importDateRangeDays = importDateRangeDays,
    lastImportTimestamp = lastImportTimestamp,
    totalImportedCount = totalImportedCount,
    updatedAt = ""
)

fun NotificationSettingsEntity.toModel(enabledPackages: Set<String> = emptySet()): NotificationSettingsModel = NotificationSettingsModel(
    isNotificationFeatureEnabled = isNotificationFeatureEnabled,
    isConsentGiven = isConsentGiven,
    liveMonitoringEnabled = liveMonitoringEnabled,
    autoSaveHighConfidence = autoSaveHighConfidence,
    enabledPackageNames = enabledPackages,
    lastProcessedTimestamp = lastProcessedTimestamp,
    totalImportedCount = totalImportedCount
)

fun NotificationSettingsModel.toEntity(): NotificationSettingsEntity = NotificationSettingsEntity(
    id = "notification_settings_root",
    isNotificationFeatureEnabled = isNotificationFeatureEnabled,
    isConsentGiven = isConsentGiven,
    liveMonitoringEnabled = liveMonitoringEnabled,
    autoSaveHighConfidence = autoSaveHighConfidence,
    lastProcessedTimestamp = lastProcessedTimestamp,
    totalImportedCount = totalImportedCount,
    updatedAt = ""
)

fun NotificationAppConfigEntity.toModel(isInstalled: Boolean = false): FinancialAppProfile = FinancialAppProfile(
    id = "app_$packageName",
    packageName = packageName,
    displayName = displayName,
    isInstalled = isInstalled,
    isEnabled = isEnabled,
    parserProfileId = parserProfileId,
    lastProcessedAt = lastProcessedAt
)

fun FinancialAppProfile.toEntity(): NotificationAppConfigEntity = NotificationAppConfigEntity(
    packageName = packageName,
    displayName = displayName,
    isEnabled = isEnabled,
    parserProfileId = parserProfileId,
    lastProcessedAt = lastProcessedAt,
    updatedAt = ""
)

fun NotificationReviewCandidateEntity.toModel(): NotificationReviewCandidateModel = NotificationReviewCandidateModel(
    id = id,
    sourcePackage = sourcePackage,
    candidate = TransactionCandidate(
        id = id,
        amount = Money(amountMinor, currencyCode),
        direction = direction,
        transactionType = transactionType,
        accountRefMasked = accountRefMasked,
        institutionName = institutionName,
        merchantName = merchantName,
        description = description,
        transactionReference = transactionReference,
        occurredAt = occurredAt,
        localTransactionDate = localTransactionDate,
        availableBalance = availableBalanceMinor?.let { Money(it, currencyCode) },
        confidenceScore = confidenceScore,
        parseStatus = parseStatus,
        rejectionReason = rejectionReason,
        rawContentSnippet = rawContentSnippet,
        sourceFingerprint = sourceFingerprint,
        sender = sender,
        suggestedAccountId = suggestedAccountId
    ),
    createdAt = createdAt,
    status = status
)

fun NotificationReviewCandidateModel.toEntity(): NotificationReviewCandidateEntity = NotificationReviewCandidateEntity(
    id = id,
    sourcePackage = sourcePackage,
    amountMinor = candidate.amount.amountInCents,
    currencyCode = candidate.amount.currencyCode,
    direction = candidate.direction,
    transactionType = candidate.transactionType,
    accountRefMasked = candidate.accountRefMasked,
    institutionName = candidate.institutionName,
    merchantName = candidate.merchantName,
    description = candidate.description,
    transactionReference = candidate.transactionReference,
    occurredAt = candidate.occurredAt,
    localTransactionDate = candidate.localTransactionDate,
    availableBalanceMinor = candidate.availableBalance?.amountInCents,
    confidenceScore = candidate.confidenceScore,
    parseStatus = candidate.parseStatus,
    rejectionReason = candidate.rejectionReason,
    rawContentSnippet = candidate.rawContentSnippet,
    sourceFingerprint = candidate.sourceFingerprint,
    sender = candidate.sender,
    suggestedAccountId = candidate.suggestedAccountId,
    status = status,
    createdAt = createdAt
)

fun ReminderEntity.toDefinitionModel(): ReminderDefinitionModel = ReminderDefinitionModel(
    id = id,
    ownerId = ownerId,
    title = title,
    description = description,
    category = category,
    priority = priority,
    isEnabled = isEnabled,
    scheduleVersion = scheduleVersion,
    sourceType = sourceType,
    sourceEntityId = linkedEntityId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    serverRevision = serverRevision,
    syncStatus = syncStatus
)

fun ReminderDefinitionModel.toEntity(dueDate: String = ""): ReminderEntity = ReminderEntity(
    id = id,
    ownerId = ownerId,
    title = title,
    description = description,
    dueDate = dueDate,
    category = category,
    priority = priority,
    isCompleted = false,
    linkedEntityType = sourceType,
    linkedEntityId = sourceEntityId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    sourceType = sourceType,
    isEnabled = isEnabled,
    scheduleVersion = scheduleVersion,
    serverRevision = serverRevision,
    syncStatus = syncStatus
)

fun ReminderScheduleEntity.toModel(): ReminderScheduleModel = ReminderScheduleModel(
    id = id,
    reminderId = reminderId,
    isRecurring = isRecurring,
    localDueDateOrTime = localDueDateOrTime,
    ianaTimezone = ianaTimezone,
    dateOnlyPolicy = dateOnlyPolicy,
    cadence = cadence,
    anchorDay = anchorDay,
    endDate = endDate,
    endCount = endCount,
    leadTimesDays = leadTimesDays,
    missedDatePolicy = missedDatePolicy,
    scheduleVersion = scheduleVersion,
    updatedAt = updatedAt
)

fun ReminderScheduleModel.toEntity(): ReminderScheduleEntity = ReminderScheduleEntity(
    id = id,
    reminderId = reminderId,
    isRecurring = isRecurring,
    localDueDateOrTime = localDueDateOrTime,
    ianaTimezone = ianaTimezone,
    dateOnlyPolicy = dateOnlyPolicy,
    cadence = cadence,
    anchorDay = anchorDay,
    endDate = endDate,
    endCount = endCount,
    leadTimesDays = leadTimesDays,
    missedDatePolicy = missedDatePolicy,
    scheduleVersion = scheduleVersion,
    updatedAt = updatedAt
)

fun ReminderOccurrenceEntity.toModel(): ReminderOccurrenceModel = ReminderOccurrenceModel(
    id = id,
    reminderId = reminderId,
    ownerId = ownerId,
    scheduleVersion = scheduleVersion,
    scheduledLocalDateTime = scheduledLocalDateTime,
    resolvedInstant = resolvedInstant,
    state = state,
    snoozeUntilISO = snoozeUntilISO,
    amount = if (amountMinor != null && currencyCode != null) Money(amountMinor, currencyCode) else null,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus
)

fun ReminderOccurrenceModel.toEntity(): ReminderOccurrenceEntity = ReminderOccurrenceEntity(
    id = id,
    reminderId = reminderId,
    ownerId = ownerId,
    scheduleVersion = scheduleVersion,
    scheduledLocalDateTime = scheduledLocalDateTime,
    resolvedInstant = resolvedInstant,
    state = state,
    snoozeUntilISO = snoozeUntilISO,
    amountMinor = amount?.amountInCents,
    currencyCode = amount?.currencyCode,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus
)

fun ReminderActionEntity.toModel(): ReminderActionModel = ReminderActionModel(
    id = id,
    occurrenceId = occurrenceId,
    reminderId = reminderId,
    ownerId = ownerId,
    actionType = actionType,
    effectiveTimeISO = effectiveTimeISO,
    baseRevision = baseRevision,
    syncStatus = syncStatus,
    conflictState = conflictState
)

fun ReminderActionModel.toEntity(): ReminderActionEntity = ReminderActionEntity(
    id = id,
    occurrenceId = occurrenceId,
    reminderId = reminderId,
    ownerId = ownerId,
    actionType = actionType,
    effectiveTimeISO = effectiveTimeISO,
    baseRevision = baseRevision,
    syncStatus = syncStatus,
    conflictState = conflictState
)

fun InAppInboxItemEntity.toModel(): InAppInboxItemModel = InAppInboxItemModel(
    id = id,
    ownerId = ownerId,
    reminderId = reminderId,
    occurrenceId = occurrenceId,
    linkedEntityType = linkedEntityType,
    linkedEntityId = linkedEntityId,
    title = title,
    subtitle = subtitle,
    severity = severity,
    reason = reason,
    sourceRevision = sourceRevision,
    freshnessISO = freshnessISO,
    isRead = isRead,
    isDismissed = isDismissed,
    isInvalidated = isInvalidated,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun InAppInboxItemModel.toEntity(): InAppInboxItemEntity = InAppInboxItemEntity(
    id = id,
    ownerId = ownerId,
    reminderId = reminderId,
    occurrenceId = occurrenceId,
    linkedEntityType = linkedEntityType,
    linkedEntityId = linkedEntityId,
    title = title,
    subtitle = subtitle,
    severity = severity,
    reason = reason,
    sourceRevision = sourceRevision,
    freshnessISO = freshnessISO,
    isRead = isRead,
    isDismissed = isDismissed,
    isInvalidated = isInvalidated,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ReminderPreferencesEntity.toModel(): ReminderPreferencesModel = ReminderPreferencesModel(
    ownerId = ownerId,
    inAppVisibility = inAppVisibility,
    quietHoursStart = quietHoursStart,
    quietHoursEnd = quietHoursEnd,
    userTimezone = userTimezone,
    displaySettingsJson = displaySettingsJson,
    enabledChannelsJson = enabledChannelsJson,
    updatedAt = updatedAt
)

fun ReminderPreferencesModel.toEntity(): ReminderPreferencesEntity = ReminderPreferencesEntity(
    ownerId = ownerId,
    inAppVisibility = inAppVisibility,
    quietHoursStart = quietHoursStart,
    quietHoursEnd = quietHoursEnd,
    userTimezone = userTimezone,
    displaySettingsJson = displaySettingsJson,
    enabledChannelsJson = enabledChannelsJson,
    updatedAt = updatedAt
)
