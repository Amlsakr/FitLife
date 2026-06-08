package com.aml_sakr.fitlife.core.data.sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_test_records")
data class SyncTestEntity(
    @PrimaryKey val id: String,
    val payload: String,
    val lastModified: Long,
    val syncStatus: String // "PENDING", "SYNCED"
)
