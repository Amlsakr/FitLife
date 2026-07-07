# Story 5.002: MPAndroidChart Integration

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a user,
I want to see visual line and bar charts of my weekly workout trends,
so that I can track my progress over time.

## Acceptance Criteria

1. [ ] MPAndroidChart v3.1.0 added as dependency via version-catalog; JitPack repository declared in `settings.gradle.kts`.
2. [ ] Reusable Compose wrappers `FitLifeLineChart` and `FitLifeBarChart` (or `MPChartBar`) created in `:feature:progress:progress-ui` using `AndroidView` interop.
3. [ ] Wrappers accept a `ChartData` domain model and theme-aware styling (colors from `FitnessAppTheme`).
4. [ ] `LineChart` shows weekly sessions-count trend (last 4 weeks); `BarChart` shows daily calories burned (current week).
5. [ ] Charts render correctly with zero-data state (empty chart with friendly message) and loading state.
6. [ ] Instrumented UI test verifies chart composable renders without crash.

## Tasks / Subtasks

- [x] Task 1: Gradle & Dependency Setup (AC: 1)
  - [x] Add JitPack repository to `settings.gradle.kts` in `dependencyResolutionManagement`.
  - [x] Add MPAndroidChart v3.1.0 to `gradle/libs.versions.toml`.
  - [x] Add dependency to `feature/progress/progress-ui/build.gradle.kts`.
- [x] Task 2: Domain Layer Expansion (Gap identified from PROG-001)
  - [x] Define `ChartData` domain model (or specific `LineChartData`/`BarChartData`).
  - [x] Create a Use Case (e.g., `GetProgressChartDataUseCase`) to fetch time-series data using existing `SessionDao.getSessionsByDateRange` (or by adding new DAO methods if needed).
- [x] Task 3: Compose Wrappers Implementation (AC: 2, 3)
  - [x] Implement `FitLifeLineChart` using `AndroidView`.
  - [x] Implement `FitLifeBarChart` (or `MPChartBar` per UX spec) using `AndroidView`.
  - [x] Apply `FitnessAppTheme` colors to the charts.
- [x] Task 4: UI States & Integration (AC: 4, 5)
  - [x] Handle loading state (shimmer matching chart dimensions).
  - [x] Handle empty state (illustration + "Start your first workout to see progress").
  - [x] Handle interactions (e.g., tap chart bar for day-view drill-down).
- [x] Task 5: Testing (AC: 6)
  - [x] Write instrumented UI test for chart composables.

## Dev Notes

### 🚨 CRITICAL MISTAKE PREVENTION 🚨
- **DO NOT search for a Compose-native MPAndroidChart**: It doesn't exist. MPAndroidChart (v3.1.0) is a legacy View-based library. You MUST use `androidx.compose.ui.viewinterop.AndroidView`.
- **Missing Domain Gap**: PROG-001 built `GetProgressAnalyticsUseCase`, but it only returns aggregate totals (`ProgressAnalytics`). Charts need time-series data (e.g., daily totals). You will need to build a new UseCase (e.g., `GetProgressChartDataUseCase`) that leverages `SessionDao.getSessionsByDateRange` (which was created in PROG-001) to map entities into your new `ChartData` domain models.
- **Repository Addition**: MPAndroidChart is hosted on JitPack. You MUST add `maven { url = uri("https://jitpack.io") }` to `settings.gradle.kts` under `dependencyResolutionManagement`.

### Architecture Patterns & Constraints
- **MVI Pattern**: The UI must follow the MVI pattern (State, Event, Action, ViewModel) consistent with the rest of the app. The charts should be driven by a `ProgressChartState`.
- **UI Structure**: `progress-ui` currently only has a placeholder `ProgressNavigation.kt`. Build the chart composables as reusable components, keeping in mind they will be assembled into a `ProgressDashboard` in PROG-003.
- **Colors**: Use the app's `FitnessAppTheme` colors (e.g., Primary `#0288D1`, Secondary/Accent `#00BCD4`). Do not hardcode arbitrary colors.

### Project Structure Notes
- Module: `:feature:progress:progress-ui`
- MPAndroidChart Dependency: `com.github.PhilJay:MPAndroidChart:v3.1.0`

### References
- Architecture Spec: `docs/fitlife-architecture-v1.md`
- Stories Spec: `docs/fitlife-stories-v1.md`
- UX Spec: `docs/fitlife-ux-spec-v1.md` (Section 3.6 & 4)

## Dev Agent Record

### Agent Model Used
Gemini 3.1 Pro (High)

### Completion Notes List
- Comprehensive story created.
- Identified and mitigated risk regarding MPAndroidChart legacy status (forced `AndroidView`).
- Identified and mitigated gap from PROG-001 where time-series chart data UseCases were missing.
- Implemented Task 1: Added JitPack to settings.gradle.kts, MPAndroidChart to libs.versions.toml, and progress-ui.
- Implemented Task 2: Created `SessionBasicInfo` and `ChartData` domain models. Added `getSessionsSince` to DAO and repo. Created `GetProgressChartDataUseCase`.
- Implemented Task 3: Created `FitLifeLineChart` and `FitLifeBarChart` composables using `AndroidView`.
- Implemented Task 4: Implemented empty state logic inside the AndroidView components and created `ShimmerModifier` and `ProgressChartState` for the loading state.
- Implemented Task 5: Added `FitLifeLineChartTest` basic instrumentation test.

### File List
- `settings.gradle.kts`
- `gradle/libs.versions.toml`
- `feature/progress/progress-ui/build.gradle.kts`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/database/SessionDao.kt`
- `feature/progress/progress-domain/src/main/java/com/aml_sakr/fitlife/feature/progress/domain/repository/IProgressRepository.kt`
- `feature/progress/progress-data/src/main/java/com/aml_sakr/fitlife/feature/progress/data/repository/ProgressRepositoryImpl.kt`
- `feature/progress/progress-domain/src/main/java/com/aml_sakr/fitlife/feature/progress/domain/model/SessionBasicInfo.kt`
- `feature/progress/progress-domain/src/main/java/com/aml_sakr/fitlife/feature/progress/domain/model/ChartData.kt`
- `feature/progress/progress-domain/src/main/java/com/aml_sakr/fitlife/feature/progress/domain/usecase/GetProgressChartDataUseCase.kt`
- `feature/progress/progress-ui/src/main/java/com/aml_sakr/fitlife/feature/progress/ui/components/FitLifeLineChart.kt`
- `feature/progress/progress-ui/src/main/java/com/aml_sakr/fitlife/feature/progress/ui/components/FitLifeBarChart.kt`
- `feature/progress/progress-ui/src/main/java/com/aml_sakr/fitlife/feature/progress/ui/components/ShimmerModifier.kt`
- `feature/progress/progress-ui/src/main/java/com/aml_sakr/fitlife/feature/progress/ui/state/ProgressChartState.kt`
- `feature/progress/progress-ui/src/androidTest/java/com/aml_sakr/fitlife/feature/progress/ui/components/FitLifeLineChartTest.kt`

### Review Findings

- [x] [Review][Patch] Missing MVI architecture components — Create full ViewModel, Event, and Action classes now.
- [x] [Review][Patch] State Management Coupling — Split into independent states.
- [x] [Review][Patch] Missing empty state illustration [FitLifeLineChart.kt:49]
- [x] [Review][Patch] Loading state (shimmer) is not applied [FitLifeLineChart.kt:1]
- [x] [Review][Patch] Missing chart interactions (tap/drill-down) [FitLifeBarChart.kt:20]
- [x] [Review][Patch] Unlocalized Strings in Domain Layer [GetProgressChartDataUseCase.kt:34]
- [x] [Review][Patch] Redundant Context Switching [ProgressRepositoryImpl.kt:143]
- [x] [Review][Patch] Fully Qualified Class Names [ProgressRepositoryImpl.kt:143]
- [x] [Review][Patch] Inefficient Time Conversions in Loops [GetProgressChartDataUseCase.kt:33]
- [x] [Review][Patch] Dangerous View Interop Recomposition [FitLifeLineChart.kt:53]
- [x] [Review][Patch] Potential IndexOutOfBounds Crash [FitLifeBarChart.kt:66]
- [x] [Review][Patch] Ineffective Unit Tests [FitLifeLineChartTest.kt:20]
- [x] [Review][Patch] Missing Lifecycle Handling for Legacy Views [FitLifeLineChart.kt:26]
- [x] [Review][Patch] Deprecated Modifier Usage [ShimmerModifier.kt:10]
- [x] [Review][Patch] Axis labels retain stale colors on theme change [FitLifeBarChart.kt:59]
- [x] [Review][Patch] Shimmer effect is blindingly light in dark mode [ShimmerModifier.kt:12]
