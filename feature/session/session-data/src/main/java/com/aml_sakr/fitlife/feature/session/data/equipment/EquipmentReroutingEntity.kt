package com.aml_sakr.fitlife.feature.session.data.equipment

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "equipment_rerouting_cache")
data class EquipmentReroutingEntity(
    @PrimaryKey val exerciseName: String,
    val alternativesJson: String,  // JSON array of ExerciseAlternative
    val fetchedAt: Long,
    val expiresAt: Long  // fetchedAt + 24 hours
)
