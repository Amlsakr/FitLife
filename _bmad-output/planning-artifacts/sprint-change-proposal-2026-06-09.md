# Sprint Change Proposal - 2026-06-09

## 1. Issue Summary

The FitLife splash screen is documented in the UX flow and referenced by the auth navigation story, but it was not represented as a tracked implementation story in `sprint-status.yaml`.

This creates a planning gap: the app needs startup routing before the auth/onboarding/main flows can behave predictably, but the sprint tracker did not give a developer a dedicated story to implement or verify that behavior.

## 2. Impact Analysis

Epic impact: Epic 1 Auth remains the correct location. No new epic is required.

Story impact: Add `AUTH-000: Splash Screen and Startup Routing` before `AUTH-001` and before `AUTH-007`. `AUTH-007` still owns the broader auth navigation graph, while `AUTH-000` owns the app startup destination and routing decision.

PRD impact: No MVP scope change. This supports existing Firebase Auth, onboarding, and startup expectations.

Architecture impact: No architecture change. The story must follow the existing single-activity Compose host, MVI state/event/action pattern, and Clean Architecture boundaries.

UX impact: No new UX requirement. This makes the existing splash screen specification executable.

Technical impact: Implementation will touch `:app`, `:feature:auth:auth-ui`, `:feature:auth:auth-domain`, and possibly `:feature:onboarding:onboarding-domain` for onboarding completion checks.

## 3. Recommended Approach

Recommended path: Direct Adjustment.

Rationale: The change is low risk and fills a clear tracking gap. It does not change product scope, module structure, or the release plan.

Effort: Low.
Risk: Low.
Timeline impact: Small; the story is size S and should reduce ambiguity before auth navigation implementation.

## 4. Detailed Change Proposals

Story source: `docs/fitlife-stories-v1.md`

OLD:
```text
EPIC 1 starts with AUTH-001 Firebase Auth Module Setup.
```

NEW:
```text
Add AUTH-000 Splash Screen and Startup Routing before AUTH-001.
```

Rationale: Splash is a startup concern and should be tracked before the auth navigation graph that includes it.

Sprint tracker: `_bmad-output/implementation-artifacts/sprint-status.yaml`

OLD:
```yaml
epic-1: backlog
auth-001-firebase-auth-module-setup: backlog
```

NEW:
```yaml
epic-1: backlog
auth-000-splash-screen-and-startup-routing: ready-for-dev
auth-001-firebase-auth-module-setup: backlog
```

Rationale: A story artifact has been created, so the tracker should mark it as ready for development.

Story artifact:
```text
_bmad-output/implementation-artifacts/auth-000-splash-screen-and-startup-routing.md
```

Rationale: Developer agents need a concrete story file with acceptance criteria, tasks, dependencies, and architecture guardrails.

## 5. Implementation Handoff

Scope classification: Minor.

Routed to: Developer agent.

Success criteria:
- Splash is the app startup destination.
- Authenticated + onboarding complete routes to main/home.
- Authenticated + onboarding incomplete routes to onboarding.
- Unauthenticated routes to login/sign-in.
- No camera permission is requested at launch.
- MVI + Clean Architecture boundaries are preserved.

## 6. Workflow Notes

Checklist summary:
- Trigger/context: Done. Splash was documented but missing from sprint tracking.
- Epic impact: Done. Epic 1 remains valid.
- Artifact conflicts: Done. Story source and sprint tracker needed updates.
- Path forward: Direct Adjustment selected.
- Handoff: Developer agent can implement `AUTH-000` next.
