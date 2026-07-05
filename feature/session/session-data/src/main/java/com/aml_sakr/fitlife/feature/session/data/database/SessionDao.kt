package com.aml_sakr.fitlife.feature.session.data.database

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

    @Query("SELECT * FROM sessions WHERE sessionId = :sessionId")
    suspend fun getSessionById(sessionId: String): SessionEntity?
}
