package com.aml_sakr.fitlife.core.data.workout

import androidx.room.*
import com.aml_sakr.fitlife.core.data.sync.SyncableDao

@Dao
interface WorkoutPlanDao : SyncableDao<WorkoutPlanEntity> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WorkoutPlanEntity)

    @Update
    override suspend fun update(entity: WorkoutPlanEntity)

    @Query("SELECT * FROM workout_plans WHERE planId = :id")
    override suspend fun getById(id: String): WorkoutPlanEntity?
    
    @Query("SELECT * FROM workout_plans WHERE syncStatus = 'NOT_SYNCED'")
    override suspend fun getUnsyncedRecords(): List<WorkoutPlanEntity>

    @Query(
        """
        SELECT * FROM workout_plans
        WHERE requestKey = :requestKey
          AND expiresAtEpochMillis > :nowEpochMillis
        ORDER BY generatedAtEpochMillis DESC
        LIMIT 1
        """
    )
    suspend fun getLatestByRequestKey(requestKey: String, nowEpochMillis: Long): WorkoutPlanEntity?

    @Query(
        """
        SELECT * FROM workout_plans
        WHERE userId = :userId
          AND requestKey = :requestKey
          AND expiresAtEpochMillis > :nowEpochMillis
        ORDER BY generatedAtEpochMillis DESC
        LIMIT 1
        """
    )
    suspend fun getLatestByUserIdAndRequestKey(
        userId: String,
        requestKey: String,
        nowEpochMillis: Long
    ): WorkoutPlanEntity?

    @Query("DELETE FROM workout_plans WHERE expiresAtEpochMillis <= :nowEpochMillis")
    suspend fun clearOld(nowEpochMillis: Long): Int
}
