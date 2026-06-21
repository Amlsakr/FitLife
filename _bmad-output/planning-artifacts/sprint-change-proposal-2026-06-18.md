# Sprint Change Proposal: Post-Auth Routing to Onboarding

Date: 2026-06-18
Project: FitnessApp

## 1. Issue Summary

The current auth implementation and its owning story still route successful authentication directly to `Home`, but the intended product behavior is different:

- After `Sign-Up`, the user should go to `Onboarding`.
- After `Login`, if onboarding is complete, the user should go to `Home`.
- After `Login`, if onboarding is not complete, the user should go to `Onboarding`.

Evidence of the mismatch:

- `AUTH-001` acceptance criteria currently state that successful registration and authentication route directly to `Home`.
- `AuthViewModel` currently emits `NavigateToAuthenticatedUser` after both sign-up and sign-in success.
- `MainActivity` currently replaces the auth root with `AppRoute.Home` on that action.
- The UX and onboarding flow already define `Register -> OnboardingWelcome` and `OnboardingCompletion -> Home`.
- Startup routing already distinguishes onboarding completion, so the post-login path should match the same rule.

This is a requirement alignment issue, not a technical dead end.

## 2. Impact Analysis

### Epic Impact

- Epic 1 (`AUTH`) needs a small story correction so post-auth routing matches the onboarding model.
- Epic 2 (`ONBOARDING`) remains valid, but its completion gate becomes the decisive rule for login navigation after auth.

### Story Impact

- `AUTH-001` needs updated acceptance criteria and implementation notes.
- `OB-004` remains the story that owns onboarding completion state and removal of onboarding screens after completion.
- `AUTH-007` is unaffected unless it inherits the updated auth navigation contract shape.

### Artifact Conflicts

- PRD and UX already support onboarding as the post-registration path, so no PRD rewrite is needed.
- The main conflict is between `AUTH-001` story text and the actual desired navigation behavior.
- Architecture still supports the change because Navigation 3, typed keys, and atomic root replacement are already in place.

### Technical Impact

- Auth success handling must differentiate between sign-up and sign-in outcomes.
- Post-login navigation must consult onboarding completion before deciding between `Onboarding` and `Home`.
- The app shell or auth ViewModel will need a post-auth destination resolver, likely reusing the same onboarding-completion logic already used at startup.

## 3. Recommended Approach

**Selected approach: Direct Adjustment**

Reasoning:

- The change is localized to auth/onboarding routing.
- No completed work needs to be rolled back.
- The onboarding flow already exists in the plan, so this is a routing correction, not a scope expansion.
- Risk is low to medium because the main work is contract and navigation wiring, not new product behavior.

Estimated effort:

- Low to medium
- About 1 story refinement plus targeted code changes

Timeline impact:

- Minimal if applied now
- Best handled before more auth/navigation work lands on top of the current contract

## 4. Detailed Change Proposals

### Story: `AUTH-001` Firebase Auth Module Setup

**Section: Acceptance Criteria**

**OLD**
- Given a visitor enters a valid email and acceptable password, when registration succeeds, then a Firebase Auth account is created, a verification email is requested, and the app navigates directly to Home without showing a verification screen.
- Given an authenticated user is evaluated at startup or after login, when navigation resolves, then the app routes directly to Home instead of showing a verification screen.
- Given an authenticated user's email is not verified, when authentication or startup routing is evaluated, then the app still routes directly to Home and does not show a verification screen after login.

**NEW**
- Given a visitor enters a valid email and acceptable password, when registration succeeds, then a Firebase Auth account is created, a verification email is requested, and the app navigates directly to Onboarding.
- Given an authenticated user is evaluated at startup or after login, when navigation resolves, then the app routes to Home if onboarding is complete or to Onboarding if onboarding is not complete.
- Given an authenticated user's email is not verified, when authentication or startup routing is evaluated, then the app preserves the verified-auth gating rules already defined by the product and does not bypass onboarding completion checks.

**Rationale**
- This aligns sign-up with the onboarding-first flow and makes post-login behavior consistent with onboarding completion state.

### Story: `OB-004` Onboarding Completion Flag & Navigation Graph

**Section: Acceptance Criteria**

**OLD**
- Flag stored in `PreferencesDataSource`.
- NavGraph checks flag on launch.
- Onboarding screens removed after flag set.

**NEW**
- Flag stored in `PreferencesDataSource`.
- NavGraph checks flag on launch and when resolving the first destination after login.
- Onboarding screens removed after flag set, and authenticated users with incomplete onboarding are routed back into onboarding until the flag is set.

**Rationale**
- This makes `OB-004` the clear source of truth for both startup and post-login onboarding routing.

### Story: `AUTH-001` Implementation Notes

**OLD**
- Replace Auth with Home after successful authentication.

**NEW**
- Replace Auth with Onboarding after sign-up success.
- After sign-in success, resolve the next destination by checking onboarding completion and replace Auth with either Onboarding or Home.

**Rationale**
- This keeps the sign-up and sign-in paths aligned with their intended user journeys.

## 5. Implementation Handoff

### Scope Classification

- **Minor to Moderate**

### Handoff

- **Developer agent**: update the auth/onboarding routing contract, story text, and tests.
- **Product/Planning review**: confirm the onboarding-first post-sign-up behavior is the intended product rule.

### Success Criteria

- Sign-up always lands on onboarding.
- Sign-in lands on onboarding when onboarding is incomplete.
- Sign-in lands on home when onboarding is complete.
- Startup behavior remains unchanged and still uses the onboarding completion gate.
- Story text and implementation behavior match.

## 6. Resolution

Status: Implemented and verified.

The auth/onboarding routing contract now matches the approved behavior:

- Sign-up routes to onboarding.
- Login routes to home only when onboarding is complete.
- Login routes to onboarding when onboarding is incomplete.

Validation completed with focused unit tests and Android test source compilation for the navigation coverage.
