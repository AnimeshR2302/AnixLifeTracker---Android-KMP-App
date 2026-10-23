package com.anix.android.anixlifetracker.database

import com.anix.android.anixlifetracker.database.entity.AccountEntity
import com.anix.android.anixlifetracker.database.entity.CategoryEntity
import com.anix.android.anixlifetracker.model.*
import kotlinx.coroutines.flow.firstOrNull

object DatabaseInitializer {

    suspend fun initializeDefaultData(db: AppDatabase) {
        // Seed default categories if none exist
        val existingCategories = db.categoryDao().observeActiveCategories().firstOrNull()
        if (existingCategories.isNullOrEmpty()) {
            val systemCategories = listOf(
                CategoryEntity("cat_sal", "local_user", "Salary", null, CategoryKind.INCOME, "income", isSystemCategory = true, isActive = true, "", ""),
                CategoryEntity("cat_fre", "local_user", "Freelance", null, CategoryKind.INCOME, "work", isSystemCategory = true, isActive = true, "", ""),
                CategoryEntity("cat_int", "local_user", "Interest", null, CategoryKind.INCOME, "savings", isSystemCategory = true, isActive = true, "", ""),
                CategoryEntity("cat_div", "local_user", "Dividend", null, CategoryKind.INCOME, "invest", isSystemCategory = true, isActive = true, "", ""),
                CategoryEntity("cat_oth_inc", "local_user", "Other Income", null, CategoryKind.INCOME, "money", isSystemCategory = true, isActive = true, "", ""),

                CategoryEntity("cat_food", "local_user", "Food & Dining", null, CategoryKind.EXPENSE, "restaurant", isSystemCategory = true, isActive = true, "", ""),
                CategoryEntity("cat_groc", "local_user", "Groceries", null, CategoryKind.EXPENSE, "cart", isSystemCategory = true, isActive = true, "", ""),
                CategoryEntity("cat_shop", "local_user", "Shopping", null, CategoryKind.EXPENSE, "bag", isSystemCategory = true, isActive = true, "", ""),
                CategoryEntity("cat_rent", "local_user", "Rent", null, CategoryKind.EXPENSE, "home", isSystemCategory = true, isActive = true, "", ""),
                CategoryEntity("cat_util", "local_user", "Utilities", null, CategoryKind.EXPENSE, "bolt", isSystemCategory = true, isActive = true, "", ""),
                CategoryEntity("cat_tran", "local_user", "Transportation", null, CategoryKind.EXPENSE, "commute", isSystemCategory = true, isActive = true, "", ""),
                CategoryEntity("cat_heal", "local_user", "Healthcare", null, CategoryKind.EXPENSE, "medical", isSystemCategory = true, isActive = true, "", ""),
                CategoryEntity("cat_ent", "local_user", "Entertainment", null, CategoryKind.EXPENSE, "movie", isSystemCategory = true, isActive = true, "", ""),
                CategoryEntity("cat_sub", "local_user", "Subscriptions", null, CategoryKind.EXPENSE, "renew", isSystemCategory = true, isActive = true, "", ""),
                CategoryEntity("cat_oth_exp", "local_user", "Other Expenses", null, CategoryKind.EXPENSE, "category", isSystemCategory = true, isActive = true, "", ""),

                CategoryEntity("cat_trf", "local_user", "Account Transfer", null, CategoryKind.TRANSFER, "swap", isSystemCategory = true, isActive = true, "", "")
            )
            db.categoryDao().insertCategories(systemCategories)
        }

        // Seed default accounts if none exist
        val existingAccounts = db.accountDao().observeAllAccounts().firstOrNull()
        if (existingAccounts.isNullOrEmpty()) {
            val defaultAccounts = listOf(
                AccountEntity(
                    id = "acc_hdfc",
                    ownerId = "local_user",
                    accountName = "HDFC Bank Account",
                    accountType = AccountCategoryType.CHECKING,
                    institutionName = "HDFC Bank",
                    accountReferenceMasked = "•••• 4821",
                    currencyCode = "INR",
                    openingBalanceMinor = 24500000L,
                    balanceSnapshotMinor = 24500000L,
                    balanceSnapshotTimestamp = "2026-02-01T00:00:00Z",
                    isActive = true,
                    createdAt = "2026-02-01T00:00:00Z",
                    updatedAt = "2026-02-01T00:00:00Z",
                    syncStatus = SyncStatus.LOCAL_ONLY
                ),
                AccountEntity(
                    id = "acc_icici",
                    ownerId = "local_user",
                    accountName = "ICICI Savings Account",
                    accountType = AccountCategoryType.SAVINGS,
                    institutionName = "ICICI Bank",
                    accountReferenceMasked = "•••• 9102",
                    currencyCode = "INR",
                    openingBalanceMinor = 58000000L,
                    balanceSnapshotMinor = 58000000L,
                    balanceSnapshotTimestamp = "2026-02-01T00:00:00Z",
                    isActive = true,
                    createdAt = "2026-02-01T00:00:00Z",
                    updatedAt = "2026-02-01T00:00:00Z",
                    syncStatus = SyncStatus.LOCAL_ONLY
                ),
                AccountEntity(
                    id = "acc_axis_cc",
                    ownerId = "local_user",
                    accountName = "Axis Credit Card",
                    accountType = AccountCategoryType.CREDIT_CARD,
                    institutionName = "Axis Bank",
                    accountReferenceMasked = "•••• 1154",
                    currencyCode = "INR",
                    openingBalanceMinor = -3245000L,
                    balanceSnapshotMinor = -3245000L,
                    balanceSnapshotTimestamp = "2026-02-01T00:00:00Z",
                    isActive = true,
                    createdAt = "2026-02-01T00:00:00Z",
                    updatedAt = "2026-02-01T00:00:00Z",
                    syncStatus = SyncStatus.LOCAL_ONLY
                ),
                AccountEntity(
                    id = "acc_cash",
                    ownerId = "local_user",
                    accountName = "Cash Wallet",
                    accountType = AccountCategoryType.CASH,
                    institutionName = "Cash",
                    accountReferenceMasked = "CASH",
                    currencyCode = "INR",
                    openingBalanceMinor = 500000L,
                    balanceSnapshotMinor = 500000L,
                    balanceSnapshotTimestamp = "2026-02-01T00:00:00Z",
                    isActive = true,
                    createdAt = "2026-02-01T00:00:00Z",
                    updatedAt = "2026-02-01T00:00:00Z",
                    syncStatus = SyncStatus.LOCAL_ONLY
                )
            )
            db.accountDao().insertAccounts(defaultAccounts)
        }
    }
}
