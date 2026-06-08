# Story SETUP-006: Technical Spike - Room + Firestore Offline Sync

Status: done

Completion Note: Ultimate context engine analysis completed - comprehensive developer guide created.

## Story

As a FitLife developer,
I want to validate the offline-first sync pattern using Room for local persistence, Firebase Firestore for remote storage, and WorkManager for background synchronization,
so that the MVP can reliably persist user data while offline and sync it automatically when connectivity is restored.

## Acceptance Criteria

1. A temporary offline sync benchmark/test harness runs Room database writes and Firestore collection uploads using the project architecture boundaries (not UI or ViewModel code).
2. The harness validates that data written to the local Room database persists offline (i.e., is immediately readable locally when simulated network is disabled).
3. The harness validates that when network connectivity is restored, a WorkManager-backed sync worker is triggered to upload/update corresponding documents in Firestore.
4. The sync worker resolves conflicts using a "latest-timestamp wins" strategy; reconciliation logic must handle server timestamps properly, and conflict resolution must be tested with simulated concurrent edits.
5. The spike tests offline-to-online transitions by:
   - Simulating offline state (using a mock connectivity monitor or network interception).
   - Writing data to the local Room database.
   - Restoring network state.
   - Triggering the WorkManager sync worker.
   - Verifying that Firestore contains the exact synchronized data.
6. Pass decision: Data successfully persists locally in Room while offline, syncs to Firestore upon network restoration, and conflict resolution resolves concurrent edits correctly based on timestamps without crashing or dropping data.
7. Fail decision: If offline-first sync cannot be reliably achieved or introduces unmanageable conflict overhead, the report recommends using Firestore only (no offline local db mirroring) and notes this scope adjustment in the sprint, matching the architecture contingency.
8. The spike does not implement production UI screens, actual workout plan creation, session tracking UI, Hilt bindings for final UI screens, or production navigation graphs.
9. `./gradlew.bat test --no-daemon --console=plain` passes, and any module touched by the harness compiles.

## Tasks / Subtasks

- [ ] Confirm spike scope and environment. (AC: 1, 2, 5, 6)
  - [ ] Identify the test models and data structures (e.g. a simple sync test entity with unique ID, payload, local timestamp, and sync status).
  - [ ] Define the simulated network toggle interface for manual and automated testing.
  - [ ] Record initial environment setup (Android API, test device, emulator or JVM testing context).
- [ ] Add required dependencies to version catalog and modules. (AC: 1, 8, 9)
  - [ ] Add Room version `2.8.4` (runtime, compiler/ksp, ktx) to [libs.versions.toml](file:///d:/LinkDevProject/FitLife/gradle/libs.versions.toml).
  - [ ] Add WorkManager version `2.11.2` (runtime-ktx) to [libs.versions.toml](file:///d:/LinkDevProject/FitLife/gradle/libs.versions.toml).
  - [ ] Add Firebase Firestore (without version, using the existing Firebase BOM) to [libs.versions.toml](file:///d:/LinkDevProject/FitLife/gradle/libs.versions.toml).
  - [ ] Add dependencies to [:core:core-data](file:///d:/LinkDevProject/FitLife/core/core-data/build.gradle.kts); do not leak Room or Firebase dependencies into [:core:core-domain](file:///d:/LinkDevProject/FitLife/core/core-domain/build.gradle.kts).
- [ ] Implement Room Local Database. (AC: 1, 2, 5)
  - [ ] Create a temporary Room entity representing a sync-test object with a unique ID, payload (string), lastModified timestamp, and syncStatus (e.g. PENDING, SYNCED).
  - [ ] Implement the Room DAO with insert, update, delete, and Flow-based queries for tracking database changes.
  - [ ] Create a Room database instance configured for in-memory testing and a production-like persistent configuration.
- [ ] Implement Firestore Remote Database integration. (AC: 1, 3, 5)
  - [ ] Set up Firestore service adapter to handle document writes and updates under a temporary sync-test collection.
  - [ ] Implement Firestore listeners or direct write APIs using Firebase coroutines interop.
  - [ ] Ensure Firestore operations are gated by emulator-first configuration (`useEmulator`) during testing.
- [ ] Build WorkManager Sync Worker. (AC: 1, 3, 4, 5)
  - [ ] Implement `SyncWorker` extending `CoroutineWorker` that reads unsynced Room records, writes them to Firestore, and marks them as synced in Room.
  - [ ] Implement conflict reconciliation (latest-timestamp wins) by comparing local and remote timestamps before updating.
  - [ ] Configure WorkManager queueing with network constraints (e.g. `NetworkType.CONNECTED`).
- [ ] Build the test/benchmark harness. (AC: 1, 2, 3, 4, 5)
  - [ ] Implement a test class or harness that runs the offline write -> restore network -> sync flow.
  - [ ] Verify local read/write works while offline.
  - [ ] Verify remote sync works and check Firestore document content after sync.
  - [ ] Test conflict resolution under simulated race conditions (simultaneous offline edits on local Room and remote Firestore).
- [ ] Capture metrics and produce a decision report. (AC: 6, 7)
  - [ ] Record latency of local write, sync execution time, conflict resolution success rate, and data integrity metrics.
  - [ ] Save the decision report in `_bmad-output/implementation-artifacts/spike-room-firestore-offline-sync-report.md`.
  - [ ] Include a clear pass/fail recommendation and follow-up guidance for future stories (e.g., `infra-001-workmanager-sync-worker-room-firestore`).
- [ ] Verify and clean scope. (AC: 8, 9)
  - [ ] Ensure no production UI or navigation is added.
  - [ ] Run `./gradlew.bat test --no-daemon --console=plain` to verify build and test passes.
  - [ ] Ensure all automated tests run against in-memory Room and Firestore emulator, with no reliance on live cloud databases or real network.

### Review Findings

#### Current Code Review - 2026-06-08

- [x] [Review][Patch] Replace fake-only benchmark with emulator-backed Room/Firestore validation [core/core-data/src/androidTest/java/com/aml_sakr/fitlife/core/data/sync/RoomFirestoreWorkManagerSyncInstrumentedTest.kt:1]
- [x] [Review][Patch] Exercise WorkManager-backed sync path instead of invoking the coordinator directly [core/core-data/src/androidTest/java/com/aml_sakr/fitlife/core/data/sync/RoomFirestoreWorkManagerSyncInstrumentedTest.kt:114]
- [x] [Review][Patch] Do not default production Firestore wiring to the local emulator [core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncModule.kt:31]
- [x] [Review][Patch] Implement server timestamp-aware conflict handling [core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/FirestoreRemoteSyncClient.kt:15]

- [x] [Review][Patch] Fix core-data compile failure from wrong coroutine import [core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/FirestoreRemoteSyncClient.kt:7]
- [x] [Review][Patch] Replace fake-only benchmark with real Room and Firestore-emulator validation [core/core-data/src/androidTest/java/com/aml_sakr/fitlife/core/data/sync/RoomFirestoreWorkManagerSyncInstrumentedTest.kt:1]
- [x] [Review][Patch] Exercise WorkManager-backed sync instead of calling the coordinator directly [core/core-data/src/androidTest/java/com/aml_sakr/fitlife/core/data/sync/RoomFirestoreWorkManagerSyncInstrumentedTest.kt:114]
- [x] [Review][Patch] Implement server timestamp-aware conflict handling [core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/FirestoreRemoteSyncClient.kt:15]
- [x] [Review][Patch] Add emulator-first Firestore gating for tests [core/core-data/src/androidTest/java/com/aml_sakr/fitlife/core/data/sync/RoomFirestoreWorkManagerSyncInstrumentedTest.kt:43]
- [x] [Review][Patch] Commit the required spike decision report instead of generating it as a test side effect [_bmad-output/implementation-artifacts/spike-room-firestore-offline-sync-report.md:1]
- [x] [Review][Patch] Return failed sync status when any upload in the batch fails [core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/OfflineSyncCoordinator.kt:50]
- [x] [Review][Patch] Do not treat Firestore read exceptions as missing remote records [core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/FirestoreRemoteSyncClient.kt:15]
- [x] [Review][Patch] Guard against overwriting newer local edits with stale sync snapshots [core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/OfflineSyncCoordinator.kt:22]
- [x] [Review][Patch] Replace nullable global worker dependencies with a reliable worker dependency path [core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncWorker.kt:14]
- [x] [Review][Patch] Remove unrelated Gemini fallback file from the SETUP-006 branch [feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/GeminiFallbackPlanProvider.kt:1]

## Dev Notes

### Current State

- SETUP-001 through SETUP-005 are complete. The project has a multi-module structure, shared domain/data/UI foundations, Hilt app setup, Firebase Analytics/Crashlytics, and completed ML Kit and Gemini spikes.
- `:core:core-domain` and `:core:core-data` exist and are the natural boundaries for local storage and Firestore data sync.
- KSP is configured (`libs.plugins.ksp`).
- Kotlin version is `2.2.10`, Hilt version is `2.59.2`.

### Existing Files To Read Before Editing

- [libs.versions.toml](file:///d:/LinkDevProject/FitLife/gradle/libs.versions.toml): all new dependency aliases belong here.
- [core-data/build.gradle.kts](file:///d:/LinkDevProject/FitLife/core/core-data/build.gradle.kts): location for Room, Firestore, and WorkManager dependencies.
- [core-domain/build.gradle.kts](file:///d:/LinkDevProject/FitLife/core/core-domain/build.gradle.kts): confirm the domain module remains platform-independent (no Android, Room, or Firebase dependencies).
- [BaseRepository.kt](file:///d:/LinkDevProject/FitLife/core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/repository/BaseRepository.kt): inspect before creating network/data layers.
- [setup-005-technical-spike-gemini-api-5-s-latency.md](file:///d:/LinkDevProject/FitLife/_bmad-output/implementation-artifacts/setup-005-technical-spike-gemini-api-5-s-latency.md): previous spike structure and task checklist.

### Architecture Compliance

- This story is a Week 1 technical spike, not the production sync feature. The pass criteria are: confirm Room write persists offline, syncs when network is restored, and latest-timestamp wins conflict resolution works correctly. Fail action is to use Firestore only (no offline) and note in scope.
- Keep Clean Architecture boundaries intact:
  - Domain may define pure Kotlin sync-result and timing models if useful.
  - Data owns Room entities, DAOs, Firestore document structures, API client adapters, and sync worker implementation.
  - UI should not contain sync logic.
- Do not implement `infra-001-workmanager-sync-worker-room-firestore` or production models in this setup spike.
- Do not introduce MVVM. Ensure unidirectional data flow is preserved if any temporary trigger is written.
- Preserve package root `com.aml_sakr.fitlife`, `minSdk = 30`, `targetSdk = 36`, and Java 11 compatibility.

### Library and Framework Requirements

- Room: Use version `2.8.4`. Add `androidx-room-runtime`, `androidx-room-ktx` as implementation and KSP compiler `androidx-room-compiler` in the version catalog.
- WorkManager: Use version `2.11.2`. Add `androidx-work-runtime-ktx` to support CoroutineWorkers.
- Firebase Firestore: Add `firebase-firestore` library managed under the existing Firebase BOM.
- Use Kotlin Coroutines and Flow to listen for database changes.
- Ensure that the Room database uses in-memory builder (`Room.inMemoryDatabaseBuilder`) during testing.
- Ensure that Firebase operations use local emulators (`Firebase.firestore.useEmulator`) during testing.

### Spike Design Guidance

- **Network Simulation**: Implement a mock connectivity monitor that allows toggling network state between online and offline during testing.
- **Offline state verification**: Write records to Room while the simulated network is offline, verify that Room reads return the new records immediately, and check that no Firestore calls are made.
- **Sync execution**: Restore the simulated network and trigger the WorkManager sync worker. Verify that the Room database sync status updates to SYNCED and that Firestore has the corresponding records.
- **Conflict Reconciliation**: Write conflicting changes to Room and Firestore with different timestamps. Verify that the reconciliation logic correctly resolves the conflict by selecting the record with the younger timestamp.

### Regression and Scope Guardrails

- Do not request location, storage, or camera permissions.
- Do not hardcode or commit Firebase credentials.
- Do not run live network or live Firestore database calls during automated tests.
- Do not build production Compose screens or navigation flows.

### Testing Requirements

- Required automated command: `./gradlew.bat test --no-daemon --console=plain`.
- JUnit tests should cover mapping, conflict resolution calculations, and state transition logic.
- Instrumented Android tests (`androidTest` under `:core:core-data`) should be used for Room and WorkManager behavior.
- Manual verification of offline sync should be recorded in the decision report.

### Previous Story Intelligence

- SETUP-005 established a useful spike pattern: domain-only metric models and decision logic, data-layer implementation details, instrumentation/manual evidence where required, a dedicated report under `_bmad-output/implementation-artifacts/`, and no accidental production navigation.
- SETUP-004 initially had inconclusive runs until the benchmark environment was corrected. Apply the same discipline here: if emulator, Firestore configuration, or network simulation makes the run invalid, report inconclusive or blocked rather than forcing a pass/fail.

### Git Intelligence

- Recent commits:
  - `5a31f9f Merge pull request #3 from Amlsakr/feature/setup-004`
  - `642563e code review SETUP-004`
  - `dfe3378 implement SETUP-004`
- Recent work has been setup-focused and strongly scoped. Continue that pattern by producing a benchmark and decision report, not by beginning full feature implementation.

### References

- Sprint status: [sprint-status.yaml](file:///d:/LinkDevProject/FitLife/_bmad-output/implementation-artifacts/sprint-status.yaml)
- Epics: [epics.md](file:///d:/LinkDevProject/FitLife/_bmad-output/planning-artifacts/epics.md)
- PRD: [fitlife-prd-v1.md](file:///d:/LinkDevProject/FitLife/_bmad-output/planning-artifacts/fitlife-prd-v1.md)
- Architecture: [fitlife-architecture-v1.md](file:///d:/LinkDevProject/FitLife/_bmad-output/planning-artifacts/fitlife-architecture-v1.md)
- Project context: [project-context.md](file:///d:/LinkDevProject/FitLife/_bmad-output/project-context.md)

## Dev Agent Record

### Agent Model Used

Gemini 3.5 Flash (Medium)

### Debug Log References

- 2026-06-08: Created SETUP-006 story file with comprehensive context for offline sync spike.

### Completion Notes List

- Defined Room, Firestore, and WorkManager dependencies for the spike.
- Established acceptance criteria for local persistence, Firestore sync, conflict reconciliation, and transition verification.
- Defined testing boundaries, emulator gating, and mock connectivity simulation guidelines.

### File List

- `_bmad-output/implementation-artifacts/setup-006-technical-spike-room-firestore-offline-sync.md`

### Change Log

- 2026-06-08: Created story file and marked ready-for-dev.
