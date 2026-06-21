# Story OB-003: Intermediate Path - Split, Goals, Optional 1RM

Status: done
Design Status: Linked

## Story

As an Intermediate user,
I want to define my current split routine and optional 1RM so recommendations match my experience.

## Acceptance Criteria

1. Given the user selected Intermediate in OB-001, when the intermediate branch opens, then the first screen is Current Split and the flow shows a clear step indicator and a back action to return to the level selector.
2. Given the user is on the Current Split screen, when they select one of the approved split templates, then the choice is kept in state, survives recomposition and process recreation, and is stored as a stable enum/string value, not free text.
3. Given the user is on the Goals screen, when they select one or more goals from the approved goal set, then the choice is kept in state, continues to survive recomposition and process recreation, and is stored as stable enum/string values, not free text.
4. Given the user is on the Optional 1RM screen, when they leave the field empty, then the flow still allows continuation and no 1RM value is persisted for that lift.
5. Given the user enters a 1RM value, when the value is invalid, non-numeric, or not positive, then validation blocks continuation and the UI shows a recoverable error without losing the entered data.
6. Given the user enters a valid 1RM value, then the value is stored under the architecture-defined `oneRepMax` shape using stable lift keys, not free text labels, and any surfaced unit picker is normalized before persistence.
7. Given the user completes the intermediate flow, then the intermediate profile is saved locally through the onboarding boundary and upserted to Firestore `users/{uid}` with merge semantics, preserving existing auth/profile fields and the saved Intermediate fitness level.
8. Given a save or Firestore sync fails, when the user tries to continue, then the UI shows a recoverable error, keeps all entered values intact, and does not advance out of the intermediate flow.
9. Given the intermediate flow finishes saving successfully, then it emits a typed one-time action or callback to the next onboarding handoff and does not set the onboarding-complete flag; OB-004 owns completion/root removal.
10. Given the flow is implemented in Compose, then it uses `FitnessAppTheme`, core-ui tokens, Inter typography, Arctic Focus colors, accessible semantics, 48dp minimum touch targets, responsive layout, and the current single-activity Navigation 3 shell.
11. Given automated verification runs, then domain, data, UI, and app-level navigation behavior are covered by focused tests without talking to real Firestore, real network, or a physical device camera.

## Tasks / Subtasks

- [x] Extend the onboarding domain boundary for intermediate profile data. Add stable models for current split, goals, and optional 1RM entries, and expose single-use-case entry points for reading and writing the intermediate draft/profile. (AC: 2-9)
  - [x] Reuse the approved onboarding vocabulary from the architecture appendix (`currentSplit`, `oneRepMax`, `FitnessGoal`) instead of inventing free-form strings.
  - [x] Keep Android/Firebase types out of domain models and return explicit `Result` values on failure.
- [x] Implement local and Firestore persistence in onboarding-data. Persist the intermediate answers through the onboarding local storage boundary for continuity and upsert `users/{uid}` in Firestore with merge semantics so existing profile data is preserved. (AC: 4-8)
  - [x] Add the Firebase Firestore dependency to `feature/onboarding/onboarding-data` through the version catalog if it is not already present there.
  - [x] Do not depend on `:feature:auth:auth-data`; onboarding-data should own its own Firestore write path or a small data-source abstraction.
  - [x] Fail closed on storage/read/write issues and map them to onboarding domain errors.
- [x] Build the intermediate onboarding MVI flow in onboarding-ui. Add state, events, one-time actions, and a ViewModel for the split -> goals -> optional 1RM wizard, plus Compose screens with validation and step-to-step progression. (AC: 1-10)
  - [x] Keep the intermediate branch flow state-driven; do not introduce a nested NavController or string routes.
  - [x] Reuse the existing `WelcomeLevelRoute` handoff and preserve the back action to the level selector.
  - [x] Keep the UI accessible, responsive, and aligned with the existing FitLife visual language rather than creating a new design system.
- [x] Wire the app host to the real intermediate branch. Replace the placeholder `BranchDestination` content in `MainActivity.kt` with the feature-owned intermediate flow while preserving the typed Navigation 3 shell and root replacement behavior. (AC: 1, 9, 10)
  - [x] Keep `AppRoute.Onboarding` and the existing onboarding completion reader contract intact for OB-004.
  - [x] Do not change the beginner branch wiring in this story except where shared onboarding plumbing forces it.
- [x] Add focused automated tests. Cover validation, state restoration, local persistence, Firestore merge behavior, error mapping, typed action emission, and the app-level intermediate branch handoff. (AC: 2-11)
  - [x] Use fake storage/data-source implementations and coroutine test utilities for unit coverage.
  - [x] Keep unit tests off real Firestore and verify only the behavior the intermediate branch owns.
  - [x] Add navigation assertions only where needed to prove the typed back stack still routes from the level selector into the intermediate flow.

## Dev Notes

### Current State

- OB-001 already created the level selector and persists selected `FitnessLevel` through `PreferencesDataSource`.
- OB-002 already implemented the beginner branch, including the onboarding domain/data/ui patterns for draft persistence, Firestore merge writes, validation, and a typed finish action.
- `MainActivity.kt` already contains placeholder copy for the intermediate branch: "We'll start with your split, goals, and optional 1RM."
- The architecture appendix defines `currentSplit: String?` and `oneRepMax: Map<String, Float>?` as intermediate-only fields.
- The existing onboarding feature is already split into `onboarding-domain`, `onboarding-data`, and `onboarding-ui`, with beginner-specific types living under a dedicated package.

### What This Story Changes

- Replaces the intermediate branch placeholder with a real wizard for split, goals, and optional 1RM capture.
- Adds intermediate-specific profile persistence that can feed later workout plan generation.
- Keeps the existing beginner/intermediate branch split from OB-001, but makes the intermediate branch actually useful.
- Leaves onboarding completion and the final removal of onboarding from the future launch path to OB-004.

### What Must Be Preserved

- The selected fitness level stored by OB-001.
- The current `AppRoute.Onboarding` root replacement behavior from auth/startup.
- The no-`NavController`, no-`NavHost`, no string routes rule.
- Existing auth and startup contracts, especially `OnboardingCompletionReader`.
- The FitLife theme, Inter typography, and Material3 composition patterns.
- The beginner flow and its persistence contract from OB-002.
- The intermediate flow must not mark onboarding complete by itself.

### Architecture Compliance

- Follow MVI + Clean Architecture.
- Keep domain free of Android, Compose, Firebase, and Firestore SDK types.
- Keep feature boundaries clean: onboarding-data may depend on core-data, Firebase Firestore, and onboarding-domain; it should not reach into auth-data.
- Use the approved intermediate profile vocabulary from `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#14.1 User Profile (Room Entity)`.
- Persist intermediate profile fields with merge semantics so future onboarding/profile fields are preserved rather than overwritten.
- Use typed Navigation 3 keys and app-owned back-stack replacement only where the app host already does so.
- Keep versions pinned to the checked-in Gradle catalog; do not upgrade Navigation 3, Compose, or Firebase versions just because official docs have newer releases.

### UX And Accessibility

- The UX spec defines the intermediate path as Current Split -> Goals -> 1RM (optional) -> Completion.
- The story source for OB-003 only scopes split, goals, and optional 1RM capture. Do not expand this slice unless the existing app flow already routes it here.
- Keep the split selector as a fixed list of templates and make the selected template obvious.
- Keep the 1RM step optional and validation-only for entered values.
- If a unit picker is surfaced, treat it as UI-only and normalize to the stored numeric representation before saving.
- Keep selection controls large enough for touch, with 48dp minimum targets and obvious selected/unselected states.
- Use accessible semantics for selectable chips/cards and for any step progress indicator.
- Keep the flow comfortable on smaller phones and larger font scales.

### Data And Sync Notes

- Begin with local persistence so in-progress selections survive process death and app restarts.
- Mirror the final intermediate profile to Firestore `users/{uid}` using merge semantics, preserving `id`, `email`, `isEmailVerified`, and any unrelated profile fields already present.
- Store split and goals as stable enum-backed values, not localized labels; `oneRepMax` should remain a stable keyed numeric map.
- Keep `oneRepMax` keys predefined and stable in the feature layer; do not accept arbitrary free-text lift names.
- Keep Firestore errors mapped to user-safe domain errors; never surface raw SDK text.

### Project Structure Notes

- Likely touch points:
  - `_bmad-output/implementation-artifacts/sprint-status.yaml`
  - `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt`
  - `feature/onboarding/onboarding-data/build.gradle.kts`
  - `feature/onboarding/onboarding-data/src/main/java/com/aml_sakr/fitlife/feature/onboarding/data/...`
  - `feature/onboarding/onboarding-data/src/test/java/com/aml_sakr/fitlife/feature/onboarding/data/...`
  - `feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/...`
  - `feature/onboarding/onboarding-domain/src/test/java/com/aml_sakr/fitlife/feature/onboarding/domain/...`
  - `feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/...`
  - `feature/onboarding/onboarding-ui/src/test/java/com/aml_sakr/fitlife/feature/onboarding/ui/...`
- Prefer an intermediate-specific package inside onboarding-ui, such as `intermediate`, so the wizard stays isolated from the level selector and beginner surfaces.
- Do not create a second onboarding module or move the intermediate work into `:app`.

### Testing Requirements

- JVM tests should use fakes for the local persistence boundary and any Firestore-facing abstraction.
- Verify repository behavior for first run, partial draft restore, invalid stored values, local write failure, and Firestore write failure.
- Verify ViewModel behavior separately from the UI: step progression, validation, loading guards, typed action emission, and error recovery.
- Verify app-level behavior only enough to confirm that the intermediate branch is reachable from the existing level selector and does not break the typed back stack.
- Keep tests deterministic and emulator-first if an integration test is absolutely needed.

### References

- Story source: `docs/fitlife-stories-v1.md`
- PRD: `_bmad-output/planning-artifacts/fitlife-prd-v1.md`
- Architecture: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md`
- UX spec: `docs/fitlife-ux-spec-v1.md`
- Project context: `_bmad-output/project-context.md`
- Previous story: `_bmad-output/implementation-artifacts/ob-002-beginner-path-goals-equipment-frequency.md`
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
- _bmad-output/design/onboarding/intermediate/ob-intermediate-split.png
- _bmad-output/design/onboarding/intermediate/ob-intermediate-goals.png
- _bmad-output/design/onboarding/intermediate/ob-intermediate-1RM.png


Design:
- _bmad-output/design/onboarding/intermediate/ob-intermediate-split.png
- _bmad-output/design/onboarding/intermediate/ob-intermediate-split-reference.html
- _bmad-output/design/onboarding/intermediate/ob-intermediate-split-design.md
- https://stitch.withgoogle.com/projects/14149816895860058914?node-id=6d2598dda4774f71aed6c69b51a9b189
- _bmad-output/design/onboarding/intermediate/ob-intermediate-goals.png
- _bmad-output/design/onboarding/intermediate/ob-intermediate-goals-reference.html
- _bmad-output/design/onboarding/intermediate/ob-intermediate-goals-design.md
- https://stitch.withgoogle.com/projects/14149816895860058914?node-id=7e25101be4734df982b9dd1eec8452f7
- _bmad-output/design/onboarding/intermediate/ob-intermediate-1RM.png
- _bmad-output/design/onboarding/intermediate/ob-intermediate-1RM-reference.html
- _bmad-output/design/onboarding/intermediate/ob-intermediate-1RM-design.md
- https://stitch.withgoogle.com/projects/14149816895860058914?node-id=db64cc984c234ca6868f3198a6049267
UX spec:
- `docs/fitlife-ux-spec-v1.md`

## Dev Agent Record

### Agent Model Used

GPT-5

### Debug Log References

- 2026-06-19: Implemented the intermediate onboarding domain models, repository contract, and save/read use cases.
- 2026-06-19: Added PreferencesDataStore-backed intermediate draft persistence and Firestore merge sync support.
- 2026-06-19: Built the intermediate onboarding Compose wizard, ViewModel, route, and app-level navigation handoff.
- 2026-06-19: Verified with `./gradlew.bat :feature:onboarding:onboarding-domain:test`, `./gradlew.bat :feature:onboarding:onboarding-data:testDebugUnitTest`, `./gradlew.bat :feature:onboarding:onboarding-ui:testDebugUnitTest`, `./gradlew.bat :app:compileDebugKotlin`, `./gradlew.bat :app:compileDebugAndroidTestKotlin`, and `./gradlew.bat :app:assembleDebugAndroidTest`.
- 2026-06-19: Fixed autosave recovery for invalid 1RM text by persisting raw intermediate inputs, and surfaced autosave failures on split/goals changes.
- 2026-06-19: Re-verified `./gradlew.bat :feature:onboarding:onboarding-domain:test`, `./gradlew.bat :feature:onboarding:onboarding-data:testDebugUnitTest`, and `./gradlew.bat :feature:onboarding:onboarding-ui:testDebugUnitTest`.
- 2026-06-19: Hardened raw 1RM persistence with safe encoding, kept split/goals recoverable in the UI, and allowed invalid 1RM continuation to surface validation errors.
- 2026-06-19: Closed the remaining review gaps by making split/goals saves synchronous, surfacing 1RM validation inline, and replacing the raw 1RM payload with an escaped draft format that preserves delimiter characters.
- 2026-06-19: Re-verified `./gradlew.bat :feature:onboarding:onboarding-domain:test`, `./gradlew.bat :feature:onboarding:onboarding-data:testDebugUnitTest`, `./gradlew.bat :feature:onboarding:onboarding-ui:testDebugUnitTest`, and `./gradlew.bat :app:assembleDebugAndroidTest`.
- 2026-06-20: Removed the premature intermediate handoff-to-Home transition so OB-003 stops at the typed completion handoff for OB-004 to complete.
- 2026-06-20: Replaced the completed intermediate wizard route with the typed completion handoff so system back cannot replay the finished flow.

### Completion Notes List

- Implemented the intermediate onboarding flow end to end across domain, data, UI, and app host layers.
- Added stable intermediate profile models for current split, goals, and optional 1RM, with KG-normalized persistence for lifted values.
- Wired a dedicated intermediate completion handoff route so the branch can finish without setting the onboarding-complete flag.
- Verified the feature with domain, data, and UI unit tests plus app compile and Android test APK assembly.
- Addressed review feedback by preserving in-progress 1RM text/unit edits across autosave and surfacing local save failures on intermediate branch transitions.
- Addressed review feedback by encoding raw 1RM text safely, keeping split/goals visible after save attempts, and exposing invalid 1RM validation through the Continue action.
- Addressed the remaining review feedback by making split/goals persistence blocking, surfacing 1RM validation directly in the screen, and preserving delimiter characters in the stored 1RM draft payload.
- Verified the final implementation with `./gradlew.bat :feature:onboarding:onboarding-domain:test`, `./gradlew.bat :feature:onboarding:onboarding-data:testDebugUnitTest`, `./gradlew.bat :feature:onboarding:onboarding-ui:testDebugUnitTest`, and `./gradlew.bat :app:assembleDebugAndroidTest`.
- Addressed review feedback by keeping the intermediate completion handoff as a typed terminal handoff instead of routing to Home before OB-004 owns onboarding completion.
- Addressed review feedback by root-replacing the completed intermediate wizard with the handoff route, leaving OB-004 to own final completion/root removal.

### File List

- `_bmad-output/implementation-artifacts/ob-003-intermediate-path-split-goals-optional-1rm.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `app/src/androidTest/java/com/aml_sakr/fitlife/FitLifeAppNavigationTest.kt`
- `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt`
- `feature/onboarding/onboarding-data/src/main/java/com/aml_sakr/fitlife/feature/onboarding/data/di/OnboardingModule.kt`
- `feature/onboarding/onboarding-data/src/main/java/com/aml_sakr/fitlife/feature/onboarding/data/repository/FirebaseIntermediateOnboardingRemoteDataSource.kt`
- `feature/onboarding/onboarding-data/src/main/java/com/aml_sakr/fitlife/feature/onboarding/data/repository/IntermediateOnboardingRemoteDataSource.kt`
- `feature/onboarding/onboarding-data/src/main/java/com/aml_sakr/fitlife/feature/onboarding/data/repository/PreferencesOnboardingRepository.kt`
- `feature/onboarding/onboarding-data/src/test/java/com/aml_sakr/fitlife/feature/onboarding/data/repository/PreferencesOnboardingRepositoryTest.kt`
- `feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/error/OnboardingError.kt`
- `feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/model/IntermediateOnboardingDraft.kt`
- `feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/model/IntermediateOneRepMaxInput.kt`
- `feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/model/IntermediateOnboardingStep.kt`
- `feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/model/IntermediateTrainingSplit.kt`
- `feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/model/OneRepMaxLift.kt`
- `feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/repository/OnboardingRepository.kt`
- `feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/usecase/ReadIntermediateDraftUseCase.kt`
- `feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/usecase/SaveIntermediateProfileUseCase.kt`
- `feature/onboarding/onboarding-domain/src/test/java/com/aml_sakr/fitlife/feature/onboarding/domain/usecase/BeginnerOnboardingUseCaseTest.kt`
- `feature/onboarding/onboarding-domain/src/test/java/com/aml_sakr/fitlife/feature/onboarding/domain/usecase/IntermediateOnboardingUseCaseTest.kt`
- `feature/onboarding/onboarding-domain/src/test/java/com/aml_sakr/fitlife/feature/onboarding/domain/usecase/OnboardingUseCaseTest.kt`
- `feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/intermediate/IntermediateOnboardingAction.kt`
- `feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/intermediate/IntermediateOnboardingEvent.kt`
- `feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/intermediate/IntermediateOnboardingRoute.kt`
- `feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/intermediate/IntermediateOnboardingScreen.kt`
- `feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/intermediate/IntermediateOnboardingState.kt`
- `feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/intermediate/IntermediateOnboardingViewModel.kt`
- `feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/intermediate/OneRepMaxAliases.kt`
- `feature/onboarding/onboarding-ui/src/test/java/com/aml_sakr/fitlife/feature/onboarding/ui/WelcomeLevelViewModelTest.kt`
- `feature/onboarding/onboarding-ui/src/test/java/com/aml_sakr/fitlife/feature/onboarding/ui/beginner/BeginnerOnboardingViewModelTest.kt`
- `feature/onboarding/onboarding-ui/src/test/java/com/aml_sakr/fitlife/feature/onboarding/ui/intermediate/IntermediateOnboardingViewModelTest.kt`

### Change Log

- 2026-06-19 - Implemented the intermediate onboarding flow across domain, data, UI, and app host layers, then moved the story to review.
- 2026-06-19 - Addressed review feedback by persisting raw intermediate 1RM inputs and surfacing autosave save failures on intermediate step transitions.

- 2026-06-19 - Addressed code review findings by removing the blocking save deadlock risk, persisting intermediate draft state through SavedStateHandle, failing closed on invalid stored split values, and tolerating partial 1RM corruption on reload.
- 2026-06-20 - Addressed code review finding by removing the intermediate handoff-to-Home action and updating the app-level navigation assertion.
- 2026-06-20 - Addressed code review finding by replacing the intermediate wizard with the handoff route on finish.

### Review Findings

- [x] [Review][Patch] Intermediate completion handoff still leaves the completed wizard underneath [app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt:344]
- [x] [Review][Medium] Completion handoff routes to Home before OB-004 owns completion [app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt:382] - resolved by rendering the intermediate typed handoff without a Home transition and asserting the handoff back stack in app navigation coverage.

- [x] [Review][Patch] UI-thread blocking save can freeze or deadlock the intermediate flow [feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/intermediate/IntermediateOnboardingViewModel.kt:225] â€” resolved by removing the blocking save path and persisting state through `SavedStateHandle` plus async repository writes.
- [x] [Review][Patch] 1RM edits are still only eventually persisted [feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/intermediate/IntermediateOnboardingViewModel.kt:198] â€” resolved by mirroring the intermediate draft into `SavedStateHandle` immediately on change so process recreation restores the latest typed values.
- [x] [Review][Patch] Invalid 1RM state leaves an enabled Finish button that can no-op [feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/intermediate/IntermediateOnboardingScreen.kt:116] â€” resolved by gating the CTA with `state.canContinue`.
- [x] [Review][Patch] Invalid stored split values are silently treated as empty instead of failing closed [feature/onboarding/onboarding-data/src/main/java/com/aml_sakr/fitlife/feature/onboarding/data/repository/PreferencesOnboardingRepository.kt:128] â€” resolved by returning `InvalidStoredIntermediateDraft` for nonblank unknown split values.
- [x] [Review][Patch] A single malformed 1RM entry makes the whole intermediate draft unloadable [feature/onboarding/onboarding-data/src/main/java/com/aml_sakr/fitlife/feature/onboarding/data/repository/PreferencesOnboardingRepository.kt:229] â€” resolved by ignoring malformed 1RM entries and keeping the rest of the draft loadable.
