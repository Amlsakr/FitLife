package com.aml_sakr.fitlife.core.data.workout

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [WorkoutPlanEntity::class], version = 1, exportSchema = false)
@TypeConverters(WorkoutPlanConverters::class)
abstract class WorkoutPlanDatabase : RoomDatabase() {
    abstract fun workoutPlanDao(): WorkoutPlanDao
}
