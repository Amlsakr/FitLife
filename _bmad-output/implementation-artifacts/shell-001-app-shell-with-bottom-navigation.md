# Story SHELL-001: App Shell with Bottom Navigation

Status: ready-for-dev

Design Status: Planned (no dedicated Stitch export; see UX spec and design-story-map TODO)

## Story

As a signed-in user,
I want a persistent bottom navigation shell,
so that I can move between Home, Workout, Progress, and Profile without losing app context.

## Acceptance Criteria

1. Persistent bottom navigation is visible only in the signed-in experience.
2. Tabs exist for Home, Workout, Progress, and Profile.
3. The shell preserves tab state and back stack per top-level destination.
4. The shell owns the main app container and does not hold workout plan generation or session state logic.
5. The shell does not expose app-wide navigation internals to feature screens.
6. The shell remains within the existing single-activity Navigation 3 architecture, using typed `NavKey` destinations and app-owned back stacks instead of `NavController`, `NavHost`, or string routes.
7. When the signed-in shell is entered from auth or onboarding, Home is the default selected tab and the back stack is root-replaced so obsolete auth/onboarding entries cannot be revisited.
8. Automated tests cover signed-in-only visibility, tab switching, tab-state restoration, and the post-auth/post-onboarding handoff into the shell.

## Tasks / Subtasks

- [ ] Create the app-owned shell host in `:app`. (AC: 1-7)
  - [ ] Add a shell composable that renders the bottom navigation scaffold and the top-level tab container.
  - [ ] Define typed Navigation 3 destinations for the shell root and its top-level tabs.
  - [ ] Keep the shell root separate from Home tab content so the shell does not become the dashboard.
  - [ ] Preserve the current single-activity Compose host and `NavDisplay` pattern.
- [ ] Wire signed-in routing into the shell. (AC: 1, 7)
  - [ ] Replace the current direct `AppRoute.Home` handoff with the shell root, selecting Home as the initial tab.
  - [ ] Keep auth and onboarding completion transitions atomic so obsolete routes do not remain on the stack.
  - [ ] Ensure the bottom navigation is not visible on splash, auth, or onboarding destinations.
- [ ] Connect tab content through feature boundaries. (AC: 2, 4, 5)
  - [ ] Route Home, Workout, Progress, and Profile tab content through callbacks or feature-owned entry registration.
  - [ ] Keep workout-plan generation, session state, and other feature logic inside their owning modules.
  - [ ] Do not leak app-owned back-stack state into feature screens.
- [ ] Preserve tab state and independent back stacks. (AC: 3, 6)
  - [ ] Keep each top-level tab's navigation state saveable and restorable.
  - [ ] Maintain the existing typed Navigation 3 back-stack ownership model.
  - [ ] Make tab switching restore the last visible destination for that tab.
- [ ] Add focused navigation tests. (AC: 1-8)
  - [ ] Verify the shell is only rendered after successful sign-in/onboarding completion.
  - [ ] Verify tab switching preserves state and restores the last selected tab.
  - [ ] Verify the shell root replaces auth/onboarding entries atomically.
  - [ ] Verify the app still uses typed Navigation 3 keys and not Navigation 2 controller tests.

## Dev Notes

### Current State

- `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt` currently routes directly to `AppRoute.Home` after auth or onboarding completion and uses a placeholder `Home` destination inside the app host.
- The app already uses a single-activity Compose host with Navigation 3 `NavDisplay`, typed `NavKey` destinations, and saveable back stacks.
- `AUTH-007` moved typed auth destination ownership into `:feature:auth:auth-ui`; `OB-004` already handles onboarding completion and root replacement.
- `WP-004` depends on this story because the Home tab dashboard should live inside the shell, not replace the shell itself.
- The current app does not yet have a persistent bottom navigation scaffold or retained top-level tab stacks.

### What This Story Changes

- Introduces the signed-in shell as the app-owned container for the main experience.
- Adds top-level tab navigation for Home, Workout, Progress, and Profile.
- Moves the post-auth and post-onboarding landing point from a direct Home root to the shell root with Home selected.
- Establishes retained tab state so users can switch tabs without losing the last visible screen for each tab.

### What Must Be Preserved

- The existing typed Navigation 3 architecture, including `NavDisplay`, saveable back stacks, and entry-scoped state.
- Atomic root replacement from splash, auth, and onboarding into the signed-in experience.
- Feature ownership boundaries: the shell owns container navigation only, while feature modules own their own UI and business logic.
- The current auth/onboarding completion flow and fail-closed routing behavior from `AUTH-000`, `AUTH-001`, `AUTH-007`, and `OB-004`.
- FitLife theme, Material3 components, and the current MVI-style feature contracts.

### Architecture Compliance

- Keep navigation typed and serializable; do not introduce `NavController`, `NavHost`, or string routes.
- Keep `MainActivity` as the single activity host and extend the existing `NavDisplay` composition root.
- Use saveable-state and ViewModel-store decorators where shell or tab entries own Compose state.
- Keep the shell in `:app`; do not create a new shell module.
- Do not move workout plan generation, session orchestration, or progress calculations into the shell layer.
- Feature UI modules should expose callbacks, entry providers, or destination keys, not app-owned back-stack internals.

### File Structure Notes

Expected touch points are likely to include:

- `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt`
- `app/src/main/java/com/aml_sakr/fitlife/<new shell navigation files>`
- `app/src/androidTest/java/com/aml_sakr/fitlife/FitLifeAppNavigationTest.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/navigation/AuthNavigation.kt`
- `feature/onboarding/onboarding-ui/src/main/java/com/aml_sakr/fitlife/feature/onboarding/ui/<handoff routes or callbacks>`
- `feature/workout/workout-ui/src/main/java/com/aml_sakr/fitlife/feature/workout/ui/<tab content entry points>`
- `feature/progress/progress-ui/src/main/java/com/aml_sakr/fitlife/feature/progress/ui/<tab content entry points>`

### Testing Requirements

- Prefer app-level typed Navigation 3 assertions over Navigation 2 controller tests.
- Verify signed-in-only visibility, tab switching, and tab-state restoration directly against the owned back stack.
- Cover the auth/onboarding handoff so the shell root replaces obsolete routes atomically.
- Keep tests offline and deterministic; do not depend on Firebase, Gemini, Room data, or device-only services.

### Project Structure Notes

- The shell is the technical container; Home is only one tab inside it.
- Keep Home tab content separate so WP-004 can focus on the dashboard states without reworking shell mechanics.
- If the shell needs per-tab entry providers, keep them close to the app composition root and aligned with the existing auth/navigation patterns.
- Preserve the signed-in-only boundary so splash, auth, and onboarding never render the bottom bar.

### References

- Story source: [docs/fitlife-stories-v1.md#EPIC-3-APP-SHELL-NAVIGATION-WEEK-3](D:/LinkDevProject/FitLife/docs/fitlife-stories-v1.md)
- Epic/story map: [docs/fitlife-stories-v1.md](D:/LinkDevProject/FitLife/docs/fitlife-stories-v1.md)
- Design story map: [\_bmad-output/planning-artifacts/design-story-map.md](D:/LinkDevProject/FitLife/_bmad-output/planning-artifacts/design-story-map.md)
- Architecture: [\_bmad-output/planning-artifacts/fitlife-architecture-v1.md#11-navigation-3-structure](D:/LinkDevProject/FitLife/_bmad-output/planning-artifacts/fitlife-architecture-v1.md)
- UX spec: [docs/fitlife-ux-spec-v1.md#3.3-app-shell-bottom-navigation](D:/LinkDevProject/FitLife/docs/fitlife-ux-spec-v1.md)
- App host: [app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt](D:/LinkDevProject/FitLife/app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt)
- Navigation tests: [app/src/androidTest/java/com/aml_sakr/fitlife/FitLifeAppNavigationTest.kt](D:/LinkDevProject/FitLife/app/src/androidTest/java/com/aml_sakr/fitlife/FitLifeAppNavigationTest.kt)
- Auth navigation contract: [feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/navigation/AuthNavigation.kt](D:/LinkDevProject/FitLife/feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/navigation/AuthNavigation.kt)
- Onboarding completion story: [\_bmad-output/implementation-artifacts/ob-004-onboarding-completion-flag-navigation-graph.md](D:/LinkDevProject/FitLife/_bmad-output/implementation-artifacts/ob-004-onboarding-completion-flag-navigation-graph.md)
- Auth navigation story: [\_bmad-output/implementation-artifacts/auth-007-auth-navigation-graph.md](D:/LinkDevProject/FitLife/_bmad-output/implementation-artifacts/auth-007-auth-navigation-graph.md)
- Home dashboard story dependency: [\_bmad-output/implementation-artifacts/wp-004-home-screen-ui-plan-states.md](D:/LinkDevProject/FitLife/_bmad-output/implementation-artifacts/wp-004-home-screen-ui-plan-states.md)

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

### Completion Notes List

### File List

- `D:/LinkDevProject/FitLife/_bmad-output/implementation-artifacts/shell-001-app-shell-with-bottom-navigation.md`
