package com.aml_sakr.fitlife.core.data.sync

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object SyncWorkScheduler {
    const val UNIQUE_WORK_NAME = "fitlife_sync_work"

    internal fun buildConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    internal fun buildRequest() =
        PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(buildConstraints())
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

    fun schedule(context: Context) {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            buildRequest()
        )
    }
}
