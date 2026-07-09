package com.aml_sakr.fitlife.core.data.database

import androidx.room.*
import com.aml_sakr.fitlife.core.data.sync.SyncableDao

@Dao
interface SessionDao : SyncableDao<SessionEntity> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Update
    override suspend fun update(entity: SessionEntity)

    @Query("SELECT * FROM sessions WHERE sessionId = :id")
    override suspend fun getById(id: String): SessionEntity?

    @Query("SELECT * FROM sessions WHERE syncStatus = 'NOT_SYNCED'")
    override suspend fun getUnsyncedRecords(): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE userId = :userId ORDER BY startTime DESC")
    suspend fun getSessionsForUser(userId: String): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE userId = :userId ORDER BY startTime DESC LIMIT :limit")
    suspend fun getRecentSessions(userId: String, limit: Int): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE userId = :userId AND startTime >= :startTime ORDER BY startTime ASC")
    suspend fun getSessionsSince(userId: String, startTime: Long): List<SessionEntity>

    @Query("SELECT COUNT(*) FROM sessions WHERE userId = :userId AND startTime >= :startTime")
    suspend fun getSessionCountInRange(userId: String, startTime: Long): Int

    @Query("SELECT SUM(totalReps) FROM sessions WHERE userId = :userId AND startTime >= :startTime")
    suspend fun getTotalRepsInRange(userId: String, startTime: Long): Int?

    @Query("SELECT SUM(fatigueEventCount) FROM sessions WHERE userId = :userId AND startTime >= :startTime")
    suspend fun getTotalFatigueEventsInRange(userId: String, startTime: Long): Int?

    @Query("SELECT SUM(durationSeconds) FROM sessions WHERE userId = :userId AND startTime >= :startTime")
    suspend fun getTotalDurationInRange(userId: String, startTime: Long): Int?
}
