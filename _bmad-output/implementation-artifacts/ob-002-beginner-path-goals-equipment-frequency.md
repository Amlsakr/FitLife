# Story OB-002: Beginner Path - Goals, Equipment, Frequency

Status: done
Design Status: Linked

## Story

As a Beginner,
I want to answer three short onboarding screens about my goals, equipment, and workout frequency,
so that FitLife can personalize the workout plan inputs and store a reusable beginner profile.

## Acceptance Criteria

1. Given the user selected Beginner in OB-001, when the beginner branch opens, then the first screen is Goals and the flow shows a clear step indicator and back action to return to the level selector.
2. Given the user is on the Goals screen, when they select one or more goals from the approved beginner goal set, then the choice is kept in state, the continue action stays disabled until validation passes, and the selected values are stored as stable enum/string values, not free text.
3. Given the user is on the Equipment screen, when they select one or more available equipment options from the approved beginner set, then the choice is kept in state, survives recomposition/process recreation, and the values are stored as stable enum/string values.
4. Given the user is on the Frequency screen, when they choose a weekly workout frequency from 1 to 7 workouts per week, then the value is required, stored as an Int, and invalid or empty values prevent continuation.
5. Given the user completes the Frequency step, then the beginner profile is saved locally through the onboarding boundary and upserted to Firestore `users/{uid}` with merge semantics, preserving existing auth/profile fields and the saved Beginner fitness level.
6. Given a save or Firestore sync fails, when the user tries to continue, then the UI shows a recoverable error, keeps all entered values intact, and does not advance out of the beginner flow.
7. Given the beginner flow finishes saving successfully, then it emits a typed one-time action or callback to the next onboarding handoff and does not set the onboarding-complete flag; OB-004 owns completion/root removal.
8. Given the flow is implemented in Compose, then it uses `FitnessAppTheme`, core-ui tokens, accessible semantics, 48dp minimum touch targets, responsive layout, and the current single-activity Navigation 3 shell.
9. Given automated verification runs, then domain, data, UI, and app-level navigation behavior are covered by focused tests without talking to real Firestore, real network, or a physical device camera.

## Tasks / Subtasks

- [x] Extend the onboarding domain boundary for beginner profile data. Add stable beginner models for goals, equipment, and frequency, keep the existing level-selection contract intact, and expose single-use-case entry points for reading and writing the beginner draft/profile. (AC: 2-5, 7)
  - [x] Reuse the approved beginner vocabulary from the architecture appendix (`FitnessGoal`, `Equipment`, `weeklyFrequency`) instead of inventing free-form strings.
  - [x] Keep Android/Firebase types out of domain models and return explicit `Result` values on failure.
- [x] Implement local and Firestore persistence in onboarding-data. Persist the beginner answers through `PreferencesDataSource` for local continuity and upsert `users/{uid}` in Firestore with merge semantics so existing profile data is preserved. (AC: 3-6)
  - [x] Add the Firebase Firestore dependency to `feature/onboarding/onboarding-data` through the version catalog if it is not already present there.
  - [x] Do not depend on `:feature:auth:auth-data`; onboarding-data should own its own Firestore write path or a small data-source abstraction.
  - [x] Fail closed on storage/read/write issues and map them to onboarding domain errors.
- [x] Build the beginner onboarding MVI flow in onboarding-ui. Add state, events, one-time actions, and a ViewModel for the three-step wizard, plus Compose screens for Goals, Equipment, and Frequency with validation and step-to-step progression. (AC: 1-8)
  - [x] Keep the beginner branch flow state-driven; do not introduce a nested NavController or string routes.
  - [x] Reuse the existing `WelcomeLevelRoute` handoff and preserve the back action to the level selector.
  - [x] Keep the UI accessible, responsive, and aligned with the existing FitLife visual language rather than creating a new design system.
- [x] Wire the app host to the real beginner branch. Replace the temporary `BranchDestination` placeholder in `MainActivity.kt` with the feature-owned beginner flow while preserving the typed Navigation 3 shell and root replacement behavior. (AC: 1, 7, 8)
  - [x] Keep `AppRoute.Onboarding` and the existing onboarding completion reader contract intact for OB-004.
  - [x] Do not change the intermediate placeholder branch in this story except where shared wiring forces it.
- [x] Add focused automated tests. Cover validation, state restoration, local persistence, Firestore merge behavior, error mapping, typed action emission, and the app-level beginner branch handoff. (AC: 2-9)
  - [x] Use fake storage/data-source implementations and coroutine test utilities for unit coverage.
  - [x] Keep unit tests off real Firestore and verify only the behavior the beginner branch owns.
  - [x] Add navigation assertions only where needed to prove the typed back stack still routes from the level selector into the beginner flow.

### Review Findings

- [x] [Review][Patch] Completion handoff still allows OB-002 to route to Home [app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt:379]
- [x] [Review][Patch] Beginner handoff test does not assert the fixed back-stack state before continuing [app/src/androidTest/java/com/aml_sakr/fitlife/FitLifeAppNavigationTest.kt:406]

## Dev Notes

### Current State

- OB-001 already created the level selector and persists selected `FitnessLevel` through `PreferencesDataSource`.
- `MainActivity.kt` currently wires `AppRoute.BeginnerOnboarding` to a `BranchDestination` placeholder with the description "We'll start with goals, equipment, and workout frequency."
- `feature/onboarding/onboarding-data` currently only knows how to persist and read the selected fitness level.
- `feature/onboarding/onboarding-ui` currently exposes `WelcomeLevelRoute`, `WelcomeLevelViewModel`, and `WelcomeLevelAction` for Beginner/Intermediate branch selection.
- The app already uses typed Navigation 3 `NavKey` keys and root replacement; this story should extend that shell, not replace it.

### What This Story Changes

- Replaces the beginner branch placeholder with a real beginner onboarding wizard.
- Adds beginner-specific profile persistence that can feed later workout plan generation.
- Keeps the existing beginner/intermediate branch split from OB-001, but makes the beginner branch actually useful.
- Leaves onboarding completion and the final removal of onboarding from the future launch path to OB-004.

### What Must Be Preserved

- The selected fitness level stored by OB-001.
- The current `AppRoute.Onboarding` root replacement behavior from auth/startup.
- The no-`NavController`, no-`NavHost`, no string routes rule.
- Existing auth and startup contracts, especially `OnboardingCompletionReader`.
- The FitLife theme, Inter typography, and Material3 composition patterns.
- The beginner flow must not mark onboarding complete by itself.

### Architecture Compliance

- Follow MVI + Clean Architecture.
- Keep domain free of Android, Compose, Firebase, and Firestore SDK types.
- Keep feature boundaries clean: onboarding-data may depend on `core-data`, Firebase Firestore, and onboarding-domain; it should not reach into auth-data.
- Use the approved beginner profile vocabulary from `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#14.1 User Profile (Room Entity)`.
- Persist beginner profile fields with merge semantics so future onboarding/profile fields are preserved rather than overwritten.
- Use typed Navigation 3 keys and app-owned back-stack replacement only where the app host already does so.
- Keep versions pinned to the checked-in Gradle catalog; do not upgrade Navigation 3, Compose, or Firebase versions just because official docs have newer releases.

### UX And Accessibility

- The UX spec defines the beginner path as Goals -> Equipment -> Frequency, with optional later screens handled elsewhere; Frequency is a discrete 1-7 workouts/week choice.
- The beginner screens should be quick to complete, low-friction, and clear about progression.
- Keep selection controls large enough for touch, with 48dp minimum targets and obvious selected/unselected states.
- Use accessible semantics for the selectable chips/cards and for any step progress indicator.
- Keep the flow comfortable on smaller phones and larger font scales.

### Data And Sync Notes

- Begin with local persistence so in-progress selections survive process death and app restarts.
- Mirror the final beginner profile to Firestore `users/{uid}` using merge semantics, preserving `id`, `email`, `isEmailVerified`, and any unrelated profile fields already present.
- Store goals and equipment as stable enum-backed values, not localized labels; `weeklyFrequency` should remain an integer.
- Keep Firestore errors mapped to user-safe domain errors; never surface raw SDK text.

### Project Structure Notes

- Likely touch points:
  - `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt`
  - `feature/onboarding/onboarding-data/build.gradle.kts`
  - `feature/onboarding/onboarding-data/src/main/java/com/aml_sakr/fitlife/feature/onboarding/data/...`
  - `feature/onboarding/onboarding-data/src/test/java/com/aml_sakr/fitlife/feature/onboarding/data/...`
  - `feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/...`
  - `feature/onboarding/onboarding-domain/src/test/java/com/aml_sakr/fitlife/feature/onboarding/domain/...`
  - `feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/...`
  - `feature/onboarding/onboarding-ui/src/test/java/com/aml_sakr/fitlife/feature/onboarding/ui/...`
- Prefer a beginner-specific package inside onboarding-ui, such as `beginner`, so the wizard stays isolated from the level selector surface.
- Do not create a second onboarding module or move the beginner work into `:app`.

### Testing Requirements

- JVM tests should use fakes for the local persistence boundary and any Firestore-facing abstraction.
- Verify repository behavior for first run, partial draft restore, invalid stored values, local write failure, and Firestore write failure.
- Verify ViewModel behavior separately from the UI: step progression, validation, loading guards, action emission, and error recovery.
- Verify app-level behavior only enough to confirm that the beginner branch is reachable from the existing level selector and does not break the typed back stack.
- Keep tests deterministic and emulator-first if an integration test is absolutely needed.

### References

- Story source: `docs/fitlife-stories-v1.md`
- PRD: `_bmad-output/planning-artifacts/fitlife-prd-v1.md`
- Architecture: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md`
- UX spec: `docs/fitlife-ux-spec-v1.md`
- Previous story: `_bmad-output/implementation-artifacts/ob-001-welcome-level-screen.md`
- App host: `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt`
- Onboarding level selector: `feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/WelcomeLevelScreen.kt`
- Onboarding level ViewModel: `feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/WelcomeLevelViewModel.kt`
- Onboarding repository: `feature/onboarding/onboarding-data/src/main/java/com/aml_sakr/fitlife/feature/onboarding/data/repository/PreferencesOnboardingRepository.kt`
- Onboarding domain contract: `feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/repository/OnboardingRepository.kt`
- Architecture appendix: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#14.1 User Profile (Room Entity)`
- Navigation 3 guide: https://developer.android.com/guide/navigation/navigation-3
- Navigation 3 save-state guide: https://developer.android.com/guide/navigation/navigation-3/save-state
- Compose accessibility: https://developer.android.com/develop/ui/compose/accessibility
- Compose touch target guidance: https://developer.android.com/guide/topics/ui/accessibility/apps

## Design References

Stitch Screen:
- _bmad-output/design/onboarding/ob-beginner-goals.png
- _bmad-output/design/onboarding/ob-beginner-equipment.png

Design:
- _bmad-output/design/onboarding/ob-beginner-goals.png
- _bmad-output/design/onboarding/ob-beginner-goals-reference.html
- _bmad-output/design/onboarding/ob-beginner-goals-design.md
- https://stitch.withgoogle.com/projects/14149816895860058914?node-id=7370d72a04b14b7caf3ff820223c3e46
- _bmad-output/design/onboarding/ob-beginner-equipment.png
- _bmad-output/design/onboarding/ob-beginner-equipment-reference.html
- _bmad-output/design/onboarding/ob-beginner-equipment-design.md
- https://stitch.withgoogle.com/projects/14149816895860058914?node-id=5c6ed790c5384e03b4999a1d0c023235
- _bmad-output/design/onboarding/ob-beginner-frequency.png
- _bmad-output/design/onboarding/ob-beginner-frequency-reference.html
- _bmad-output/design/onboarding/ob-beginner-frequency-design.md
- https://stitch.withgoogle.com/projects/14149816895860058914?node-id=b57fee0d01c74dd1b1b1d8efefa0bb65
## Dev Agent Record

### Agent Model Used

GPT-5

### Debug Log References

- `.\gradlew.bat :feature:onboarding:onboarding-domain:test :feature:onboarding:onboarding-data:testDebugUnitTest :feature:onboarding:onboarding-ui:testDebugUnitTest :app:compileDebugKotlin :app:compileDebugAndroidTestKotlin`
- `.\gradlew.bat :feature:onboarding:onboarding-ui:compileDebugKotlin`
- `.\gradlew.bat :app:compileDebugKotlin`
- `.\gradlew.bat :app:compileDebugAndroidTestKotlin`
- `.\gradlew.bat :feature:onboarding:onboarding-ui:testDebugUnitTest --tests com.aml_sakr.fitlife.feature.onboarding.ui.beginner.BeginnerOnboardingViewModelTest`

### Completion Notes List

- Added beginner onboarding domain models, draft read/save use cases, and explicit onboarding errors for invalid stored data and remote sync failures.
- Implemented preferences-backed draft persistence plus a Firestore merge upsert path for `users/{uid}`.
- Built the beginner MVI flow with state restoration, validation, autosave, and a typed finish action back to the app host.
- Replaced the app-level beginner placeholder with the real feature route and a loading state while the auth session id resolves.
- Resolved the beginner handoff review finding by routing the finish action to a dedicated app-level completion handoff instead of removing the root in the beginner branch.
- Resolved the weekly frequency validation review finding by constraining accepted values to `1..7` and rejecting invalid events before they can enable continuation.
- Updated the onboarding tests and app navigation tests to cover the new beginner flow and repository contract.

### File List

- `D:/LinkDevProject/FitLife/_bmad-output/implementation-artifacts/ob-002-beginner-path-goals-equipment-frequency.md`
- `D:/LinkDevProject/FitLife/_bmad-output/implementation-artifacts/sprint-status.yaml`
- `D:/LinkDevProject/FitLife/app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt`
- `D:/LinkDevProject/FitLife/app/src/androidTest/java/com/aml_sakr/fitlife/FitLifeAppNavigationTest.kt`
- `D:/LinkDevProject/FitLife/feature/onboarding/onboarding-data/build.gradle.kts`
- `D:/LinkDevProject/FitLife/feature/onboarding/onboarding-data/src/main/java/com/aml_sakr/fitlife/feature/onboarding/data/di/OnboardingModule.kt`
- `D:/LinkDevProject/FitLife/feature/onboarding/onboarding-data/src/main/java/com/aml_sakr/fitlife/feature/onboarding/data/repository/BeginnerOnboardingRemoteDataSource.kt`
- `D:/LinkDevProject/FitLife/feature/onboarding/onboarding-data/src/main/java/com/aml_sakr/fitlife/feature/onboarding/data/repository/FirebaseBeginnerOnboardingRemoteDataSource.kt`
- `D:/LinkDevProject/FitLife/feature/onboarding/onboarding-data/src/main/java/com/aml_sakr/fitlife/feature/onboarding/data/repository/PreferencesOnboardingRepository.kt`
- `D:/LinkDevProject/FitLife/feature/onboarding/onboarding-data/src/test/java/com/aml_sakr/fitlife/feature/onboarding/data/repository/PreferencesOnboardingRepositoryTest.kt`
- `D:/LinkDevProject/FitLife/feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/error/OnboardingError.kt`
- `D:/LinkDevProject/FitLife/feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/model/BeginnerOnboardingDraft.kt`
- `D:/LinkDevProject/FitLife/feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/model/BeginnerOnboardingStep.kt`
- `D:/LinkDevProject/FitLife/feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/model/Equipment.kt`
- `D:/LinkDevProject/FitLife/feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/model/FitnessGoal.kt`
- `D:/LinkDevProject/FitLife/feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/repository/OnboardingRepository.kt`
- `D:/LinkDevProject/FitLife/feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/usecase/ReadBeginnerDraftUseCase.kt`
- `D:/LinkDevProject/FitLife/feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/usecase/SaveBeginnerProfileUseCase.kt`
- `D:/LinkDevProject/FitLife/feature/onboarding/onboarding-domain/src/test/java/com/aml_sakr/fitlife/feature/onboarding/domain/usecase/BeginnerOnboardingUseCaseTest.kt`
- `D:/LinkDevProject/FitLife/feature/onboarding/onboarding-domain/src/test/java/com/aml_sakr/fitlife/feature/onboarding/domain/usecase/OnboardingUseCaseTest.kt`
- `D:/LinkDevProject/FitLife/feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/beginner/BeginnerOnboardingAction.kt`
- `D:/LinkDevProject/FitLife/feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/beginner/BeginnerOnboardingEvent.kt`
- `D:/LinkDevProject/FitLife/feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/beginner/BeginnerOnboardingRoute.kt`
- `D:/LinkDevProject/FitLife/feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/beginner/BeginnerOnboardingScreen.kt`
- `D:/LinkDevProject/FitLife/feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/beginner/BeginnerOnboardingState.kt`
- `D:/LinkDevProject/FitLife/feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/beginner/BeginnerOnboardingViewModel.kt`
- `D:/LinkDevProject/FitLife/feature/onboarding/onboarding-ui/src/test/java/com/aml_sakr/fitlife/feature/onboarding/ui/WelcomeLevelViewModelTest.kt`

### Change Log

- Addressed code review findings for OB-002 - 2 items resolved (Date: 2026-06-19).
