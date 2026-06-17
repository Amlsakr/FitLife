# Story AUTH-004: Firebase Security Rules

Status: review
Completion Note: Firestore rules, emulator config, and emulator-backed verification were implemented and deployed.

## Story

As a FitLife user,
I want my data to be private,
so that only I can read and write my own records.

## Acceptance Criteria

1. Firestore security rules are defined in `rules_version = '2'` and deny access by default.
2. Authenticated users can only read and write documents they own, using `request.auth.uid` as the ownership boundary.
3. The rule set covers the current and planned Firestore paths used by FitLife, including `users/{uid}`, `users/{uid}/workoutPlans/{planId}`, `users/{uid}/sessions/{sessionId}`, and top-level `progress/{progressId}` documents that carry a `userId` field owned by the authenticated user.
4. No Firestore document is readable or writable from unauthenticated mobile/web clients.
5. The rules are loaded locally through the Firebase Emulator Suite via `firebase.json`, and emulator-based tests verify the rules before any production write path depends on them.
6. Unauthorized access attempts fail safely in the emulator, including unauthenticated reads/writes and authenticated attempts against another user's documents.
7. The rules are specific enough to protect the current auth-owned Firestore document flow from AUTH-002 and AUTH-003, and they remain compatible with the later workout, session, and progress sync stories.
8. The implementation does not broaden into GDPR cascade deletion, background sync logic, or UI changes; it stays focused on Firestore authorization and test coverage.

## Tasks / Subtasks

- [x] Create the production Firestore rules file. (AC: 1-4, 7)
  - [x] Add `firestore.rules` at the repository root.
  - [x] Set `rules_version = '2'` and use a deny-by-default structure.
  - [x] Add a small helper for authenticated ownership checks based on `request.auth.uid`.
  - [x] Lock down `users/{uid}` and its `workoutPlans/{planId}` and `sessions/{sessionId}` subcollections to the owning user only.
  - [x] Require the nested workout-plan and session documents to be reachable only through the parent `users/{uid}` path and disallow cross-user writes even if a caller guesses another document id.
  - [x] Add a rule path for the top-level `progress/{progressId}` collection that only allows the authenticated owner to access their own progress documents and validates `userId == request.auth.uid` on writes.
  - [x] Keep rules readable and maintainable; do not rely on open rules or broad recursive wildcards.

- [x] Wire the Firebase Emulator Suite to the local rules file. (AC: 5)
  - [x] Add or update `firebase.json` so the emulator loads `firestore.rules`.
  - [x] Keep the emulator configuration aligned with the project identifier used by the Android test harness.
  - [x] Do not point any automated checks at production Firestore.

- [x] Deploy the rules so production Firestore uses the checked-in authorization policy. (AC: 1-7)
  - [x] Publish the reviewed rules to the Firebase project after emulator verification passes.
  - [x] Keep the deployed policy in sync with the checked-in `firestore.rules` file.
  - [x] If deployment is manual in this repo, document the exact command or console step in the implementation notes.

- [x] Add emulator-backed security-rule verification. (AC: 5, 6, 7)
  - [x] Add tests that authenticate as the owning user and verify allowed reads/writes on owned documents.
  - [x] Add tests that attempt to read/write without auth and confirm denial.
  - [x] Add tests that attempt to read/write another user's documents and confirm denial.
  - [x] Cover the current auth-owned user document path and at least one representative future data path for workout/session/progress access.
  - [x] Keep the tests offline-safe and deterministic by using the Firestore emulator only.

- [x] Validate the rule set against the current app data flow. (AC: 3, 7)
  - [x] Confirm the auth user-document flow still works when the signed-in user owns the target document path.
  - [x] Confirm the rules do not accidentally block the merge/upsert pattern used by Google sign-in initialization.
  - [x] Confirm the deletion path from AUTH-003 can still remove the owned user document without requiring elevated access.

## Dev Notes

### Current State

- The auth data layer already writes to Firestore through `users/{uid}` for Google sign-in account setup and deletes the same document during account deletion.
- There is no checked-in `firestore.rules` file or Firebase CLI config in the repo yet, so the project currently has no local source-of-truth for Firestore authorization.
- The architecture already defines Firestore-backed future data under `users/{userId}` with `workoutPlans/{planId}` and `sessions/{sessionId}` subcollections, and the story docs also reserve a top-level `progress` collection for analytics documents keyed by `userId`.
- AUTH-002 and AUTH-003 already depend on Firestore writes succeeding for the owning user; this story must secure those paths without breaking their current merge/delete behavior.
- Future stories for workout, session, and progress persistence must not be able to ship until the backend rules are in place and verified.

### Architecture Compliance

- Keep this story at the Firebase infrastructure boundary; do not add domain, UI, or navigation code.
- The rules file and emulator configuration live outside the Android module boundaries, but the verification tests should stay emulator-first and deterministic.
- Use Firebase Authentication as the identity source and `request.auth.uid` as the owner check.
- Deny by default, then open only the exact paths and methods needed by the current and planned Firestore model.
- If a schema needs a userId field for a top-level collection, the rule must validate that field explicitly instead of assuming implicit ownership.

### Library And Framework Requirements

- Use Cloud Firestore Security Rules `rules_version = '2'`.
- Security rules should be written for the mobile client surface, not for server-side IAM access.
- The Firebase Emulator Suite should load the local `firestore.rules` file from `firebase.json`.
- Firebase docs state that every mobile/web client request is evaluated against security rules before any read/write is allowed.
- Firebase emulator docs also note that the emulator can run local tests and simulate authenticated and unauthenticated requests, which is the required verification path here.

### File Structure Requirements

Expected new or updated areas:

- `firestore.rules`
- `firebase.json`
- `feature/auth/auth-data/src/androidTest/java/com/aml_sakr/fitlife/feature/auth/data/FirestoreSecurityRulesEmulatorTest.kt`
- Optional supporting emulator helpers under `app/src/androidTest` or `core/core-data/src/androidTest` if the existing test harness needs a shared launcher or cleanup utility

Prefer one focused rules file and one focused emulator-backed test suite over spreading rule logic across multiple modules.

### Testing Requirements

- Tests must use the Firestore emulator, not production Firestore.
- Verify allow/deny behavior for authenticated owner access, unauthenticated access, and cross-user access.
- Verify at least one write path, one read path, and one delete path.
- Verify the rules do not break the existing auth user-document flow.
- If the implementation uses any helper abstractions for rules tests, keep them small and local to the test harness.
- Do not require a real Firebase project, production auth account, or network access to pass the automated suite.
- After emulator verification passes, deploy the same rules to the Firebase project so production behavior matches local test coverage.

### Previous Story Intelligence

- AUTH-003 already performs Firestore user-document deletion as part of account deletion, so the new rules must permit the owner to delete their own `users/{uid}` document while still blocking everyone else.
- AUTH-002 already performs Firestore user-document upserts with merge semantics after Google sign-in, so the new rules must allow the authenticated owner to create and update their own document without clobbering future profile fields.
- AUTH-001 established the real Firebase Auth session and verification flow, which means `request.auth.uid` is now the correct identity source for rules enforcement.

### Git Intelligence

- Recent auth work centralized Firebase document access in `feature/auth/auth-data`, with a dedicated `FirebaseUserDocumentRemoteDataSource` handling `users/{uid}` writes and deletes.
- The current repo does not yet include a checked-in Firestore rules file, so this story introduces the first local authorization boundary for Firestore.
- Keep the rules aligned with the existing collection naming used by auth-data and the architecture document instead of inventing a parallel data path.

### Latest Technical Information

- Firestore security rules get started guide: https://firebase.google.com/docs/firestore/security/get-started
- Firestore rules conditions guide: https://firebase.google.com/docs/firestore/security/rules-conditions
- Firestore rules emulator testing guide: https://firebase.google.com/docs/firestore/security/test-rules-emulator
- Firebase Emulator Suite Firestore connection guide: https://firebase.google.com/docs/emulator-suite/connect_firestore
- Security rules and Firebase Authentication: https://firebase.google.com/docs/rules/rules-and-auth

### References

- Story source: `docs/fitlife-stories-v1.md#AUTH-004`
- PRD: `_bmad-output/planning-artifacts/fitlife-prd-v1.md#58-Firebase-Auth--User-Profile`
- Architecture: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#4-Firestore-Collections-Structure--Sync-Strategy`
- Architecture: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#11-Navigation-3-Structure`
- Project context: `_bmad-output/project-context.md`
- Previous story: `_bmad-output/implementation-artifacts/auth-003-forgot-password-account-deletion.md`
- Previous auth baseline: `_bmad-output/implementation-artifacts/auth-002-google-sign-in-integration.md`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log

- Story context assembled from sprint status, the original AUTH-004 spec, PRD, architecture, UX, project context, current auth code, and official Firebase documentation.
- Confirmed there is no existing checked-in `firestore.rules` or Firebase CLI config in the repository, so the implementation must add both the rules source and emulator wiring.
- Confirmed auth-data already performs Firestore writes and deletes against `users/{uid}`, so the rules must be owner-based and backwards compatible with those flows.

### Completion Notes

- Added checked-in `firestore.rules` and `firebase.json` emulator wiring for Firestore security coverage.
- Added emulator-backed Android instrumentation tests for owned, unauthorized, and cross-user Firestore access.
- Verified the implementation with `adb -s emulator-5554 shell am instrument -w -r -e class com.aml_sakr.fitlife.feature.auth.data.FirestoreSecurityRulesEmulatorTest com.aml_sakr.fitlife.feature.auth.data.test/androidx.test.runner.AndroidJUnitRunner` and `adb -s emulator-5554 shell am instrument -w -r -e class com.aml_sakr.fitlife.feature.auth.data.FirebaseAuthEmulatorInstrumentedTest com.aml_sakr.fitlife.feature.auth.data.test/androidx.test.runner.AndroidJUnitRunner`.
- Verified the new rules and auth flow on the Android emulator with the Firebase emulator suite, then deployed the rules to project `fitlife-1fdd1`.
- Published the final policy with `firebase deploy --only firestore:rules --project fitlife-1fdd1 --non-interactive`.

### File List

- `_bmad-output/implementation-artifacts/auth-004-firebase-security-rules.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `firebase.json`
- `firestore.rules`
- `feature/auth/auth-data/src/androidTest/AndroidManifest.xml`
- `feature/auth/auth-data/src/androidTest/java/com/aml_sakr/fitlife/feature/auth/data/FirestoreSecurityRulesEmulatorTest.kt`
