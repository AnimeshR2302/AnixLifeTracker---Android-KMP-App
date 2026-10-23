package com.anix.android.anixlifetracker.database

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual fun getDatabaseBuilder(context: Any?): RoomDatabase.Builder<AppDatabase> {
    val userHome = System.getProperty("user.home") ?: "."
    val appDir = File(userHome, ".anixlifetracker")
    if (!appDir.exists()) {
        appDir.mkdirs()
    }
    val dbFile = File(appDir, "anix_life_tracker.db")
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath
    )
}
