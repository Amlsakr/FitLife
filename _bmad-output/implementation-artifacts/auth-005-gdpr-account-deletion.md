# Story AUTH-005: GDPR Account Deletion

Status: review

## Story

As a FitLife user,
I want to permanently delete my account and all my data,
so that I keep full control over my privacy.

## Acceptance Criteria

1. A destructive confirmation step is required before any deletion starts.
2. Deletion removes the Firebase Auth user and all owned Firestore docs: `users/{uid}`, nested `workoutPlans` and `sessions`, and top-level `progress/{progressId}` where `userId == uid`.
3. Deletion removes owned Room rows through a shared purge contract for future user-owned modules.
4. The delete flow completes within 30 seconds in emulator/test runs.
5. The user is signed out and returned to the auth root only after the purge succeeds.
6. Failures map to safe auth errors; `requires recent login` stays distinct.
7. Keep AUTH-003 and AUTH-004 behavior intact. This story extends the purge scope; it does not replace the existing auth-owned Firestore delete or rules work.
8. Automated coverage verifies domain, data, UI, and navigation behavior. All Firebase/Firestore checks run against the emulator.

## Tasks / Subtasks

- [x] Extend the deletion contract so one use case drives the full GDPR purge. (AC: 2-8)
  - [x] Keep `DeleteAccountUseCase` as the public auth-domain entry point.
  - [x] Make the repository delete all owned local and remote data before success.
  - [x] Preserve `ReauthenticationRequired` mapping and safe error handling.
  - [x] Keep Firebase, Firestore, and Room types out of domain APIs.

- [x] Implement the full purge in the data layer. (AC: 2-6)
  - [x] Delete the Firebase Auth user last, after owned local and Firestore data.
  - [x] Keep the `users/{uid}` cleanup working.
  - [x] Purge top-level `progress` docs owned by `userId`.
  - [x] Add hooks for user-scoped Room tables and future `userId`-keyed stores.
  - [x] Keep the operation idempotent where possible.
  - [x] Preserve cancellation handling.

- [x] Keep the destructive entry point aligned with the existing auth flow. (AC: 1, 5, 8)
  - [x] Reuse the current confirmation affordance.
  - [x] Keep the protected-shell delete path working until profile owns it.
  - [x] Replace the navigation root atomically after deletion.

- [x] Add verification that proves the purge is complete and safe. (AC: 2-8)
  - [x] Unit-test repository sequencing and error mapping.
  - [x] Unit-test confirmation, loading, success, and failure states.
  - [x] Add emulator-backed Firestore coverage if delete behavior changes.
  - [x] Add in-memory Room coverage once purge hooks exist.
  - [x] Keep tests offline-safe and deterministic.

## Implementation Contract

- AUTH-003 already deletes the Firebase Auth user and auth-owned `users/{uid}` doc.
- AUTH-004 already locks down `users/{uid}`, nested user collections, and `progress`.
- Keep the deletion contract in auth domain only; keep Firebase, Firestore, and Room details out.
- Delete owned local and remote data before success, then replace the Navigation 3 root.
- Delete `users/{uid}`, nested `workoutPlans` and `sessions`, and top-level `progress/{progressId}` where `userId == uid`.
- Use one shared purge contract for owned Room data and future `userId`-keyed stores.
- Touch points: `AuthRepository`, `DeleteAccountUseCase`, `AuthError`, `FirebaseAuthRepository`, `FirebaseUserDocumentRemoteDataSource`, `FirebaseAuthRemoteDataSource`, `AuthViewModel`, `AuthScreen`, `strings.xml`, and `MainActivity` only if the shell still owns delete.
- Testing: JUnit4 + coroutine test stack; emulator-backed Firestore; in-memory Room or fakes; cancellation preserved.
- Firebase refs: auth manage users, Firestore delete data, rules emulator testing, rules and auth, delete-user-data extension.
- Recent sign-in is required for `FirebaseUser.delete()`.
- Use the emulator as the default verification path.

## Previous Story Intelligence

- AUTH-003 established the delete flow, recent-login handling, and safe UI messaging.
- AUTH-005 must delete all owned Firestore docs, not just the auth-owned root doc.
- The auth UI and shell already know how to leave the protected area after deletion.

## Git Intelligence

- Recent auth work landed in review/implement pairs.
- Keep this story small and reviewable: one purge contract, one data implementation, one test pass.

### References

- `docs/fitlife-stories-v1.md#AUTH-005`
- `_bmad-output/planning-artifacts/fitlife-prd-v1.md#58-Firebase-Auth--User-Profile`
- `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#4-Firestore-Collections-Structure--Sync-Strategy`
- `docs/fitlife-ux-spec-v1.md#36-Profile-Screen`
- `_bmad-output/implementation-artifacts/auth-003-forgot-password-account-deletion.md`
- `_bmad-output/implementation-artifacts/auth-004-firebase-security-rules.md`

## Change Log

- 2026-06-17: Added shared purge contract, Firestore contributor, delete sequencing, and coverage updates.
- 2026-06-17: Added snapshot/restore rollback so late auth delete failures rehydrate owned Firestore data.
- 2026-06-17: Removed stale-data rollback from account deletion and cleared Google credential state on success.
- 2026-06-17: Restored rollback with safe restore-only-when-missing behavior and validated the flow on a real Android device.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story context assembled from sprint status, the auth story catalog, current auth code, auth rules tests, PRD, architecture, UX, project context, and official Firebase docs.
- Confirmed the repository already contains the auth-owned Firestore deletion path and emulator-tested Firestore rules.
- Added the shared user-data purge contract in `core-data` and kept the cleanup boundary explicit for future user-owned stores.

### Completion Notes List

- Added a shared user-data purge contract in `core-data` and wired the auth module into it with a Firestore contributor.
- `FirebaseAuthRepository.deleteAccount()` now purges owned Firestore data, deletes `users/{uid}`, then deletes the Firebase Auth user with no rollback.
- Verified with auth-data unit tests, core-data contract tests, app compile/test, and auth-data Android-test compilation on a connected Android device against the local Firebase emulators.
- Restored owned-data snapshot/restore rollback while making restore idempotent for existing documents so late `deleteCurrentUser()` failures do not overwrite concurrent Firestore updates.
- Resolved review feedback by keeping rollback safe for concurrent writes and by clearing Google credential state after successful delete.

### File List

- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/purge/UserDataPurgeModule.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/purge/UserDataPurgeContributor.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/purge/UserDataPurgeCoordinator.kt`
- `core/core-data/src/test/java/com/aml_sakr/fitlife/core/data/purge/UserDataPurgeCoordinatorTest.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/AuthDataConstants.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/di/AuthModule.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseAuthRepository.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseOwnedUserDataArchiveDataSource.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseFirestoreOwnedUserDataArchiveDataSource.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseFirestoreUserDataPurgeContributor.kt`
- `feature/auth/auth-data/src/androidTest/java/com/aml_sakr/fitlife/feature/auth/data/FirebaseAuthEmulatorInstrumentedTest.kt`
- `feature/auth/auth-data/src/test/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseAuthRepositoryTest.kt`
- `_bmad-output/implementation-artifacts/auth-005-gdpr-account-deletion.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
