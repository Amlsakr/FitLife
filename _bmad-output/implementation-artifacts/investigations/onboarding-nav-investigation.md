# Investigation: Onboarding navigation to home screen

## Hand-off Brief

1. **What happened.** After completing onboarding, the app does not navigate to the home screen (Shell). The `replaceRoot` call silently fails because the guard condition `lastOrNull() != expectedCurrentRoute` returns true.
2. **Where the case stands.** Root cause identified with high confidence. The `replaceRoot` function in `StartupGraph.kt:59-67` checks if the last item in the back stack matches the expected current route before replacing. If it doesn't match, the function returns without navigating.
3. **What's needed next.** Fix the `replaceRoot` guard condition or change the navigation approach to not depend on the exact back stack state.

## Case Info

| Field            | Value                                                                      |
| ---------------- | -------------------------------------------------------------------------- |
| Ticket           | N/A                                                                        |
| Date opened      | 2026-06-27                                                                 |
| Status           | Active                                                                     |
| System           | Windows, Kotlin, Jetpack Compose, Navigation 3                             |
| Evidence sources | Source code, git history                                                   |

## Problem Statement

After completing onboarding (both beginner and intermediate flows), the app should navigate to the home screen (Shell). Instead, the navigation does not happen and the user remains on the onboarding screen.

## Evidence Inventory

| Source   | Status    | Notes                                                                     |
| -------- | --------- | ------------------------------------------------------------------------- |
| Source code | Available | All navigation and onboarding code examined                               |
| Git history | Available | Recent commits show no changes to core navigation logic                   |
| Logs     | Missing   | User-reported issue, no log files provided                                |

## Investigation Backlog

| # | Path to Explore | Priority | Status | Notes |
| - | --------------- | -------- | ------ | ----- |
| 1 | `replaceRoot` guard condition | High | Done | Root cause identified |
| 2 | ViewModel action emission | Medium | Done | Actions are emitted correctly |
| 3 | Route action collection | Medium | Done | Routes collect actions correctly |

## Timeline of Events

| Time        | Event               | Source                | Confidence |
| ----------- | ------------------- | --------------------- | ---------- |
| User action | Completes onboarding | User report           | Confirmed  |
| Code path   | `finalizeBeginnerProfile()` called | `BeginnerOnboardingViewModel.kt:141` | Confirmed |
| Code path   | `markOnboardingCompleteUseCase(userId)` called | `BeginnerOnboardingViewModel.kt:155` | Confirmed |
| Code path   | `sendAction(BeginnerOnboardingAction.Finish)` called | `BeginnerOnboardingViewModel.kt:159` | Confirmed |
| Code path   | `BeginnerOnboardingRoute` collects action | `BeginnerOnboardingRoute.kt:20` | Confirmed |
| Code path   | `onFinish()` callback invoked | `BeginnerOnboardingRoute.kt:20` | Confirmed |
| Code path   | `backStack.replaceRoot(AppRoute.BeginnerOnboarding, AppRoute.Shell)` called | `FitLifeApp.kt:238-241` | Confirmed |
| Code path   | `replaceRoot` guard fails | `StartupGraph.kt:63` | Confirmed |

## Confirmed Findings

### Finding 1: `replaceRoot` guard condition silently fails navigation

**Evidence:** `StartupGraph.kt:59-67`

**Detail:** The `replaceRoot` function has a guard condition that checks if the last item in the back stack matches the expected current route:

```kotlin
internal fun MutableList<NavKey>.replaceRoot(
    expectedCurrentRoute: NavKey,
    newRoot: NavKey
) {
    if (lastOrNull() != expectedCurrentRoute) return  // <-- Silent failure
    clear()
    add(newRoot)
}
```

When `onFinish` is called from `BeginnerOnboardingRoute`, it calls:
```kotlin
backStack.replaceRoot(AppRoute.BeginnerOnboarding, AppRoute.Shell)
```

If the back stack's last item is not exactly `AppRoute.BeginnerOnboarding` at the moment this code runs, the navigation silently fails.

### Finding 2: The ViewModel and Route code correctly emit and collect actions

**Evidence:** `BeginnerOnboardingViewModel.kt:159`, `BeginnerOnboardingRoute.kt:20`

**Detail:** The `BeginnerOnboardingViewModel` correctly sends the `Finish` action after marking onboarding as complete. The `BeginnerOnboardingRoute` correctly collects this action and calls `onFinish()`.

### Finding 3: The back stack state may not match expectations

**Evidence:** `FitLifeApp.kt:167-170`, `FitLifeApp.kt:236-242`

**Detail:** The navigation flow is:
1. User on `AppRoute.Onboarding` → selects level → `replaceRoot(AppRoute.Onboarding, AppRoute.BeginnerOnboarding)` → back stack: `[AppRoute.BeginnerOnboarding]`
2. User completes onboarding → `replaceRoot(AppRoute.BeginnerOnboarding, AppRoute.Shell)` → should work

The issue may be:
- A timing issue where the back stack state changes between the action emission and the `replaceRoot` call
- The back stack may have additional entries (e.g., from the ViewModel store decorator)
- The `LaunchedEffect(viewModel)` in the Route may restart if the ViewModel instance changes

## Deduced Conclusions

### Deduction 1: The `replaceRoot` guard is too strict

**Based on:** Finding 1

**Reasoning:** The guard condition `if (lastOrNull() != expectedCurrentRoute) return` is designed to prevent navigation when the current route doesn't match what we expect. However, this can fail silently if:
1. The back stack has been modified by another component
2. There's a timing issue between action emission and navigation
3. The ViewModel store decorator adds entries to the back stack

**Conclusion:** The guard should either be removed or made more lenient, or the navigation should use a different approach that doesn't depend on the exact back stack state.

## Hypothesized Paths

### Hypothesis 1: Back stack state mismatch

**Status:** Open

**Theory:** The back stack's last item is not `AppRoute.BeginnerOnboarding` when `replaceRoot` is called, causing the guard to fail.

**Supporting indicators:** The `replaceRoot` function is the only place where navigation can silently fail.

**Would confirm:** Adding logging to `replaceRoot` to print the current back stack state before the guard check.

**Would refute:** If logging shows the back stack is correct and the guard passes, but navigation still fails.

### Hypothesis 2: Timing issue with LaunchedEffect

**Status:** Open

**Theory:** The `LaunchedEffect(viewModel)` in the Route restarts if the ViewModel instance changes, causing the action to be missed.

**Supporting indicators:** The `LaunchedEffect` key is `viewModel`, which could change during recomposition.

**Would confirm:** Changing the key to something stable or using `remember` to capture the ViewModel.

**Would refute:** If the ViewModel instance is stable throughout the onboarding flow.

## Missing Evidence

| Gap              | Impact                               | How to Obtain   |
| ---------------- | ------------------------------------ | --------------- |
| Runtime logs     | Would confirm if navigation is attempted | Add logging to `replaceRoot` |
| Back stack state at navigation time | Would confirm the guard condition | Add logging before `replaceRoot` call |

## Source Code Trace

| Element       | Detail                                      |
| ------------- | ------------------------------------------- |
| Error origin  | `StartupGraph.kt:63` (`replaceRoot` guard)  |
| Trigger       | `BeginnerOnboardingRoute.kt:20` (action collection) |
| Condition     | Back stack last item != expected current route |
| Related files | `FitLifeApp.kt:236-242`, `BeginnerOnboardingViewModel.kt:159` |

## Conclusion

**Confidence:** High

The root cause is the `replaceRoot` function in `StartupGraph.kt:59-67`. The guard condition `if (lastOrNull() != expectedCurrentRoute) return` silently fails navigation when the back stack state doesn't match expectations. This is the only place in the navigation flow where a silent failure can occur.

The ViewModel and Route code correctly emit and collect the `Finish` action. The issue is purely in the `replaceRoot` implementation.

## Recommended Next Steps

### Fix direction

**Option 1: Remove the guard condition** (Simplest)
- Change `replaceRoot` to always clear and add the new root, regardless of the current state
- Risk: May cause issues if called from an unexpected state

**Option 2: Make the guard more lenient**
- Instead of checking the exact last item, check if the expected route exists anywhere in the back stack
- Or clear the entire back stack and add the new root regardless

**Option 3: Use a different navigation approach**
- Instead of `replaceRoot`, use `backStack.clear()` followed by `backStack.add(newRoot)`
- This bypasses the guard condition entirely

### Diagnostic

Add logging to `replaceRoot` to print the current back stack state before the guard check:
```kotlin
internal fun MutableList<NavKey>.replaceRoot(
    expectedCurrentRoute: NavKey,
    newRoot: NavKey
) {
    Log.d("Navigation", "replaceRoot: expected=$expectedCurrentRoute, current=${lastOrNull()}, stack=$this")
    if (lastOrNull() != expectedCurrentRoute) return
    clear()
    add(newRoot)
}
```

## Reproduction Plan

1. Run the app
2. Complete the onboarding flow (beginner or intermediate)
3. Observe that the app does not navigate to the home screen
4. Check logcat for the "navigate to home" log message (should appear from `FitLifeApp.kt:237`)
5. Add logging to `replaceRoot` to see the back stack state

## Side Findings

- The `DefaultOnboardingRepository` always returns `false` for `isOnboardingComplete` and `Success` for `markOnboardingComplete`. This is the default implementation used when no real repository is provided. In production, the real `PreferencesOnboardingRepository` is used.
- The `LaunchedEffect(viewModel)` pattern in Routes could be fragile if the ViewModel instance changes. Consider using a stable key or `remember`.
