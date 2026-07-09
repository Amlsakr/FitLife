package com.aml_sakr.fitlife.core.data.sync

data class MockSyncableEntity(
    override val syncId: String,
    val payload: String,
    override val lastModified: Long,
    override val syncStatus: SyncStatus
) : SyncableEntity<MockSyncableEntity> {
    override fun withSyncStatus(status: SyncStatus): MockSyncableEntity =
        copy(syncStatus = status)
        
    override fun withSyncMetadata(status: SyncStatus, lastModified: Long): MockSyncableEntity =
        copy(syncStatus = status, lastModified = lastModified)
}
