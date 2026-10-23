package com.anix.android.anixlifetracker.database

import androidx.room.RoomDatabase

expect fun getDatabaseBuilder(context: Any? = null): RoomDatabase.Builder<AppDatabase>
