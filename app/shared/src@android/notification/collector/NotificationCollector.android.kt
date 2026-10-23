package com.anix.android.anixlifetracker.notification.collector

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.anix.android.anixlifetracker.model.AccountCategoryType
import com.anix.android.anixlifetracker.model.FinancialAppProfile

actual class NotificationCollector(private val context: Context?) {

    actual constructor() : this(null)

    actual fun isNotificationAccessGranted(): Boolean {
        val ctx = context ?: return false
        val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(ctx)
        if (enabledPackages.contains(ctx.packageName)) return true

        val flat = Settings.Secure.getString(ctx.contentResolver, "enabled_notification_listeners") ?: ""
        return flat.contains(ctx.packageName)
    }

    actual fun openNotificationListenerSettings() {
        val ctx = context ?: return
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(intent)
        } catch (_: Exception) {
            val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(fallbackIntent)
        }
    }

    actual fun isListenerServiceConnected(): Boolean {
        return com.anix.android.anixlifetracker.notification.service.FinancialNotificationListenerService.isServiceConnected
    }

    actual fun getInstalledSupportedApps(): List<FinancialAppProfile> {
        val knownApps = listOf(
            FinancialAppProfile("app_gpay", "com.google.android.apps.nfc.phone", "Google Pay", parserProfileId = "upi_gpay", categoryHint = AccountCategoryType.WALLET),
            FinancialAppProfile("app_phonepe", "com.phonepe.app", "PhonePe", parserProfileId = "upi_phonepe", categoryHint = AccountCategoryType.WALLET),
            FinancialAppProfile("app_paytm", "net.one97.paytm", "Paytm", parserProfileId = "upi_paytm", categoryHint = AccountCategoryType.WALLET),
            FinancialAppProfile("app_cred", "com.dreamplug.android.cred", "CRED", parserProfileId = "cc_cred", categoryHint = AccountCategoryType.CREDIT_CARD),
            FinancialAppProfile("app_hdfc", "com.snapwork.hdfc", "HDFC Bank MobileBanking", parserProfileId = "bank_hdfc", categoryHint = AccountCategoryType.CHECKING),
            FinancialAppProfile("app_sbi", "com.sbi.lotusintouch", "SBI YONO", parserProfileId = "bank_sbi", categoryHint = AccountCategoryType.SAVINGS),
            FinancialAppProfile("app_icici", "com.csam.icici.bank.imobile", "ICICI iMobile", parserProfileId = "bank_icici", categoryHint = AccountCategoryType.CHECKING),
            FinancialAppProfile("app_axis", "com.axis.mobile", "Axis Mobile", parserProfileId = "bank_axis", categoryHint = AccountCategoryType.CHECKING),
            FinancialAppProfile("app_kotak", "com.msf.ksecure", "Kotak Mobile Banking", parserProfileId = "bank_kotak", categoryHint = AccountCategoryType.SAVINGS),
            FinancialAppProfile("app_pnb", "com.pnb.one", "PNB One", parserProfileId = "bank_pnb", categoryHint = AccountCategoryType.SAVINGS),
            FinancialAppProfile("app_jupiter", "me.jupiter.app", "Jupiter Money", parserProfileId = "neobank_jupiter", categoryHint = AccountCategoryType.CHECKING),
            FinancialAppProfile("app_slice", "org.altruist.slice", "Slice", parserProfileId = "cc_slice", categoryHint = AccountCategoryType.CREDIT_CARD),
            FinancialAppProfile("app_navi", "com.naviapp", "Navi UPI", parserProfileId = "upi_navi", categoryHint = AccountCategoryType.WALLET),
            FinancialAppProfile("app_amazon", "in.amazon.mShop.android.shopping", "Amazon Pay", parserProfileId = "upi_amazon", categoryHint = AccountCategoryType.WALLET)
        )

        val ctx = context ?: return knownApps

        val pm = ctx.packageManager
        return knownApps.map { app ->
            val installed = try {
                pm.getPackageInfo(app.packageName, 0)
                true
            } catch (_: PackageManager.NameNotFoundException) {
                false
            }
            app.copy(isInstalled = installed)
        }
    }
}
