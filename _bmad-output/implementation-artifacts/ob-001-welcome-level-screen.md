# Story OB-001: Welcome Level Screen

Status: done
Design Status: Linked

Completion Note: Ultimate context engine analysis completed - comprehensive developer guide created.

## Story

As a new user,
I want to select my fitness level (Beginner or Intermediate) right after auth,
so the app can tailor the onboarding and workout content.

## Acceptance Criteria

1. Given onboarding starts after a verified auth session, when the welcome screen loads, then the user sees two large selectable cards labeled Beginner and Intermediate.
2. Given the user taps a level card, when the selection changes, then only one level is active at a time and the selected card is visually distinct from the unselected card.
3. Given the user taps Select on a level card, when the choice is persisted, then the selected `FitnessLevel` is stored through `PreferencesDataSource` and survives app restart or process death.
4. Given a stored level exists, when the welcome screen is shown again, then the previously selected level is restored or preselected and remains editable before continuing.
5. Given the selected level is Beginner, when the user continues, then navigation proceeds to the beginner onboarding branch and does not show intermediate-only steps.
6. Given the selected level is Intermediate, when the user continues, then navigation proceeds to the intermediate onboarding branch and does not show beginner-only steps.
7. Given persistence or restoration fails, when the screen is shown or the user attempts to continue, then the UI shows a recoverable error state and keeps the user on the onboarding flow.
8. Given the feature is implemented, then state, events, one-time actions, and persistence remain separated according to MVI + Clean Architecture.
9. Given the screen is rendered in Compose, then it uses `FitnessAppTheme`, core-ui tokens, Inter typography, Arctic Focus colors, responsive layout, accessibility semantics, and 48dp minimum touch targets.
10. Given automated verification runs, then domain, data, UI, and navigation behavior are covered by focused tests and the app still uses the single-activity Navigation 3 shell.

## Tasks / Subtasks

- [x] Define onboarding-domain contracts and selection use cases. Add a `FitnessLevel`-backed selection model, a repository interface, and suspend use cases for reading and saving the selected level. (AC: 3, 4, 7, 8)
- [x] Implement onboarding-data persistence on top of `PreferencesDataSource`. Store the selected level as a stable string value, fail closed on unknown stored values, and keep DataStore access out of UI code. (AC: 3, 4, 7)
- [x] Build the welcome selector MVI flow in onboarding-ui. Add state, events, one-time actions, a ViewModel, and a Compose screen with two large cards, clear selected state, accessible semantics, and recoverable error handling. (AC: 1, 2, 5, 6, 8, 9)
- [x] Wire the onboarding entry into the app navigation shell. Replace the current placeholder onboarding content with the feature-owned welcome screen and route to the beginner or intermediate branch through typed Navigation 3 keys or callbacks, not string routes or NavController. (AC: 5, 6, 10)
- [x] Add focused automated tests. Cover selection persistence, restoration, invalid stored values, loading/error states, navigation actions, and app-level onboarding back-stack behavior. (AC: 3-10)
- [x] Verify the smallest relevant build/test surface. Prefer module JVM tests first, then the app unit test suite, and only add instrumented coverage where it is actually needed. (AC: 10)

## Dev Notes

### Current State

- The onboarding modules already exist as Gradle shells, but there is no production source yet under `feature/onboarding/onboarding-data`, `feature/onboarding/onboarding-domain`, or `feature/onboarding/onboarding-ui`.
- `MainActivity.kt` currently exposes `AppRoute.Onboarding` as a placeholder protected destination. This story should replace that placeholder with the real welcome selector experience without breaking the single-activity Navigation 3 host or the splash/auth flow.
- The PRD and UX spec agree that onboarding is the first level-specific decision point after auth. OB-002 and OB-003 own the detailed beginner and intermediate questionnaires, while OB-004 owns the final completion flag and removal of onboarding from future launches.
- `PreferencesDataSource` in `core-data` is the shared local persistence boundary for this story. Use it from data-layer code and expose Flow-based reads through the domain boundary.

### Architecture Compliance

- Follow MVI + Clean Architecture. Compose screens should render immutable state and send events upward; navigation and snackbar-like effects belong in one-time actions.
- Keep domain code Android-free. Domain owns the selection model, repository contract, and use cases. Data owns the `PreferencesDataSource` implementation. UI owns the Compose screen and ViewModel.
- Keep the app on Navigation 3. Use serializable typed keys, `NavDisplay`, and entry-provider registration. Do not introduce `NavController`, `NavHost`, or string routes for this flow.
- Do not move the onboarding completion flag into this story. OB-004 owns that behavior.
- Use the checked-in Gradle catalog values as the source of truth. Official docs may show newer Navigation 3 or DataStore guidance, but this story should not auto-upgrade versions unless the repo is intentionally changed.

### UX And Accessibility

- Match the UX spec for the welcome / level selector screen: two large cards, selected state with elevation and accent border, and a straightforward tap target for each level.
- Keep the interaction low-friction. The user should be able to choose Beginner or Intermediate quickly and move into the correct branch without extra setup.
- Preserve accessibility basics: 48dp touch targets, logical focus order, content descriptions where needed, and readable layouts at larger font scales.
- If no design asset exists for the screen, use a code-native Compose implementation with the existing Arctic Focus palette and Inter typography rather than introducing a new visual system.

### Scope Boundaries

- This story only stores and routes the selected fitness level. It does not collect goals, equipment, frequency, current split, or 1RM data.
- Do not write Firestore data, create workout plans, or modify auth/session persistence here.
- Do not implement onboarding completion gating here. The level selector should feed the beginner or intermediate branch, and OB-004 will close the loop later.
- Keep the onboarding branch handoff easy for OB-002 and OB-003 to extend. The branch-start destination can be a typed key or feature callback, but it should remain stable enough for the next onboarding stories.

### Library And Framework Requirements

- Preferences DataStore is the correct persistence primitive for simple onboarding preferences. Keep persistence in the data layer and expose the value through the domain boundary instead of reading or writing DataStore from composables.
- Navigation 3 back stacks store keys, not content. If onboarding keys are added, make them serializable and keep them compatible with the app-owned Navigation 3 shell.
- The repo currently uses Navigation 3 `1.1.2`, Lifecycle Navigation 3 integration aligned to `2.10.0`, and Kotlin `2.2.10`. Do not change those versions in this story just because official docs advertise newer releases.

### File Structure Requirements

Expected new or updated areas:

- `_bmad-output/implementation-artifacts/ob-001-welcome-level-screen.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt`
- `feature/onboarding/onboarding-data/src/main/java/com/aml_sakr/fitlife/feature/onboarding/data/`
- `feature/onboarding/onboarding-data/src/test/java/com/aml_sakr/fitlife/feature/onboarding/data/`
- `feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/`
- `feature/onboarding/onboarding-domain/src/test/java/com/aml_sakr/fitlife/feature/onboarding/domain/`
- `feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/`
- `feature/onboarding/onboarding-ui/src/test/java/com/aml_sakr/fitlife/feature/onboarding/ui/`

### Testing Requirements

- JVM tests should use fakes for `PreferencesDataSource` and coroutines test utilities. Do not initialize real DataStore, Firebase, or network dependencies in unit tests.
- Verify selection persistence and restoration, including the fail-closed behavior for invalid stored values.
- Verify the MVI flow separately from the UI: state transitions, loading guards, error handling, and the navigation action emitted for Beginner versus Intermediate.
- Verify the app-level navigation path only as far as needed to confirm that the onboarding branch replaces the current route correctly in the Navigation 3 back stack.

### References

- Story source: `docs/fitlife-stories-v1.md`
- PRD: `_bmad-output/planning-artifacts/fitlife-prd-v1.md`
- Architecture: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md`
- UX: `docs/fitlife-ux-spec-v1.md`
- Project context: `_bmad-output/project-context.md`
- DataStore guide: [developer.android.com/topic/libraries/architecture/datastore](https://developer.android.com/topic/libraries/architecture/datastore)
- Navigation 3 guide: [developer.android.com/guide/navigation/navigation-3](https://developer.android.com/guide/navigation/navigation-3)
- Navigation 3 save-state guide: [developer.android.com/guide/navigation/navigation-3/save-state](https://developer.android.com/guide/navigation/navigation-3/save-state)

## Design References

Design:
- _bmad-output/design/onboarding/welcome-level-screen.png
- _bmad-output/design/onboarding/welcome-level-screen-reference.html
- _bmad-output/design/onboarding/welcome-level-screen-design.md

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Implementation Plan

- [x] Add a small onboarding-domain contract around saving and reading the selected fitness level.
- [x] Implement PreferencesDataStore-backed persistence in onboarding-data using the existing `PreferencesDataSource`.
- [x] Build the welcome selector screen and ViewModel in onboarding-ui with a clean MVI state/action flow.
- [x] Replace the current app placeholder for onboarding with the real feature-owned welcome screen and typed branch navigation.
- [x] Add focused unit and UI tests before any broader app verification.

### Completion Notes

- Built the onboarding level selector across domain, data, UI, and app layers.
- Persisted the selected fitness level through `PreferencesDataSource` and restored it on launch.
- Routed the flow into typed beginner and intermediate onboarding branches.
- Matched the screen to the provided design with responsive cards, updated illustrations, and an accessible 48dp back control.
- Verified the feature with focused unit, UI, Android, and lint coverage.

### Debug Log References

- 2026-06-18: Created onboarding story context from sprint status, Epic 2, PRD, architecture, UX specification, current app navigation, onboarding module scaffolding, and official Android DataStore/Navigation 3 documentation.
- 2026-06-18: Implemented onboarding domain, data, UI, and app navigation wiring.
- 2026-06-18: Verified with `.\gradlew.bat test --no-daemon --console=plain`, `.\gradlew.bat :feature:onboarding:onboarding-domain:test :feature:onboarding:onboarding-data:testDebugUnitTest :feature:onboarding:onboarding-ui:testDebugUnitTest :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest --no-daemon --console=plain`, and `.\gradlew.bat lint --no-daemon --console=plain`.
- 2026-06-18: Applied follow-up fixes for auth snackbar sequencing and `WelcomeLevelRoute` constructor safety.
- 2026-06-18: Re-verified with `.\gradlew.bat test --no-daemon --console=plain` and `.\gradlew.bat lint --no-daemon --console=plain` after addressing auth test expectations.

## File List

- `_bmad-output/implementation-artifacts/ob-001-welcome-level-screen.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `app/build.gradle.kts`
- `app/src/androidTest/java/com/aml_sakr/fitlife/FitLifeAppNavigationTest.kt`
- `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/screen/AuthScreen.kt`
- `feature/auth/auth-ui/src/test/java/com/aml_sakr/fitlife/feature/auth/auth_ui/auth/viewmodel/AuthViewModelTest.kt`
- `feature/onboarding/onboarding-data/build.gradle.kts`
- `feature/onboarding/onboarding-data/src/main/java/com/aml_sakr/fitlife/feature/onboarding/data/di/OnboardingModule.kt`
- `feature/onboarding/onboarding-data/src/main/java/com/aml_sakr/fitlife/feature/onboarding/data/repository/PreferencesOnboardingRepository.kt`
- `feature/onboarding/onboarding-data/src/test/java/com/aml_sakr/fitlife/feature/onboarding/data/repository/PreferencesOnboardingRepositoryTest.kt`
- `feature/onboarding/onboarding-domain/build.gradle.kts`
- `feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/error/OnboardingError.kt`
- `feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/model/FitnessLevel.kt`
- `feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/repository/OnboardingRepository.kt`
- `feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/usecase/ReadSelectedFitnessLevelUseCase.kt`
- `feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/usecase/SaveSelectedFitnessLevelUseCase.kt`
- `feature/onboarding/onboarding-domain/src/test/java/com/aml_sakr/fitlife/feature/onboarding/domain/usecase/OnboardingUseCaseTest.kt`
- `feature/onboarding/onboarding-ui/build.gradle.kts`
- `feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/WelcomeLevelAction.kt`
- `feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/WelcomeLevelEvent.kt`
- `feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/WelcomeLevelScreen.kt`
- `feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/WelcomeLevelState.kt`
- `feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/WelcomeLevelViewModel.kt`
- `feature/onboarding/onboarding-ui/src/test/java/com/aml_sakr/fitlife/feature/onboarding/ui/WelcomeLevelViewModelTest.kt`

## Change Log

- 2026-06-18: Created comprehensive OB-001 implementation context and marked the story ready for development.
- 2026-06-18: Implemented the onboarding level selector feature and validated the initial build/test surface.
- 2026-06-18: Applied follow-up fixes for snackbar sequencing and onboarding route safety.
- 2026-06-19: Renamed the onboarding UI surface to `WelcomeLevel*` and redesigned the screen to match the supplied reference.
- 2026-06-19: Restored immediate persistence on level selection and finalized accessibility and interaction fixes.
