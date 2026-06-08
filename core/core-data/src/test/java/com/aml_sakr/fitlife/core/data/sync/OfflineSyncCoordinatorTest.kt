package com.aml_sakr.fitlife.core.data.sync

import com.aml_sakr.fitlife.core.data.connectivity.MutableConnectivityMonitor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class OfflineSyncCoordinatorTest {

    private lateinit var dao: FakeSyncTestDao
    private lateinit var remoteClient: FakeRemoteSyncClient
    private lateinit var connectivityMonitor: MutableConnectivityMonitor
    private lateinit var coordinator: OfflineSyncCoordinator

    @Before
    fun setUp() {
        dao = FakeSyncTestDao()
        remoteClient = FakeRemoteSyncClient()
        connectivityMonitor = MutableConnectivityMonitor(isOnline = true)
        coordinator = OfflineSyncCoordinator(dao, remoteClient, connectivityMonitor)
    }

    @Test
    fun sync_whenOffline_returnsNoConnectivity() = runBlocking {
        // Given
        connectivityMonitor.setConnected(false)
        dao.insert(SyncTestEntity("1", "Local Data", 1000L, "PENDING"))

        // When
        val result = coordinator.sync()

        // Then
        assertFalse(result.success)
        assertEquals("No connectivity", result.error)
        assertEquals("PENDING", dao.getById("1")?.syncStatus)
        assertNull(remoteClient.getRecord("1"))
    }

    @Test
    fun sync_whenOnline_noRemoteRecord_uploadsLocal() = runBlocking {
        // Given
        dao.insert(SyncTestEntity("1", "Local Data", 1000L, "PENDING"))

        // When
        val result = coordinator.sync()

        // Then
        assertTrue(result.success)
        assertEquals(1, result.successCount)
        assertEquals("SYNCED", dao.getById("1")?.syncStatus)
        assertEquals("Local Data", remoteClient.getRecord("1")?.payload)
    }

    @Test
    fun sync_whenOnline_conflict_localNewer_overwritesRemote() = runBlocking {
        // Given
        val local = SyncTestEntity("1", "Newer Local Data", 2000L, "PENDING")
        val remote = SyncTestEntity("1", "Older Remote Data", 1000L, "SYNCED")
        dao.insert(local)
        remoteClient.simulateRemoteWrite(remote)

        // When
        val result = coordinator.sync()

        // Then
        assertTrue(result.success)
        assertEquals(1, result.successCount)
        assertEquals("SYNCED", dao.getById("1")?.syncStatus)
        assertEquals("Newer Local Data", remoteClient.getRecord("1")?.payload)
        assertEquals(2000L, remoteClient.getRecord("1")?.lastModified)
    }

    @Test
    fun sync_whenOnline_conflict_remoteNewer_overwritesLocal() = runBlocking {
        // Given
        val local = SyncTestEntity("1", "Older Local Data", 1000L, "PENDING")
        val remote = SyncTestEntity("1", "Newer Remote Data", 2000L, "SYNCED")
        dao.insert(local)
        remoteClient.simulateRemoteWrite(remote)

        // When
        val result = coordinator.sync()

        // Then
        assertTrue(result.success)
        assertEquals(1, result.successCount)
        assertEquals(1, result.conflictResolvedCount)
        
        // Local Room should be updated with Remote newer data
        val localRecord = dao.getById("1")
        assertNotNull(localRecord)
        assertEquals("Newer Remote Data", localRecord?.payload)
        assertEquals(2000L, localRecord?.lastModified)
        assertEquals("SYNCED", localRecord?.syncStatus)
    }

    @Test
    fun sync_whenOnline_conflict_identicalTimestamps_localWins() = runBlocking {
        // Given
        val local = SyncTestEntity("1", "Local Data", 1000L, "PENDING")
        val remote = SyncTestEntity("1", "Remote Data Same Time", 1000L, "SYNCED")
        dao.insert(local)
        remoteClient.simulateRemoteWrite(remote)

        // When
        val result = coordinator.sync()

        // Then
        assertTrue(result.success)
        assertEquals(1, result.successCount)
        assertEquals("SYNCED", dao.getById("1")?.syncStatus)
        assertEquals("Local Data", remoteClient.getRecord("1")?.payload)
    }
}
