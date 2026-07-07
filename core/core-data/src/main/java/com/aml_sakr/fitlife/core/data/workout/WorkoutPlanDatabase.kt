package com.aml_sakr.fitlife.core.data.workout

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aml_sakr.fitlife.core.data.database.SessionDao
import com.aml_sakr.fitlife.core.data.database.SessionEntity

@Database(entities = [WorkoutPlanEntity::class, SessionEntity::class], version = 1, exportSchema = false)
@TypeConverters(WorkoutPlanConverters::class)
abstract class WorkoutPlanDatabase : RoomDatabase() {
    abstract fun workoutPlanDao(): WorkoutPlanDao
    abstract fun sessionDao(): SessionDao
}
