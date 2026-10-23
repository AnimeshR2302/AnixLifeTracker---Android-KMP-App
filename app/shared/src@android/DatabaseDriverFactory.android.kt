package com.anix.android.anixlifetracker.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual fun getDatabaseBuilder(context: Any?): RoomDatabase.Builder<AppDatabase> {
    val ctx = (context as? Context) ?: throw IllegalArgumentException("Android Context required for Room Database")
    val dbFile = ctx.getDatabasePath("anix_life_tracker.db")
    return Room.databaseBuilder<AppDatabase>(
        context = ctx.applicationContext,
        name = dbFile.absolutePath
    )
}
