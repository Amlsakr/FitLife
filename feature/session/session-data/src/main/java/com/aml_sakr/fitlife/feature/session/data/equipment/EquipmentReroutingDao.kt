package com.aml_sakr.fitlife.feature.session.data.equipment

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EquipmentReroutingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlternatives(entity: EquipmentReroutingEntity)

    @Query("SELECT * FROM equipment_rerouting_cache WHERE exerciseName = :exerciseName")
    suspend fun getAlternativesForExercise(exerciseName: String): EquipmentReroutingEntity?
}
