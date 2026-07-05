package com.aml_sakr.fitlife.feature.session.domain.repository

import com.aml_sakr.fitlife.feature.session.domain.model.Session
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.core.domain.DomainError

interface ISessionRepository {
    suspend fun saveSession(session: Session): Result<Unit, DomainError>
    suspend fun getSession(sessionId: String): Result<Session?, DomainError>
    suspend fun getSessionsForUser(userId: String): Result<List<Session>, DomainError>
}
