package com.anix.android.anixlifetracker.platform

actual object PlatformEntryPoint {
    actual fun getPlatformDetails(): PlatformDetails = PlatformDetails(
        name = "Web (WasmJS)",
        isMobile = false,
        isDesktop = false,
        isWeb = true,
        supportsSmsIngestion = false
    )
}
