package com.aml_sakr.fitlife.core.data.sync

import android.content.Context
import android.os.Build
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.aml_sakr.fitlife.core.data.connectivity.MutableConnectivityMonitor
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomFirestoreWorkManagerSyncInstrumentedTest {

    private lateinit var context: Context
    private lateinit var database: SyncTestDatabase
    private lateinit var firestore: FirebaseFirestore
    private lateinit var remoteClient: RemoteSyncClient
    private lateinit var connectivityMonitor: MutableConnectivityMonitor
    private lateinit var appName: String
    private lateinit var idPrefix: String

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, SyncTestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        appName = "sync-spike-${UUID.randomUUID()}"
        idPrefix = "record-${UUID.randomUUID()}"
        firestore = FirebaseFirestore.getInstance(createFirebaseApp(context, appName)).also {
            it.useEmulator(resolveFirestoreEmulatorHost(), FIRESTORE_EMULATOR_PORT)
        }
        remoteClient = FirestoreRemoteSyncClient(firestore)
        connectivityMonitor = MutableConnectivityMonitor(isOnline = true)

        val coordinator = OfflineSyncCoordinator(database.syncTestDao(), remoteClient, connectivityMonitor)
        val config = Configuration.Builder()
            .setExecutor(SynchronousExecutor())
            .setWorkerFactory(SyncHarnessWorkerFactory(coordinator))
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @After
    fun tearDown() {
        database.close()
        FirebaseApp.getInstance(appName).delete()
    }

    @Test
    fun offlineRoomWrite_syncsToFirestoreThroughWorkManager_whenConnectivityReturns() = runBlocking {
        connectivityMonitor.setConnected(false)
        val record = SyncTestEntity("$idPrefix-offline", "offline payload", 1_000L, SyncStatus.PENDING)

        database.syncTestDao().insert(record)

        assertEquals(record, database.syncTestDao().getById(record.id))
        assertNull(remoteClient.getRecord(record.id))

        connectivityMonitor.setConnected(true)

        runWorkManagerSync()

        assertEquals(SyncStatus.SYNCED, database.syncTestDao().getById(record.id)?.syncStatus)
        assertEquals("offline payload", remoteClient.getRecord(record.id)?.payload)
    }

    @Test
    fun workManagerSync_resolvesConcurrentEditsWithLatestTimestampWins() = runBlocking {
        val localWins = SyncTestEntity("$idPrefix-local", "local newer", 3_000L, SyncStatus.PENDING)
        val remoteLoses = SyncTestEntity("$idPrefix-local", "remote older", 2_000L, SyncStatus.SYNCED)
        val localLoses = SyncTestEntity("$idPrefix-remote", "local older", 4_000L, SyncStatus.PENDING)
        val remoteWins = SyncTestEntity("$idPrefix-remote", "remote newer", 5_000L, SyncStatus.SYNCED)

        database.syncTestDao().insert(localWins)
        database.syncTestDao().insert(localLoses)
        remoteClient.saveRecord(remoteLoses)
        remoteClient.saveRecord(remoteWins)

        runWorkManagerSync()

        assertEquals("local newer", remoteClient.getRecord(localWins.id)?.payload)
        assertEquals("remote newer", database.syncTestDao().getById(localLoses.id)?.payload)
        assertEquals(SyncStatus.SYNCED, database.syncTestDao().getById(localWins.id)?.syncStatus)
        assertEquals(SyncStatus.SYNCED, database.syncTestDao().getById(localLoses.id)?.syncStatus)
    }

    private fun runWorkManagerSync() {
        val request = OneTimeWorkRequestBuilder<SyncHarnessWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        val workManager = WorkManager.getInstance(context)
        workManager.enqueue(request).result.get(10, TimeUnit.SECONDS)
        val testDriver = requireNotNull(WorkManagerTestInitHelper.getTestDriver(context))
        testDriver.setAllConstraintsMet(request.id)
        val deadlineMillis = System.currentTimeMillis() + 30_000L
        while (System.currentTimeMillis() < deadlineMillis) {
            val workInfo = requireNotNull(workManager.getWorkInfoById(request.id).get(10, TimeUnit.SECONDS))
            if (workInfo.state.isFinished) {
                assertEquals("Expected WorkManager sync to succeed", WorkInfo.State.SUCCEEDED, workInfo.state)
                return
            }
            Thread.sleep(500L)
        }

        val finalWorkInfo = requireNotNull(workManager.getWorkInfoById(request.id).get(10, TimeUnit.SECONDS))
        assertEquals("Expected WorkManager sync to succeed", WorkInfo.State.SUCCEEDED, finalWorkInfo.state)
    }

    private fun createFirebaseApp(context: Context, name: String): FirebaseApp {
        val options = FirebaseOptions.Builder()
            .setProjectId(FIRESTORE_EMULATOR_PROJECT_ID)
            .setApplicationId("1:1234567890:android:${UUID.randomUUID().toString().replace("-", "")}")
            .setApiKey("fake-api-key")
            .build()
        return FirebaseApp.initializeApp(context, options, name)
    }

    private fun resolveFirestoreEmulatorHost(): String {
        val args = InstrumentationRegistry.getArguments()
        val override = args.getString("fitlifeFirestoreHost")
            ?: System.getProperty("fitlife.firestore.host")
        if (!override.isNullOrBlank()) {
            return override
        }
        return if (Build.FINGERPRINT.contains("generic", ignoreCase = true) ||
            Build.FINGERPRINT.contains("emulator", ignoreCase = true) ||
            Build.MODEL.contains("Emulator", ignoreCase = true)
        ) {
            "10.0.2.2"
        } else {
            "127.0.0.1"
        }
    }

    companion object {
        private const val FIRESTORE_EMULATOR_PORT = 8080
        private const val FIRESTORE_EMULATOR_PROJECT_ID = "fitlife-sync-spike"
    }
}

class SyncHarnessWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val coordinator: OfflineSyncCoordinator
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        return if (coordinator.sync().success) {
            Result.success()
        } else {
            Result.failure()
        }
    }
}

private class SyncHarnessWorkerFactory(
    private val coordinator: OfflineSyncCoordinator
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): CoroutineWorker? {
        return if (workerClassName == SyncHarnessWorker::class.java.name) {
            SyncHarnessWorker(appContext, workerParameters, coordinator)
        } else {
            null
        }
    }
}
