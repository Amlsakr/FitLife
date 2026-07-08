package com.aml_sakr.fitlife.feature.progress.domain.repository

import com.aml_sakr.fitlife.core.domain.DomainError
import com.aml_sakr.fitlife.core.domain.Result

interface IProgressRepository {
    suspend fun getSessionCount(userId: String, startTime: Long): Result<Int, DomainError>
    suspend fun getTotalReps(userId: String, startTime: Long): Result<Int, DomainError>
    suspend fun getTotalFatigueEvents(userId: String, startTime: Long): Result<Int, DomainError>
    suspend fun getTotalDuration(userId: String, startTime: Long): Result<Int, DomainError>
    suspend fun getSessionsSince(userId: String, startTime: Long): Result<List<com.aml_sakr.fitlife.feature.progress.domain.model.SessionBasicInfo>, DomainError>
    suspend fun getSessionHistory(userId: String, limit: Int = 20): Result<List<com.aml_sakr.fitlife.feature.progress.domain.model.SessionBasicInfo>, DomainError>
}
