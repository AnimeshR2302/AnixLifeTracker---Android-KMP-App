package com.anix.android.anixlifetracker.platform

actual object PlatformEntryPoint {
    actual fun getPlatformDetails(): PlatformDetails = PlatformDetails(
        name = "Desktop (JVM)",
        isMobile = false,
        isDesktop = true,
        isWeb = false,
        supportsSmsIngestion = false
    )
}
