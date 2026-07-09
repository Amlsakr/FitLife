package com.aml_sakr.fitlife.core.data.sync

import com.aml_sakr.fitlife.core.data.connectivity.MutableConnectivityMonitor
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class OfflineSyncCoordinatorTest {

    private lateinit var dao: FakeSyncableDao<MockSyncableEntity>
    private lateinit var remoteClient: FakeRemoteSyncClient<MockSyncableEntity>
    private lateinit var connectivityMonitor: MutableConnectivityMonitor
    private lateinit var coordinator: OfflineSyncCoordinator

    @Before
    fun setUp() {
        dao = FakeSyncableDao()
        remoteClient = FakeRemoteSyncClient()
        connectivityMonitor = MutableConnectivityMonitor(isOnline = true)
        
        val agent = DefaultSyncAgent(dao, remoteClient)
        coordinator = OfflineSyncCoordinator(listOf(agent), connectivityMonitor)
    }

    @Test
    fun sync_whenOffline_returnsNoConnectivity() = runBlocking {
        // Given
        connectivityMonitor.setConnected(false)
        dao.insert(MockSyncableEntity("1", "Local Data", 1000L, SyncStatus.NOT_SYNCED))

        // When
        val result = coordinator.sync()

        // Then
        assertFalse(result.success)
        assertEquals("No connectivity", result.error)
        assertEquals(SyncStatus.NOT_SYNCED, dao.getById("1")?.syncStatus)
        assertNull(remoteClient.getRecord("1"))
    }

    @Test
    fun sync_whenOnline_noRemoteRecord_uploadsLocal() = runBlocking {
        // Given
        dao.insert(MockSyncableEntity("1", "Local Data", 1000L, SyncStatus.NOT_SYNCED))

        // When
        val result = coordinator.sync()

        // Then
        assertTrue(result.success)
        assertEquals(1, result.successCount)
        assertEquals(SyncStatus.SYNCED, dao.getById("1")?.syncStatus)
        assertEquals("Local Data", remoteClient.getRecord("1")?.payload)
    }

    @Test
    fun sync_whenOnline_conflict_localNewer_overwritesRemote() = runBlocking {
        // Given
        val local = MockSyncableEntity("1", "Newer Local Data", 2000L, SyncStatus.NOT_SYNCED)
        val remote = MockSyncableEntity("1", "Older Remote Data", 1000L, SyncStatus.SYNCED)
        dao.insert(local)
        remoteClient.simulateRemoteWrite(remote)

        // When
        val result = coordinator.sync()

        // Then
        assertTrue(result.success)
        assertEquals(1, result.successCount)
        assertEquals(SyncStatus.SYNCED, dao.getById("1")?.syncStatus)
        assertEquals("Newer Local Data", remoteClient.getRecord("1")?.payload)
        assertEquals(2000L, remoteClient.getRecord("1")?.lastModified)
    }

    @Test
    fun sync_whenOnline_conflict_remoteNewer_overwritesLocal() = runBlocking {
        // Given
        val local = MockSyncableEntity("1", "Older Local Data", 1000L, SyncStatus.NOT_SYNCED)
        val remote = MockSyncableEntity("1", "Newer Remote Data", 2000L, SyncStatus.SYNCED)
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
        assertEquals(SyncStatus.SYNCED, localRecord?.syncStatus)
    }

    @Test
    fun sync_whenOnline_conflict_identicalTimestamps_localWins() = runBlocking {
        // Given
        val local = MockSyncableEntity("1", "Local Data", 1000L, SyncStatus.NOT_SYNCED)
        val remote = MockSyncableEntity("1", "Remote Data Same Time", 1000L, SyncStatus.SYNCED)
        dao.insert(local)
        remoteClient.simulateRemoteWrite(remote)

        // When
        val result = coordinator.sync()

        // Then
        assertTrue(result.success)
        assertEquals(1, result.successCount)
        assertEquals(SyncStatus.SYNCED, dao.getById("1")?.syncStatus)
        assertEquals("Local Data", remoteClient.getRecord("1")?.payload)
    }

    @Test
    fun sync_reconcilesServerTimestampAfterUpload() = runBlocking {
        // Given
        val local = MockSyncableEntity("1", "Local Data", 1000L, SyncStatus.NOT_SYNCED)
        dao.insert(local)
        remoteClient.nextServerTime = 5000L // Server assigns a much later timestamp

        // When
        val result = coordinator.sync()

        // Then
        assertTrue(result.success)
        val localRecord = dao.getById("1")
        assertEquals(5000L, localRecord?.lastModified)
        assertEquals(SyncStatus.SYNCED, localRecord?.syncStatus)
    }
}
