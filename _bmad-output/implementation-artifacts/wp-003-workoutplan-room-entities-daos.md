# Story WP-003: WorkoutPlan Room Entities & DAOs

Status: done

Completion Note: Comprehensive Room persistence context created for the workout plan cache layer.

## Story

As a FitLife developer,
I want Room-backed entities and DAOs for generated workout plans,
so that the app can restore plans offline and keep local persistence consistent with the existing cache key semantics.

## Acceptance Criteria

1. A production Room database entry point exists in `:core:core-data` for workout-plan persistence and is separate from the `SyncTestDatabase` spike database.
2. `WorkoutPlanEntity` stores the canonical workout-plan snapshot plus lookup metadata needed for cache freshness and exact-match retrieval: `planId`, `userId`, `requestKey`, `generatedAtEpochMillis`, `expiresAtEpochMillis`, `fitnessLevel`, `location`, `weekNumber`, `isFallback`, and a lossless plan payload for days/exercises.
3. Room `TypeConverter`s exist for every non-primitive workout field stored directly on the entity, and JSON handling stays in the data layer rather than leaking into domain or UI code.
4. `WorkoutPlanDao` supports `insert`, `getLatestByRequestKey`, `getLatestByUserIdAndRequestKey`, and `clearOld` semantics so the repository can restore the freshest valid plan and remove stale rows.
5. Query behavior preserves WP-002 cache semantics: if the request fingerprint changes, the DAO must not return a cached plan for a different fitness level, goal set, equipment set, location, or requested day count.
6. Stale plans older than the cache validity window are excluded from restore reads and are eligible for cleanup without deleting fresh rows.
7. The production workout repository can be switched from `PreferencesDataSource`-backed cache storage to the new Room DAO without changing the public `WorkoutPlanRepository` contract, fallback path, or Gemini generation flow.
8. Hilt/KSP wiring compiles cleanly for the Room database and DAO, and the database is provided from the shared data boundary rather than from UI code.
9. Offline JVM/in-memory tests cover entity round-trip, latest-plan ordering, exact-match lookup, stale cleanup, and invalid payload handling without touching live Gemini, Firestore, or the Android UI.
10. This story does not implement the home screen, weekly overview UI, Firestore sync, or session/progress tables. Those belong to later stories.

## Tasks / Subtasks

- [x] Add the production Room persistence surface in `:core:core-data`. Create the workout-plan entity, DAO, database entry point, and required type converters in a dedicated package under `com.aml_sakr.fitlife.core.data` rather than reusing the spike-only sync database. (AC: 1, 2, 3, 4)
  - [x] Store the stable request fingerprint used by WP-002 as a first-class column so exact-match cache lookup stays deterministic. (AC: 5)
  - [x] Keep the canonical plan payload lossless so the full 7-day plan can round-trip back into the domain model. (AC: 2, 9)
- [x] Wire the workout repository in `:feature:workout:workout-data` to the new DAO only where needed for save/load cache behavior. Keep the repository API, Gemini transport, fallback asset path, and retry logic unchanged. (AC: 7)
  - [x] Remove the old preferences-backed cache write/read path once Room is confirmed as the source of truth. (AC: 7)
- [x] Add Hilt bindings for the new database and DAO in the shared data layer. Follow the existing `core-data` module pattern and keep Room construction out of composables and view models. (AC: 1, 8)
- [x] Add in-memory Room tests and repository tests that prove the new persistence path behaves like the existing cache semantics. (AC: 4, 5, 6, 7, 9)
  - [x] Verify the latest matching plan is returned for the same request fingerprint and that mismatched requests miss the cache.
  - [x] Verify stale rows are not restored and are removable through the cleanup query.
  - [x] Verify malformed persisted payloads fail safely instead of crashing the repository.
- [x] Verify the smallest relevant build surface after implementation. Prefer module unit tests first, then any compile checks required for the new Hilt/Room wiring. (AC: 8, 9)

## Dev Notes

### Current State

- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/repository/WorkoutPlanRepositoryImpl.kt` still stores and restores cached plans through `PreferencesDataSource`.
- The repository already preserves exact-match cache semantics by hashing the full request shape into a stable cache key. WP-003 must keep that behavior when moving to Room.
- The only production Room code currently in the repo is the `core-data` sync spike (`SyncTestEntity`, `SyncTestDao`, `SyncTestDatabase`, `SyncModule`). Do not extend that test-only database for workout persistence.
- `feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/model/WorkoutPlan.kt` already defines the canonical plan snapshot and freshness helper.
- WP-002 already owns the Gemini request/parse/fallback pipeline and the `WorkoutPlanRepository` contract. This story should not rework that orchestration.

### What This Story Changes

- Introduces a production Room-backed local persistence layer for workout plans.
- Adds the database shape, DAO queries, and type converters needed to store and restore a full generated plan offline.
- Preserves the existing request-fingerprint cache semantics while replacing the current preferences-backed cache with Room.
- Prepares the workout data boundary for later Firestore sync and home-screen consumption.

### What Must Be Preserved

- Keep the `WorkoutPlanRepository` interface unchanged.
- Keep Gemini transport, fallback asset selection, and plan mapping below the data boundary.
- Preserve exact-match cache semantics from WP-002. A plan for a different combination of fitness level, goals, equipment, location, or requested day count must not be returned from cache.
- Preserve the 7-day plan invariant and the freshness window used by `WorkoutPlan.isFresh(...)`.
- Keep the existing `core-data` sync spike isolated. The workout plan database must be a separate production concern, not a rename of `SyncTestDatabase`.

### Architecture Compliance

- Follow the architecture room model in `_bmad-output/planning-artifacts/fitlife-architecture-v1.md`: `WorkoutPlanEntity` is the production persistence row, and the plan payload should remain JSON-compatible.
- Room entities should be simple data holders. Do not model entity-to-entity object references; Room explicitly favors explicit queries or a payload column over object references.
- Use suspend DAO methods for write/read operations and keep Room off the main thread.
- Keep database creation and DAO provisioning in the shared data layer, following the existing `core-data` Hilt style.
- If a new database class is introduced, give it a stable version from day one and keep schema changes explicit for future workout/session/progress stories.

### Library And Framework Requirements

- The repo already pins Room `2.8.4` and WorkManager `2.11.2` in `gradle/libs.versions.toml`; do not upgrade just to match the docs.
- Use the existing Room compiler setup via KSP.
- Keep all Room annotations in `androidx.room.*` and use `@Database`, `@Entity`, `@Dao`, `@TypeConverter`, and `@TypeConverters` in the standard AndroidX style.
- Current official Room docs show `@Dao` interfaces or abstract classes with suspend methods for asynchronous access, and they recommend explicit data access instead of object references between entities.

### File Structure Requirements

Expected new or updated areas:

- `_bmad-output/implementation-artifacts/wp-003-workoutplan-room-entities-daos.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/workout/`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/di/`
- `core/core-data/src/test/java/com/aml_sakr/fitlife/core/data/workout/`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/repository/WorkoutPlanRepositoryImpl.kt`
- `feature/workout/workout-data/src/test/java/com/aml_sakr/fitlife/feature/workout/data/repository/WorkoutPlanRepositoryImplTest.kt`

### Testing Requirements

- Use in-memory Room databases or pure JVM fakes for all unit tests.
- Verify insert and restore round-trips for a full workout plan snapshot, including the nested day/exercise payload.
- Verify exact-match lookup uses the same request fingerprint as WP-002 and does not return a plan for a different request.
- Verify `clearOld` only removes expired rows.
- Verify malformed or structurally invalid stored payloads fail safely and do not crash the repository.
- Do not call live Gemini, Firestore, or UI layers from tests in this story.

### Project Structure Notes

- The workout feature already has production domain models and a repository contract; this story should layer Room beneath that contract rather than inventing a parallel persistence API.
- The `core-data` module is already the shared place for persistence infrastructure in this repo, so keep the workout DB there instead of scattering Room setup across feature modules.
- If a small adapter is needed inside `feature/workout/workout-data` to translate between repository and DAO, keep it narrow and private to the data boundary.

### References

- Story source: `docs/fitlife-stories-v1.md`
- Epic and story map: `docs/fitlife-stories-v1.md#WP-003`
- PRD: `_bmad-output/planning-artifacts/fitlife-prd-v1.md#5-2-AI-Workout-Plan-Generation-Gemini-API`
- Architecture: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#3-Room-Database-Schema`
- Architecture: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#14-Data-Model-Appendix`
- Previous story: `_bmad-output/implementation-artifacts/wp-002-generateworkoutplan-use-case-with-fallback-asset.md`
- Current repository cache path: `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/repository/WorkoutPlanRepositoryImpl.kt`
- Existing Room spike pattern: `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncTestDatabase.kt`
- Existing Room spike DAO pattern: `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/sync/SyncTestDao.kt`
- Official Room docs: [Room overview](https://developer.android.com/training/data-storage/room), [Room entities](https://developer.android.com/training/data-storage/room/defining-data), [Room DAOs](https://developer.android.com/training/data-storage/room/accessing-data), [Room relationships](https://developer.android.com/training/data-storage/room/relationships), [Room release page](https://developer.android.com/jetpack/androidx/releases/room)

## Change Log

- 2026-06-20: Created WP-003 implementation story context for the Room-backed workout plan cache layer.
- 2026-06-20: Verified the production workout Room database, DAO, entity, and repository wiring already exist and compile cleanly after a mapper visibility fix.
- 2026-06-20: Updated story status to review after confirming targeted module tests and app compile passed.
- 2026-06-20: Hardened the workout list type converter to use JSON encoding with legacy fallback and added emulator-backed Room integration coverage for lossless list round-trip plus DAO query semantics.
- 2026-06-20: Added offline JVM converter/DAO semantics tests and restored repository assertions for DAO cache lookup arguments.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-06-20: Created WP-003 story context from sprint status, Epic 3, the PRD, architecture, the WP-002 implementation notes, the current workout repository, and the existing `core-data` Room pattern.
- 2026-06-20: Confirmed the production repository still uses `PreferencesDataSource` for cache storage, so this story explicitly preserves the request fingerprint while moving persistence to Room.
- 2026-06-20: Confirmed the repo already pins Room `2.8.4` and WorkManager `2.11.2`; no dependency bump is needed for this story.
- 2026-06-20: Verified the production workout Room database, DAO, entity, and repository wiring are already present in the tree and compile cleanly after a visibility fix on `WorkoutPlanRoomMapper`.
- 2026-06-20: Ran `./gradlew.bat :core:core-data:testDebugUnitTest :feature:workout:workout-data:testDebugUnitTest :feature:workout:workout-domain:test --no-daemon --console=plain` and `./gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain`; both passed.
- 2026-06-20: Replaced the separator-based workout list converter with JSON encoding plus a separator fallback so existing cached rows remain readable while new rows round-trip losslessly.
- 2026-06-20: Added `WorkoutPlanDatabaseInstrumentedTest` under `core-data` and verified it with `./gradlew.bat :core:core-data:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aml_sakr.fitlife.core.data.workout.WorkoutPlanDatabaseInstrumentedTest --no-daemon --console=plain` on `emulator-5554`.
- 2026-06-20: Re-ran `./gradlew.bat :core:core-data:compileDebugAndroidTestKotlin --no-daemon --console=plain` and `./gradlew.bat :feature:workout:workout-data:testDebugUnitTest --tests com.aml_sakr.fitlife.feature.workout.data.repository.WorkoutPlanRepositoryImplTest --no-daemon --console=plain`; both passed.
- 2026-06-20: Added offline JVM coverage for `WorkoutPlanConverters` and pure in-memory DAO semantics under `core-data/src/test`.
- 2026-06-20: Updated `WorkoutPlanRepositoryImplTest` so the recording DAO captures `requestKey` and `nowEpochMillis` used by `getCachedPlan`.
- 2026-06-20: Re-ran `./gradlew.bat :core:core-data:testDebugUnitTest --tests com.aml_sakr.fitlife.core.data.workout.WorkoutPlanConvertersTest --tests com.aml_sakr.fitlife.core.data.workout.WorkoutPlanDaoSemanticsTest --no-daemon --console=plain` and `./gradlew.bat :feature:workout:workout-data:testDebugUnitTest --tests com.aml_sakr.fitlife.feature.workout.data.repository.WorkoutPlanRepositoryImplTest --no-daemon --console=plain`; both passed.

### Completion Notes

- Implemented and verified the Room-backed workout-plan cache path in the shared data layer.
- Preserved WP-002 request fingerprint semantics while keeping Gemini orchestration, fallback selection, and repository contracts intact.
- Confirmed module tests and the app Kotlin compile pass after the visibility fix for `WorkoutPlanRoomMapper`.
- Eliminated the lossy list-column encoding by serializing workout string lists as JSON while still decoding legacy separator-encoded rows.
- Added emulator-backed Room integration coverage for converter round-trip, latest-row ordering, request-key filtering, user lookup, and stale-row cleanup.
- Added offline JVM tests for converter round-trip, legacy row decoding, latest-plan ordering, exact-match lookup, and stale cleanup.
- Restored repository-level verification that `getCachedPlan` calls the DAO with the request-derived key and current clock value.

### File List

- `_bmad-output/implementation-artifacts/wp-003-workoutplan-room-entities-daos.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `core/core-data/build.gradle.kts`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/di/CoreDataModule.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/workout/WorkoutPlanConverters.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/workout/WorkoutPlanDao.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/workout/WorkoutPlanDatabase.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/workout/WorkoutPlanEntity.kt`
- `core/core-data/src/androidTest/java/com/aml_sakr/fitlife/core/data/workout/WorkoutPlanDatabaseInstrumentedTest.kt`
- `core/core-data/src/test/java/com/aml_sakr/fitlife/core/data/workout/WorkoutPlanConvertersTest.kt`
- `core/core-data/src/test/java/com/aml_sakr/fitlife/core/data/workout/WorkoutPlanDaoSemanticsTest.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/di/WorkoutDataModule.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/repository/WorkoutPlanRepositoryImpl.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/repository/WorkoutPlanRoomMapper.kt`
- `feature/workout/workout-data/src/test/java/com/aml_sakr/fitlife/feature/workout/data/repository/WorkoutPlanRepositoryImplTest.kt`
