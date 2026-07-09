package com.aml_sakr.fitlife.core.data.sync

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncTestDao : SyncableDao<SyncTestEntity> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SyncTestEntity)

    @Update
    override suspend fun update(entity: SyncTestEntity)

    @Query("SELECT * FROM sync_test_records WHERE id = :id")
    override suspend fun getById(id: String): SyncTestEntity?

    @Query("SELECT * FROM sync_test_records WHERE syncStatus = 'NOT_SYNCED'")
    override suspend fun getUnsyncedRecords(): List<SyncTestEntity>

    @Query("SELECT COUNT(*) FROM sync_test_records WHERE syncStatus = 'NOT_SYNCED'")
    fun observeUnsyncedCount(): Flow<Int>

    @Query("SELECT * FROM sync_test_records")
    suspend fun getAllRecords(): List<SyncTestEntity>

    @Query("DELETE FROM sync_test_records")
    suspend fun deleteAll()
}
