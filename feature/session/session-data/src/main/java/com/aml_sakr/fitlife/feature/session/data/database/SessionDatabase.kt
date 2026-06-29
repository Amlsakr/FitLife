package com.aml_sakr.fitlife.feature.session.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aml_sakr.fitlife.feature.session.data.equipment.EquipmentReroutingDao
import com.aml_sakr.fitlife.feature.session.data.equipment.EquipmentReroutingEntity

@Database(
    entities = [EquipmentReroutingEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SessionDatabase : RoomDatabase() {
    abstract fun equipmentReroutingDao(): EquipmentReroutingDao
}
