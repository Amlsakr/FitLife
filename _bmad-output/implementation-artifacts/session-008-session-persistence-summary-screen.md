# Story 4.8: Session Persistence & Summary Screen

Status: done

## Story

As a FitLife user,
I want my session data saved and a summary screen displayed after finishing a workout,
so that I can review my performance metrics and share my achievement with friends via WhatsApp.

## Acceptance Criteria

1. [x] **Session Persistence**: Complete session data is stored in the local Room database (`SessionEntity`) upon completion.
2. [x] **Summary Metrics**: The summary screen displays:
    - Total workout duration.
    - Total repetitions completed.
    - Total fatigue events detected (from SESSION-003).
    - Calories burned (estimated based on duration and workout intensity).
3. [x] **WhatsApp Badge Sharing**:
    - A "Share to WhatsApp" button opens the Android share sheet.
    - Generates a branded image card (badge) containing the key metrics.
    - Logging of `whatsapp_share_tapped` analytics event.
4. [x] **Session Completion Flow**:
    - A "Finish Session" action in the active session UI triggers the save and navigation to summary.
    - The summary screen allows navigation back to the Home dashboard (root replacement).
5. [x] **Architecture Compliance**:
    - Data layer: `SessionEntity`, `SessionDao`, and `SessionDatabase` implementation in `:feature:session:session-data`.
    - Domain layer: `ISessionRepository`, `SaveSessionUseCase`, and `Session` model in `:feature:session:session-domain`.
    - UI layer: `SessionSummaryViewModel` (MVI) and `SessionSummaryScreen` in `:feature:session:session-ui`.
6. [x] **NFR Compliance**:
    - Summary screen rendering time ≤ 1s.
    - Share image generation does not block the main thread.

## Tasks / Subtasks

- **Task 1: Data Layer Implementation (AC: 1, 5)**
    - [x] Define `SessionEntity` in `feature/session/session-data/src/main/java/com/aml_sakr/fitlife/feature/session/data/database/SessionEntity.kt`.
    - [x] Create `SessionDao` with `insertSession` and `getSessionsForUser` methods.
    - [x] Update `SessionDatabase` to include the `SessionEntity` and `SessionDao`.
    - [x] Implement `SessionRepositoryImpl` in `feature/session/session-data`.
- **Task 2: Domain Layer Implementation (AC: 1, 5)**
    - [x] Define `Session` domain model in `feature/session/session-domain/src/main/java/com/aml_sakr/fitlife/feature/session/domain/model/Session.kt`.
    - [x] Define `ISessionRepository` interface.
    - [x] Create `SaveSessionUseCase` to handle session persistence logic.
- **Task 3: Active Session UI Update (AC: 4)**
    - [x] Add `FinishSession` event to `ActiveSessionContracts.kt`.
    - [x] Add "Finish Session" button to `ActiveSessionOverlay` in `ActiveSessionCameraRoute.kt`.
    - [x] Update `ActiveSessionViewModel` to handle `FinishSession`: calculate final duration, save session, and trigger navigation action.
- **Task 4: Session Summary Screen (AC: 2, 4, 5)**
    - [x] Create `SessionSummaryContracts.kt` (State, Event, Action).
    - [x] Implement `SessionSummaryViewModel` to load the just-finished session data.
    - [x] Create `SessionSummaryScreen.kt` with Compose UI showing metrics (duration, reps, fatigue, calories).
    - [x] Add "Home" button to navigate back to dashboard.
- **Task 5: WhatsApp Sharing Logic (AC: 3, 6)**
    - [x] Implement share image generation utility (using `Canvas` or `Picture` to draw a badge).
    - [x] Implement `ShareToWhatsAppUseCase` or logic in ViewModel using `Intent.ACTION_SEND`.
    - [x] Log `whatsapp_share_tapped` event via `AnalyticsLogger`.
- **Task 6: Navigation Integration (AC: 4)**
    - [x] Add `SessionSummaryKey` to `SessionEntryDestination.kt`.
    - [x] Register `SessionSummaryScreen` in the navigation graph.
- **Task 7: Testing & Verification**
    - [x] Unit tests for `SaveSessionUseCase` and `SessionSummaryViewModel`.
    - [x] DAO integration tests for `SessionDao`.
    - [x] UI tests for `SessionSummaryScreen`.

### Review Findings

- [x] [Review][Patch] Main Thread Block & OOM Risk [feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/utils/SessionShareUtils.kt]
- [x] [Review][Patch] Missing 'Total Sets' Metric [feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/summary/SessionSummaryScreen.kt]
- [x] [Review][Patch] Structured Concurrency Violation [feature/session/session-data/src/main/java/com/aml_sakr/fitlife/feature/session/data/repository/SessionRepositoryImpl.kt:16]
- [x] [Review][Patch] Session Save Race Condition [feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/ActiveSessionViewModel.kt:168]
- [x] [Review][Patch] FileProvider & Image Race Condition [feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/utils/SessionShareUtils.kt:92]
- [x] [Review][Patch] Hardcoded Test Data [feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/ActiveSessionViewModel.kt]
- [x] [Review][Patch] Fragile API Key Loading [feature/session/session-data/src/main/java/com/aml_sakr/fitlife/feature/session/data/di/EquipmentReroutingModule.kt]
- [x] [Review][Patch] Calorie Logic Leakage [feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/summary/SessionSummaryViewModel.kt]
- [x] [Review][Patch] Hardcoded Badge Theme [feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/utils/SessionShareUtils.kt]

## Dev Notes

- **Architecture Compliance**:
    - Follow the established MVI pattern: `UIState`, `UIEvent`, `OneTimeAction`.
    - Use Hilt for DI: `SessionModule` in `session-data` to provide Repository and DAO.
- **Data Sharing**:
    - Use `Intent.createChooser` for the share sheet to ensure compatibility.
    - The share image should be stored in a temporary file using `FileProvider`.
- **Metrics Calculation**:
    - Calories = `duration_minutes * MET_value * weight_kg / 60`. For MVP, use a constant average MET (e.g., 6.0 for moderate intensity) and default weight (e.g., 70kg) if not in profile.
- **Navigation 3**:
    - Use typed keys and `NavDisplay`. When navigating to Summary, pass the `sessionId`.

### Project Structure Notes

- `feature/session/session-data/src/main/java/com/aml_sakr/fitlife/feature/session/data/database/`
- `feature/session/session-domain/src/main/java/com/aml_sakr/fitlife/feature/session/domain/repository/`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/summary/`

### References

- [Source: docs/fitlife-stories-v1.md#509-SESSION-008]
- [Source: _bmad-output/planning-artifacts/fitlife-prd-v1.md#5.9-WhatsApp-Badge-Sharing]
- [Source: _bmad-output/planning-artifacts/fitlife-architecture-v1.md#14.5-Session]
- [Source: _bmad-output/implementation-artifacts/session-007-guided-session-ui-with-lottie-demos.md] (Previous story)
- [Source: _bmad-output/implementation-artifacts/session-003-fatigue-detection-use-case.md] (Fatigue event source)

## Dev Agent Record

### Agent Model Used

BMad Developer Agent

### Debug Log References

### Completion Notes List

- Implemented full session persistence using Room (`SessionEntity`, `SessionDao`).
- Created `ISessionRepository` and `SaveSessionUseCase` for clean domain access.
- Integrated "Finish Session" logic in `ActiveSessionViewModel` and UI.
- Developed `SessionSummaryScreen` with performance metrics and calorie calculation.
- Implemented WhatsApp badge sharing with image generation and Android Share Sheet.
- Configured `FileProvider` for secure image sharing.
- Integrated Summary screen into Navigation 3 backstack.
- Added unit tests for UseCase and ViewModel.

### File List

- `feature/session/session-data/src/main/java/com/aml_sakr/fitlife/feature/session/data/database/SessionEntity.kt`
- `feature/session/session-data/src/main/java/com/aml_sakr/fitlife/feature/session/data/database/SessionDao.kt`
- `feature/session/session-data/src/main/java/com/aml_sakr/fitlife/feature/session/data/database/SessionDatabase.kt`
- `feature/session/session-data/src/main/java/com/aml_sakr/fitlife/feature/session/data/repository/SessionRepositoryImpl.kt`
- `feature/session/session-domain/src/main/java/com/aml_sakr/fitlife/feature/session/domain/model/Session.kt`
- `feature/session/session-domain/src/main/java/com/aml_sakr/fitlife/feature/session/domain/repository/ISessionRepository.kt`
- `feature/session/session-domain/src/main/java/com/aml_sakr/fitlife/feature/session/domain/usecase/SaveSessionUseCase.kt`
- `feature/session/session-domain/src/main/java/com/aml_sakr/fitlife/feature/session/domain/usecase/GetSessionUseCase.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/ActiveSessionContracts.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/ActiveSessionViewModel.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/preview/ActiveSessionCameraRoute.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/summary/SessionSummaryContracts.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/summary/SessionSummaryViewModel.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/summary/SessionSummaryScreen.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/utils/SessionShareUtils.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/navigation/SessionNavigation.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/SessionEntryDestination.kt`
- `app/src/main/java/com/aml_sakr/fitlife/FitLifeApp.kt`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/xml/file_paths.xml`
- `feature/session/session-ui/src/test/java/com/aml_sakr/fitlife/feature/session/ui/summary/SessionSummaryViewModelTest.kt`
- `feature/session/session-domain/src/test/java/com/aml_sakr/fitlife/feature/session/domain/usecase/SaveSessionUseCaseTest.kt`
- `feature/session/session-ui/src/androidTest/java/com/aml_sakr/fitlife/feature/session/ui/summary/SessionSummaryScreenTest.kt`
