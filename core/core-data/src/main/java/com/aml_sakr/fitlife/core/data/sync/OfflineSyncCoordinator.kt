package com.aml_sakr.fitlife.core.data.sync

import com.aml_sakr.fitlife.core.data.connectivity.ConnectivityMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OfflineSyncCoordinator(
    private val agents: List<SyncAgent>,
    private val connectivityMonitor: ConnectivityMonitor
) {
    suspend fun sync(): SyncResult = withContext(Dispatchers.IO) {
        if (!connectivityMonitor.isConnected()) {
            return@withContext SyncResult(success = false, error = "No connectivity")
        }

        var successCount = 0
        var failureCount = 0
        var conflictResolvedCount = 0
        var totalError: String? = null

        for (agent in agents) {
            val result = agent.sync()
            successCount += result.successCount
            failureCount += result.failureCount
            conflictResolvedCount += result.conflictResolvedCount
            if (!result.success) {
                totalError = if (totalError == null) result.error else "$totalError; ${result.error}"
            }
        }

        val overallSuccess = failureCount == 0
        SyncResult(
            success = overallSuccess,
            successCount = successCount,
            failureCount = failureCount,
            conflictResolvedCount = conflictResolvedCount,
            error = if (overallSuccess) null else (totalError ?: "Partial sync failure")
        )
    }
}

data class SyncResult(
    val success: Boolean,
    val successCount: Int = 0,
    val failureCount: Int = 0,
    val conflictResolvedCount: Int = 0,
    val error: String? = null
)
