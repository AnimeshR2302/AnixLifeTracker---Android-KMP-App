package com.anix.android.anixlifetracker.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DatabaseManager {
    private var instance: AppDatabase? = null

    fun getDatabase(context: Any? = null): AppDatabase {
        return instance ?: run {
            val builder = getDatabaseBuilder(context)
            val newInstance = builder.build()
            instance = newInstance

            CoroutineScope(Dispatchers.Default).launch {
                try {
                    DatabaseInitializer.initializeDefaultData(newInstance)
                } catch (_: Exception) {
                    // Pre-population fallback
                }
            }
            newInstance
        }
    }
}
