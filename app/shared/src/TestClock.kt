package com.anix.android.anixlifetracker.test

import kotlin.time.Clock

fun testClock(): Long {
    return Clock.System.now().toEpochMilliseconds()
}
