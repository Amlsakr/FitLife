package com.aml_sakr.fitlife.feature.session.domain.usecase

import com.aml_sakr.fitlife.core.domain.DomainError
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.session.domain.model.Session
import com.aml_sakr.fitlife.feature.session.domain.repository.ISessionRepository
import javax.inject.Inject

class GetSessionUseCase @Inject constructor(
    private val repository: ISessionRepository
) {
    suspend operator fun invoke(sessionId: String): Result<Session?, DomainError> {
        return repository.getSession(sessionId)
    }
}
