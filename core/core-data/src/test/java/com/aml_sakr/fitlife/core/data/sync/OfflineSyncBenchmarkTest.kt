package com.aml_sakr.fitlife.core.data.sync

import com.aml_sakr.fitlife.core.data.connectivity.MutableConnectivityMonitor
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OfflineSyncBenchmarkTest {

    private lateinit var dao: FakeSyncTestDao
    private lateinit var remoteClient: FakeRemoteSyncClient<SyncTestEntity>
    private lateinit var connectivityMonitor: MutableConnectivityMonitor
    private lateinit var coordinator: OfflineSyncCoordinator

    @Before
    fun setUp() {
        dao = FakeSyncTestDao()
        remoteClient = FakeRemoteSyncClient()
        connectivityMonitor = MutableConnectivityMonitor(isOnline = true)
        val agent = DefaultSyncAgent(dao, remoteClient)
        coordinator = OfflineSyncCoordinator(listOf(agent), connectivityMonitor)
    }

    @Test
    fun runOfflineSyncBenchmark() = runBlocking {
        println("=== STARTING ROOM + FIRESTORE SYNC BENCHMARK ===")
        val writeTimes = mutableListOf<Long>()
        
        // --- Phase 1: Offline Writes ---
        connectivityMonitor.setConnected(false)
        val numRecords = 20
        
        for (i in 1..numRecords) {
            val recordId = "record_$i"
            val payload = "payload_content_for_record_$i"
            val start = System.nanoTime()
            dao.insert(SyncTestEntity(recordId, payload, System.currentTimeMillis(), SyncStatus.NOT_SYNCED))
            val end = System.nanoTime()
            writeTimes.add(end - start)
        }
        
        // Assert all local writes are NOT_SYNCED and 0 remote writes
        assertEquals(numRecords, dao.getUnsyncedRecords().size)
        for (i in 1..numRecords) {
            assertTrue(remoteClient.getRecord("record_$i") == null)
        }
        
        // --- Phase 2: Offline-to-Online Transition & Sync ---
        connectivityMonitor.setConnected(true)
        val phase2Start = System.nanoTime()
        val syncResult = coordinator.sync()
        val phase2End = System.nanoTime()
        
        assertTrue(syncResult.success)
        assertEquals(numRecords, syncResult.successCount)
        assertEquals(0, dao.getUnsyncedRecords().size)
        
        // Verify Firestore has all records
        for (i in 1..numRecords) {
            val remoteRecord = remoteClient.getRecord("record_$i")
            assertTrue(remoteRecord != null)
            assertEquals("payload_content_for_record_$i", remoteRecord?.payload)
        }

        // --- Phase 3: Conflict Reconciliation ---
        // Record 1-10: Local is newer
        // Record 11-20: Remote is newer
        val now = System.currentTimeMillis()
        for (i in 1..10) {
            dao.insert(SyncTestEntity("record_$i", "Locally Updated $i", now + 10000L, SyncStatus.NOT_SYNCED))
            remoteClient.simulateRemoteWrite(SyncTestEntity("record_$i", "Remotely Outdated $i", now - 10000L, SyncStatus.SYNCED))
        }
        for (i in 11..20) {
            dao.insert(SyncTestEntity("record_$i", "Locally Outdated $i", now - 10000L, SyncStatus.NOT_SYNCED))
            remoteClient.simulateRemoteWrite(SyncTestEntity("record_$i", "Remotely Updated $i", now + 10000L, SyncStatus.SYNCED))
        }

        val phase3Start = System.nanoTime()
        val reconciliationResult = coordinator.sync()
        val phase3End = System.nanoTime()

        assertTrue(reconciliationResult.success)
        assertEquals(20, reconciliationResult.successCount)
        assertEquals(10, reconciliationResult.conflictResolvedCount) // remote wins
        
        // Verify results
        for (i in 1..10) {
            // Local wins -> Firestore has local
            assertEquals("Locally Updated $i", remoteClient.getRecord("record_$i")?.payload)
            assertEquals("Locally Updated $i", dao.getById("record_$i")?.payload)
        }
        for (i in 11..20) {
            // Remote wins -> Room has remote
            assertEquals("Remotely Updated $i", dao.getById("record_$i")?.payload)
            assertEquals("Remotely Updated $i", remoteClient.getRecord("record_$i")?.payload)
        }

        // --- Metric Calculations ---
        val writeTimesMs = writeTimes.map { it / 1_000_000.0 }
        val avgWriteMs = writeTimesMs.average()
        val p50WriteMs = writeTimesMs.sorted()[numRecords / 2]
        val p95WriteMs = writeTimesMs.sorted()[(numRecords * 0.95).toInt()]
        val maxWriteMs = writeTimesMs.maxOrNull() ?: 0.0
        val minWriteMs = writeTimesMs.minOrNull() ?: 0.0
        
        val syncDurationMs = (phase2End - phase2Start) / 1_000_000.0
        val reconciliationDurationMs = (phase3End - phase3Start) / 1_000_000.0

        println("=== BENCHMARK COMPLETE ===")
    }
}
