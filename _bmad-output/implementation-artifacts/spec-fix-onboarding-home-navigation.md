---
title: 'Fix: Onboarding does not navigate to Home after completion'
type: 'bugfix'
created: '2026-06-27'
status: 'in-review'
route: 'one-shot'
---

## Intent

**Problem:** After completing either the Beginner or Intermediate onboarding flow, the app did not navigate to the Home screen. The user remained stuck on the onboarding screen with no visible error.

**Approach:** Remove the `userId: String? = null` parameter that was shadowing the class-level `userId` constructor property inside the private `saveDraft()` helper in both `BeginnerOnboardingViewModel` and `IntermediateOnboardingViewModel`. The use-cases now receive the correct non-null user ID on every call, allowing the save -> mark-complete -> Finish action -> navigate-to-Home chain to complete successfully.

## Suggested Review Order

1. `feature/onboarding/onboarding-ui/src/.../beginner/BeginnerOnboardingViewModel.kt:189` -- root of the bug: `userId: String? = null` param removed, class `userId` now used
2. `feature/onboarding/onboarding-ui/src/.../intermediate/IntermediateOnboardingViewModel.kt:276` -- identical fix applied
3. `feature/onboarding/onboarding-ui/src/.../beginner/BeginnerOnboardingViewModel.kt:141` -- confirm success path: save -> markOnboardingComplete -> sendAction(Finish)
4. `feature/onboarding/onboarding-ui/src/.../intermediate/IntermediateOnboardingViewModel.kt:186` -- same chain for intermediate
5. `app/src/main/java/com/aml_sakr/fitlife/FitLifeApp.kt:236` -- confirm `backStack.replaceRoot(..., AppRoute.Shell)` wired correctly on both branches
