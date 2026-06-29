# Story 4.7: Guided Session UI with Lottie Demos

Status: ready-for-dev

## Story

As a FitLife user,
I want animated exercise demos during a session,
so that I can follow proper form and maintain motivation through visual guidance.

## Acceptance Criteria

1. [ ] Lottie animation plays automatically for the current exercise in the active session.
2. [ ] The animation loops continuously while the exercise set is active.
3. [ ] The demo component is positioned such that it does not obscure the camera preview's skeleton overlay or critical session controls.
4. [ ] If a Lottie asset path is null or the file is missing, the UI shows a high-quality static placeholder image for the exercise.
5. [ ] Rep completion events (detected in SESSION-003) trigger a subtle visual feedback on the demo area (e.g., a brief scale pulse).
6. [ ] Adding Lottie animations does not degrade pose detection performance; the app must maintain >= 15 FPS on target devices.
7. [ ] The implementation uses `lottie-compose` and follows the project's MVI + Clean Architecture patterns.

## Tasks / Subtasks

- [ ] Task 1: Add Lottie Dependencies (AC: 7)
  - [ ] Add `lottie = "6.7.1"` to `gradle/libs.versions.toml`.
  - [ ] Add `lottie-compose` to `feature/session/session-ui/build.gradle.kts`.
- [ ] Task 2: Update Session Contracts and ViewModel (AC: 1, 4, 5)
  - [ ] Add `currentExerciseLottiePath: String?` to `ActiveSessionState` in `ActiveSessionContracts.kt`.
  - [ ] Update `ActiveSessionViewModel` to populate `currentExerciseLottiePath` when an exercise is started or changed (e.g., during equipment rerouting).
  - [ ] Map `WorkoutExercise` or `ExerciseAlternative` to the state's lottie path.
- [ ] Task 3: Create ExerciseDemo Composable (AC: 2, 4, 5)
  - [ ] Create `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/components/ExerciseDemo.kt`.
  - [ ] Use `LottieAnimation` from `lottie-compose`.
  - [ ] Handle `null` or invalid paths by rendering a placeholder from resources.
  - [ ] Implement a `Modifier.animateContentSize()` or scale pulse triggered by rep completion.
- [ ] Task 4: Integrate with ActiveSessionCameraRoute (AC: 1, 3, 6)
  - [ ] Add `ExerciseDemo` to the `ActiveSessionOverlay` in `ActiveSessionCameraRoute.kt`.
  - [ ] Position the demo in a non-obstructive corner (e.g., top-right or bottom-right as per UX spec).
  - [ ] Verify that adding the composable does not block the camera frame analysis channel.
- [ ] Task 5: Testing & Verification (AC: 6)
  - [ ] Add UI test `ExerciseDemoTest.kt` to verify animation loading and placeholder fallback.
  - [ ] Update `ActiveSessionViewModelTest.kt` to verify state updates for lottie paths.
  - [ ] Perform a performance smoke test to ensure pose detection FPS remains stable.

## Dev Notes

- **Architecture Compliance**:
  - Keep MVI unidirectional flow: ViewModel updates state with Lottie path -> UI renders.
  - No business logic in `ExerciseDemo.kt`.
- **Performance**:
  - Lottie animations should be loaded from `assets/lottie/`.
  - Use `rememberLottieComposition` to avoid re-reading the asset on every recomposition.
- **Source Tree**:
  - `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/components/` (New component)
  - `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/ActiveSessionContracts.kt` (Update state)
  - `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/ActiveSessionViewModel.kt` (Update logic)
  - `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/preview/ActiveSessionCameraRoute.kt` (Update UI)

### Project Structure Notes

- Naming follows established `{Feature}{Type}` pattern (e.g., `ExerciseDemo`).
- Lottie dependency is centralized in `libs.versions.toml`.

### References

- [Source: docs/fitlife-stories-v1.md#496-SESSION-007]
- [Source: docs/fitlife-ux-spec-v1.md#215-LottieAnimation]
- [Source: _bmad-output/planning-artifacts/fitlife-architecture-v1.md#14.4-Exercise-Library]
- [Source: _bmad-output/implementation-artifacts/session-001-camerax-preview-composable.md] (UI baseline)
- [Source: _bmad-output/implementation-artifacts/session-006-equipment-rerouting-bottom-sheet-gemini-api.md] (Lottie path context)

## Dev Agent Record

### Agent Model Used

BMad Story Context Engine

### Debug Log References

### Completion Notes List

### File List
