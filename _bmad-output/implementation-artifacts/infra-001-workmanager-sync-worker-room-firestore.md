# Story 6.1: WorkManager Sync Worker (Room → Firestore)

Status: done

## Story

As a developer,
I want background sync,
so that user data stays consistent across devices.

## Acceptance Criteria

1. Worker runs every 6 hours (or on network change). [Source: docs/fitlife-stories-v1.md#INFRA-001]
2. Syncs unsynced entities (`WorkoutPlan` and `Session`). [Source: docs/fitlife-stories-v1.md#INFRA-001]
3. Retries with exponential back-off. [Source: docs/fitlife-stories-v1.md#INFRA-001]
4. Conflict resolution: latest-timestamp wins (server timestamps are stored for reconciliation). [Source: _bmad-output/planning-artifacts/fitlife-architecture-v1.md#Section-4]

## Tasks / Subtasks

- [x] Refactor Sync Infrastructure to be Generic (AC: #2)
  - [x] Convert `RemoteSyncClient` and `OfflineSyncCoordinator` to support multiple entity types or use generics.
  - [x] Remove `SyncTestEntity` / `SyncTestDao` dependencies in production code.
- [x] Update Room Entities for Sync (AC: #2, #4)
  - [x] Add `syncStatus` (enum: SYNCED, NOT_SYNCED) and `lastModified` (Long) to `WorkoutPlanEntity`.
  - [x] Add `syncStatus` and `lastModified` to `SessionEntity`.
  - [x] Update `WorkoutPlanDao` and `SessionDao` to include `getUnsyncedRecords()` and `updateSyncStatus()`.
- [x] Implement Firestore Sync Logic (AC: #2, #4)
  - [x] Implement `WorkoutPlanFirestoreClient` and `SessionFirestoreClient` (or a generic one).
  - [x] Ensure `serverUpdatedAt` is used for reconciliation.
- [x] Configure WorkManager (AC: #1, #3)
  - [x] Update `SyncWorker` to handle the generic sync process.
  - [x] Update `SyncWorkScheduler` to use `PeriodicWorkRequestBuilder` with 6-hour interval.
  - [x] Ensure exponential back-off is configured in the work request.
- [x] Verification
  - [x] Verify sync starts on network restoration.
  - [x] Verify local changes are pushed to Firestore.
  - [x] Verify remote changes (newer timestamp) are pulled to Room.

### Review Findings

- [x] [Review][Patch] Robustness: Single record failure kills agent sync [core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncAgent.kt:15]
- [x] [Review][Patch] Reconciliation: Clock skew & `serverUpdatedAt` [core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncAgent.kt:33]
- [x] [Review][Patch] Performance: Missing index on `syncStatus` [core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/workout/WorkoutPlanEntity.kt:10]
- [x] [Review][Patch] Cleanup: Redundant DAO methods [core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/workout/WorkoutPlanDao.kt:20]

## Dev Notes

- **Architecture Pattern:** Room is the source of truth. Worker watches Room changes (or runs periodically) and syncs to Firestore. [Source: _bmad-output/planning-artifacts/fitlife-architecture-v1.md#Section-4]
- **Conflict Resolution:** Latest-timestamp wins. `lastModified` on local entity vs `lastModified`/`serverUpdatedAt` on Firestore document.
- **Dependencies:** Uses `WorkManager` for background tasks. Already has a baseline implementation from SETUP-006 technical spike.
- **Modules Affected:** `:core:core-data`.

### Project Structure Notes

- Sync logic resides in `com.aml_sakr.fitlife.core.data.sync`.
- Entities are in `com.aml_sakr.fitlife.core.data.workout` and `com.aml_sakr.fitlife.core.data.database`.
- Firestore interaction uses `com.google.firebase.firestore.FirebaseFirestore`.

### References

- [Source: docs/fitlife-stories-v1.md#INFRA-001]
- [Source: _bmad-output/planning-artifacts/fitlife-architecture-v1.md#Section-4]
- [Source: com.aml_sakr.fitlife.core.data.sync baseline from SETUP-006 spike]

## Dev Agent Record

### Agent Model Used

- Gemini 2.0 Flash

### Debug Log References

- 2026-07-08: Generic sync infrastructure implemented. Verified via unit tests (`OfflineSyncCoordinatorTest`).
- 2026-07-08: Room entities and DAOs updated for `WorkoutPlan` and `Session`.
- 2026-07-08: Firestore clients implemented for sync.
- 2026-07-08: WorkManager configured for 6-hour periodic sync with exponential backoff.

### Completion Notes List

- Refactored sync to be generic via `SyncableEntity`, `SyncableDao`, and `SyncAgent`.
- Implemented `WorkoutPlanFirestoreClient` and `SessionFirestoreClient`.
- Updated `SyncWorker` and `SyncWorkScheduler` to use the new architecture.
- Cleaned up production code by removing old sync test artifacts from `SyncModule`.

### File List

- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncStatus.kt` (UPDATE)
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncableEntity.kt` (NEW)
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncableDao.kt` (NEW)
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncAgent.kt` (NEW)
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/RemoteSyncClient.kt` (UPDATE)
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/FirestoreRemoteSyncClient.kt` (UPDATE)
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/WorkoutPlanFirestoreClient.kt` (NEW)
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SessionFirestoreClient.kt` (NEW)
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/OfflineSyncCoordinator.kt` (UPDATE)
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncModule.kt` (UPDATE)
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncEntryPoint.kt` (UPDATE)
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncWorker.kt` (UPDATE)
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncWorkScheduler.kt` (UPDATE)
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/workout/WorkoutPlanEntity.kt` (UPDATE)
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/database/SessionEntity.kt` (UPDATE)
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/workout/WorkoutPlanDao.kt` (UPDATE)
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/database/SessionDao.kt` (UPDATE)
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/workout/WorkoutPlanDatabase.kt` (UPDATE)
- `core/core-data/src/test/java/com/aml_sakr/fitlife/core/data/sync/FakeSyncableDao.kt` (NEW)
- `core/core-data/src/test/java/com/aml_sakr/fitlife/core/data/sync/FakeRemoteSyncClient.kt` (UPDATE)
- `core/core-data/src/test/java/com/aml_sakr/fitlife/core/data/sync/MockSyncableEntity.kt` (NEW)
- `core/core-data/src/test/java/com/aml_sakr/fitlife/core/data/sync/OfflineSyncCoordinatorTest.kt` (UPDATE)
