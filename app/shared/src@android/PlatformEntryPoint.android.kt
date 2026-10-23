package com.anix.android.anixlifetracker.platform

actual object PlatformEntryPoint {
    actual fun getPlatformDetails(): PlatformDetails = PlatformDetails(
        name = "Android",
        isMobile = true,
        isDesktop = false,
        isWeb = false,
        supportsSmsIngestion = true
    )
}
