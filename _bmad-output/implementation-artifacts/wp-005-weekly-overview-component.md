# Story WP-005: Weekly Overview Component

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a FitLife user,
I want a concise overview of each day’s workouts on the Home tab success state,
so that I can plan my week at a glance and navigate to the details of each day's exercises.

## Acceptance Criteria

1. A horizontal scrolling list of day cards (the `WeeklyOverview` component) is implemented in the success state of `WorkoutHomeScreen`.
2. Each day card represents a workout day in the plan and displays:
   - The day number and title (e.g., "Day 1: Full-body foundation").
   - The calculated total duration (e.g., "30 min").
   - The calculated total reps across all exercises for that day (e.g., "54 reps").
3. Total reps is calculated dynamically for each day by summing up the product of `sets * reps` for all exercises in that day. The parsing logic must handle string-based reps safely (e.g., extraction of numbers from formats like "10", "8-10", "As many reps as possible", or empty strings).
4. Tapping a day card navigates the user to a detailed screen for that day (`WorkoutDayDetailScreen`).
5. Navigation is implemented following Navigation 3 conventions:
   - A new serializable destination `WorkoutDestination.DayDetail(val day: Int)` is added to the navigation graph.
   - The day detail screen is registered in `registerWorkoutEntries()` within `WorkoutNavigation.kt`.
   - The navigation back stack updates dynamically using the existing `NavBackStack` callback.
6. The styling uses the project's Compose design system: `FitnessAppTheme`, custom Material 3 cards, and tokens from `core-ui` (spacing, corner radius, colors).
7. Unit tests cover:
   - The reps calculation logic under various string formats.
   - Day card layout rendering and details navigation trigger on click.
   - Day details screen displaying correct exercise names, sets, and reps.
8. The project compiles successfully and all unit tests in `:feature:workout:workout-ui` pass.

## Tasks / Subtasks

- [ ] Implement `WorkoutDay.calculateTotalReps()` calculation logic
  - [ ] Add helper function to parse/sum up sets * reps from strings safely.
  - [ ] Write JVM unit tests for this utility (testing "10", "8-10", "12 reps", "", etc.).
- [ ] Add the `WeeklyOverview` horizontal scrolling composable
  - [ ] Create `WeeklyOverview` using a `LazyRow` with appropriate item spacing.
  - [ ] Build `WorkoutDayCard` with premium card design (rounded corners, subtle borders, high visual contrast).
  - [ ] Display Day title, duration, and calculated total reps.
  - [ ] Wire the card click listener to nav callback.
- [ ] Add Day Detail navigation using Navigation 3
  - [ ] Modify `WorkoutDestination` in `WorkoutNavigation.kt` to add `@Serializable data class DayDetail(val day: Int)`.
  - [ ] Update `registerWorkoutEntries()` to support the new `DayDetail` destination.
  - [ ] Implement `WorkoutDayDetailScreen` rendering the list of exercises, sets, reps, and durations.
  - [ ] Connect the back action to remove the last entry.
- [ ] Integrate into `WorkoutHomeScreen`
  - [ ] Add `onNavigateToDayDetail` callback to `WorkoutHomeScreen` and `WorkoutHomeRoute`.
  - [ ] Render `WeeklyOverview` in the `SuccessPlanState` below the main plan stats summary.
- [ ] Add unit tests for the updated UI components
  - [ ] Verify card click triggers navigation event.
  - [ ] Verify day detail screen maps exercises correctly.

## Dev Notes

### Relevant Architecture Patterns and Constraints
- **MVI Architecture**: Ensure all state updates flow unidirectionally. Since navigation is a one-time side-effect, route it through callbacks to the parent back stack.
- **Clean Architecture Module Boundaries**: `:feature:workout:workout-ui` depends on `:feature:workout:workout-domain` and `:core:core-ui`. Do not leak data layer models (Room entities, Retrofit DTOs) or platform-specific dependencies into this module.
- **Navigation 3 standard**: Use typed `NavKey` destinations and avoid strings/NavController/NavHost.

### Source Tree Components to Touch
- `feature/workout/workout-ui/src/main/java/com/aml_sakr/fitlife/feature/workout/ui/WorkoutHomeScreen.kt`
- `feature/workout/workout-ui/src/main/java/com/aml_sakr/fitlife/feature/workout/ui/WorkoutHomeRoute.kt`
- `feature/workout/workout-ui/src/main/java/com/aml_sakr/fitlife/feature/workout/ui/navigation/WorkoutNavigation.kt`
- New: `feature/workout/workout-ui/src/main/java/com/aml_sakr/fitlife/feature/workout/ui/WorkoutDayDetailScreen.kt` (or nested in navigation)

### Testing Standards Summary
- JVM unit tests belong in `src/test`.
- Cover ViewModel state changes, helper parsing logic, and MVI actions without launching emulator.

### Project Structure Notes
- Keep styling centralized via `FitnessAppTheme` and `FitLifeDimens` from `core-ui`.
- Day card horizontal scrolls should not trigger parent vertical scroll conflicts.

### References
- Story source: [fitlife-stories-v1.md](file:///d:/LinkDevProject/FitLife/docs/fitlife-stories-v1.md#L388-L401)
- UX Specifications: [fitlife-ux-spec-v1.md](file:///d:/LinkDevProject/FitLife/docs/fitlife-ux-spec-v1.md#L149)
- Previous Story spec: [wp-004-home-screen-ui-plan-states.md](file:///d:/LinkDevProject/FitLife/_bmad-output/implementation-artifacts/wp-004-home-screen-ui-plan-states.md)

## Dev Agent Record

### Agent Model Used

Gemini 3.5 Flash (Medium)

### Debug Log References

### Completion Notes List

### File List
