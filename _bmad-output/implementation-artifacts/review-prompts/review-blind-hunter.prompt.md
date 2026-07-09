# Blind Hunter Prompt

You are an elite code reviewer in "Blind Hunter" mode. You have NO context about the project's architecture, requirements, or existing codebase beyond what is visible in the provided diff. Your goal is to find technical issues, anti-patterns, security risks, and logical errors in the changes themselves.

## Diff to Review

```diff
diff --git a/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncStatus.kt b/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncStatus.kt
index b0aa89f..e3c0e7d 100644
--- a/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncStatus.kt
+++ b/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncStatus.kt
@@ -1,6 +1,6 @@
 package com.aml_sakr.fitlife.core.data.sync
 
 enum class SyncStatus {
-    PENDING,
+    NOT_SYNCED,
     SYNCED
 }
diff --git a/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncableEntity.kt b/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncableEntity.kt
new file mode 100644
index 0000000..1872a09
--- /dev/null
+++ b/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncableEntity.kt
@@ -0,0 +1,9 @@
+package com.aml_sakr.fitlife.core.data.sync
+
+interface SyncableEntity<T : SyncableEntity<T>> {
+    val syncId: String
+    val lastModified: Long
+    val syncStatus: SyncStatus
+    
+    fun withSyncStatus(status: SyncStatus): T
+}
diff --git a/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncableDao.kt b/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncableDao.kt
new file mode 100644
index 0000000..093f27d
--- /dev/null
+++ b/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncableDao.kt
@@ -0,0 +1,7 @@
+package com.aml_sakr.fitlife.core.data.sync
+
+interface SyncableDao<T : SyncableEntity<T>> {
+    suspend fun getById(id: String): T?
+    suspend fun getUnsyncedRecords(): List<T>
+    suspend fun update(entity: T)
+}
diff --git a/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncAgent.kt b/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncAgent.kt
new file mode 100644
index 0000000..38676d6
--- /dev/null
+++ b/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncAgent.kt
@@ -0,0 +1,60 @@
+package com.aml_sakr.fitlife.core.data.sync
+
+interface SyncAgent {
+    suspend fun sync(): SyncResult
+}
+
+class DefaultSyncAgent<T : SyncableEntity<T>>(
+    private val dao: SyncableDao<T>,
+    private val remoteClient: RemoteSyncClient<T>
+) : SyncAgent {
+    override suspend fun sync(): SyncResult {
+        var successCount = 0
+        var failureCount = 0
+        var conflictResolvedCount = 0
+
+        val unsynced = dao.getUnsyncedRecords()
+        for (staleLocal in unsynced) {
+            val local = dao.getById(staleLocal.syncId) ?: continue
+            if (local.syncStatus == SyncStatus.SYNCED) continue
+
+            val remote = remoteClient.getRecord(local.syncId)
+            if (remote == null) {
+                if (remoteClient.saveRecord(local)) {
+                    dao.update(local.withSyncStatus(SyncStatus.SYNCED))
+                    successCount++
+                } else {
+                    failureCount++
+                }
+                continue
+            }
+
+            when {
+                local.lastModified > remote.lastModified -> {
+                    if (remoteClient.saveRecord(local)) {
+                        dao.update(local.withSyncStatus(SyncStatus.SYNCED))
+                        successCount++
+                    } else {
+                        failureCount++
+                    }
+                }
+                local.lastModified < remote.lastModified -> {
+                    dao.update(remote.withSyncStatus(SyncStatus.SYNCED))
+                    conflictResolvedCount++
+                    successCount++
+                }
+                else -> {
+                    if (remoteClient.saveRecord(local)) {
+                        dao.update(local.withSyncStatus(SyncStatus.SYNCED))
+                        conflictResolvedCount++
+                        successCount++
+                    } else {
+                        failureCount++
+                    }
+                }
+            }
+        }
+
+        return SyncResult(
+            success = failureCount == 0,
+            successCount = successCount,
+            failureCount = failureCount,
+            conflictResolvedCount = conflictResolvedCount
+        )
+    }
+}
diff --git a/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/RemoteSyncClient.kt b/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/RemoteSyncClient.kt
index 26c4ab2..06311c5 100644
--- a/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/RemoteSyncClient.kt
+++ b/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/RemoteSyncClient.kt
@@ -1,6 +1,6 @@
 package com.aml_sakr.fitlife.core.data.sync
 
-interface RemoteSyncClient {
-    suspend fun getRecord(id: String): SyncTestEntity?
-    suspend fun saveRecord(record: SyncTestEntity): Boolean
+interface RemoteSyncClient<T : SyncableEntity<T>> {
+    suspend fun getRecord(id: String): T?
+    suspend fun saveRecord(record: T): Boolean
 }
diff --git a/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/OfflineSyncCoordinator.kt b/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/OfflineSyncCoordinator.kt
index b6b2b41..033c48f 100644
--- a/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/OfflineSyncCoordinator.kt
+++ b/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/OfflineSyncCoordinator.kt
@@ -1,65 +1,46 @@
 package com.aml_sakr.fitlife.core.data.sync
 
 import com.aml_sakr.fitlife.core.data.connectivity.ConnectivityMonitor
 import kotlinx.coroutines.Dispatchers
 import kotlinx.coroutines.withContext
 
 class OfflineSyncCoordinator(
-    private val dao: SyncTestDao,
-    private val remoteClient: RemoteSyncClient,
+    private val agents: List<SyncAgent>,
     private val connectivityMonitor: ConnectivityMonitor
 ) {
     suspend fun sync(): SyncResult = withContext(Dispatchers.IO) {
         if (!connectivityMonitor.isConnected()) {
             return@withContext SyncResult(success = false, error = "No connectivity")
         }
 
         var successCount = 0
         var failureCount = 0
         var conflictResolvedCount = 0
+        var totalError: String? = null
 
-        try {
-            val unsynced = dao.getUnsyncedRecords()
-            for (staleLocal in unsynced) {
-                val local = dao.getById(staleLocal.id) ?: continue
-                if (local.syncStatus == SyncStatus.SYNCED) {
-                    continue
-                }
-
-                val remote = remoteClient.getRecord(local.id)
-                if (remote == null) {
-                    val uploaded = remoteClient.saveRecord(local)
-                    if (uploaded) {
-                        dao.update(local.copy(syncStatus = SyncStatus.SYNCED))
-                        successCount++
-                    } else {
-                        failureCount++
-                    }
-                    continue
-                }
-
-                when {
-                    local.lastModified > remote.lastModified -> {
-                        val uploaded = remoteClient.saveRecord(local)
-                        if (uploaded) {
-                            dao.update(local.copy(syncStatus = SyncStatus.SYNCED))
-                            successCount++
-                        } else {
-                            failureCount++
-                        }
-                    }
-                    local.lastModified < remote.lastModified -> {
-                        dao.update(remote.copy(syncStatus = SyncStatus.SYNCED))
-                        conflictResolvedCount++
-                        successCount++
-                    }
-                    else -> {
-                        val uploaded = remoteClient.saveRecord(local)
-                        if (uploaded) {
-                            dao.update(local.copy(syncStatus = SyncStatus.SYNCED))
-                            conflictResolvedCount++
-                            successCount++
-                        } else {
-                            failureCount++
-                        }
-                    }
-                }
-            }
-            val overallSuccess = failureCount == 0
-            SyncResult(
-                success = overallSuccess,
-                successCount = successCount,
-                failureCount = failureCount,
-                conflictResolvedCount = conflictResolvedCount,
-                error = if (overallSuccess) null else "Partial sync failure"
-            )
-        } catch (e: Exception) {
-            SyncResult(success = false, error = e.message ?: "Unknown error")
+        for (agent in agents) {
+            val result = agent.sync()
+            successCount += result.successCount
+            failureCount += result.failureCount
+            conflictResolvedCount += result.conflictResolvedCount
+            if (!result.success) {
+                totalError = if (totalError == null) result.error else "$totalError; ${result.error}"
+            }
         }
+
+        val overallSuccess = failureCount == 0
+        SyncResult(
+            success = overallSuccess,
+            successCount = successCount,
+            failureCount = failureCount,
+            conflictResolvedCount = conflictResolvedCount,
+            error = if (overallSuccess) null else (totalError ?: "Partial sync failure")
+        )
     }
 }
```

## Instructions

1.  Review the diff meticulously.
2.  Focus on:
    *   **Logical Errors**: Incorrect conditions, off-by-one, etc.
    *   **Concurrency**: Thread safety issues.
    *   **Generic Safety**: Type safety in generic implementation.
    *   **Error Handling**: Swallowed exceptions, incomplete error reporting.
3.  Output findings as a Markdown list. Each finding should have a one-line title and a clear explanation.
4.  Be adversarial. Find what's wrong.
