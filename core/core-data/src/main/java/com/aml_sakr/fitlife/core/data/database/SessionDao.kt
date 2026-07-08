package com.aml_sakr.fitlife.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE userId = :userId ORDER BY startTime DESC")
    suspend fun getSessionsForUser(userId: String): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE userId = :userId ORDER BY startTime DESC LIMIT :limit")
    suspend fun getRecentSessions(userId: String, limit: Int): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE userId = :userId AND startTime >= :startTime ORDER BY startTime ASC")
    suspend fun getSessionsSince(userId: String, startTime: Long): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE sessionId = :sessionId")
    suspend fun getSessionById(sessionId: String): SessionEntity?

    @Query("SELECT COUNT(*) FROM sessions WHERE userId = :userId AND startTime >= :startTime")
    suspend fun getSessionCountInRange(userId: String, startTime: Long): Int

    @Query("SELECT SUM(totalReps) FROM sessions WHERE userId = :userId AND startTime >= :startTime")
    suspend fun getTotalRepsInRange(userId: String, startTime: Long): Int?

    @Query("SELECT SUM(fatigueEventCount) FROM sessions WHERE userId = :userId AND startTime >= :startTime")
    suspend fun getTotalFatigueEventsInRange(userId: String, startTime: Long): Int?

    @Query("SELECT SUM(durationSeconds) FROM sessions WHERE userId = :userId AND startTime >= :startTime")
    suspend fun getTotalDurationInRange(userId: String, startTime: Long): Int?
}
