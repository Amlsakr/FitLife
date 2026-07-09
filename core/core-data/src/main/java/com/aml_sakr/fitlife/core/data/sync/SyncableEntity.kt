package com.aml_sakr.fitlife.core.data.sync

interface SyncableEntity<T : SyncableEntity<T>> {
    val syncId: String
    val lastModified: Long
    val syncStatus: SyncStatus
    
    fun withSyncStatus(status: SyncStatus): T
    fun withSyncMetadata(status: SyncStatus, lastModified: Long): T
}
