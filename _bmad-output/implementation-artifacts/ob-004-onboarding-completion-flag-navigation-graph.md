# Story OB-004: Onboarding Completion Flag & Navigation Graph

Status: done
Design Status: Pending (UX spec available; no dedicated Stitch export)

Completion Note: Implemented onboarding completion persistence and startup wiring, then addressed follow-up review findings for email-verification-safe post-auth routing, sign-up verification handling, and fail-closed completion reads.

## Story

As a developer,
I want a persisted onboarding-complete flag and a dedicated startup gate,
so that FitLife only shows onboarding before the user has finished it and routes directly to the main app on future launches.

## Acceptance Criteria

1. Given a signed-in user completes the final beginner or intermediate onboarding step, when the finish action succeeds, then the app persists `isOnboardingComplete = true` for that user before it leaves the onboarding flow.
2. Given the completion flag is stored for the current user, when startup routing evaluates an authenticated session, then `DetermineStartupDestinationUseCase` resolves to Home for completed onboarding and Onboarding for incomplete or missing completion state.
3. Given a signed-in user has not completed onboarding, when the app launches or the user signs back in, then onboarding remains reachable and the app does not bypass the onboarding graph.
4. Given onboarding completion succeeds, when the app leaves the onboarding branch, then the onboarding routes are removed atomically from the Navigation 3 back stack and system back cannot return to them.
5. Given the completion flag is missing, unreadable, or fails to write, when the app evaluates completion state, then the implementation fails closed, keeps the user in onboarding, and does not corrupt existing onboarding drafts or profile data.
6. Given the completion flag is implemented, then it stays within the existing single-activity Navigation 3 shell, typed `NavKey` destinations, MVI flow, and FitLife theme without introducing `NavController`, `NavHost`, or string routes.
7. Given automated verification runs, then repository/use-case tests cover completion persistence and startup resolution, and app navigation tests cover complete and incomplete users plus the post-finish root replacement behavior.

## Tasks / Subtasks

- [ ] Extend the onboarding domain contract with completion state ownership. Add a small completion-focused API to the onboarding domain and focused use cases for reading and marking onboarding complete. (AC: 1-7)
  - [ ] Keep the completion contract in onboarding-domain so auth-domain can keep its startup reader boundary without depending on app-local state.
  - [ ] Use stable `Result<Unit, OnboardingError>` / `Result<Boolean, OnboardingError>` style outcomes instead of throwing storage exceptions across layer boundaries.
  - [ ] Do not create a second onboarding persistence store if the existing repository can be extended safely.
- [ ] Persist the completion flag in onboarding-data using `PreferencesDataSource`. Store the flag per authenticated user so one account finishing onboarding does not hide onboarding for another account on the same device. (AC: 1, 2, 5)
  - [ ] Reuse the existing onboarding preferences repository and keying conventions where possible.
  - [ ] Fail closed on storage read/write errors and do not treat corruption as completion.
  - [ ] Keep the flag separate from beginner/intermediate drafts so saving completion does not overwrite profile data.
- [ ] Replace the app-local fake onboarding-completion reader with a real adapter. Wire the app startup bindings to the onboarding completion use case/repository instead of the current always-false placeholder. (AC: 2-6)
  - [ ] Keep `OnboardingCompletionReader` in auth-domain intact; adapt to it from the composition root.
  - [ ] Remove or replace the `StartupBindingsModule` placeholder provider and the `DefaultOnboardingCompletionReader` fallback so app startup no longer depends on a fake implementation.
  - [ ] Preserve the existing auth startup contract and `DetermineStartupDestinationUseCase`.
- [ ] Update the onboarding finish path. When the beginner or intermediate flow completes successfully, mark onboarding complete first, then root-replace to the post-onboarding destination. (AC: 1, 4, 5)
  - [ ] Keep the beginner and intermediate branch flows themselves unchanged except for the final handoff.
  - [ ] Do not mark completion if the final profile save/sync fails.
  - [ ] Treat the current completion handoff routes as transient implementation detail only; they must not remain in the back stack after completion.
- [ ] Add focused automated tests. Cover domain/data completion persistence, startup routing, finish-path root replacement, and fail-closed behavior. (AC: 1-7)
  - [ ] Unit tests should use fakes for `PreferencesDataSource` or the onboarding repository boundary.
  - [ ] App-level tests should assert the typed Navigation 3 back stack directly.
  - [ ] Keep tests off real Firebase, real network, and real device-specific services.

### Review Findings

- [x] [Review][Patch] Branch onboarding routes accept invalid or unverified sessions before allowing completion [D:/LinkDevProject/FitLife/app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt:272]
- [x] [Review][Patch] App-local fake onboarding completion fallback still bypasses persisted completion state [D:/LinkDevProject/FitLife/app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt:146]
- [x] [Review][Patch] Onboarding completion persistence catches `Throwable` and swallows coroutine cancellation [D:/LinkDevProject/FitLife/feature/onboarding/onboarding-data/src/main/java/com/aml_sakr/fitlife/feature/onboarding/data/repository/PreferencesOnboardingRepository.kt:134]
- [x] [Review][Defer] Global root back handling can empty the Navigation 3 back stack [D:/LinkDevProject/FitLife/app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt:165] — deferred, pre-existing
- [x] [Review][Patch] Branch session lookup failures do not fail closed to Auth [D:/LinkDevProject/FitLife/app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt:275]
- [x] [Review][Patch] Level-selection navigation tests do not provide the verified branch session now required by branch guards [D:/LinkDevProject/FitLife/app/src/androidTest/java/com/aml_sakr/fitlife/FitLifeAppNavigationTest.kt:393]
- [x] [Review][Patch] Onboarding route ViewModel initializer is syntactically broken [D:/LinkDevProject/FitLife/app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt:251]
- [x] [Review][Patch] Selected-level checkmark renders mojibake instead of an intentional icon [D:/LinkDevProject/FitLife/feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/WelcomeLevelScreen.kt:346]
- [x] [Review][Defer] Non-completion onboarding persistence paths still catch `Throwable` and swallow coroutine cancellation [D:/LinkDevProject/FitLife/feature/onboarding/onboarding-data/src/main/java/com/aml_sakr/fitlife/feature/onboarding/data/repository/PreferencesOnboardingRepository.kt:39] - deferred, pre-existing
- [x] [Review][Defer] Selected level and draft preferences remain device-global instead of per-user [D:/LinkDevProject/FitLife/feature/onboarding/onboarding-data/src/main/java/com/aml_sakr/fitlife/feature/onboarding/data/repository/PreferencesOnboardingRepository.kt:429] - deferred, pre-existing

## Dev Notes

### Current State

- OB-002 and OB-003 already own the beginner and intermediate onboarding content, local draft persistence, and Firestore profile sync.
- Both branch stories currently stop at a temporary completion handoff instead of owning the real completion flag.
- `MainActivity.kt` already has typed completion handoff routes for beginner and intermediate flows.
- `StartupBindingsModule.kt` currently provides an app-local `OnboardingCompletionReader` that always returns `false`.
- `DetermineStartupDestinationUseCase` already consumes `OnboardingCompletionReader`, so the startup seam exists; the reader implementation is the missing piece.
- `PreferencesOnboardingRepository` already owns onboarding preferences and is the right place to add the completion flag instead of creating a parallel store.

### What This Story Changes

- Adds a real onboarding-complete contract and storage path.
- Replaces the always-false startup placeholder with a real completion reader.
- Makes the onboarding finish action persist completion before leaving the branch.
- Ensures completed onboarding is removed from the navigation root instead of lingering in the back stack.

### What Must Be Preserved

- Beginner and intermediate draft/profile persistence from OB-002 and OB-003.
- The selected fitness level stored by OB-001.
- Auth startup and email-verification behavior from AUTH-000/AUTH-001.
- The current typed Navigation 3 shell, app-owned root replacement, and serializable NavKey pattern.
- FitLife theme, Inter typography, accessible controls, and the current Compose MVI approach.
- The existing onboarding completion reader contract in auth-domain.
- Fail-closed behavior for missing, blank, or corrupted completion state.

### Architecture Compliance

- Keep onboarding completion state in the onboarding feature boundary, not in auth or app-only state.
- Keep domain free of Android, Compose, Firebase, Firestore, and Navigation types.
- Use the existing onboarding repository and add completion methods or a clearly scoped companion contract rather than inventing a second persistence layer.
- Adapt the onboarding completion use case/repository to `OnboardingCompletionReader` at the app composition root.
- Do not add a nested `NavController`, a second `NavHost`, or string routes.
- Keep the story within the current module graph: `onboarding-data`, `onboarding-domain`, `onboarding-ui`, and the app composition root.

### UX And Accessibility

- Completion should feel like a terminal step, not a new screen family.
- Preserve the current beginner/intermediate branch UI and step progression.
- Keep touch targets at least 48dp and avoid hiding the finish action behind non-obvious gestures.
- Do not introduce visual churn that would make the final step feel disconnected from the rest of onboarding.
- The completion transition should be brief, deterministic, and not leave the user stranded on a spinner if the flag write succeeds.

### Data And Sync Notes

- Store completion per user ID so one account finishing onboarding does not affect another account on the same device.
- If the completion flag cannot be read, assume onboarding is not complete.
- If the completion flag cannot be written, do not advance to Home and do not flip the flag locally.
- Keep completion separate from beginner/intermediate profile payloads; the flag is routing metadata, not profile content.
- No Firestore write is required for the completion flag unless the implementation explicitly decides to mirror it, but the default design should remain local and deterministic.

### Startup And Navigation Notes

- `DetermineStartupDestinationUseCase` already resolves Home vs Onboarding from auth session plus completion state; this story should feed it the real completion reader.
- `MainActivity.kt` currently has `BeginnerCompletionHandoff` and `IntermediateCompletionHandoff`; after this story, the finish path should end with `AppRoute.Home` and the onboarding root should not stay on the stack.
- Keep post-login routing consistent with AUTH-001's onboarding-first correction.
- Do not change the beginner/intermediate content decisions from OB-002/OB-003 unless the completion write requires a small terminal handoff adjustment.

### Project Structure Notes

- Likely touch points:
  - `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt`
  - `app/src/main/java/com/aml_sakr/fitlife/StartupBindingsModule.kt`
  - `app/src/androidTest/java/com/aml_sakr/fitlife/FitLifeAppNavigationTest.kt`
  - `feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/repository/OnboardingRepository.kt`
  - `feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/usecase/ReadOnboardingCompletionUseCase.kt`
  - `feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/usecase/MarkOnboardingCompleteUseCase.kt`
  - `feature/onboarding/onboarding-data/src/main/java/com/aml_sakr/fitlife/feature/onboarding/data/repository/PreferencesOnboardingRepository.kt`
  - `feature/onboarding/onboarding-data/src/test/java/com/aml_sakr/fitlife/feature/onboarding/data/repository/PreferencesOnboardingRepositoryTest.kt`
  - `feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/beginner/BeginnerOnboardingViewModel.kt`
  - `feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/intermediate/IntermediateOnboardingViewModel.kt`
  - Matching onboarding UI tests for the finish behavior
- Do not create a new onboarding module or move the completion logic into `:app`.

### Testing Requirements

- Verify completion read/write behavior with fakes and coroutine test utilities.
- Verify missing/blank user ID handling fails closed.
- Verify startup routing for complete and incomplete users.
- Verify the finish path does not navigate to Home until completion persistence succeeds.
- Verify the Navigation 3 back stack contains only the post-onboarding destination after completion.
- Keep tests deterministic and offline.

### Previous Story Intelligence

- OB-002 and OB-003 intentionally left onboarding completion to OB-004; do not reimplement their branch logic.
- Their finish actions currently route to dedicated handoff entries, so the expected final-state assertions will change when completion is wired in.
- Both stories already established the state-driven onboarding UI pattern, local draft persistence, and typed terminal actions; this story should preserve that pattern.
- AUTH-001 already depends on onboarding completion to decide whether login lands on Onboarding or Home, so this story must satisfy that contract rather than adding a parallel one.

### References

- Story source: `docs/fitlife-stories-v1.md#EPIC-2-ONBOARDING`
- PRD: `_bmad-output/planning-artifacts/fitlife-prd-v1.md`
- Architecture: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#11-Navigation-3-Structure`
- Architecture appendix: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#14.1 User Profile (Room Entity)`
- UX spec: `docs/fitlife-ux-spec-v1.md#3.2 Onboarding Screens`
- App startup bindings: `app/src/main/java/com/aml_sakr/fitlife/StartupBindingsModule.kt`
- App host: `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt`
- Auth startup contract: `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/startup/OnboardingCompletionReader.kt`
- Onboarding repository: `feature/onboarding/onboarding-domain/src/main/java/com/aml_sakr/fitlife/feature/onboarding/domain/repository/OnboardingRepository.kt`
- Onboarding persistence: `feature/onboarding/onboarding-data/src/main/java/com/aml_sakr/fitlife/feature/onboarding/data/repository/PreferencesOnboardingRepository.kt`
- Beginner flow: `feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/beginner/BeginnerOnboardingRoute.kt`
- Intermediate flow: `feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/intermediate/IntermediateOnboardingRoute.kt`
- Previous story context: `_bmad-output/implementation-artifacts/ob-002-beginner-path-goals-equipment-frequency.md`
- Previous story context: `_bmad-output/implementation-artifacts/ob-003-intermediate-path-split-goals-optional-1rm.md`

## Dev Agent Record

### Agent Model Used

GPT-5

### Debug Log References

- Story created from current repo state, Epic 2, the UX spec, the architecture doc, OB-002/OB-003 handoff behavior, and current startup bindings.
- Addressed review follow-ups for AUTH-001 parity in post-auth routing and fail-closed startup completion checks.
- Addressed second review pass findings for sign-up verification routing, direct startup fail-closed coverage, and onboarding title expectation.

### Completion Notes List

- Story context created, implemented, and moved to review.
- Captured the current fake onboarding-completion reader seam in app startup bindings.
- Captured the existing beginner/intermediate completion handoff behavior that OB-004 must replace or retire.
- Wired onboarding completion persistence into the shared onboarding repository and use-case layer.
- Replaced the app startup completion placeholder with the real onboarding completion reader adapter.
- Updated beginner and intermediate finish flows to persist completion before root-replacing to Home.
- Verified with `./gradlew.bat :feature:onboarding:onboarding-domain:test :feature:onboarding:onboarding-data:testDebugUnitTest :feature:onboarding:onboarding-ui:testDebugUnitTest :app:compileDebugKotlin :app:compileDebugAndroidTestKotlin --no-daemon --console=plain`.
- Restored the email-verification gate in post-auth routing so unverified users stay in Auth instead of reaching onboarding or Home.
- Made both startup and post-auth onboarding-completion reads fail closed to Onboarding when the reader throws.
- Added regression coverage for completion-read failures and unverified post-auth users.
- Routed unverified sign-up results through authenticated-user handling so the app-level verification gate keeps them in Auth.
- Added direct startup fail-closed coverage for completion-reader exceptions.
- Corrected the incomplete-onboarding navigation test to wait for the actual welcome title.
- Tightened completion-reader exception handling from `Throwable` to `Exception` while preserving cancellation rethrow.

### File List

- `D:/LinkDevProject/FitLife/_bmad-output/implementation-artifacts/ob-004-onboarding-completion-flag-navigation-graph.md`
- `D:/LinkDevProject/FitLife/app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt`
- `D:/LinkDevProject/FitLife/app/src/androidTest/java/com/aml_sakr/fitlife/FitLifeAppNavigationTest.kt`
- `D:/LinkDevProject/FitLife/feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/startup/DetermineStartupDestinationUseCase.kt`
- `D:/LinkDevProject/FitLife/feature/auth/auth-domain/src/test/java/com/aml_sakr/fitlife/feature/auth/domain/startup/DetermineStartupDestinationUseCaseTest.kt`
- `D:/LinkDevProject/FitLife/feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/viewmodel/AuthViewModel.kt`
- `D:/LinkDevProject/FitLife/feature/auth/auth-ui/src/test/java/com/aml_sakr/fitlife/feature/auth/auth_ui/auth/viewmodel/AuthViewModelTest.kt`
