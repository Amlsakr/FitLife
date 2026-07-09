# Blind Hunter Prompt (Chunk 2: Entities & DAOs)

You are an elite code reviewer. Review the provided diff for Room Entities and DAOs related to the sync feature.

## Diff to Review

```diff
diff --git a/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/workout/WorkoutPlanEntity.kt b/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/workout/WorkoutPlanEntity.kt
index 5483f9a..83c0e7d 100644
--- a/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/workout/WorkoutPlanEntity.kt
+++ b/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/workout/WorkoutPlanEntity.kt
@@ -1,6 +1,6 @@
 package com.aml_sakr.fitlife.core.data.workout
 
 import androidx.room.Entity
 import androidx.room.PrimaryKey
+import com.aml_sakr.fitlife.core.data.sync.SyncStatus
+import com.aml_sakr.fitlife.core.data.sync.SyncableEntity
 
 @Entity(tableName = "workout_plans")
 data class WorkoutPlanEntity(
     @PrimaryKey val planId: String,
     val userId: String,
     val requestKey: String,
     val generatedAtEpochMillis: Long,
     val expiresAtEpochMillis: Long,
     val fitnessLevel: String,
     val location: String,
     val requestedDays: Int,
     val goalNames: List<String>,
     val equipmentNames: List<String>,
     val weekNumber: Int,
     val isFallback: Boolean,
-    val planJson: String
-)
+    val planJson: String,
+    override val syncStatus: SyncStatus = SyncStatus.NOT_SYNCED,
+    override val lastModified: Long = generatedAtEpochMillis
+) : SyncableEntity<WorkoutPlanEntity> {
+    override val syncId: String get() = planId
+    
+    override fun withSyncStatus(status: SyncStatus): WorkoutPlanEntity = 
+        copy(syncStatus = status)
+        
+    override fun withSyncMetadata(status: SyncStatus, lastModified: Long): WorkoutPlanEntity =
+        copy(syncStatus = status, lastModified = lastModified)
+}
diff --git a/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/database/SessionEntity.kt b/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/database/SessionEntity.kt
index 5483f9a..83c0e7d 100644
--- a/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/database/SessionEntity.kt
+++ b/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/database/SessionEntity.kt
@@ -1,6 +1,6 @@
 package com.aml_sakr.fitlife.core.data.database
 
 import androidx.room.Entity
 import androidx.room.PrimaryKey
+import com.aml_sakr.fitlife.core.data.sync.SyncStatus
+import com.aml_sakr.fitlife.core.data.sync.SyncableEntity
 
 @Entity(tableName = "sessions")
 data class SessionEntity(
     @PrimaryKey val sessionId: String,
     val userId: String,
     val planId: String,
     val workoutDayId: String,
     val startTime: Long,
     val endTime: Long?,
     val durationSeconds: Int?,
     val totalReps: Int,
     val totalSets: Int,
     val fatigueEventCount: Int,
     val audioFallbackUsed: Boolean,
     val completionPercentage: Float,
-    val whatsAppShared: Boolean
-)
+    val whatsAppShared: Boolean,
+    override val syncStatus: SyncStatus = SyncStatus.NOT_SYNCED,
+    override val lastModified: Long = startTime
+) : SyncableEntity<SessionEntity> {
+    override val syncId: String get() = sessionId
+    
+    override fun withSyncStatus(status: SyncStatus): SessionEntity = 
+        copy(syncStatus = status)
+        
+    override fun withSyncMetadata(status: SyncStatus, lastModified: Long): SessionEntity =
+        copy(syncStatus = status, lastModified = lastModified)
+}
diff --git a/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/workout/WorkoutPlanDao.kt b/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/workout/WorkoutPlanDao.kt
index 5483f9a..83c0e7d 100644
--- a/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/workout/WorkoutPlanDao.kt
+++ b/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/workout/WorkoutPlanDao.kt
@@ -1,6 +1,6 @@
 package com.aml_sakr.fitlife.core.data.workout
 
-import androidx.room.Dao
-import androidx.room.Insert
-import androidx.room.OnConflictStrategy
-import androidx.room.Query
+import androidx.room.*
+import com.aml_sakr.fitlife.core.data.sync.SyncableDao
 
 @Dao
-interface WorkoutPlanDao {
+interface WorkoutPlanDao : SyncableDao<WorkoutPlanEntity> {
     @Insert(onConflict = OnConflictStrategy.REPLACE)
     suspend fun insert(entity: WorkoutPlanEntity)
 
+    @Update
+    override suspend fun update(entity: WorkoutPlanEntity)
+
+    @Query("SELECT * FROM workout_plans WHERE planId = :id")
+    override suspend fun getById(id: String): WorkoutPlanEntity?
+    
+    @Query("SELECT * FROM workout_plans WHERE syncStatus = 'NOT_SYNCED'")
+    override suspend fun getUnsyncedRecords(): List<WorkoutPlanEntity>
+...
diff --git a/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/database/SessionDao.kt b/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/database/SessionDao.kt
index 5483f9a..83c0e7d 100644
--- a/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/database/SessionDao.kt
+++ b/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/database/SessionDao.kt
@@ -1,6 +1,6 @@
 package com.aml_sakr.fitlife.core.data.database
 
-import androidx.room.Dao
-import androidx.room.Insert
-import androidx.room.OnConflictStrategy
-import androidx.room.Query
+import androidx.room.*
+import com.aml_sakr.fitlife.core.data.sync.SyncableDao
 
 @Dao
-interface SessionDao {
+interface SessionDao : SyncableDao<SessionEntity> {
     @Insert(onConflict = OnConflictStrategy.REPLACE)
     suspend fun insertSession(session: SessionEntity)
 
+    @Update
+    override suspend fun update(entity: SessionEntity)
+
+    @Query("SELECT * FROM sessions WHERE sessionId = :id")
+    override suspend fun getById(id: String): SessionEntity?
+
+    @Query("SELECT * FROM sessions WHERE syncStatus = 'NOT_SYNCED'")
+    override suspend fun getUnsyncedRecords(): List<SessionEntity>
+...
diff --git a/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/workout/WorkoutPlanDatabase.kt b/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/workout/WorkoutPlanDatabase.kt
index 5483f9a..83c0e7d 100644
--- a/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/workout/WorkoutPlanDatabase.kt
+++ b/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/workout/WorkoutPlanDatabase.kt
@@ -1,6 +1,6 @@
 package com.aml_sakr.fitlife.core.data.workout
 
 import androidx.room.Database
 import androidx.room.RoomDatabase
 import androidx.room.TypeConverters
 import com.aml_sakr.fitlife.core.data.database.SessionDao
 import com.aml_sakr.fitlife.core.data.database.SessionEntity
+import com.aml_sakr.fitlife.core.data.sync.SyncTypeConverters
 
-@Database(entities = [WorkoutPlanEntity::class, SessionEntity::class], version = 1, exportSchema = false)
-@TypeConverters(WorkoutPlanConverters::class)
+@Database(entities = [WorkoutPlanEntity::class, SessionEntity::class], version = 1, exportSchema = false)
+@TypeConverters(WorkoutPlanConverters::class, SyncTypeConverters::class)
 abstract class WorkoutPlanDatabase : RoomDatabase() {
```

## Instructions

1.  Check for **Schema Integrity**: Are `syncStatus` and `lastModified` correctly integrated?
2.  Check for **DAO Consistency**: Do the overridden methods correctly match the SQL queries?
3.  Check for **Room best practices**: Indexing, conflict strategies, type converters.
4.  Be adversarial.
