package com.aml_sakr.fitlife.feature.progress.domain.usecase

import com.aml_sakr.fitlife.core.domain.DomainError
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.progress.domain.model.SessionBasicInfo
import com.aml_sakr.fitlife.feature.progress.domain.repository.IProgressRepository
import javax.inject.Inject

class GetSessionHistoryUseCase @Inject constructor(
    private val repository: IProgressRepository
) {
    suspend operator fun invoke(userId: String, limit: Int = 20): Result<List<SessionBasicInfo>, DomainError> {
        return repository.getSessionHistory(userId, limit)
    }
}
