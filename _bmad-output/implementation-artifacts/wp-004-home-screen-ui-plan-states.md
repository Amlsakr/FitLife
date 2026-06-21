# Story WP-004: Workout Dashboard UI - Plan States

Status: ready-for-dev

Completion Note: Comprehensive implementation context created for the Home tab workout dashboard state machine and plan rendering flow.

## Story

As a FitLife user,
I want to see my weekly plan on the Home tab with clear states for loading, success, empty, and error,
so that I always understand whether my plan is ready, refreshing, missing, or failed without guessing.

## Acceptance Criteria

1. A production Home tab workout dashboard UI exists in `:feature:workout:workout-ui` and renders four explicit states: loading, success, empty, and error.
2. The screen uses the existing workout domain boundary and `GenerateWorkoutPlanUseCase`; it does not call Gemini, Room, Firebase, or any transport/data-layer API directly.
3. The refresh action triggers `GenerateWorkoutPlanUseCase` exactly once per user action, disables repeat taps while loading, and preserves the current visible state until the new result arrives.
4. The empty state shows the copy `Generate a plan` and gives the user a clear CTA to start plan generation.
5. The success state renders the current weekly workout plan using the pure domain model `WorkoutPlan`, not Gemini DTOs, Room entities, or UI-specific duplicates.
6. The error state shows a safe user-facing message and a retry path; raw exceptions, HTTP bodies, stack traces, and Firebase/Gemini details stay out of the UI.
7. The UI keeps the 7-day plan invariant from the domain layer and must not invent a shorter or longer plan.
8. The screen follows the existing FitLife Compose design system and MVI style used elsewhere in the app, including `BaseMviViewModel`-style state handling and the shared theme tokens from `core-ui`.
9. The implementation does not add weekly-overview scrollers, day-detail navigation, session start logic, or widget behavior here; those belong to WP-005 and later stories.
10. Unit tests cover state transitions, refresh behavior, error mapping, and the four render branches without requiring live Gemini, Firestore, Android navigation, or device-only APIs.

## Tasks / Subtasks

- [ ] Add the Home tab workout dashboard MVI state layer in `:feature:workout:workout-ui`.
  - [ ] Create `WorkoutHomeState`, `WorkoutHomeEvent`, and `WorkoutHomeAction` with explicit branches for loading, success, empty, and error.
  - [ ] Add `WorkoutHomeViewModel` that depends on `GenerateWorkoutPlanUseCase` and a pure `WorkoutGenerationRequest` input.
  - [ ] Keep loading, success, empty, and error transitions deterministic and easy to test.
- [ ] Add the Compose screen and route wrapper in `:feature:workout:workout-ui`.
  - [ ] Build `WorkoutHomeRoute` that collects state and one-time actions from the ViewModel.
  - [ ] Build `WorkoutHomeScreen` that renders the four states and exposes a refresh CTA.
  - [ ] Keep the success UI compact and plan-focused; do not prebuild the WP-005 weekly-overview component here.
- [ ] Update the workout UI module dependencies and build wiring.
  - [ ] Add the minimal Compose lifecycle dependencies needed for the ViewModel and state collection pattern already used elsewhere in the repo.
  - [ ] Keep dependency changes limited to the workout UI module unless a shared library gap is discovered.
- [ ] Add offline unit tests for the state machine and the screen contract.
  - [ ] Verify refresh dispatch, disabled-loading behavior, empty copy, success rendering, and error rendering.
  - [ ] Verify the ViewModel does not call the use case more than once per refresh event.
- [ ] Verify the smallest relevant build surface after implementation.
  - [ ] Prefer the workout UI unit tests first.
  - [ ] Compile the workout UI module and any directly affected dependent module if needed.

## Dev Notes

### Current State

- `feature/workout/workout-ui` currently contains only `build.gradle.kts`; there is no production source or test code yet.
- `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt` still uses a placeholder `Home` destination inside the App Shell scaffold. It does not yet host the final workout dashboard content.
- `feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/usecase/GenerateWorkoutPlanUseCase.kt` already exists and is the correct orchestration entry point for loading or regenerating plans.
- `feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/model/WorkoutPlan.kt` already defines the pure plan model and freshness behavior.
- `feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/repository/WorkoutPlanRepository.kt` already separates cache, remote generation, fallback loading, and save responsibilities.
- `feature/workout/workout-data` and `core/core-data` already own the Gemini transport, fallback asset, and Room persistence layers. This story must not duplicate those responsibilities in UI code.

### What This Story Changes

- Introduces the Home tab workout dashboard presentation layer for the plan lifecycle states.
- Connects the screen to the existing workout domain use case through a narrow ViewModel boundary.
- Defines the user experience for loading, empty, success, and error without leaking transport or persistence details.
- Prepares the workout feature for later weekly-overview and navigation work without implementing those stories early.

### What Must Be Preserved

- Keep the workout plan generation flow in the domain/data layers. The UI may request regeneration, but it must not know how Gemini, fallback selection, or Room persistence work.
- Preserve the 7-day plan invariant from the domain model.
- Preserve the existing MVI pattern and shared FitLife theme system.
- Preserve the current app-level `Home` placeholder until a separate navigation story explicitly replaces it.
- Keep the workout UI module independent of onboarding, auth, and widget implementation details.

### Architecture Compliance

- Follow the module structure in `docs/fitlife-stories-v1.md` and `_bmad-output/planning-artifacts/fitlife-architecture-v1.md`: workout UI depends on workout domain and core UI only.
- Use the shared MVI style from `core-ui` rather than inventing a new UI state framework.
- Keep UI state pure and serializable where practical. Do not store Android `Context`, Room entities, Retrofit DTOs, or repository implementations in the state.
- If a `WorkoutGenerationRequest` must be passed into the ViewModel, keep it as a pure domain object and do not derive it from onboarding or auth layers inside the UI module.
- Do not add Navigation 2 code or direct `NavController` dependencies. Any future wiring should follow the Navigation 3 typed-key pattern already used elsewhere in the app.

### Library And Framework Requirements

- Use the existing Compose stack already pinned in `gradle/libs.versions.toml`.
- For the workout UI module, add only the Compose/lifecycle dependencies required to collect ViewModel state and render Material 3 UI consistently with the rest of the app.
- Reuse the shared `FitnessAppTheme` and core UI tokens from `core-ui` instead of introducing a second design system.
- Keep all state handling on the ViewModel side in coroutines and avoid blocking calls from composables.
- Prefer explicit state branches over hidden side effects so the loading/success/empty/error behavior stays obvious in tests.

### File Structure Requirements

Expected new or updated areas:

- `_bmad-output/implementation-artifacts/wp-004-home-screen-ui-plan-states.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `feature/workout/workout-ui/build.gradle.kts`
- `feature/workout/workout-ui/src/main/java/com/aml_sakr/fitlife/feature/workout/ui/...`
- `feature/workout/workout-ui/src/test/java/com/aml_sakr/fitlife/feature/workout/ui/...`

Likely new files in the workout UI package:

- `WorkoutHomeState.kt`
- `WorkoutHomeEvent.kt`
- `WorkoutHomeAction.kt`
- `WorkoutHomeViewModel.kt`
- `WorkoutHomeScreen.kt`
- `WorkoutHomeRoute.kt`
- Optional lightweight UI model helpers if the raw domain model needs a presentation adapter

### Testing Requirements

- Use JVM unit tests for the ViewModel state machine and any pure mapping helpers.
- Verify the empty state copy exactly matches `Generate a plan`.
- Verify the success branch renders a `WorkoutPlan` without requiring Android navigation, Gemini, or Room.
- Verify loading state disables repeated refresh requests until the current request finishes.
- Verify error state maps to a safe user-facing message and supports retry.
- Do not introduce device-only UI tests unless the implementation truly needs them; keep the smallest stable test surface.

### Project Structure Notes

- The workout UI module is currently empty, so this story should create the first production source files there rather than modifying unrelated modules.
- The Home tab workout dashboard should stay small and composable. If plan-summary formatting becomes more complex, keep the formatting helper private to the workout UI boundary.
- If the screen needs an initial plan input from the caller, keep that input pure and explicit instead of reading onboarding state directly from the UI layer.
- Success-state presentation should stay compact enough that WP-005 can later add the dedicated weekly overview component without untangling duplicated layout code.

### References

- Story source: `docs/fitlife-stories-v1.md`
- Epic and story map: `docs/fitlife-stories-v1.md#WP-004`
- PRD: `_bmad-output/planning-artifacts/fitlife-prd-v1.md#5-11-home-screen-widget` and `_bmad-output/planning-artifacts/fitlife-prd-v1.md#5-2-ai-workout-plan-generation-gemini-api`
- Architecture: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#2-mvi-implementation-example-workout-feature`
- Architecture: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#5-repository-interfaces-domain-layer`
- Architecture: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#6-use-case-implementations-domain`
- Previous story: `_bmad-output/implementation-artifacts/wp-003-workoutplan-room-entities-daos.md`
- Workout domain use case: `feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/usecase/GenerateWorkoutPlanUseCase.kt`
- Workout domain repository contract: `feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/repository/WorkoutPlanRepository.kt`
- App placeholder home entry: `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt`
- Existing MVI example: `feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/WelcomeLevelViewModel.kt`
- Existing Compose example: `feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/WelcomeLevelScreen.kt`

## Change Log

- 2026-06-20: Created WP-004 implementation story context for the Home tab workout dashboard plan state UI.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story context loaded from the WP-004 spec in `docs/fitlife-stories-v1.md`, the current sprint status, the PRD, architecture, the workout domain use case and repository contract, the app shell placeholder Home destination, and existing onboarding/auth MVI examples.
- Confirmed the workout UI module currently has no source files, so the first implementation should establish the UI state machine and screen contract cleanly.
- Confirmed the app still exposes a placeholder `Home` destination inside the App Shell, so this story should stay within the workout UI boundary and not attempt to solve navigation integration yet.
- Confirmed the workout domain already owns generation, caching, fallback, and persistence orchestration. The UI layer should only request refresh and render state.

### Completion Notes

- Story file created for the Home tab workout dashboard plan-state UI.
- Sprint tracking should move `wp-004-home-screen-ui-plan-states` from `backlog` to `ready-for-dev` after this artifact is saved.

### File List

- `_bmad-output/implementation-artifacts/wp-004-home-screen-ui-plan-states.md`

### Story Status

ready-for-dev
