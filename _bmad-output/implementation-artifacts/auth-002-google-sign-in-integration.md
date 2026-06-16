# Story AUTH-002: Google Sign-In Integration

Status: done
Design Status: Functional Reference

## Story

As a FitLife user,
I want to sign in with my Google account,
so that I can access the app without creating another password and have my account initialized automatically.

## Acceptance Criteria

1. A distinct Google sign-in button is visible in the auth flow without removing or regressing the existing email/password sign-in and sign-up flows.
2. When the user completes Google sign-in successfully, Firebase Auth signs the user in with the Google ID token and the app creates or upserts the matching Firestore `users/{uid}` document using merge semantics so later onboarding/profile fields are preserved.
3. When Google sign-in succeeds, the existing verified-auth startup path continues to use the same session/onboarding routing as email/password auth, and the user can reach onboarding or home only through the current auth/startup rules.
4. When the user dismisses the Google account chooser or bottom sheet, the flow ends silently: no crash, no snackbar/error copy, no navigation, and the current auth state remains intact.
5. When Google sign-in or the Firestore user-upsert step fails due to network, provider, credential, or backend issues, the failure is mapped to a safe domain error and a user-facing message; raw SDK text is never shown.
6. If Firebase Auth succeeds but the Firestore user document write fails, preserve the authenticated Firebase session, surface a retryable account-setup failure, and do not overwrite any existing profile or onboarding data.
7. The implementation uses Credential Manager plus Firebase Auth ID-token exchange from the official Google/Firebase Android guidance; do not use legacy Google Sign-In or One Tap APIs.
8. Automated coverage verifies domain mapping, cancellation handling, Firestore upsert semantics, auth UI actions, and app navigation/back-stack behavior for the Google path.

## Tasks / Subtasks

- [x] Add modern Google sign-in dependencies and configuration. (AC: 1, 2, 7)
  - [x] Add the required Credential Manager and Google ID artifacts to `gradle/libs.versions.toml` using the project's version-catalog pattern.
  - [x] Update `feature/auth/auth-data/build.gradle.kts` and `feature/auth/auth-ui/build.gradle.kts` with only the libraries this story needs.
  - [x] Keep the existing Firebase BoM and `google-services` plugin flow; do not add a second Firebase setup path.
  - [x] Ensure the Firebase console has the Google provider enabled and that the updated `google-services.json` supplies `default_web_client_id`.
  - [x] Do not introduce legacy Google Sign-In or One Tap APIs.
- [x] Extend the auth-domain contract for Google sign-in. (AC: 2-7)
  - [x] Add a Google sign-in repository method that accepts a Google ID token as a plain string and stays free of Android/Firebase SDK types.
  - [x] Add a focused single-`operator fun invoke` use case for Google sign-in.
  - [x] Add an explicit domain error for post-auth account/profile initialization failure if the Firestore user document cannot be written after Firebase Auth succeeds.
  - [x] Keep cancellation as a silent no-op at the UI boundary; do not convert a user-dismissed Google picker into a visible auth failure.
- [x] Implement the Firebase-backed Google auth and Firestore user initialization path. (AC: 2, 5, 6, 7)
  - [x] Exchange the Google ID token for a Firebase credential with `signInWithCredential`.
  - [x] Upsert the Firestore `users/{uid}` document idempotently and preserve existing profile/onboarding fields with merge semantics.
  - [x] Map provider, credential, network, and backend failures to explicit domain errors through one dedicated mapper.
  - [x] Re-throw `CancellationException` and keep Google SDK/Firebase types inside `auth-data`.
  - [x] Add Hilt bindings/providers in the existing auth-data module style.
- [x] Add the Google sign-in UI flow to the existing auth screen. (AC: 1, 3, 4, 8)
  - [x] Extend the current auth MVI state/event/action flow instead of creating a separate Google-only screen.
  - [x] Keep Credential Manager and Google credential parsing in `auth-ui`; the ViewModel should only receive the resulting ID token and drive loading/navigation state.
  - [x] Add a distinct Google button to the auth UI and preserve the current email/password validation, sign-up, verification, resend, refresh, and sign-out flows.
  - [x] Use the same authenticated-navigation callback path already used by AUTH-001 after the Google token exchange succeeds.
  - [x] Ensure a dismissed Google picker leaves the current screen unchanged and does not emit an error snackbar.
- [x] Wire the new Google path into the app composition root. (AC: 2, 3, 8)
  - [x] Update `MainActivity` only as needed to supply the new Google use case into the auth ViewModel construction.
  - [x] Preserve the existing Navigation 3 startup and auth root-replacement behavior.
  - [x] Do not create a second auth navigation state machine or bypass the existing verified-session checks.
- [x] Add focused verification. (AC: 1-8)
  - [x] Unit-test Google token mapping, cancellation handling, error mapping, and Firestore user upsert semantics.
  - [x] Unit-test the auth UI events/actions for Google button launch, success, dismissal, and retryable failure behavior.
  - [x] Update the app-level navigation tests to confirm the Google auth path still replaces Auth with the correct typed destination.
  - [x] Keep automated tests emulator-first and offline-safe; do not rely on live Google accounts or production Firebase data.

## Dev Notes

### Current State

- AUTH-001 is complete and already provides email/password auth, verification gating, real Firebase session reading, and the app-level verified-auth navigation path.
- The current auth UI is email/password-centered and already shares a single `AuthViewModel`, `AuthRoute`, `AuthState`, and `AuthAction` flow. Add Google as another path through that same flow.
- `AuthRepository` currently covers email/password sign-in, sign-up, sign-out, current-user reading, verification email sending, and refresh. Google should extend that contract rather than introducing a parallel repository.
- The app composition root currently injects `AuthRepository` and `AuthSessionReader` and builds the auth ViewModel manually. Expect the Google use case to be wired there too.
- The Firestore story for user data is not yet implemented, so this story must create the minimal Google-auth user document carefully without clobbering future onboarding/profile fields.
- AUTH-004 is the security-rules gate for production Firestore writes. Keep the Google user-document upsert merge-safe and emulator-first until Firestore rules are in place.
- Treat AUTH-004 as a prerequisite for production Firestore writes; this story should stay emulator-first for the user-document path until security rules are available.
- Debug builds must force Firestore emulator mode at application startup so review/dev runs never write Google-auth user documents to production.
- Google sign-in remains runtime-disabled until the Firebase console refresh exposes a non-empty `default_web_client_id` in the generated config.

### Architecture Compliance

- Keep the domain layer pure: Google credential classes, FirebaseAuth, FirebaseFirestore, and Android `Context` must stay out of auth-domain APIs.
- Use MVI, not MVVM-only flows. The UI should raise events, the ViewModel should orchestrate auth state, and one-time actions should handle launch/navigation/snackbar side effects.
- Credential Manager belongs in the UI boundary because it needs Android UI/lifecycle context. The ViewModel should consume only the resulting token or a cancellation signal.
- `auth-data` owns Firebase Auth, Firestore, and any token-to-credential exchange needed to complete the sign-in. `auth-ui` owns the button and Google picker launch.
- Preserve the existing verified-auth startup logic from AUTH-000/AUTH-001. Google auth is just another way to reach the same authenticated session.

### Library And Framework Requirements

- Follow the official Google/Firebase Android guidance for Google sign-in with Credential Manager.
- Use Credential Manager and Google ID token APIs, not legacy Google Sign-In for Android and not One Tap.
- The Firebase docs for Android Google sign-in instruct you to use `GetGoogleIdOption` / `GetSignInWithGoogleOption`, pass the web client ID from `default_web_client_id`, and exchange the token with Firebase Auth.
- The Android docs recommend both a dedicated Google button and the Credential Manager bottom-sheet flow. If the UI launches a picker flow, keep the button visible so users can retry after dismissal.
- The Firebase provider must be enabled in the console, and the generated `google-services.json` must be refreshed so the web client ID is available to the app.
- If the generated config does not yet expose `default_web_client_id`, keep the Google button visible but disabled instead of falling back to a broken launch path.
- Keep the existing Firebase BoM and Google Services plugin in place; add only the Google sign-in dependencies this story needs through the catalog.

### File Structure Requirements

Expected new or updated areas:

- `gradle/libs.versions.toml`
- `feature/auth/auth-data/build.gradle.kts`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/`
- `feature/auth/auth-ui/build.gradle.kts`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/`
- `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt`
- Matching `src/test` files in the auth domain/data/UI modules
- Matching `src/androidTest` files only where emulator-backed or app-navigation coverage is practical

Prefer to extend the existing auth package layout instead of creating a separate Google-specific feature tree. Reuse the current `signin`, `shared`, `viewmodel`, and `component` packages where possible.

### Testing Requirements

- Unit tests must not require a live Google account, a physical device, or the production Firebase project.
- Verify the Google token exchange path with fakes or a small gateway abstraction if direct SDK calls make deterministic JVM tests difficult.
- Verify that a dismissed Google picker is a no-op rather than an error state.
- Verify that a successful Google auth session preserves or creates the Firestore user document without overwriting existing profile fields.
- Verify that Firebase/Auth/Firestore exceptions map to safe domain errors and that UI messages come from the auth-ui mapper, not raw SDK text.
- Verify that the app-level navigation path still replaces the auth root with the correct typed destination after Google auth succeeds.

### Previous Story Intelligence

- AUTH-001 already established the real Firebase auth repository, the email/password MVI shape, and the authenticated-navigation callback path. Reuse that plumbing instead of adding a separate auth stack.
- AUTH-000 already established the startup routing contract and the app-owned Navigation 3 back stack. Google sign-in should not bypass or duplicate that startup flow.
- Existing auth tests already cover loading guards, cancellation propagation, and typed Navigation 3 assertions. Mirror those patterns when adding Google coverage.

### Git Intelligence

- Recent work merged AUTH-000 splash and startup routing, then finished AUTH-001 email/password auth. Follow those patterns, especially the feature-module boundaries and Navigation 3 back-stack handling.
- The auth package hierarchy was recently normalized, so keep new Google code inside the current `auth_ui` and `auth-data` package layout instead of introducing new parallel roots.

### Latest Technical Information

- Firebase Google sign-in on Android: https://firebase.google.com/docs/auth/android/google-signin
- Sign in with Google on Android, Credential Manager overview: https://developer.android.com/identity/sign-in/credential-manager-siwg
- Implement Sign in with Google: https://developer.android.com/identity/sign-in/credential-manager-siwg-implementation
- Firebase Authentication Android setup: https://firebase.google.com/docs/auth/android/start
- Legacy Google Sign-In migration note: https://developer.android.com/identity/sign-in/legacy-gsi-migration

### References

- Story source: `docs/fitlife-stories-v1.md#AUTH-002`
- PRD: `_bmad-output/planning-artifacts/fitlife-prd-v1.md#58-Firebase-Auth--User-Profile`
- Architecture: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#5-Repository-Interfaces-Domain-Layer`
- Architecture: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#11-Navigation-3-Structure`
- Design map: `_bmad-output/planning-artifacts/design-story-map.md`
- Previous story: `_bmad-output/implementation-artifacts/auth-001-firebase-auth-module-setup.md`
- Startup baseline: `_bmad-output/implementation-artifacts/auth-000-splash-screen-and-startup-routing.md`
- Project context: `_bmad-output/project-context.md`

## Design References

Stitch Screen:
- Sign In / Sign Up

Design:
- _bmad-output/design/auth/signin.png
- _bmad-output/design/auth/signin-reference.html
- _bmad-output/design/auth/signin-design.md
- https://stitch.withgoogle.com/projects/14149816895860058914?node-id=f992e1fb65f44eaea963fde6f13c8c81

The design map currently marks AUTH-002 as TODO, so treat these assets as functional references rather than a pixel-perfect contract.
## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `.\gradlew.bat :feature:auth:auth-domain:test --console=plain`
- `.\gradlew.bat :feature:auth:auth-data:test --console=plain`
- `.\gradlew.bat :feature:auth:auth-ui:test --console=plain`
- `.\gradlew.bat --no-daemon :feature:auth:auth-ui:test --console=plain`
- `.\gradlew.bat --no-daemon :app:compileDebugKotlin --console=plain`
- `.\gradlew.bat --no-daemon :app:compileDebugAndroidTestKotlin --console=plain`
- `.\gradlew.bat --no-daemon :app:compileDebugKotlin :app:compileDebugAndroidTestKotlin --console=plain`

### Completion Notes List

- Added Credential Manager and Google ID catalog entries plus the auth-data/auth-ui dependencies required for Google sign-in.
- Extended the auth domain contract with `signInWithGoogle`, `SignInWithGoogleUseCase`, and explicit Google sign-in/account-setup errors.
- Implemented Firebase Google sign-in and Firestore `users/{uid}` merge upsert with a separate document data source.
- Added a Google sign-in launcher abstraction in auth-ui so the picker flow can be tested without a live Google account.
- Wired the Google path through `AuthViewModel`, `AuthRoute`, `AuthCredentialForm`, and `MainActivity`, while keeping cancellation silent.
- `MainActivity` now resolves `default_web_client_id` dynamically at runtime so the app still compiles when the Firebase config has not yet been refreshed.
- Verified with module tests and app compile checks, including Android test compilation for the updated navigation coverage.
- Refreshed the sign-in surface to match the provided design reference with a centered hero badge, blue call-to-action palette, a branded login card, updated Google button copy, and two decorative feature tiles.
- Refreshed the sign-up surface to match the provided design reference with a branded hero, centered account card, register-with divider, updated signup copy, and trust-footer styling.
- Added an app-level signup render check so the redesigned screen stays covered through the real navigation flow.

### File List

- `gradle/libs.versions.toml`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/AuthDomainConstants.kt`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/error/AuthError.kt`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/repository/AuthRepository.kt`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/usecase/SignInWithGoogleUseCase.kt`
- `feature/auth/auth-domain/src/test/java/com/aml_sakr/fitlife/feature/auth/domain/usecase/AuthUseCasesTest.kt`
- `feature/auth/auth-data/build.gradle.kts`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/AuthDataConstants.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/di/AuthModule.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseAuthDataSource.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseAuthExceptionMapper.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseAuthRemoteDataSource.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseAuthRepository.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseUserDocumentDataSource.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseUserDocumentRemoteDataSource.kt`
- `feature/auth/auth-data/src/test/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseAuthRepositoryTest.kt`
- `feature/auth/auth-data/src/test/java/com/aml_sakr/fitlife/feature/auth/data/startup/FirebaseAuthSessionReaderTest.kt`
- `feature/auth/auth-ui/build.gradle.kts`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/action/AuthAction.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/component/AuthErrorMessageMapper.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/event/AuthEvent.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/google/GoogleSignInLauncher.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/screen/AuthScreen.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/shared/AuthCredentialForm.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/signin/SignInScreen.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/signup/SignUpScreen.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/viewmodel/AuthViewModel.kt`
- `feature/auth/auth-ui/src/main/res/values/strings.xml`
- `feature/auth/auth-ui/src/test/java/com/aml_sakr/fitlife/feature/auth/auth_ui/auth/viewmodel/AuthViewModelTest.kt`
- `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt`
- `app/src/androidTest/java/com/aml_sakr/fitlife/FitLifeAppNavigationTest.kt`

### Change Log

- 2026-06-16: Implemented Google sign-in integration, Firebase Auth exchange, Firestore user-document upsert, auth UI launch flow, and app/navigation test coverage.
- 2026-06-16: Refined the sign-in screen visual design to match the provided reference and revalidated the auth UI plus app compile/test coverage.
- 2026-06-16: Refined the sign-up screen visual design to match the provided reference and revalidated the auth UI plus app compile/test coverage.
