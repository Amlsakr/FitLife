package com.aml_sakr.fitlife.feature.progress.data.repository

import com.aml_sakr.fitlife.core.data.database.SessionDao
import com.aml_sakr.fitlife.core.domain.AnalyticsLogger
import com.aml_sakr.fitlife.core.domain.DomainError
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.progress.domain.repository.IProgressRepository
import com.aml_sakr.fitlife.feature.progress.domain.model.SessionBasicInfo
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class ProgressRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao,
    private val analyticsLogger: AnalyticsLogger
) : IProgressRepository {

    override suspend fun getSessionCount(userId: String, startTime: Long): Result<Int, DomainError> =
        safeCall { sessionDao.getSessionCountInRange(userId, startTime) }

    override suspend fun getTotalReps(userId: String, startTime: Long): Result<Int, DomainError> =
        safeCall { sessionDao.getTotalRepsInRange(userId, startTime) ?: 0 }

    override suspend fun getTotalFatigueEvents(userId: String, startTime: Long): Result<Int, DomainError> =
        safeCall { sessionDao.getTotalFatigueEventsInRange(userId, startTime) ?: 0 }

    override suspend fun getTotalDuration(userId: String, startTime: Long): Result<Int, DomainError> =
        safeCall { sessionDao.getTotalDurationInRange(userId, startTime) ?: 0 }

    override suspend fun getSessionsSince(userId: String, startTime: Long): Result<List<SessionBasicInfo>, DomainError> =
        safeCall {
            sessionDao.getSessionsSince(userId, startTime).map { entity ->
                SessionBasicInfo(
                    sessionId = entity.sessionId,
                    startTime = entity.startTime,
                    durationSeconds = entity.durationSeconds
                )
            }
        }

    override suspend fun getSessionHistory(userId: String, limit: Int): Result<List<SessionBasicInfo>, DomainError> =
        safeCall {
            sessionDao.getRecentSessions(userId, limit).map { entity ->
                SessionBasicInfo(
                    sessionId = entity.sessionId,
                    startTime = entity.startTime,
                    durationSeconds = entity.durationSeconds
                )
            }
        }

    private suspend fun <T> safeCall(call: suspend () -> T): Result<T, DomainError> {
        return try {
            Result.Success(call())
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            analyticsLogger.logEvent(
                "progress_db_error",
                mapOf("message" to (e.message ?: "Unknown error"))
            )
            Result.Failure(ProgressDataError("DB_ERROR", e.message ?: "Database error"))
        }
    }

    private data class ProgressDataError(override val code: String, override val message: String) : DomainError
}
