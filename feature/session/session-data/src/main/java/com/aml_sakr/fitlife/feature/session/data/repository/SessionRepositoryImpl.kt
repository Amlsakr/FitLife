package com.aml_sakr.fitlife.feature.session.data.repository

import com.aml_sakr.fitlife.core.domain.DomainError
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.session.data.database.SessionDao
import com.aml_sakr.fitlife.feature.session.data.database.SessionEntity
import com.aml_sakr.fitlife.feature.session.domain.model.Session
import com.aml_sakr.fitlife.feature.session.domain.repository.ISessionRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao
) : ISessionRepository {

    override suspend fun saveSession(session: Session): Result<Unit, DomainError> {
        return try {
            sessionDao.insertSession(session.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.Failure(SessionError("SAVE_FAILED", e.message ?: "Unknown error saving session"))
        }
    }

    override suspend fun getSession(sessionId: String): Result<Session?, DomainError> {
        return try {
            val entity = sessionDao.getSessionById(sessionId)
            Result.Success(entity?.toDomain())
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.Failure(SessionError("FETCH_FAILED", e.message ?: "Error fetching session"))
        }
    }

    override suspend fun getSessionsForUser(userId: String): Result<List<Session>, DomainError> {
        return try {
            val sessions = sessionDao.getSessionsForUser(userId).map { it.toDomain() }
            Result.Success(sessions)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.Failure(SessionError("LIST_FAILED", e.message ?: "Error fetching sessions"))
        }
    }

    private fun Session.toEntity() = SessionEntity(
        sessionId = sessionId,
        userId = userId,
        planId = planId,
        workoutDayId = workoutDayId,
        startTime = startTime,
        endTime = endTime,
        durationSeconds = durationSeconds,
        totalReps = totalReps,
        totalSets = totalSets,
        fatigueEventCount = fatigueEventCount,
        audioFallbackUsed = audioFallbackUsed,
        completionPercentage = completionPercentage,
        whatsAppShared = whatsAppShared
    )

    private fun SessionEntity.toDomain() = Session(
        sessionId = sessionId,
        userId = userId,
        planId = planId,
        workoutDayId = workoutDayId,
        startTime = startTime,
        endTime = endTime,
        durationSeconds = durationSeconds,
        totalReps = totalReps,
        totalSets = totalSets,
        fatigueEventCount = fatigueEventCount,
        audioFallbackUsed = audioFallbackUsed,
        completionPercentage = completionPercentage,
        whatsAppShared = whatsAppShared
    )

    private data class SessionError(override val code: String, override val message: String) : DomainError
}
