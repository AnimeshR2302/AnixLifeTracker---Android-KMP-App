package com.anix.android.anixlifetracker.platform

data class PlatformDetails(
    val name: String,
    val isMobile: Boolean,
    val isDesktop: Boolean,
    val isWeb: Boolean,
    val supportsSmsIngestion: Boolean
)

expect object PlatformEntryPoint {
    fun getPlatformDetails(): PlatformDetails
}
