# Story AUTH-000: Splash Screen and Startup Routing

Status: in-progress
Design Status: Linked

## Story

As a user,
I want FitLife to open with a branded loading screen and route me to the right next screen,
so that the app starts smoothly and predictably. 

## Acceptance Criteria

1. Given the app starts, when startup routing is in progress, then a fullscreen branded splash screen is shown with the FitLife brand mark or wordmark centered.
2. Given startup routing is in progress, when the splash screen is displayed, then the user sees a clear loading state and no blocking controls.
3. Given the user is authenticated and onboarding is complete, when startup routing finishes, then the app navigates to the main/home flow.
4. Given the user is authenticated and onboarding is not complete, when startup routing finishes, then the app navigates to onboarding.
5. Given the user is not authenticated, when startup routing finishes, then the app navigates to the login/sign-in flow.
6. Given camera permission is privacy-sensitive, when the app launches into the splash screen, then no camera permission is requested from splash or startup routing.
7. Given the app uses MVI + Clean Architecture, when splash routing state is implemented, then UI rendering, events, state, one-time navigation actions, and domain checks remain separated.
8. Given startup checks fail unexpectedly, when routing cannot be determined, then the user remains on splash, an error is logged, and a retryable fallback action is emitted.
9. Given the app uses Navigation 3, when startup routing is composed, then Splash is a serializable `NavKey`, `NavDisplay` renders the active entry, destination ViewModels are entry-scoped, and routing atomically replaces Splash as the only root entry.

## Tasks / Subtasks

- [x] Build the splash UI. (AC: 1, 2)
  - [x] Create a Compose splash screen in `:feature:auth:auth-ui`.
  - [x] Use `FitnessAppTheme`/core UI tokens rather than hardcoded visual styling.
  - [x] Include a simple loading indicator without interactive controls.
- [x] Add startup routing state. (AC: 3, 4, 5, 7)
  - [x] Add splash state/event/action types following the existing MVI naming pattern.
  - [x] Add a ViewModel or equivalent MVI state holder that starts the routing check once.
  - [x] Emit one-time navigation actions for unauthenticated, onboarding-required, and main/home destinations.
- [x] Connect domain checks. (AC: 3, 4, 5, 7)
  - [x] Read the current auth session through a dedicated domain use case.
  - [x] Read onboarding completion through the onboarding-domain boundary or a temporary interface if onboarding persistence is not implemented yet.
  - [x] Keep Firebase, DataStore, Room, and Android framework details out of domain APIs.
- [x] Wire app navigation. (AC: 3, 4, 5)
  - [x] Make Splash the initial key in the app-owned Navigation 3 back stack.
  - [x] Route from Splash to Auth, Onboarding, or Home through atomic root replacement.
  - [x] Render destinations through `NavDisplay` and typed entry-provider registrations.
  - [x] Scope the splash ViewModel and saveable state to the Splash `NavEntry`.
  - [x] Preserve the single-activity Compose host.
- [x] Verify privacy and startup behavior. (AC: 2, 6)
  - [x] Confirm no camera permission is requested at app launch.
  - [x] Add focused unit tests for routing decisions where practical.
  - [x] Run the smallest relevant Gradle verification command.

### Review Findings

- [x] [Review][Defer] Runtime app cannot satisfy authenticated startup routes yet — deferred to AUTH-001, which owns the real auth/session boundary; keep AUTH-000 in progress until that integration exists [app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt:122]
- [x] [Review][Patch] Create `SplashViewModel` through a `ViewModelStoreOwner` instead of `remember` [app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt:53]
- [x] [Review][Patch] Expose a retryable fallback UI/action instead of discarding `ShowRetryableFallback` [feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/ui/splash/SplashScreen.kt:54]
- [x] [Review][Patch] Do not swallow coroutine cancellation in startup route failures [feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/ui/splash/SplashViewModel.kt:35]
- [x] [Review][Patch] Guard or cancel concurrent startup route checks [feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/ui/splash/SplashViewModel.kt:24]
- [x] [Review][Patch] Ignore stale splash navigation actions after leaving splash [app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt:85]
- [x] [Review][Patch] Treat blank `AuthSession.userId` as unauthenticated or invalid [feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/startup/DetermineStartupDestinationUseCase.kt:8]
- [ ] [Review][Patch] Reconnect the state-driven `SplashRoute`; the current app renders an unconnected splash forever [app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt:67]
- [ ] [Review][Patch] Restore the unauthenticated navigation callback instead of discarding `NavigateToAuth` [feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/ui/splash/SplashScreen.kt:54]
- [ ] [Review][Patch] Remove the untracked `ss.kt` prototype and its remote-logo/Coil path; it bypasses MVI/theme tokens and contains a Kotlin compile error [feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/ui/splash/ss.kt:107]
- [ ] [Review][Patch] Revert unrelated dependency upgrades and keep any required dependency additions in the version catalog [gradle/libs.versions.toml:10]
- [ ] [Review][Patch] Make the retry fallback usable with system-bar insets, short displays, large text, and screen-reader announcements [feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/ui/splash/SplashScreen.kt:95]
- [x] [Review][Patch] Add an application-level Navigation 3 test covering splash actions, typed destination changes, and splash root replacement [app/src/androidTest/java/com/aml_sakr/fitlife/FitLifeAppNavigationTest.kt:1]

## Dev Notes

### Current State

- Splash is already part of the UX flow and auth navigation expectation but was missing as a tracked sprint story.
- The UX spec defines a fullscreen splash with centered brand logo, loading indicator, and loading-only state.
- The original story source used Navigation 2 `NavHost` terminology; the approved 2026-06-14 course correction replaces it with typed Navigation 3 keys and entry-provider registration.
- Sprint status previously listed `auth-001` through `auth-007` but no splash story.

### Architecture Compliance

- Follow MVI + Clean Architecture. Compose UI renders immutable state and sends events; navigation should be emitted as one-time actions.
- Keep `MainActivity` as the single Activity host with Compose `setContent`.
- Do not request camera permission at app launch. Camera disclosure remains in `AUTH-006` / session flow.
- Do not call Firebase or onboarding persistence directly from composables.

### Dependencies

- Depends on `SETUP-001`, `SETUP-002`, and `SETUP-003`.
- Should be implemented before or alongside `AUTH-007` because the typed auth navigation contract must own the Splash key and entry registration.

### References

- UX spec: `docs/fitlife-ux-spec-v1.md`
- Story source: `docs/fitlife-stories-v1.md`
- Architecture: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md`
- Project context: `_bmad-output/project-context.md`

## Design References

Stitch Screen:
- Splash

Design:
- _bmad-output/design/auth/splash.png
- _bmad-output/design/auth/splash-reference.html
- https://stitch.withgoogle.com/projects/14149816895860058914?node-id=4ea971a3c33a419c836c6a39e5fe33dc

Required States:
- Loading
- NavigateToAuth
- NavigateToOnboarding
- NavigateToHome

Implementation Notes:
- Match spacing and hierarchy from Stitch.
- Colors and typography must use core-ui tokens.
- If Stitch conflicts with core-ui tokens, architecture constraints, or acceptance criteria, the story requirements take precedence and Correct Course should be triggered.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Implementation Plan

- Add a pure Kotlin startup-routing use case in `auth-domain` with small interfaces for current auth session and onboarding completion checks.
- Add MVI splash state, events, actions, ViewModel, and error logging hook in `auth-ui`.
- Build a code-native Compose splash screen based on the Stitch reference while using Material3/core-ui theme tokens.
- Replace the starter app greeting with Navigation 3 `NavDisplay`, a saveable app-owned back stack, and typed root replacement from Splash to Auth, Onboarding, or Home.

### Debug Log References

- 2026-06-09: Red phase added domain startup routing tests and splash ViewModel routing/failure tests.
- 2026-06-09: Initial focused test run failed as expected because startup-routing and splash MVI classes did not exist.
- 2026-06-09: Focused verification passed with `.\gradlew.bat :feature:auth:auth-domain:test :feature:auth:auth-ui:test :app:testDebugUnitTest --no-daemon --console=plain`.
- 2026-06-09: Full JVM regression passed with `.\gradlew.bat test --no-daemon --console=plain`.
- 2026-06-09: Verified `app/src/main` and `feature/auth` contain no camera permission references.
- 2026-06-10: Red phase confirmed blank authenticated user IDs were not rejected by startup routing.
- 2026-06-10: Focused routing and ViewModel tests passed (11 tests), including blank IDs, retry concurrency, cancellation, navigation actions, and fallback behavior.
- 2026-06-10: Debug APK assembled successfully with `.\gradlew.bat :app:assembleDebug --no-daemon --console=plain`.
- 2026-06-10: Full JVM regression passed with `.\gradlew.bat test --no-daemon --console=plain`.
- 2026-06-10: No Android device was attached for rendered-device screenshot verification.
- 2026-06-14: Migrated startup navigation from Navigation Compose 2.9.8 to stable Navigation 3 `1.1.2` using serializable `NavKey` objects, `rememberNavBackStack`, `NavDisplay`, saveable-state/ViewModel decorators, and direct typed-stack instrumentation assertions.
- 2026-06-14: `:app:compileDebugKotlin` and `:app:compileDebugAndroidTestKotlin` passed after the Navigation 3 migration.
- 2026-06-14: Focused auth/startup tests, `:app:assembleDebug`, and `:app:assembleDebugAndroidTest` passed.
- 2026-06-14: Full `test` and `lint` verification passed; JVM reports contain 80 tests, 0 failures, 0 errors, and 1 skipped emulator-dependent test.
- 2026-06-14: No Android device was attached, so the migrated Navigation 3 instrumentation suite was compiled into the Android-test APK but not executed.

### Completion Notes List

- Implemented `DetermineStartupDestinationUseCase` with unauthenticated, onboarding-required, and home destinations.
- Added test coverage for startup routing decisions and startup-check failure propagation.
- Added splash MVI state, event, action, ViewModel, and retryable-failure logging behavior.
- Added a responsive Compose splash screen matching the Stitch hierarchy: dotted light background, centered FitLife brand lockup, tagline, loading indicator, and startup status text.
- Replaced the starter `Greeting` screen with a single-activity Navigation 3 `NavDisplay` where Splash is the initial key and navigation atomically replaces it.
- Used app-local default startup readers that currently route unauthenticated users to auth until future auth/onboarding persistence stories provide real implementations.
- Confirmed no camera permission is requested by the app/auth startup path.
- Redesigned the splash to match the supplied Arctic reference with a soft blue gradient, dotted field, code-native FitLife performance/AI mark, centered brand lockup, and bottom loading status.
- Added a visible retry action for startup failures while keeping the normal loading state free of controls.
- Scoped `SplashViewModel` to the splash navigation entry and rejected navigation attempts after splash leaves the active destination.
- Removed Navigation 2 controller/string-route dependencies and added stable Navigation 3 runtime/UI with serializable typed keys.
- Updated application navigation tests to assert the owned typed back stack directly.
- Preserved coroutine cancellation, prevented concurrent startup checks, and treated blank session IDs as unauthenticated.
- Kept the story in progress because the real authenticated session/onboarding data source remains dependent on AUTH-001 and onboarding persistence work.

## File List

- `_bmad-output/implementation-artifacts/auth-000-splash-screen-and-startup-routing.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `app/build.gradle.kts`
- `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt`
- `app/src/androidTest/java/com/aml_sakr/fitlife/FitLifeAppNavigationTest.kt`
- `feature/auth/auth-domain/build.gradle.kts`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/startup/AuthSession.kt`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/startup/AuthSessionReader.kt`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/startup/DetermineStartupDestinationUseCase.kt`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/startup/OnboardingCompletionReader.kt`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/startup/StartupDestination.kt`
- `feature/auth/auth-domain/src/test/java/com/aml_sakr/fitlife/feature/auth/domain/startup/DetermineStartupDestinationUseCaseTest.kt`
- `feature/auth/auth-ui/build.gradle.kts`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/ui/splash/SplashAction.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/ui/splash/SplashEvent.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/ui/splash/SplashScreen.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/ui/splash/SplashState.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/ui/splash/SplashViewModel.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/ui/splash/StartupRouteErrorLogger.kt`
- `feature/auth/auth-ui/src/test/java/com/aml_sakr/fitlife/feature/auth/ui/splash/SplashViewModelTest.kt`
- `gradle/libs.versions.toml`
- `_bmad-output/design/auth/splash-reference.html`

## Change Log

- 2026-06-09: Implemented AUTH-000 splash screen and startup routing; story moved to review.
- 2026-06-10: Redesigned splash UI and resolved 6 code review patches; story returned to in-progress pending the authenticated runtime source decision.
- 2026-06-14: Approved course correction migrated AUTH-000 startup routing and navigation tests to Navigation 3.
