# Implementation Plan - Room + Firestore Offline Sync Spike

This implementation plan details the strategy to design, implement, and verify the offline-first sync spike (`SETUP-006`) for the FitLife project.

## User Review Required

> [!IMPORTANT]
> **Dependency Versions:** We propose adding Room version `2.8.4` and WorkManager version `2.11.2`. These are the latest stable versions matching our project context requirements.
> **Emulator-First Testing:** Real Firestore and Room operations will be tested in instrumented tests using the Firebase Local Emulator and in-memory databases. No live cloud Firestore instance is used in automated tests.
> **Clean Architecture Boundary:** Room entities, DAOs, and Firestore client SDK dependencies are restricted to the `:core:core-data` module. The `:core:core-domain` module will remain a pure Kotlin/JVM library.

## Open Questions

> [!NOTE]
> There are no blocking open questions. The spike is fully specified in the story file.

---

## Proposed Changes

### Version Catalog & Module Dependencies

#### [MODIFY] [libs.versions.toml](file:///d:/LinkDevProject/FitLife/gradle/libs.versions.toml)
*   Add versions: `room = "2.8.4"`, `workmanager = "2.11.2"`.
*   Add libraries:
    *   `androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }`
    *   `androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }`
    *   `androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }`
    *   `androidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "workmanager" }`
    *   `firebase-firestore = { group = "com.google.firebase", name = "firebase-firestore" }` (managed under BOM)

#### [MODIFY] [core-data/build.gradle.kts](file:///d:/LinkDevProject/FitLife/core/core-data/build.gradle.kts)
*   Apply KSP plugin: `alias(libs.plugins.ksp)` (already present).
*   Add dependencies: Room runtime, Room compiler (via `ksp`), Room KTX extension, WorkManager runtime KTX, and Firebase Firestore.
*   Add testing dependencies: `androidx.work:work-testing` for WorkManager testing (if needed, otherwise manually run sync).

---

### Sync Engine Core (:core:core-data)

#### [NEW] [SyncTestEntity.kt](file:///d:/LinkDevProject/FitLife/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncTestEntity.kt)
*   Define a Room entity representing a test item for sync:
    ```kotlin
    @Entity(tableName = "sync_test_records")
    data class SyncTestEntity(
        @PrimaryKey val id: String,
        val payload: String,
        val lastModified: Long,
        val syncStatus: String // "PENDING", "SYNCED"
    )
    ```

#### [NEW] [SyncTestDao.kt](file:///d:/LinkDevProject/FitLife/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncTestDao.kt)
*   Define DAO operations for local persistence: `insert`, `update`, `getById`, `getUnsyncedRecords`, and Flow-based query `observeUnsyncedCount`.

#### [NEW] [SyncTestDatabase.kt](file:///d:/LinkDevProject/FitLife/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncTestDatabase.kt)
*   Define Room database with `SyncTestEntity`.

#### [NEW] [FirestoreSyncAdapter.kt](file:///d:/LinkDevProject/FitLife/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/FirestoreSyncAdapter.kt)
*   Writers and readers for Firebase Firestore. Maps `SyncTestEntity` to Firestore documents and handles latest-timestamp wins conflict reconciliation logic.

#### [NEW] [SyncWorker.kt](file:///d:/LinkDevProject/FitLife/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncWorker.kt)
*   A WorkManager `CoroutineWorker` that fetches pending local records, checks connection, reconciles timestamps with remote documents, uploads to Firestore, and marks local records as `SYNCED`.

#### [NEW] [SyncHarness.kt](file:///d:/LinkDevProject/FitLife/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncHarness.kt)
*   A coordinator class to toggle mock network status, write to Room, trigger sync, and query Firestore to output sync latency and success metrics.

---

### Verification Plan

### Automated Tests
*   **Unit Tests:** Pure JVM tests under `core/core-data/src/test` verifying mapping, conflict resolution calculations, and sync status state transitions.
    *   Command: `./gradlew.bat :core:core-data:testDebugUnitTest --no-daemon --console=plain`
*   **Android Instrumented Tests:** Tests under `core/core-data/src/androidTest` verifying real Room in-memory database interactions, transaction safety, and worker execution.
    *   Command: `./gradlew.bat :core:core-data:connectedAndroidTest` (gated/run if device is attached)

### Manual Verification
*   Execute a local sync verification using the test harness and output the benchmark results in a decision report at `_bmad-output/implementation-artifacts/spike-room-firestore-offline-sync-report.md`.
