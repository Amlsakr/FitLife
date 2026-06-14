# Sprint Change Proposal: Navigation Compose 2 to Navigation 3

**Date:** 2026-06-14  
**Project:** FitnessApp  
**Change Scope:** Moderate  
**Recommended Path:** Direct adjustment within Epic 1
**Approval:** Approved by Amal.Sakr on 2026-06-14
**Implementation Status:** Complete on 2026-06-14

## 1. Issue Summary

FitLife's active AUTH-000 and AUTH-001 implementation uses Navigation Compose 2.9.8 with string routes, `NavHostController`, `NavHost`, and `TestNavHostController`. The approved direction is to migrate the app to Navigation 3 before AUTH-007 establishes the long-lived auth navigation contract.

The migration was triggered while AUTH-000 is in progress and AUTH-001 is in review. Deferring it would make AUTH-007 formalize Navigation 2 concepts and create immediate rework across onboarding and the future main flow.

Evidence:

- `MainActivity.kt` owns string routes and a Navigation 2 `NavHostController`.
- `FitLifeAppNavigationTest.kt` depends on `navigation-testing` and `TestNavHostController`.
- `gradle/libs.versions.toml` pins Navigation Compose 2.9.8.
- Architecture Section 11 and project context require `NavHost`, feature NavGraphs, and `NavController`.
- AUTH-000 and AUTH-007 acceptance criteria explicitly use Navigation 2 graph terminology.
- Android's Navigation 3 migration guide requires typed `NavKey` routes, app-owned back-stack state, `entryProvider`, `NavDisplay`, and removal of Navigation 2 dependencies.

## 2. Impact Analysis

### Epic Impact

- Epic 1 remains achievable without changing its goal, order, or MVP scope.
- AUTH-000 changes its implementation contract from a Navigation 2 graph to a Navigation 3 splash-first back stack.
- AUTH-001 keeps its auth behavior but replaces controller-based post-auth navigation with typed back-stack replacement.
- AUTH-007 is redefined from "Auth Navigation Graph" to a typed Navigation 3 auth destination contract and modular entry-provider integration.
- Future OB-004 and main-flow navigation stories must extend Navigation 3 keys and back-stack ownership rather than add nested Navigation 2 graphs.

### Artifact Impact

- **PRD:** No functional requirement or MVP change.
- **Epics/story source:** AUTH-000, AUTH-001, and AUTH-007 terminology and technical tasks require updates.
- **Architecture:** Section 2 navigation example, Section 11, dependency guidance, and diagrams require updates.
- **Project context:** Replace the `NavHost` rule with Navigation 3 rules.
- **UX:** No screen, flow, interaction, or accessibility change.
- **Sprint status:** No story additions, removals, renumbering, or status changes are required.

### Technical Impact

- Add stable Navigation 3 `1.1.2` runtime and UI artifacts.
- Add Kotlin serialization support and `kotlinx-serialization-core` for saveable typed keys.
- Add `lifecycle-viewmodel-navigation3` aligned with the existing Lifecycle `2.10.0`.
- Remove `navigation-compose`, `navigation-testing`, `NavHostController`, and string route usage.
- Use serializable `NavKey` objects, `rememberNavBackStack`, `NavDisplay`, and `entryProvider`.
- Add saveable-state and ViewModel-store entry decorators so splash/auth ViewModels remain scoped to their destination entries.
- Replace splash/auth roots atomically so protected transitions cannot return to splash or auth.
- Update Compose navigation tests to inspect the owned Navigation 3 back stack directly.

### Migration Guide Variance

Google's generic migration guide assumes Home-first startup and one or more top-level stacks. FitLife currently has a splash-first, single startup/auth stack and no implemented bottom navigation. The migration will therefore preserve FitLife's current behavior and use one saveable Navigation 3 back stack. Multiple retained top-level stacks remain future main-flow work.

The architecture mentions future deep links, but none are implemented in the current code. Deep-link handling is outside this migration and must later map incoming intents to typed Navigation 3 keys.

## 3. Recommended Approach

Use a single atomic direct adjustment:

1. Update planning and story contracts.
2. Replace dependencies and add serialization.
3. Migrate the app composition root to Navigation 3.
4. Replace Navigation 2 instrumentation helpers with back-stack assertions.
5. Run focused tests, assemble the app and Android-test APK, then run full JVM tests and lint.

**Effort:** Medium  
**Risk:** Medium  
**Timeline Impact:** Less than one development day if current AUTH behavior remains unchanged.

Rollback is not recommended because AUTH-007 has not started and Navigation 2 has not spread into later feature modules. MVP review is unnecessary because this is an internal architecture correction.

## 4. Detailed Change Proposals

### AUTH-000: Splash Screen and Startup Routing

**Section:** Acceptance criteria and navigation tasks

**OLD:**

- Make splash the startup destination in the Compose navigation graph.
- Route from splash to auth, onboarding, or main/home without leaving splash on the back stack.
- Replace the starter app with a Compose `NavHost`.

**NEW:**

- Make the serializable Splash `NavKey` the initial key in a saveable Navigation 3 back stack.
- Resolve splash actions by atomically replacing the back stack with Auth, Onboarding, or Home.
- Render destinations through `NavDisplay` and an `entryProvider`.
- Scope splash state and ViewModel ownership to its `NavEntry`.
- Verify stale splash actions cannot mutate the stack after Splash is removed.

**Rationale:** Preserves all existing startup behavior while removing Navigation 2 concepts.

### AUTH-001: Firebase Auth Module Setup

**Section:** Acceptance criteria, tasks, architecture guidance, and verification

**OLD:**

- Preserve splash/auth back-stack removal through controller navigation.
- Keep route structure compatible with AUTH-007's final dedicated auth graph.
- Use Navigation 2 application tests and `navigation-testing`.

**NEW:**

- Replace Auth with the resolved Onboarding or Home `NavKey` after verified authentication.
- Keep auth destination keys and entry-provider registration compatible with AUTH-007 modularization.
- Verify unverified sessions remain on Auth and verified sessions replace Auth without retaining Splash.
- Test the observable Navigation 3 back stack directly without `TestNavHostController`.

**Rationale:** Authentication behavior is unchanged; only navigation ownership and verification change.

### AUTH-007: Typed Auth Navigation Contract

**Section:** Entire story definition

**OLD:**

- Dedicated navigation graph for auth screens.
- `NavHost` includes SignIn, SignUp, ForgotPassword, Splash.
- Create `auth_nav_graph.xml` or Compose NavGraph.

**NEW:**

- Define serializable typed auth `NavKey` destinations for Splash, SignIn, SignUp, ForgotPassword, and verification-required state.
- Register auth destinations through a feature-owned Navigation 3 entry-provider builder.
- Expose navigation callbacks or events that mutate an app-owned back stack without exposing data/UI internals.
- Keep auth-ui independent of `:app`, auth-data, and other feature modules.
- Add tests for forward navigation, back behavior, root replacement, and process-restorable keys.

**Rationale:** AUTH-007 should establish the Navigation 3 modular contract rather than recreate a Navigation 2 graph.

### Architecture

**Section 2 OLD:**

- One-time actions call `navController.navigate("session/${planId}")`.

**Section 2 NEW:**

- One-time actions are handled by the composition root or feature navigation adapter, which pushes a typed `Session(planId)` key onto the owned back stack.

**Section 11 OLD:**

- `MainActivity` hosts a `NavHost`.
- Feature NavGraphs define auth, onboarding, workout, session, and progress.
- Bottom navigation links to graph destinations.

**Section 11 NEW:**

- `MainActivity` hosts `NavDisplay`.
- The app owns one or more saveable typed back stacks.
- Feature UI modules contribute `NavKey` types and entry-provider registrations.
- Root replacement handles splash, auth, sign-out, and onboarding completion.
- Future bottom navigation owns one retained back stack per top-level destination.
- Deep links are parsed at the app boundary and translated into typed keys.

### Dependencies

**REMOVE:**

- `androidx.navigation:navigation-compose`
- `androidx.navigation:navigation-testing`

**ADD:**

- `androidx.navigation3:navigation3-runtime:1.1.2`
- `androidx.navigation3:navigation3-ui:1.1.2`
- `androidx.lifecycle:lifecycle-viewmodel-navigation3:2.10.0`
- `org.jetbrains.kotlinx:kotlinx-serialization-core:1.9.0`
- Kotlin serialization Gradle plugin aligned to Kotlin `2.2.10`

### Navigation Tests

**OLD:**

- Construct `TestNavHostController`.
- Add `ComposeNavigator`.
- Assert `currentDestination.route`.
- Call `popBackStack()` to verify root removal.

**NEW:**

- Provide a Navigation 3 back stack to `FitLifeApp`.
- Assert rendered content and the final typed key.
- Assert the stack contains exactly one root after splash/auth replacement.
- Add a stale-action guard assertion where practical.
- Compile Android-test APK; execute device tests when an emulator/device is available.

## 5. Checklist Results

- [x] Trigger and evidence identified.
- [x] Epic 1 remains viable with direct adjustment.
- [x] No new epic, resequencing, rollback, or MVP reduction required.
- [x] PRD and UX changes are not applicable.
- [x] Architecture, stories, project context, dependencies, code, and tests require updates.
- [x] Direct adjustment selected; effort and risk are Medium.
- [x] Handoff is defined.
- [x] User approval received on 2026-06-14.
- [N/A] Sprint status structure update; no story IDs or statuses change.

## 6. Implementation Handoff

**Classification:** Moderate  
**Recipients:** Product Owner/Developer for story alignment; Developer for implementation.

Success criteria:

- No Navigation 2 production or test dependencies remain.
- All app routes are serializable typed Navigation 3 keys.
- Splash and Auth are removed through atomic root replacement.
- Destination ViewModels are scoped to Navigation 3 entries.
- Existing auth/startup behavior remains unchanged.
- Focused tests, `:app:assembleDebug`, `:app:assembleDebugAndroidTest`, full JVM tests, and lint pass.
- Architecture, project context, AUTH-000, AUTH-001, AUTH-007, and source story documentation agree with the implementation.

Implementation handoff completed:

- Developer implemented the approved migration.
- Product/backlog artifacts were aligned without changing story IDs or sprint statuses.
- Verification passed for focused auth/startup tests, debug APK assembly, Android-test APK assembly, full JVM tests, and lint.
- JVM reports contain 80 tests, 0 failures, 0 errors, and 1 skipped emulator-dependent test.
- No Navigation 2 production or test dependency remains.
- No Android device was attached; instrumentation sources compiled and the Android-test APK assembled, but device tests were not executed.

## 7. Sources

- Android Navigation 3 migration guide: https://developer.android.com/guide/navigation/navigation-3/migration-guide
- Android Navigation 3 setup: https://developer.android.com/guide/navigation/navigation-3/get-started
- Android Navigation 3 state guidance: https://developer.android.com/guide/navigation/navigation-3/save-state
- AndroidX Navigation 3 releases: https://developer.android.com/jetpack/androidx/releases/navigation3
- AndroidX Lifecycle releases: https://developer.android.com/jetpack/androidx/releases/lifecycle
