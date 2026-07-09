package com.aml_sakr.fitlife.core.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.aml_sakr.fitlife.core.data.sync.SyncStatus
import com.aml_sakr.fitlife.core.data.sync.SyncableEntity

@Entity(
    tableName = "sessions",
    indices = [Index(value = ["syncStatus"])]
)
data class SessionEntity(
    @PrimaryKey val sessionId: String,
    val userId: String,
    val planId: String,
    val workoutDayId: String,
    val startTime: Long,
    val endTime: Long?,
    val durationSeconds: Int?,
    val totalReps: Int,
    val totalSets: Int,
    val fatigueEventCount: Int,
    val audioFallbackUsed: Boolean,
    val completionPercentage: Float,
    val whatsAppShared: Boolean,
    override val syncStatus: SyncStatus = SyncStatus.NOT_SYNCED,
    override val lastModified: Long = startTime
) : SyncableEntity<SessionEntity> {
    override val syncId: String get() = sessionId
    
    override fun withSyncStatus(status: SyncStatus): SessionEntity = 
        copy(syncStatus = status)
        
    override fun withSyncMetadata(status: SyncStatus, lastModified: Long): SessionEntity =
        copy(syncStatus = status, lastModified = lastModified)
}
