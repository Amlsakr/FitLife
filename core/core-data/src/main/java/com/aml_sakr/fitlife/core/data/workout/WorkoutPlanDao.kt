package com.aml_sakr.fitlife.core.data.workout

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WorkoutPlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WorkoutPlanEntity)

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
