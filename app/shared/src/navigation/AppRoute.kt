package com.anix.android.anixlifetracker.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class AppRoute(
    val path: String,
    val title: String,
    val iconSymbol: String
) {
    @Serializable
    data object Dashboard : AppRoute("dashboard", "Dashboard", "📊")

    @Serializable
    data object Finances : AppRoute("finances", "Finances", "💳")

    @Serializable
    data object Investments : AppRoute("investments", "Investments", "📈")

    @Serializable
    data object Reminders : AppRoute("reminders", "Reminders", "⏰")

    @Serializable
    data object Travel : AppRoute("travel", "Travel", "✈️")

    @Serializable
    data object Events : AppRoute("events", "Events", "📅")

    @Serializable
    data object Settings : AppRoute("settings", "Settings", "⚙️")

    companion object {
        val ALL: List<AppRoute> = listOf(
            Dashboard,
            Finances,
            Investments,
            Reminders,
            Travel,
            Events,
            Settings
        )

        fun fromPath(path: String): AppRoute {
            val clean = path.trimStart('#', '/').lowercase()
            return ALL.find { it.path == clean } ?: Dashboard
        }
    }
}
