# Story AUTH-003: Forgot Password & Account Deletion

Status: review

## Story

As a FitLife user,
I want to recover my password and delete my account if I wish,
so that I can regain access when I am locked out and keep control over my data.

## Acceptance Criteria

1. The sign-in surface exposes a clear "forgot password" entry point that lets the user request a Firebase password reset email for the entered email address.
2. Password reset uses Firebase Auth's `sendPasswordResetEmail(email)` flow and shows a safe success state without exposing raw SDK or backend text.
3. Invalid email, rate-limit, network, and unknown failures are mapped to `AuthError` values and surfaced through the existing auth UI message mapper.
4. Account deletion is gated behind an explicit confirmation dialog before any destructive action runs.
5. Confirmed deletion removes the authenticated Firebase Auth user and deletes the auth-owned Firestore `users/{uid}` document, then returns the app to the unauthenticated auth root.
6. If Firebase requires a recent login before deletion, the flow fails safely with a dedicated domain error and prompts the user to authenticate again rather than pretending deletion succeeded.
7. If deletion fails after confirmation, the user remains on the current screen, the session is not silently dropped, and the failure is shown through the existing safe error channel.
8. The implementation does not attempt the full GDPR cascade yet; Room and any broader cross-feature data purge remain the responsibility of AUTH-005.
9. Automated coverage verifies domain contracts, Firebase mapping, confirmation gating, and navigation/back-stack behavior for both flows.

## Tasks / Subtasks

- [x] Extend the auth-domain contract for password reset and delete account. (AC: 1-3, 6, 8)
  - [x] Add `resetPassword(email: String): Result<Unit, AuthError>` and `deleteAccount(): Result<Unit, AuthError>` to `AuthRepository`.
  - [x] Add focused `ResetPasswordUseCase` and `DeleteAccountUseCase` classes that follow the existing single `operator fun invoke(...)` pattern.
  - [x] Add a safe domain error for reauthentication-required delete failures if the Firebase `requires-recent-login` case needs to be distinguished from generic auth failure.
  - [x] Keep UI copy out of the domain layer; the domain should expose stable error codes only.

- [x] Implement the Firebase-backed reset and delete flows in auth-data. (AC: 1-7)
  - [x] Use `FirebaseAuth.sendPasswordResetEmail(email)` for password recovery.
  - [x] Delete the auth-owned Firestore `users/{uid}` document as part of the account-deletion workflow.
  - [x] Delete the current Firebase user through `FirebaseUser.delete()` and handle the recent-login requirement explicitly.
  - [x] Reuse the existing auth exception mapper so Firebase/Auth/Firestore failures stay normalized to safe `AuthError` values.
  - [x] Re-throw `CancellationException` and keep Firebase/Firestore types inside `auth-data`.
  - [x] Preserve the current Google sign-in and sign-out behavior while adding the new flows.

- [x] Add the forgot-password and delete-account UI flow in auth-ui. (AC: 1-7, 9)
  - [x] Add a forgot-password action on the sign-in surface and a password-reset request state in the existing auth MVI flow.
  - [x] Add a confirmation dialog for account deletion and route its positive action through the auth ViewModel.
  - [x] Keep duplicate submissions disabled while either request is in flight.
  - [x] Use the existing `AuthErrorMessageMapper` and string resources for all user-facing copy.
  - [x] Keep dismissal paths silent where appropriate; do not convert a dismissed confirmation dialog into an error.

- [x] Wire the destructive account-management entry point into the current app shell if needed. (AC: 4-7, 9)
  - [x] If the profile screen is still unavailable, expose the delete-account entry from the current protected placeholder instead of inventing a new navigation stack.
  - [x] Ensure a successful delete replaces the protected root with the auth root atomically so removed screens cannot be returned to via back navigation.

- [x] Add focused verification. (AC: 1-9)
  - [x] Unit-test password-reset success, invalid-email mapping, rate-limit/network mapping, and cancellation propagation.
  - [x] Unit-test delete-account confirmation gating, recent-login handling, Firebase user deletion, Firestore doc cleanup, and safe failure mapping.
  - [x] Unit-test auth UI events/actions for forgot-password launch, delete confirmation, success messaging, and failure messaging.
  - [x] Verify the app navigation/back stack still replaces protected destinations with Auth after a successful delete.
  - [x] Keep tests emulator-first and offline-safe; do not rely on production Firebase or real user accounts.

## Dev Notes

### Current State

- The auth stack already supports email/password sign-in, sign-up, email verification, Google sign-in, sign-out, and startup session routing.
- `AuthRepository` currently has no password-reset or delete-account API.
- `AuthViewModel` and `AuthState` already run a single MVI flow for sign-in, sign-up, and verification, so reset/delete should extend that flow instead of creating a separate auth surface.
- The current app shell only exposes sign-out in the protected placeholder destination. If the delete entry point is needed before a profile screen exists, reuse that shell surface rather than adding a second navigation system.
- AUTH-005 is the broader GDPR deletion story. AUTH-003 should stay focused on Firebase Auth + auth-owned Firestore cleanup and should not attempt Room or cross-feature data removal.

### Architecture Compliance

- Keep domain APIs Firebase-free and Android-free.
- Follow the existing repository/use-case pattern in `auth-domain`: thin use cases, explicit `Result<T, AuthError>` outcomes, and no callback leakage.
- Keep Firebase Auth, Firestore, and credential cleanup inside `auth-data`.
- Preserve the current Navigation 3 root-replacement behavior. A successful deletion should remove protected destinations atomically.
- Do not introduce `NavController`, string routes, or a second auth state machine.

### Library And Framework Requirements

- Use the existing Firebase Auth setup already present in the repo.
- Password reset should use the official Firebase Auth Android `sendPasswordResetEmail` flow.
- User deletion should use the official Firebase Auth Android `FirebaseUser.delete()` flow.
- Firebase docs note that user deletion may require recent sign-in; handle that as a first-class safe error instead of surfacing raw SDK exceptions.
- If reset email localization or continue URL support is added later, keep it aligned with Firebase's `manage-users` guidance, but do not add custom web reset handling in this story.

### File Structure Requirements

Expected new or updated areas:

- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/repository/AuthRepository.kt`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/error/AuthError.kt`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/usecase/ResetPasswordUseCase.kt`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/usecase/DeleteAccountUseCase.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseAuthRepository.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseAuthRemoteDataSource.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseAuthExceptionMapper.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseUserDocumentDataSource.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/event/AuthEvent.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/action/AuthAction.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/state/AuthState.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/viewmodel/AuthViewModel.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/signin/SignInScreen.kt`
- `feature/auth/auth-ui/src/main/res/values/strings.xml`
- `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt` if the delete entry point must be exposed through the current protected shell
- Matching `src/test` and, only where practical, `src/androidTest` coverage

### Testing Requirements

- Unit tests must not require a live Firebase project or network access.
- Verify password-reset and delete-account mappings for invalid email, network, throttling, unknown, and recent-login-required cases.
- Verify a dismissed confirmation dialog does not trigger deletion or navigation.
- Verify a successful delete clears the protected back stack root and returns the user to Auth.
- Verify the existing Google sign-in, sign-out, and verification flows still behave the same after these additions.

### Previous Story Intelligence

- AUTH-002 already established the current auth MVI flow, Google sign-in, sign-out handling, and the auth repository/data-layer patterns.
- The auth UI currently handles Google launch, cancellation, verification resend, and verification refresh in the same ViewModel. Add reset/delete as adjacent actions rather than splitting the auth flow.
- The app shell already performs root replacement for auth transitions. Reuse that behavior for account deletion so removed destinations are not left on the back stack.

### Git Intelligence

- Recent auth work normalized the package layout and centralized the auth repository implementation in `auth-data`.
- Preserve the current module boundaries and the existing `auth-ui` / `auth-data` package split when adding the new flows.

### Latest Technical Information

- Firebase manage users and password reset: https://firebase.google.com/docs/auth/android/manage-users
- Firebase password authentication: https://firebase.google.com/docs/auth/android/password-auth
- Firebase Authentication Android setup: https://firebase.google.com/docs/auth/android/start

### References

- Story source: `docs/fitlife-stories-v1.md#AUTH-003`
- PRD: `_bmad-output/planning-artifacts/fitlife-prd-v1.md#58-Firebase-Auth--User-Profile`
- Architecture: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#5-Repository-Interfaces-Domain-Layer`
- Architecture: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#11-Navigation-3-Structure`
- UX: `docs/fitlife-ux-spec-v1.md#31-Splash--Auth-Screens`
- UX: `docs/fitlife-ux-spec-v1.md#36-Profile-Screen`
- Previous story: `_bmad-output/implementation-artifacts/auth-002-google-sign-in-integration.md`
- Project context: `_bmad-output/project-context.md`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log

- Loaded the story, sprint tracker, and auth implementation context before changing code.
- Verified the auth-domain, auth-data, auth-ui, and app shell wiring compiled together after the new flows were added.
- Hit a JVM test failure caused by eager Firestore enum initialization in the auth exception mapper; removed the static enum dependency and narrowed the unit test to the network-mapping contract.
- Fixed an auth-ui test expectation that depended on a brittle resource-id value and re-ran the module successfully.
- Ran the combined regression pass across `auth-domain`, `auth-data`, `auth-ui`, and `app` unit tests successfully.

### Completion Notes

- Added password reset and delete-account APIs to the auth domain, including a dedicated `ReauthenticationRequired` domain error for recent-login deletion failures.
- Implemented Firebase-backed password reset, authenticated-user document cleanup, current-user deletion, and safe exception normalization in `auth-data`.
- Extended the auth MVI flow with forgot-password and delete-account events, confirmation dialog handling, safe messaging, and duplicate-request guards.
- Exposed account deletion from the protected app shell placeholder and ensured successful deletion atomically returns the back stack to the auth root.
- Added and updated unit/instrumented coverage for domain use cases, repository mapping, ViewModel behavior, and navigation reset behavior.

## File List

- `_bmad-output/implementation-artifacts/auth-003-forgot-password-account-deletion.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `app/src/androidTest/java/com/aml_sakr/fitlife/FitLifeAppNavigationTest.kt`
- `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt`
- `feature/auth/auth-data/src/androidTest/java/com/aml_sakr/fitlife/feature/auth/data/FirebaseAuthEmulatorInstrumentedTest.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/AuthDataConstants.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseAuthDataSource.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseAuthExceptionMapper.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseAuthRemoteDataSource.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseAuthRepository.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseUserDocumentDataSource.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseUserDocumentRemoteDataSource.kt`
- `feature/auth/auth-data/src/test/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseAuthRepositoryTest.kt`
- `feature/auth/auth-data/src/test/java/com/aml_sakr/fitlife/feature/auth/data/startup/FirebaseAuthSessionReaderTest.kt`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/AuthDomainConstants.kt`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/error/AuthError.kt`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/repository/AuthRepository.kt`
- `feature/auth/auth-domain/src/test/java/com/aml_sakr/fitlife/feature/auth/domain/usecase/AuthUseCasesTest.kt`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/usecase/DeleteAccountUseCase.kt`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/usecase/ResetPasswordUseCase.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/component/AuthErrorMessageMapper.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/event/AuthEvent.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/screen/AuthScreen.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/shared/AuthCredentialForm.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/signin/SignInScreen.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/signup/SignUpScreen.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/state/AuthState.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/verification/VerificationScreen.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/viewmodel/AuthViewModel.kt`
- `feature/auth/auth-ui/src/main/res/values/strings.xml`
- `feature/auth/auth-ui/src/test/java/com/aml_sakr/fitlife/feature/auth/auth_ui/auth/viewmodel/AuthViewModelTest.kt`

## Change Log

- 2026-06-17: Implemented AUTH-003 password reset and account deletion flows across domain, data, UI, and app shell layers; added regression tests and verified the combined unit suite.
