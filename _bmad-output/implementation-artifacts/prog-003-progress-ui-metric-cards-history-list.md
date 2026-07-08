# Story 5.003: Progress UI – Metric Cards & History List

Status: in-progress

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a user,
I want a dashboard with metric cards and a session history list,
so that I can see a quick summary of my progress and review past workouts.

## Acceptance Criteria

1. [x] `ProgressDashboard` composable created in `:feature:progress:progress-ui`.
2. [x] Dashboard displays four metric cards: total sessions, total calories, fatigue events, and average duration.
3. [x] Dashboard displays a session history list.
4. [x] History list shows an empty state if no sessions exist.
5. [x] Dashboard integrates `FitLifeLineChart` and `FitLifeBarChart` (built in PROG-002) if appropriate for the layout.
6. [x] UI state is driven by a MVI ViewModel consuming `GetProgressAnalyticsUseCase` (from PROG-001) and `GetProgressChartDataUseCase` (from PROG-002).

## Tasks

- [x] Domain: Create `GetSessionHistoryUseCase` and update repository.
- [x] UI: Create `MetricCard` reusable component.
- [x] UI: Create `SessionHistoryItem` reusable component.
- [x] UI: Implement `ProgressDashboardState`, `Event`, and `Action`.
- [ ] UI: Implement `ProgressDashboardViewModel` with MVI.
- [x] UI: Implement `ProgressDashboard` Composable with charts and history.
- [x] UI: Add unit tests for `ProgressDashboardViewModel`.
- [x] UI: Add basic UI tests for `ProgressDashboard`.

### Review Findings

- [x] [Review][Patch] Unused Import in ProgressNavigation.kt [ProgressNavigation.kt]
- [x] [Review][Patch] Lack of Refresh Feedback [ProgressDashboard.kt]
- [x] [Review][Patch] Missing "Average Duration" Unit Label [ProgressDashboard.kt:177]
- [x] [Review][Patch] Total dashboard failure on single use case error [ProgressDashboardViewModel.kt:53]
- [x] [Review][Defer] Hardcoded Placeholder for User ID [ProgressDashboardViewModel.kt] — deferred, pre-existing intent
- [x] [Review][Defer] Potential Lifecycle Issue with AndroidView in LazyColumn [FitLifeLineChart.kt] — deferred, pre-existing component
- [x] [Review][Defer] Timezone inconsistency in history formatting [SessionHistoryItem.kt:17] — deferred, project-wide pattern

## Developer Context

### Technical Requirements

- **MVI Pattern**: Use the MVI pattern (State, Event, Action, ViewModel) consistent with the rest of the app for `ProgressDashboardViewModel`.
- **Use Cases**: Use `GetProgressAnalyticsUseCase` to populate the metric cards. Use `GetProgressChartDataUseCase` to fetch data for the charts. Fetch session history via existing domain use cases (you may need to create `GetSessionHistoryUseCase` if it doesn't exist, using `SessionDao`).
- **Composables**: The `ProgressDashboard` should be a scrollable screen. Place the metric cards at the top (e.g., in a 2x2 grid), followed by the charts, and then the session history list at the bottom.

### Architecture Compliance

- Maintain unidirectional data flow. The ViewModel must handle all interactions and expose a single `ProgressDashboardState`.
- Do not let Room entities or data layer models leak into the UI. Map them to UI models in the ViewModel or use the existing domain models (`SessionBasicInfo`, `ProgressAnalytics`).

### Library / Framework Requirements

- Use Jetpack Compose Material3 for the UI components.
- Use `FitnessAppTheme` colors for styling, avoid hardcoding colors.

### File Structure Requirements

- `:feature:progress:progress-ui`
  - `src/main/java/com/aml_sakr/fitlife/feature/progress/ui/ProgressDashboard.kt` (Composable)
  - `src/main/java/com/aml_sakr/fitlife/feature/progress/ui/ProgressDashboardViewModel.kt`
  - `src/main/java/com/aml_sakr/fitlife/feature/progress/ui/state/ProgressDashboardState.kt` (and Events/Actions)
  - `src/main/java/com/aml_sakr/fitlife/feature/progress/ui/components/MetricCard.kt` (Reusable component)
  - `src/main/java/com/aml_sakr/fitlife/feature/progress/ui/components/SessionHistoryItem.kt` (Reusable component)

### Testing Requirements

- Write unit tests for `ProgressDashboardViewModel` using coroutines test rule.
- Write basic UI tests for `ProgressDashboard` to ensure it renders without crashing (handling loading, empty, and populated states).

## Previous Story Intelligence (PROG-002 & PROG-001)

- **Charts Integration**: `FitLifeLineChart` and `FitLifeBarChart` are built using `AndroidView` (MPAndroidChart) and require specific state handling to avoid dangerous view interop recompositions. Ensure that when you use them in the dashboard, the state passed to them is stable.
- **State Management Coupling**: A previous review caught issues with state coupling. Make sure your MVI states are independent and well-structured.
- **Data available**: `GetProgressAnalyticsUseCase` returns aggregate totals (`ProgressAnalytics`), which is perfect for the 4 metric cards.

## Project Context Reference

- Use MVI + Clean Architecture as the default app pattern; the PRD explicitly says no MVVM.
- Compose screens should render from immutable state and send events upward.
- Use explicit error models for failures; avoid passing raw exception messages into UI state.
- Treat `docs/fitlife-architecture-v1.md` and `docs/fitlife-prd-v1.md` as the planning source.

## File List

- `feature/progress/progress-domain/src/main/java/com/aml_sakr/fitlife/feature/progress/domain/repository/IProgressRepository.kt`
- `feature/progress/progress-data/src/main/java/com/aml_sakr/fitlife/feature/progress/data/repository/ProgressRepositoryImpl.kt`
- `feature/progress/progress-domain/src/main/java/com/aml_sakr/fitlife/feature/progress/domain/usecase/GetSessionHistoryUseCase.kt`
- `feature/progress/progress-ui/src/main/java/com/aml_sakr/fitlife/feature/progress/ui/components/MetricCard.kt`
- `feature/progress/progress-ui/src/main/java/com/aml_sakr/fitlife/feature/progress/ui/components/SessionHistoryItem.kt`
- `feature/progress/progress-ui/src/main/java/com/aml_sakr/fitlife/feature/progress/ui/ProgressDashboardViewModel.kt`
- `feature/progress/progress-ui/src/main/java/com/aml_sakr/fitlife/feature/progress/ui/ProgressDashboard.kt`
- `feature/progress/progress-ui/src/main/java/com/aml_sakr/fitlife/feature/progress/ui/navigation/ProgressNavigation.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/database/SessionDao.kt`
- `feature/progress/progress-ui/build.gradle.kts`
- `feature/progress/progress-domain/src/test/java/com/aml_sakr/fitlife/feature/progress/domain/usecase/GetSessionHistoryUseCaseTest.kt`
- `feature/progress/progress-ui/src/test/java/com/aml_sakr/fitlife/feature/progress/ui/ProgressDashboardViewModelTest.kt`
- `feature/progress/progress-ui/src/androidTest/java/com/aml_sakr/fitlife/feature/progress/ui/ProgressDashboardTest.kt`

## Change Log

- 2026-07-08: Initial implementation of Progress Dashboard.
- 2026-07-08: Added GetSessionHistoryUseCase and repository updates.
- 2026-07-08: Created MetricCard and SessionHistoryItem components.
- 2026-07-08: Implemented ProgressDashboardViewModel (MVI) and ProgressDashboard screen.
- 2026-07-08: Integrated weekly trend and daily calories charts.
- 2026-07-08: Added unit and UI tests.

## Dev Agent Record

### Implementation Plan

1. Update `SessionDao` and `IProgressRepository` to support fetching session history.
2. Implement `GetSessionHistoryUseCase`.
3. Create reusable UI components: `MetricCard` and `SessionHistoryItem`.
4. Implement `ProgressDashboardViewModel` using MVI pattern, aggregating data from 4 use cases.
5. Build `ProgressDashboard` screen with `LazyColumn` containing metrics, charts, and history.
6. Verify with unit and UI tests.

### Debug Log

- Encountered missing Hilt navigation and testing dependencies in `progress-ui`; added them and performed Gradle sync.
- Refactored `ProgressDashboard` into stateless `ProgressDashboardContent` to enable UI testing without a real ViewModel.
- Fixed experimental Material3 API warning with `@OptIn`.

### Completion Notes

- All acceptance criteria satisfied.
- Dashboard successfully displays 4 metrics, 2 charts, and a history list.
- MVI pattern followed strictly.
- Code coverage added for domain use case and UI ViewModel.

## Status

Status: done
