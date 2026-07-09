package com.aml_sakr.fitlife.core.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.android.EntryPointAccessors

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            SyncEntryPoint::class.java
        )
        
        val coordinator = entryPoint.offlineSyncCoordinator()
        val syncResult = coordinator.sync()

        return if (syncResult.success) {
            Result.success()
        } else {
            if (syncResult.error?.contains("No connectivity") == true) {
                Result.retry()
            } else {
                // For other errors, we might still want to retry or fail depending on policy.
                // AC #3 says "Retries with exponential back-off".
                // Result.retry() triggers the back-off configured in the work request.
                Result.retry()
            }
        }
    }
}
