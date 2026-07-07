package com.aml_sakr.fitlife.feature.progress.domain.repository

import com.aml_sakr.fitlife.core.domain.DomainError
import com.aml_sakr.fitlife.core.domain.Result

interface IProgressRepository {
    suspend fun getSessionCount(userId: String, startTime: Long): Result<Int, DomainError>
    suspend fun getTotalReps(userId: String, startTime: Long): Result<Int, DomainError>
    suspend fun getTotalFatigueEvents(userId: String, startTime: Long): Result<Int, DomainError>
    suspend fun getTotalDuration(userId: String, startTime: Long): Result<Int, DomainError>
}
