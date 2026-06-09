# Story AUTH-000: Splash Screen and Startup Routing

Status: review
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
  - [x] Make splash the startup destination in the Compose navigation graph.
  - [x] Route from splash to auth, onboarding, or main/home without leaving splash on the back stack.
  - [x] Preserve the single-activity Compose host.
- [x] Verify privacy and startup behavior. (AC: 2, 6)
  - [x] Confirm no camera permission is requested at app launch.
  - [x] Add focused unit tests for routing decisions where practical.
  - [x] Run the smallest relevant Gradle verification command.

## Dev Notes

### Current State

- Splash is already part of the UX flow and auth navigation expectation but was missing as a tracked sprint story.
- The UX spec defines a fullscreen splash with centered brand logo, loading indicator, and loading-only state.
- The legacy story source says the auth NavHost includes SignIn, SignUp, ForgotPassword, and Splash.
- Sprint status previously listed `auth-001` through `auth-007` but no splash story.

### Architecture Compliance

- Follow MVI + Clean Architecture. Compose UI renders immutable state and sends events; navigation should be emitted as one-time actions.
- Keep `MainActivity` as the single Activity host with Compose `setContent`.
- Do not request camera permission at app launch. Camera disclosure remains in `AUTH-006` / session flow.
- Do not call Firebase or onboarding persistence directly from composables.

### Dependencies

- Depends on `SETUP-001`, `SETUP-002`, and `SETUP-003`.
- Should be implemented before or alongside `AUTH-007` because the auth navigation graph must include Splash.

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
- Replace the starter app greeting with a Compose `NavHost` that starts at splash and navigates to auth, onboarding, or home with splash removed from the back stack.

### Debug Log References

- 2026-06-09: Red phase added domain startup routing tests and splash ViewModel routing/failure tests.
- 2026-06-09: Initial focused test run failed as expected because startup-routing and splash MVI classes did not exist.
- 2026-06-09: Focused verification passed with `.\gradlew.bat :feature:auth:auth-domain:test :feature:auth:auth-ui:test :app:testDebugUnitTest --no-daemon --console=plain`.
- 2026-06-09: Full JVM regression passed with `.\gradlew.bat test --no-daemon --console=plain`.
- 2026-06-09: Verified `app/src/main` and `feature/auth` contain no camera permission references.

### Completion Notes List

- Implemented `DetermineStartupDestinationUseCase` with unauthenticated, onboarding-required, and home destinations.
- Added test coverage for startup routing decisions and startup-check failure propagation.
- Added splash MVI state, event, action, ViewModel, and retryable-failure logging behavior.
- Added a responsive Compose splash screen matching the Stitch hierarchy: dotted light background, centered FitLife brand lockup, tagline, loading indicator, and startup status text.
- Replaced the starter `Greeting` screen with a single-activity Compose startup `NavHost` where splash is the start destination and navigation removes splash from the back stack.
- Used app-local default startup readers that currently route unauthenticated users to auth until future auth/onboarding persistence stories provide real implementations.
- Confirmed no camera permission is requested by the app/auth startup path.

## File List

- `_bmad-output/implementation-artifacts/auth-000-splash-screen-and-startup-routing.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `app/build.gradle.kts`
- `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt`
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

## Change Log

- 2026-06-09: Implemented AUTH-000 splash screen and startup routing; story moved to review.
