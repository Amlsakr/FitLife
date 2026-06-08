package com.aml_sakr.fitlife.core.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val dao = SyncDependencyProvider.dao
        val remoteClient = SyncDependencyProvider.remoteClient
        val connectivityMonitor = SyncDependencyProvider.connectivityMonitor

        if (dao == null || remoteClient == null || connectivityMonitor == null) {
            return Result.failure()
        }

        val coordinator = OfflineSyncCoordinator(dao, remoteClient, connectivityMonitor)
        val syncResult = coordinator.sync()

        return if (syncResult.success) {
            Result.success()
        } else {
            if (syncResult.error == "No connectivity") {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
