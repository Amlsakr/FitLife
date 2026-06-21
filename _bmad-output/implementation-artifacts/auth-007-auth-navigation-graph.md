# Story AUTH-007: Typed Auth Navigation Contract

Status: review
Design Status: Not required. This is a navigation-contract story; there is no dedicated Stitch export.

## Story

As a developer,
I need typed Navigation 3 destinations for auth screens,
so that auth navigation is modular, restorable, and isolated from the main flow.

## Acceptance Criteria

1. Serializable `NavKey` destinations exist for Splash, SignIn, SignUp, and ForgotPassword.
2. Auth destinations are registered through a feature-owned Navigation 3 entry-provider helper.
3. Auth navigation uses callbacks or MVI actions and does not expose app-owned back-stack state to auth screens.
4. Forward, back, root-replacement, and key-restoration behavior is covered by tests.

## Tasks / Subtasks

- [x] Define the typed auth navigation contract in `:feature:auth:auth-ui`. (AC: 1, 2, 3)
  - [x] Add serializable `NavKey` types for the auth destinations the app must preserve.
  - [x] Add a feature-owned entry-provider helper that registers auth destinations through Navigation 3.
  - [x] Keep destination wiring callback-driven or action-driven so auth screens stay isolated from app back-stack state.
  - [x] Keep the auth navigation API free of `NavController`, `NavHost`, string routes, `navigation-compose`, and `navigation-testing`.
- [x] Move app-owned auth routing glue into the feature boundary. (AC: 2, 3)
  - [x] Remove the temporary app-owned auth key ownership from `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt`.
  - [x] Update the app composition root to consume the auth-ui helper instead of owning auth destination registration inline.
  - [x] Keep splash, onboarding, session, and home root handling working exactly as before.
- [x] Preserve existing auth and startup behavior while changing the navigation contract. (AC: 3)
  - [x] Keep current auth MVI state, events, actions, and screen behavior intact.
  - [x] Preserve verified sign-in, sign-out, and account-deletion root replacement behavior.
  - [x] Ensure auth screens still receive callbacks or one-time actions rather than navigation state objects.
- [x] Add Navigation 3 coverage for the auth contract. (AC: 4)
  - [x] Add tests for forward navigation between auth destinations.
  - [x] Add tests for back behavior and root replacement.
  - [x] Add tests that confirm typed keys survive restoration and still resolve to the correct destination content.
  - [x] Keep app-level typed back-stack assertions instead of recreating Navigation 2 controller tests.

## Dev Notes

### Current State

- `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt` currently owns the temporary `AppRoute` sealed interface, including `Splash`, `Auth`, onboarding, session, and home.
- The app host already uses Navigation 3 `NavDisplay`, `rememberNavBackStack`, `entryProvider`, saveable-state decorators, and typed `NavKey` objects.
- `SplashRoute` and `AuthRoute` already live in `:feature:auth:auth-ui`, but the app still wires the auth entries and root replacement callbacks inline.
- `SplashAction.NavigateToAuth` currently routes through the app-owned auth root, and auth success currently returns control to the app for post-auth root replacement.
- Existing auth UI and domain stories already cover sign-in, sign-up, sign-out, reset password, delete account, and startup routing. This story must not rewrite those flows.

### What This Story Changes

- Auth destination ownership moves from `:app` into `:feature:auth:auth-ui`.
- The auth feature should expose a reusable Navigation 3 entry-provider helper that the app can mount into its single back stack.
- The app should stop pretending that auth is a single umbrella `AppRoute.Auth` destination and instead consume the typed auth destinations directly.
- The current screen-level MVI contracts should stay in place; only navigation ownership and registration move.

### What Must Be Preserved

- Splash startup routing behavior, including atomic root replacement and the no-camera-permission-at-launch rule.
- Auth screen behavior, validation, loading guards, sign-out, and delete-account flows.
- Auth screen behavior, validation, loading guards, sign-out, and delete-account flows.
- Onboarding and home root replacement behavior already used by the app host.
- The single-activity Compose host and the existing Navigation 3 back stack ownership model.
- The existing separation where auth screens emit events or actions instead of mutating app navigation state directly.

### Architecture Compliance

- Follow the multi-module Clean Architecture graph in `_bmad-output/planning-artifacts/fitlife-architecture-v1.md`.
- Keep feature UI code in `:feature:auth:auth-ui` and keep app composition-root code thin.
- Use serializable typed `NavKey` routes, `rememberNavBackStack`, `NavDisplay`, and `entryProvider` for Navigation 3.
- Use saveable-state and ViewModel-store decorators when a destination owns state or a ViewModel.
- Do not reintroduce `NavController`, `NavHost`, string routes, `navigation-compose`, or `navigation-testing`.
- If `hiltViewModel()` is needed in Compose, use the standalone `androidx.hilt:hilt-lifecycle-viewmodel-compose` artifact and package, not the older transitive-navigation path.

### File Structure Notes

- Expected touch points are likely to include:
  - `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt`
  - `app/src/androidTest/java/com/aml_sakr/fitlife/FitLifeAppNavigationTest.kt`
  - `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/...`
  - `feature/auth/auth-ui/src/test/java/com/aml_sakr/fitlife/feature/auth/auth_ui/...`
- Prefer a navigation-focused package inside auth-ui, such as `navigation` or `graph`, rather than adding more app-owned route code.
- Keep the feature boundary clean: auth-ui may expose navigation keys, entry registration, and callbacks, but it should not expose app back-stack internals.

### Testing Requirements

- Verify the auth navigation contract with typed back-stack assertions, not Navigation 2 controller tests.
- Cover forward navigation between auth destinations, back behavior, root replacement, and state restoration.
- Keep tests focused on the auth contract; do not duplicate the auth-domain or auth-ui MVI coverage that already exists.
- Preserve the current auth and splash tests while updating any app-level assertions that still assume `AppRoute.Auth` is app-owned.

### Project Structure Notes

- The current app host still owns a temporary `AppRoute.Auth` umbrella destination. This story should remove that ownership in favor of feature-owned typed auth destinations.
- Auth UI already contains `SplashScreen`, `AuthScreen`, `SignInScreen`, `SignUpScreen`, and the relevant MVI types. Reuse those instead of creating parallel screens.
- Keep the auth feature independent of `:app` and `:feature:auth:auth-data`; navigation should remain a UI contract.

### References

- Story source: `docs/fitlife-stories-v1.md`
- Architecture: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md`
- Project context: `_bmad-output/project-context.md`
- Previous story: `_bmad-output/implementation-artifacts/auth-000-splash-screen-and-startup-routing.md`
- Previous story: `_bmad-output/implementation-artifacts/auth-001-firebase-auth-module-setup.md`
- UX spec: `docs/fitlife-ux-spec-v1.md`
- Navigation 3 overview: https://developer.android.com/guide/navigation/navigation-3
- Navigation 3 migration guide: https://developer.android.com/guide/navigation/navigation-3/migration-guide
- Hilt Compose ViewModel artifact note: https://developer.android.com/jetpack/androidx/releases/hilt

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-06-18: Story created from the approved AUTH-007 backlog item, the current sprint tracker, the auth story source, the architecture doc, the project context, and the active AUTH-000/AUTH-001 implementation notes.
- 2026-06-19: Moved auth destination ownership into `:feature:auth:auth-ui`, added serializable typed auth keys plus a feature-owned Navigation 3 entry-provider helper, and updated the app host to consume it.
- 2026-06-19: Added app-level coverage for auth forward navigation, back behavior, typed-key restoration, root replacement, and the forgot-password destination, then verified compile, unit tests, lint, and Android test packaging.

### Completion Notes List

- Comprehensive developer guidance created for the auth navigation contract migration.
- Story scoped to feature-owned Navigation 3 auth keys and entry-provider registration.
- Existing auth behavior, startup routing, and root-replacement semantics explicitly preserved.
- Navigation 3 test expectations recorded for forward, back, root-replacement, and restoration behavior.
- Implemented `AuthDestination` serializable keys for Splash, SignIn, SignUp, and ForgotPassword in `:feature:auth:auth-ui`.
- Added `registerAuthEntries(...)` so auth navigation wiring now lives in the feature module instead of `MainActivity`.
- Preserved the existing sign-in/password-reset/auth-state MVI flow while enabling typed back-stack transitions for sign-up and startup routing.
- Added app-level Navigation 3 tests covering forward auth navigation, back behavior, typed-key restoration, root replacement, and forgot-password destination rendering.
- Verified `:feature:auth:auth-ui:compileDebugKotlin`, `:app:compileDebugKotlin`, `:app:compileDebugAndroidTestKotlin`, `:feature:auth:auth-ui:test`, `:app:testDebugUnitTest`, `:app:assembleDebug`, `:app:assembleDebugAndroidTest`, `:app:lintDebug`, and `:feature:auth:auth-ui:lintDebug`.

### File List

- `_bmad-output/implementation-artifacts/auth-007-auth-navigation-graph.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `app/src/androidTest/java/com/aml_sakr/fitlife/FitLifeAppNavigationTest.kt`
- `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt`
- `feature/auth/auth-ui/build.gradle.kts`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/forgotpassword/ForgotPasswordRoute.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/navigation/AuthDestination.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/navigation/AuthNavigation.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/screen/AuthScreen.kt`
- `feature/auth/auth-ui/src/main/res/values/strings.xml`

## Change Log

- 2026-06-19: Implemented AUTH-007 typed auth navigation contract, moved auth destination registration into `:feature:auth:auth-ui`, and added typed Navigation 3 coverage.
