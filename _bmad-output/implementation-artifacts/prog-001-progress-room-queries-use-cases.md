# Story 5.001: Progress Room Queries & Use-Cases

**Status**: done
**Epic**: 5 - Progress Tracking  
**Developer**: [Assigned Agent]

## 1. Story Foundation

**User Story**:  
*As a user, I want to see my weekly stats so I can track my improvement and stay motivated.*

**Acceptance Criteria**:
1. [ ] **Shared Data Access**: `SessionEntity` and `SessionDao` are accessible to both the Session and Progress features without circular or illegal module dependencies.
2. [ ] **Progress Queries**: `SessionDao` (or a dedicated `ProgressDao`) provides:
    - Total sessions count for a given user and time range.
    - Total repetitions completed in a given time range.
    - Total fatigue events detected in a given time range.
    - Total duration (in seconds) for a given time range.
3. [ ] **Analytics Use-Case**: `GetProgressAnalyticsUseCase` in `:feature:progress:progress-domain` returns a structured `ProgressAnalytics` object containing:
    - `totalSessions: Int`
    - `totalCalories: Int` (calculated using shared calorie logic)
    - `totalFatigueEvents: Int`
    - `averageDurationSeconds: Int`
4. [ ] **Domain Model**: `ProgressAnalytics` is defined in the Progress domain layer.

## 2. Developer Context & Guardrails

### 🏗️ Architecture Compliance (CRITICAL)

- **Module Refactoring Required**: 
  - **Move** `SessionEntity.kt` and `SessionDao.kt` from `:feature:session:session-data` to `:core:core-data`.
  - **Move** `CalculateCaloriesUseCase.kt` from `:feature:session:session-domain` to `:core:core-domain`.
  - This ensures `:feature:progress` can access session data via `:core:core-data` without depending on `:feature:session`.
- **Repository Pattern**:
  - Define `IProgressRepository` in `:feature:progress:progress-domain`.
  - Implement `ProgressRepositoryImpl` in `:feature:progress:progress-data` using the shared `SessionDao`.
- **Hilt**: 
  - Update `SessionModule` in `session-data` and create `ProgressModule` in `progress-data`.
  - Ensure `WorkoutPlanDatabase` (or a consolidated database) in `core-data` includes `SessionEntity`.

### 📂 File Structure Requirements

- **core-data**:
  - `com.aml_sakr.fitlife.core.data.database.SessionEntity`
  - `com.aml_sakr.fitlife.core.data.database.SessionDao`
- **core-domain**:
  - `com.aml_sakr.fitlife.core.domain.usecase.CalculateCaloriesUseCase`
- **feature-progress**:
  - `feature/progress/progress-domain/src/main/java/com/aml_sakr/fitlife/feature/progress/domain/model/ProgressAnalytics.kt`
  - `feature/progress/progress-domain/src/main/java/com/aml_sakr/fitlife/feature/progress/domain/repository/IProgressRepository.kt`
  - `feature/progress/progress-domain/src/main/java/com/aml_sakr/fitlife/feature/progress/domain/usecase/GetProgressAnalyticsUseCase.kt`
  - `feature/progress/progress-data/src/main/java/com/aml_sakr/fitlife/feature/progress/data/repository/ProgressRepositoryImpl.kt`

### 🧪 Testing Requirements

- **Unit Tests**:
  - `GetProgressAnalyticsUseCaseTest`: Mock repository and verify calorie calculation aggregation.
  - `CalculateCaloriesUseCaseTest`: Ensure no regression after move to core.
- **Integration Tests**:
  - `SessionDaoTest`: Verify Room queries for weekly/monthly ranges.

## 3. Implementation Intelligence

### 🧠 Logic Details
- **Weekly Stats Range**: Queries should filter by `startTime` where `startTime >= startOfWeekTimestamp`.
- **Calorie Aggregation**: Since calories aren't stored in `SessionEntity`, the Use-Case must fetch all sessions for the range, sum their durations, and apply `CalculateCaloriesUseCase`.
- **MET Values**: Use a standard MET value of 6.0 (Moderate Intensity) for progress estimation as per MVP specs.

### 🔗 Dependencies
- **Pre-requisite**: `SESSION-008` (Done) - provides the initial `SessionEntity` and `SaveSession` logic.
- **Consumer**: `PROG-002` (Integration with Charts) will consume the data produced here.

## 4. Project Context Reference
- **Architecture Spec**: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md` (Sections 3, 5, 6, 14.5)
- **PRD**: `_bmad-output/planning-artifacts/fitlife-prd-v1.md` (Section 5.10)
- **Story Source**: `docs/fitlife-stories-v1.md` (#511 - PROG-001)

---
**Note**: Ultimate context engine analysis completed - comprehensive developer guide created.

## Tasks / Subtasks

- [x] **Task 1: Refactor Session Data to Core**
    - [x] Move `SessionEntity.kt` and `SessionDao.kt` to `:core:core-data`.
    - [x] Update `SessionDatabase` and ensure it's properly provided by Hilt.
    - [x] Move `CalculateCaloriesUseCase.kt` to `:core:core-domain`.
    - [x] Fix imports in `:feature:session` modules.
- [x] **Task 2: Progress Domain Implementation**
    - [x] Create `ProgressAnalytics` model in `:feature:progress:progress-domain`.
    - [x] Define `IProgressRepository` in `:feature:progress:progress-domain`.
    - [x] Implement `GetProgressAnalyticsUseCase` in `:feature:progress:progress-domain`.
- [x] **Task 3: Progress Data Implementation**
    - [x] Implement `ProgressRepositoryImpl` in `:feature:progress:progress-data`.
    - [x] Configure Hilt `ProgressModule` in `:feature:progress:progress-data`.
- [x] **Task 4: Verification & Testing**
    - [x] Unit test `GetProgressAnalyticsUseCase`.
    - [x] Unit test `CalculateCaloriesUseCase` in its new location.
    - [x] Integration test `SessionDao` for the new progress queries.

## Dev Agent Record

### Implementation Plan
I will start by refactoring the existing session data and domain logic into the core modules. This is a critical prerequisite to ensure clean architecture and avoid circular dependencies when implementing the progress feature. Once the core is stable, I'll implement the progress-specific domain and data layers.

### Debug Log
- [2026-07-05] Initializing implementation of Story 5.001.

### Completion Notes
- Refactored `SessionEntity`, `SessionDao`, and `CalculateCaloriesUseCase` to core modules (`:core:core-data` and `:core:core-domain`) to resolve potential circular dependencies and ensure shared access.
- Implemented `ProgressAnalytics` domain model and `IProgressRepository` interface.
- Created `GetProgressAnalyticsUseCase` which aggregates session data and calculates calories using the shared core logic.
- Implemented `ProgressRepositoryImpl` in `:feature:progress:progress-data` using the shared `SessionDao`.
- Added new queries to `SessionDao` to support sessions count, reps sum, fatigue events sum, and duration sum in a given time range.
- Verified implementation with unit tests for `GetProgressAnalyticsUseCase` and `CalculateCaloriesUseCase`, and integration tests for `SessionDao`.
- Updated Hilt modules and Gradle files to support the new dependencies.

## File List
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/database/SessionEntity.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/database/SessionDao.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/workout/WorkoutPlanDatabase.kt` (modified)
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/di/CoreDataModule.kt` (modified)
- `core/core-domain/src/main/java/com/aml_sakr/fitlife/core/domain/usecase/CalculateCaloriesUseCase.kt`
- `core/core-domain/src/test/java/com/aml_sakr/fitlife/core/domain/usecase/CalculateCaloriesUseCaseTest.kt`
- `feature/progress/progress-domain/src/main/java/com/aml_sakr/fitlife/feature/progress/domain/model/ProgressAnalytics.kt`
- `feature/progress/progress-domain/src/main/java/com/aml_sakr/fitlife/feature/progress/domain/repository/IProgressRepository.kt`
- `feature/progress/progress-domain/src/main/java/com/aml_sakr/fitlife/feature/progress/domain/usecase/GetProgressAnalyticsUseCase.kt`
- `feature/progress/progress-domain/src/test/java/com/aml_sakr/fitlife/feature/progress/domain/usecase/GetProgressAnalyticsUseCaseTest.kt`
- `feature/progress/progress-data/src/main/java/com/aml_sakr/fitlife/feature/progress/data/repository/ProgressRepositoryImpl.kt`
- `feature/progress/progress-data/src/main/java/com/aml_sakr/fitlife/feature/progress/data/di/ProgressModule.kt`
- `feature/progress/progress-data/build.gradle.kts` (modified)
- `feature/progress/progress-domain/build.gradle.kts` (modified)
- `feature/session/session-data/src/main/java/com/aml_sakr/fitlife/feature/session/data/database/SessionDatabase.kt` (modified)
- `feature/session/session-data/src/main/java/com/aml_sakr/fitlife/feature/session/data/di/EquipmentReroutingModule.kt` (modified)
- `feature/session/session-data/src/main/java/com/aml_sakr/fitlife/feature/session/data/repository/SessionRepositoryImpl.kt` (modified)
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/summary/SessionSummaryViewModel.kt` (modified)

## Change Log
- 2026-07-05: Initial task list created.
- 2026-07-05: Completed refactoring session data and domain to core modules.
- 2026-07-05: Implemented progress domain and data layers.
- 2026-07-05: Verified implementation with unit and integration tests.

### Review Findings

- [x] [Review][Decision] All-or-Nothing UseCase Failure — `GetProgressAnalyticsUseCase` fails if any repository call (count, fatigue, or duration) fails. Should the app show partial results (e.g., sessions only) if some data is unavailable?
- [x] [Review][Patch] Repository Exception Swallowing [ProgressRepositoryImpl.kt:34]
- [x] [Review][Patch] DAO Nullability Handling [SessionDao.kt:21-27]
- [x] [Review][Patch] MET Value Hardcoding [CalculateCaloriesUseCase.kt:13]
- [x] [Review][Patch] Inconsistent Hilt Styling [ProgressModule.kt]
- [x] [Review][Patch] Missing Repository Dispatcher [ProgressRepositoryImpl.kt]
- [x] [Review][Patch] Average Duration Truncation [GetProgressAnalyticsUseCase.kt:24]
- [x] [Review][Patch] Arithmetic Overflow Guard [CalculateCaloriesUseCase.kt:13]
- [x] [Review][Patch] Test Timezone Flakiness [SessionDaoTest.kt]
- [x] [Review][Patch] UseCase AC 3 Gap: Missing User Weight [GetProgressAnalyticsUseCase.kt]

