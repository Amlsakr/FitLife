# Sprint Change Proposal: AUTH-002 Google Sign-In Runtime Readiness

**Date:** 2026-06-16  
**Project:** FitnessApp  
**Change Scope:** Moderate  
**Recommended Path:** Direct adjustment within Epic 1

## 1. Issue Summary

AUTH-002 was implemented and compiled successfully, but review uncovered two runtime readiness blockers:

1. The app's Google sign-in entry path is effectively disabled when `default_web_client_id` is missing from the generated Firebase config. The checked-in `app/google-services.json` does not currently contain an OAuth client entry, so the app cannot obtain a usable web client ID from Firebase at runtime.
2. The Firestore user-upsert path was not staying emulator-first during debug development. That conflicts with the story note that AUTH-002 should remain emulator-first until AUTH-004 introduces production Firestore rules.

Evidence:

- `app/google-services.json` has an empty `oauth_client` array.
- `MainActivity.kt` resolves `default_web_client_id` dynamically and falls back to an empty string when missing.
- `AuthRoute` disables Google launch when the client ID is blank.
- The auth data layer writes user documents through the shared `FirebaseFirestore` instance.
- The project architecture already expects Firestore emulator usage to be controlled centrally.

## 2. Impact Analysis

### Epic Impact

- Epic 1 remains viable.
- AUTH-002 remains in review until the Firebase web client ID is refreshed.
- AUTH-004 continues to be the production Firestore rules gate for user-document writes.

### Story Impact

- **AUTH-002** needs a small scope correction to separate runtime configuration readiness from the code implementation.
- The story should explicitly note that Google sign-in cannot be considered complete until the Firebase console configuration is refreshed and the generated config exposes a web client ID.

### Artifact Impact

- **Story:** update AUTH-002 notes/acceptance language so the configuration dependency is explicit.
- **Implementation:** keep the debug build pinned to Firestore emulator mode before Hilt resolves Firestore.
- **Architecture:** no functional architecture change is required; the existing emulator-first guidance already fits the intended direction.
- **UX:** no UI redesign is needed; the Google button can remain disabled until the runtime client ID exists.

### Technical Impact

- Debug startup now needs to force `fitlife.firestore.useEmulator=true` before Firestore is resolved.
- Production behavior remains unchanged.
- The Google sign-in runtime path still depends on a refreshed Firebase config file containing a valid web client ID.

## 3. Recommended Approach

Use a direct adjustment, not a rollback.

1. Keep the debug emulator-first startup fix in `FitnessApplication`.
2. Treat Firebase config refresh as a required external dependency for Google sign-in runtime enablement.
3. Keep AUTH-002 in `review` until the config is refreshed and the Google button can launch with a real client ID.

Why this path:

- It preserves the implemented Google sign-in code.
- It prevents accidental production Firestore writes during review.
- It makes the remaining blocker explicit instead of hiding it behind a silent fallback.

Effort:

- Code follow-up: small.
- External Firebase console refresh: dependent on project admin access.
- Timeline impact: limited, but the story cannot be closed until the config is updated.

## 4. Detailed Change Proposals

### AUTH-002 Story

**Section:** Dev Notes / Testing Requirements

**OLD:**

- Ensure the Firebase console has the Google provider enabled and that the updated `google-services.json` supplies `default_web_client_id`.

**NEW:**

- Google sign-in remains runtime-disabled until the Firebase console is refreshed and the generated `google-services.json` exposes a non-empty `default_web_client_id`.
- Keep the Google button visible but disabled when the client ID is unavailable.
- Do not mark AUTH-002 complete until the Firebase web client ID exists in the generated config and the Google launch path can execute end to end.

**Rationale:** The implementation is complete, but the runtime configuration is still missing. The story should reflect that dependency instead of implying the app can already launch Google sign-in.

### AUTH-002 Story

**Section:** Dev Notes / Architecture Compliance

**OLD:**

- AUTH-004 is the security-rules gate for production Firestore writes. Keep the Google user-document upsert merge-safe and emulator-first until Firestore rules are in place.

**NEW:**

- AUTH-004 is the security-rules gate for production Firestore writes. Keep the Google user-document upsert merge-safe and emulator-first until Firestore rules are in place.
- Debug builds should force the Firestore emulator flag during application startup so review/dev usage never writes Google-auth user documents to production.

**Rationale:** This aligns the implementation with the story note and prevents accidental production writes while AUTH-002 is still under review.

## 5. Implementation Handoff

**Classification:** Moderate  
**Recipients:** Developer for final code confirmation; Firebase/config owner for `google-services.json` refresh; Product/QA for review gating.

Success criteria:

- Debug builds boot with Firestore emulator mode enabled.
- The Google sign-in button remains disabled until a valid web client ID is present.
- `google-services.json` is refreshed with a real OAuth web client ID.
- AUTH-002 stays in review until runtime sign-in can actually execute.
- No production Firestore writes occur from this story before AUTH-004.

## 6. Summary

- Implemented the debug emulator-first startup fix.
- Identified the remaining external blocker: missing Firebase web client ID.
- Recommended next step: refresh Firebase config, then re-run the AUTH-002 review and code review workflow.

