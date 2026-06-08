package com.aml_sakr.fitlife.core.data.sync

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SyncTestEntity::class], version = 1, exportSchema = false)
abstract class SyncTestDatabase : RoomDatabase() {
    abstract fun syncTestDao(): SyncTestDao
}
