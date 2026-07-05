package com.aml_sakr.fitlife.feature.session.domain.usecase

import com.aml_sakr.fitlife.core.domain.DomainError
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.session.domain.model.Session
import com.aml_sakr.fitlife.feature.session.domain.repository.ISessionRepository
import javax.inject.Inject

class SaveSessionUseCase @Inject constructor(
    private val repository: ISessionRepository
) {
    suspend operator fun invoke(session: Session): Result<Unit, DomainError> {
        return repository.saveSession(session)
    }
}
