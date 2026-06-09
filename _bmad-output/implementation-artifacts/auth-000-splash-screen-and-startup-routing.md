# Story AUTH-000: Splash Screen and Startup Routing

Status: ready-for-dev

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

## Tasks / Subtasks

- [ ] Build the splash UI. (AC: 1, 2)
  - [ ] Create a Compose splash screen in `:feature:auth:auth-ui`.
  - [ ] Use `FitnessAppTheme`/core UI tokens rather than hardcoded visual styling.
  - [ ] Include a simple loading indicator without interactive controls.
- [ ] Add startup routing state. (AC: 3, 4, 5, 7)
  - [ ] Add splash state/event/action types following the existing MVI naming pattern.
  - [ ] Add a ViewModel or equivalent MVI state holder that starts the routing check once.
  - [ ] Emit one-time navigation actions for unauthenticated, onboarding-required, and main/home destinations.
- [ ] Connect domain checks. (AC: 3, 4, 5, 7)
  - [ ] Read the current auth session through an auth-domain use case or repository interface.
  - [ ] Read onboarding completion through the onboarding-domain boundary or a temporary interface if onboarding persistence is not implemented yet.
  - [ ] Keep Firebase, DataStore, Room, and Android framework details out of domain APIs.
- [ ] Wire app navigation. (AC: 3, 4, 5)
  - [ ] Make splash the startup destination in the Compose navigation graph.
  - [ ] Route from splash to auth, onboarding, or main/home without leaving splash on the back stack.
  - [ ] Preserve the single-activity Compose host.
- [ ] Verify privacy and startup behavior. (AC: 2, 6)
  - [ ] Confirm no camera permission is requested at app launch.
  - [ ] Add focused unit tests for routing decisions where practical.
  - [ ] Run the smallest relevant Gradle verification command.

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
