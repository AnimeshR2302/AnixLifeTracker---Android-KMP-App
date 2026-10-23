package com.anix.android.anixlifetracker

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
