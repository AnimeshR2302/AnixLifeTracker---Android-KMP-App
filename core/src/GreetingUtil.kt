package com.anix.android.anixlifetracker

import kotlinx.datetime.Clock

fun sayHello(to: String): String {
    val nowMs = Clock.System.now().toEpochMilliseconds()
    return "Hello, $to at $nowMs!"
}
