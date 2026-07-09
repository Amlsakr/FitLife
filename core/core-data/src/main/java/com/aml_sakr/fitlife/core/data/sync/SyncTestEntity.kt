package com.aml_sakr.fitlife.core.data.sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_test_records")
data class SyncTestEntity(
    @PrimaryKey val id: String,
    val payload: String,
    override val lastModified: Long,
    override val syncStatus: SyncStatus
) : SyncableEntity<SyncTestEntity> {
    override val syncId: String get() = id
    override fun withSyncStatus(status: SyncStatus): SyncTestEntity = copy(syncStatus = status)
    override fun withSyncMetadata(status: SyncStatus, lastModified: Long): SyncTestEntity = 
        copy(syncStatus = status, lastModified = lastModified)
}
