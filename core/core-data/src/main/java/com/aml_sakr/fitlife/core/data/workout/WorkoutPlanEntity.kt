package com.aml_sakr.fitlife.core.data.workout

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.aml_sakr.fitlife.core.data.sync.SyncStatus
import com.aml_sakr.fitlife.core.data.sync.SyncableEntity

@Entity(
    tableName = "workout_plans",
    indices = [Index(value = ["syncStatus"])]
)
data class WorkoutPlanEntity(
    @PrimaryKey val planId: String,
    val userId: String,
    val requestKey: String,
    val generatedAtEpochMillis: Long,
    val expiresAtEpochMillis: Long,
    val fitnessLevel: String,
    val location: String,
    val requestedDays: Int,
    val goalNames: List<String>,
    val equipmentNames: List<String>,
    val weekNumber: Int,
    val isFallback: Boolean,
    val planJson: String,
    override val syncStatus: SyncStatus = SyncStatus.NOT_SYNCED,
    override val lastModified: Long = generatedAtEpochMillis
) : SyncableEntity<WorkoutPlanEntity> {
    override val syncId: String get() = planId
    
    override fun withSyncStatus(status: SyncStatus): WorkoutPlanEntity = 
        copy(syncStatus = status)
        
    override fun withSyncMetadata(status: SyncStatus, lastModified: Long): WorkoutPlanEntity =
        copy(syncStatus = status, lastModified = lastModified)
}
