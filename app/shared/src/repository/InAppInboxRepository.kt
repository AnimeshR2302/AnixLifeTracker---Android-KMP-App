package com.anix.android.anixlifetracker.repository

import com.anix.android.anixlifetracker.database.AppDatabase
import com.anix.android.anixlifetracker.database.entity.toEntity
import com.anix.android.anixlifetracker.database.entity.toModel
import com.anix.android.anixlifetracker.model.InAppInboxItemModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

interface InAppInboxRepository {
    fun observeActiveInboxItems(): Flow<List<InAppInboxItemModel>>
    fun observeUnreadInboxItems(): Flow<List<InAppInboxItemModel>>
    suspend fun addInboxItem(item: InAppInboxItemModel)
    suspend fun addInboxItems(items: List<InAppInboxItemModel>)
    suspend fun markAsRead(id: String)
    suspend fun dismissItem(id: String)
    suspend fun invalidateItemsForEntity(entityType: String, entityId: String)
}

class InAppInboxRepositoryImpl(private val db: AppDatabase) : InAppInboxRepository {

    override fun observeActiveInboxItems(): Flow<List<InAppInboxItemModel>> {
        return db.inAppInboxDao().observeActiveInboxItems().map { list -> list.map { it.toModel() } }
    }

    override fun observeUnreadInboxItems(): Flow<List<InAppInboxItemModel>> {
        return db.inAppInboxDao().observeUnreadInboxItems().map { list -> list.map { it.toModel() } }
    }

    override suspend fun addInboxItem(item: InAppInboxItemModel) {
        db.inAppInboxDao().insertInboxItem(item.toEntity())
    }

    override suspend fun addInboxItems(items: List<InAppInboxItemModel>) {
        db.inAppInboxDao().insertInboxItems(items.map { it.toEntity() })
    }

    override suspend fun markAsRead(id: String) {
        val nowIso = Clock.System.now().toString()
        db.inAppInboxDao().markAsRead(id, nowIso)
    }

    override suspend fun dismissItem(id: String) {
        val nowIso = Clock.System.now().toString()
        db.inAppInboxDao().dismissItem(id, nowIso)
    }

    override suspend fun invalidateItemsForEntity(entityType: String, entityId: String) {
        val nowIso = Clock.System.now().toString()
        db.inAppInboxDao().invalidateItemsForEntity(entityType, entityId, nowIso)
    }
}
