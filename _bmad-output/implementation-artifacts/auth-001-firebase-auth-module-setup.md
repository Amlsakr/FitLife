# Story AUTH-001: Firebase Auth Module Setup

Status: done
Design Status: Pending (UX specification available; no linked Stitch export)

Completion Note: Ultimate context engine analysis completed - comprehensive developer guide created.

## Story

As a new user,
I want to create an account and sign in with email and password,
so that my FitLife account is secure, persists across launches, and protects workout access until my email is verified.

## Acceptance Criteria

1. Given a visitor enters a valid email and acceptable password, when registration succeeds, then a Firebase Auth account is created, a verification email is requested, and the app navigates directly to Home without showing a verification screen.
2. Given a registered user enters valid email/password credentials, when sign-in succeeds, then the domain returns an authenticated user model containing a non-blank Firebase UID, email, and current email-verification status.
3. Given an authenticated user is evaluated at startup or after login, when navigation resolves, then the app routes directly to Home instead of showing a verification screen.
4. Given an authenticated user's email is not verified, when authentication or startup routing is evaluated, then the app still routes directly to Home and does not show a verification screen after login.
5. Given a signed-in user chooses sign out, when sign-out succeeds, then the Firebase session is cleared and the app returns to the unauthenticated auth destination with protected destinations removed from the back stack.
6. Given registration or sign-in fails, when Firebase returns invalid email, weak password, email already in use, invalid credentials, disabled user, rate-limit, network, or unknown failures, then the data layer maps the exception to an explicit domain auth error and the UI displays a safe actionable message without exposing raw exception text.
7. Given the app restarts with an existing Firebase session, when AUTH-000 reads the current auth session, then it uses the real Firebase-backed `AuthSessionReader`; blank UIDs route to auth, while authenticated sessions continue to Home.
8. Given the auth UI is loading, when an auth request is active, then duplicate submissions are disabled, progress is visible, entered passwords are never logged or persisted, and coroutine cancellation is rethrown rather than converted to an auth failure.
9. Given the project uses Clean Architecture and MVI, when AUTH-001 is implemented, then Firebase SDK types stay in `:feature:auth:auth-data`, repository contracts/models/errors/use cases stay in `:feature:auth:auth-domain`, and Compose state/events/actions/ViewModels stay in `:feature:auth:auth-ui`.
10. Given automated verification runs, then repository/use-case/ViewModel tests use fakes for JVM coverage, Firebase integration coverage uses the Authentication Emulator rather than production accounts, and focused tests plus `:app:assembleDebug` pass.
11. Given authentication completes, when the app navigates to Home, then the app atomically replaces Auth in the Navigation 3 back stack and does not retain Splash or Auth as a back destination.

## Tasks / Subtasks

- [x] Add the supported Firebase Auth dependencies and DI wiring. (AC: 7, 9, 10)
  - [x] Add a `firebase-auth` version-catalog alias and use the existing Firebase BoM; do not add deprecated `firebase-auth-ktx`.
  - [x] Add `kotlinx-coroutines-play-services` through the catalog if `Task.await()` is used to expose suspend APIs.
  - [x] Configure Hilt/KSP only in modules that require generated bindings or `@HiltViewModel`.
  - [x] Make `:app` include `:feature:auth:auth-data` on its runtime graph so auth bindings/implementations are available at the composition root.
  - [x] Keep the Google Services plugin and `google-services.json` app-owned; do not apply the plugin or copy the JSON into feature modules.
- [x] Define auth-domain contracts and use cases. (AC: 1-6, 9)
  - [x] Add an Android/Firebase-free `AuthUser` model with `id`, `email`, and `isEmailVerified`.
  - [x] Add a sealed `AuthError : DomainError` with stable codes for validation, credentials, duplicate account, disabled user, throttling, network, and unknown failures.
  - [x] Add an `AuthRepository` interface for email sign-up, email sign-in, sign-out, current user, verification email, and current-user refresh.
  - [x] Add focused single-`operator fun invoke` use cases for sign-up, sign-in, sign-out, resend verification, refresh verification, and current-session access.
  - [x] Keep Google credential/token APIs out of this contract until AUTH-002.
- [x] Implement the Firebase-backed auth data layer. (AC: 1-8)
  - [x] Implement `FirebaseAuthRepository` around `FirebaseAuth`; convert Firebase `Task` completion to suspend results without callback APIs leaking upward.
  - [x] After account creation, request `sendEmailVerification()` and return an unverified domain user; define deterministic behavior if account creation succeeds but the verification-email request fails.
  - [x] Call `FirebaseUser.reload()` before checking a user-triggered verification refresh, then remap the refreshed `currentUser`.
  - [x] Map Firebase exceptions in one dedicated mapper; do not route auth failures through generic `SafeCall` if it would erase auth-specific meaning.
  - [x] Re-throw `CancellationException`, never log passwords or tokens, and do not expose `FirebaseUser`, `FirebaseAuth`, `Task`, or Firebase exceptions outside auth-data.
  - [x] Add `AuthModule` bindings/providers following the existing Hilt module style.
- [x] Replace AUTH-000's temporary auth source with the real Firebase session. (AC: 3, 4, 7)
  - [x] Implement `AuthSessionReader` in auth-data from `FirebaseAuth.currentUser`.
  - [x] Extend `AuthSession` to carry verification status and update `DetermineStartupDestinationUseCase` so unverified users route to `StartupDestination.Auth`.
  - [x] Replace `DefaultAuthSessionReader` in the app composition root while preserving the temporary onboarding reader until onboarding persistence is implemented.
  - [x] Reconnect the existing state-driven `SplashRoute` and `NavigateToAuth` callback; do not duplicate splash routing or create a second startup state machine.
  - [x] Preserve splash back-stack removal and the single-activity Compose host.
- [x] Build email/password auth MVI and Compose UI. (AC: 1-6, 8, 9)
  - [x] Add sign-in and sign-up state/event/action/ViewModel flows using `BaseMviViewModel`.
  - [x] Implement email, password, and confirm-password validation before repository calls; keep validation errors field-specific.
  - [x] Render functional Sign In and Register states using `FitnessAppTheme`, Material3, core-ui tokens, 48dp minimum targets, dynamic text support, and polite screen-reader error announcements.
  - [x] Keep navigation and snackbars as one-time actions; Compose screens render immutable state and send events upward.
  - [x] Disable submit/resend/refresh controls during their own request and prevent concurrent duplicate requests.
  - [x] Use the UX specification as the baseline because the design-story map has no linked AUTH-001 Stitch asset; do not claim pixel matching.
- [x] Wire the current app route without absorbing later auth stories. (AC: 3-5, 7)
  - [x] Replace the current `"Sign in"` placeholder with the AUTH-001 auth route and handle successful authentication by replacing Auth with Home.
  - [x] Replace authenticated Auth with the resolved typed Navigation 3 Home key.
  - [x] Keep temporary app-owned keys compatible with AUTH-007, which will move auth keys and entry-provider registration into auth-ui.
  - [x] Do not implement Google sign-in, forgot-password behavior, account deletion, Firestore profile creation, or Firebase security rules in this story.
- [x] Add focused automated verification. (AC: 1-10)
  - [x] Unit-test auth exception mapping, domain model mapping, all use cases, verification gating, sign-out, and cancellation propagation.
  - [x] Unit-test MVI loading, validation, success, error, resend, refresh, duplicate-submit, and one-time-action behavior.
  - [x] Update AUTH-000 routing tests for verified and unverified sessions.
  - [x] Replace Navigation 2 `TestNavHostController` assertions with direct Navigation 3 typed back-stack assertions.
  - [x] Add emulator-first integration coverage for create/sign-in/sign-out and verification where practical; guard emulator configuration so production builds never call `useEmulator`.
  - [x] Run `.\gradlew.bat :feature:auth:auth-domain:test :feature:auth:auth-data:testDebugUnitTest :feature:auth:auth-ui:testDebugUnitTest :app:testDebugUnitTest --no-daemon --console=plain`.
  - [x] Run `.\gradlew.bat :app:assembleDebug --no-daemon --console=plain` and the full JVM regression suite.

### Review Findings

- [x] [Review][Patch] Reset verification state when refresh finds no current user [feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/ui/AuthViewModel.kt:154]
- [x] [Review][Patch] Make missing verification status fail closed instead of defaulting to verified [feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/startup/AuthSession.kt:5]
- [x] [Review][Patch] Handle post-auth startup resolution failures without crashing or stranding navigation [app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt:139]
- [x] [Review][Patch] Provide a reachable sign-out path for verified users in protected destinations [app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt:145]
- [x] [Review][Patch] Make Firebase emulator coverage executable, enforced, repository-backed, and cleanup-safe [feature/auth/auth-data/src/androidTest/java/com/aml_sakr/fitlife/feature/auth/data/FirebaseAuthEmulatorInstrumentedTest.kt:19]
- [x] [Review][Patch] Cover every auth exception mapping branch claimed by the story [feature/auth/auth-data/src/test/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseAuthRepositoryTest.kt:109]
- [x] [Review][Patch] Add post-auth Home root replacement coverage and strengthen malformed-email validation [app/src/androidTest/java/com/aml_sakr/fitlife/FitLifeAppNavigationTest.kt:55]

## Dev Notes

### Current State

- Firebase app configuration, Analytics, Crashlytics, the Google Services plugin, and Firebase BoM already exist. Add only the Auth library needed by this story.
- The auth modules already exist, but `auth-data` has no production source and the current auth-domain code only contains AUTH-000 startup contracts.
- `MainActivity` currently uses `DefaultAuthSessionReader`, which always returns `null`; AUTH-000 therefore cannot route a real authenticated user yet.
- AUTH-000 is still `in-progress` and has unresolved local edits. Work with those edits. In particular, restore the state-driven `SplashRoute`, restore `NavigateToAuth`, and do not overwrite the current splash redesign.
- The working tree contains unrelated dependency/version changes and an untracked splash prototype. Do not normalize versions, remove files, or repair unrelated AUTH-000 review items unless required for AUTH-001 integration.

### Domain Contract Guidance

Use the existing `com.aml_sakr.fitlife.core.domain.Result<T, E>` and `DomainError`; do not create another result wrapper.

Recommended contract shape:

```kotlin
data class AuthUser(
    val id: String,
    val email: String?,
    val isEmailVerified: Boolean
)

interface AuthRepository {
    suspend fun signUp(email: String, password: String): Result<AuthUser, AuthError>
    suspend fun signIn(email: String, password: String): Result<AuthUser, AuthError>
    suspend fun signOut(): Result<Unit, AuthError>
    suspend fun currentUser(): Result<AuthUser?, AuthError>
    suspend fun sendEmailVerification(): Result<Unit, AuthError>
    suspend fun refreshCurrentUser(): Result<AuthUser?, AuthError>
}
```

- Keep UI copy out of `AuthError`; expose stable error codes and map them to user-facing strings in auth-ui.
- Treat a blank Firebase UID as invalid/unauthenticated.
- A Firebase session is not sufficient for protected access: `isEmailVerified` must also be true.
- Decide and test the partial-success case where account creation succeeds but verification email sending fails. The account must not be reported as fully verified or granted protected access.

### Architecture Compliance

- Dependency direction remains `auth-ui -> auth-domain`, `auth-data -> auth-domain + core-data`, and `auth-domain -> core-domain`.
- `:app` is the composition root and may depend on auth-data for runtime bindings; auth-ui must not depend on auth-data.
- Keep Firebase and Android framework types out of domain APIs.
- Use MVI, not MVVM: UI -> event -> ViewModel -> immutable state -> UI, with one-time actions for navigation/snackbars.
- Repository and use-case methods are suspend APIs. Do not leak Firebase listeners or callback-shaped APIs into domain/UI.
- Use a dedicated auth exception mapper. Generic network mapping is not enough for weak password, duplicate account, invalid credentials, disabled user, or throttling.

### UX And Accessibility

- Login baseline: email, password, submit, register link, loading state, and error feedback.
- Register baseline: email, password, confirm password, submit, sign-in link, field validation, loading state, and error feedback.
- Pending-verification baseline: explain that workout access is blocked pending verification, show the account email when available, and provide resend, refresh/check, and sign-out actions.
- Password fields must use obscured input and appropriate keyboard/autofill semantics. Do not store passwords in saved state, logs, analytics, Crashlytics keys, or test fixtures.
- The design-story map marks AUTH-001 as `TODO`; use the UX spec and core tokens for a functional implementation and leave pixel-specific design refinement for a linked design.

### Scope Boundaries

- AUTH-001 owns email/password registration, sign-in, sign-out, email verification, auth errors, real session reading, and the minimum UI needed to exercise those flows.
- AUTH-002 owns Google sign-in, Google credentials, Google button behavior, and Firestore user creation after Google authentication.
- AUTH-003 owns forgot-password and its account-deletion wording; AUTH-005 owns complete GDPR cascade deletion.
- AUTH-004 owns Firestore security rules and emulator rule tests.
- AUTH-007 owns the final typed auth Navigation 3 contract. Keep keys, route callbacks, and entry registrations easy to move into auth-ui.
- User profile fields and Firestore persistence are not part of this story.

### Library And Framework Requirements

- The current catalog uses Firebase BoM `34.14.1`. Preserve the checked-in BoM unless a separate dependency correction is approved.
- Use `com.google.firebase:firebase-auth`, not `firebase-auth-ktx`. Firebase stopped releasing separate KTX modules and removed them from BoM `34.0.0` in July 2025; Kotlin APIs now ship in the main modules.
- Firebase account creation signs the new user in. Explicitly send a verification email and gate downstream access using `FirebaseUser.isEmailVerified`.
- For a user-triggered verification check, reload the Firebase user before reading verification state.
- Use the Authentication Emulator for integration testing. Android emulator host access is `10.0.2.2:9099`; keep emulator activation debug/test-only and project-ID consistent with the Firebase CLI.
- If Hilt Compose ViewModels are introduced, use the current AndroidX Hilt `1.3.0` API/artifact (`hilt-lifecycle-viewmodel-compose`) rather than adding the legacy navigation-compose API by habit.
- App navigation uses stable Navigation 3 `1.1.2`, Lifecycle Navigation 3 entry decorators aligned to `2.10.0`, and serializable typed keys. Do not reintroduce `navigation-compose` or `navigation-testing`.

### File Structure Requirements

Expected new or updated areas:

- `gradle/libs.versions.toml`
- `app/build.gradle.kts`
- `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt`
- `feature/auth/auth-data/build.gradle.kts`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/`
- `feature/auth/auth-ui/build.gradle.kts`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/ui/`
- Matching `src/test` and, only where emulator integration is practical, `src/androidTest` files.

Prefer packages such as `domain.model`, `domain.error`, `domain.repository`, `domain.usecase`, `data.repository`, `data.mapper`, `data.di`, and UI packages per screen. Keep the existing `domain.startup` and `ui.splash` packages intact.

### Testing Requirements

- JVM tests must not initialize real Firebase or require network access.
- Repository tests should isolate Firebase SDK interaction behind a small data-source/gateway abstraction if direct Firebase classes make deterministic JVM tests impractical.
- Verify every `AuthError` mapping and ensure raw Firebase messages are not surfaced.
- Verify registration validation, password confirmation, loading guards, cancellation propagation, verification resend/refresh, and sign-out.
- Verify startup routing for: no user, blank UID, unverified user, verified + onboarding incomplete, and verified + onboarding complete.
- Verify verified authentication leaves exactly one typed Onboarding or Home root in the Navigation 3 back stack.
- Emulator tests must use disposable users and clean state. Do not point automated tests at the production Firebase project.

### Previous Story Intelligence

- AUTH-000 established `AuthSession`, `AuthSessionReader`, `DetermineStartupDestinationUseCase`, splash MVI, and splash navigation. Extend these contracts instead of creating parallel session/startup APIs.
- AUTH-000 review found that the runtime still uses a fake unauthenticated reader; AUTH-001 is the owning story for replacing it with the real Firebase session.
- AUTH-000 tests already cover blank UIDs, retry concurrency, cancellation, and splash actions. Preserve and extend this coverage.
- Current local AUTH-000 work temporarily disconnects `SplashRoute` and `NavigateToAuth`. AUTH-001 cannot satisfy end-to-end auth startup until those connections are restored.

### Git Intelligence

- Recent commit `874137c` implemented AUTH-000 across app, auth-domain, auth-ui, tests, and Gradle.
- Recent commits `576b419` and `e309512` added the splash design/story context.
- Follow the module/package/testing patterns established there, but do not copy the temporary app-local fake auth reader into new code.

### Latest Technical Information

- Firebase KTX migration: https://firebase.google.com/docs/android/kotlin-migration
- Email/password authentication: https://firebase.google.com/docs/auth/android/password-auth
- Email verification and user refresh: https://firebase.google.com/docs/auth/android/manage-users
- Authentication Emulator: https://firebase.google.com/docs/emulator-suite/connect_auth
- AndroidX Hilt releases: https://developer.android.com/jetpack/androidx/releases/hilt

### References

- Story source: `docs/fitlife-stories-v1.md#EPIC-1-AUTH-Weeks-1-2`
- PRD: `_bmad-output/planning-artifacts/fitlife-prd-v1.md#58-Firebase-Auth--User-Profile`
- Architecture: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#1-Module-Structure--Gradle-Dependency-Graph`
- Repository contract pattern: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#5-Repository-Interfaces-Domain-Layer`
- Navigation: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#11-Navigation-Graph-Structure`
- UX: `docs/fitlife-ux-spec-v1.md#31-Splash--Auth-Screens`
- Design map: `_bmad-output/planning-artifacts/design-story-map.md`
- Project context: `_bmad-output/project-context.md`
- Previous story: `_bmad-output/implementation-artifacts/auth-000-splash-screen-and-startup-routing.md`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Implementation Plan

- Add Firebase-free auth models, errors, repository contracts, and focused use cases in auth-domain.
- Wrap Firebase Auth behind an auth-data gateway, repository, exception mapper, session reader, and Hilt module.
- Add one MVI auth flow covering sign-in, registration, resend, refresh, and sign-out.
- Replace the app auth placeholder and temporary startup reader with the Hilt-provided production implementations.
- Verify with focused JVM tests, guarded emulator integration coverage, navigation tests, APK builds, full regression, and lint.

### Debug Log References

- 2026-06-11: Created AUTH-001 story context from sprint status, Epic 1, PRD, architecture, UX specification, design map, project context, current Gradle/source state, AUTH-000 implementation/review notes, recent git history, and current official Firebase/AndroidX documentation.
- 2026-06-11: Red phase confirmed missing auth-domain models, repository, use cases, and email-verification startup state.
- 2026-06-11: Auth-domain tests passed after adding six use cases, explicit auth errors, domain user mapping, and unverified startup gating.
- 2026-06-11: Red phase confirmed the Firebase repository, gateway, exception mapper, session reader, and Hilt bindings were absent.
- 2026-06-11: Auth-data JVM tests passed for account creation, verification dispatch/partial failure, sign-in/out, refresh, error mapping, cancellation, and session mapping.
- 2026-06-11: Red phase confirmed auth MVI state/event/action/ViewModel contracts were absent.
- 2026-06-11: Auth UI tests passed for validation, verified/unverified outcomes, partial verification-email failure recovery, safe errors, resend, refresh, sign-out, and duplicate-submit prevention.
- 2026-06-11: Focused auth verification passed with `.\gradlew.bat :feature:auth:auth-domain:test :feature:auth:auth-data:testDebugUnitTest :feature:auth:auth-ui:testDebugUnitTest :app:testDebugUnitTest --no-daemon --console=plain`.
- 2026-06-11: `.\gradlew.bat :app:assembleDebug --no-daemon --console=plain` passed.
- 2026-06-11: `.\gradlew.bat test --no-daemon --console=plain` passed.
- 2026-06-11: `.\gradlew.bat lint --no-daemon --console=plain` passed.
- 2026-06-11: Auth-data and app Android-test APKs assembled successfully. No Android device was attached, so instrumented navigation and Firebase Auth Emulator tests were compiled but not executed.
- 2026-06-14: Migrated AUTH-001 post-auth routing and application navigation tests from Navigation Compose 2 to stable Navigation 3.
- 2026-06-14: Navigation 3 production and Android-test Kotlin compilation passed.
- 2026-06-14: Focused auth/startup tests, debug APK assembly, Android-test APK assembly, full JVM regression, and lint passed.
- 2026-06-14: No Android device was attached, so Navigation 3 and Firebase emulator instrumentation tests were not executed.
- 2026-06-14: Code-review fixes added fail-closed verification state, refresh-session recovery, post-auth retry handling, verified-user sign-out, stricter email validation, repository-backed emulator coverage, and expanded mapping/navigation tests.
- 2026-06-14: Full JVM regression, debug APK, app/auth-data Android-test APK assembly, and lint passed after review fixes. No Android device was attached, so instrumentation tests were compiled but not executed.

### Completion Notes List

- Story file created and marked ready-for-dev.
- Scope reconciled so AUTH-001 implements email/password auth and verification while Google sign-in remains AUTH-002.
- Added explicit guardrails for real AUTH-000 session integration, unverified-user startup gating, auth-specific error mapping, emulator-first tests, and deprecated KTX avoidance.
- Added Firebase-free `AuthUser`, `AuthError`, `AuthRepository`, and focused auth use cases.
- Added Firebase Auth gateway/repository mapping, explicit Firebase error translation, cancellation propagation, verification refresh, session reading, and Hilt bindings.
- Added sign-in and registration auth flows backed by a single MVI ViewModel with validation, loading guards, snackbars, resend, refresh, and sign-out.
- Wired the real Firebase session into AUTH-000 startup routing.
- Replaced the app auth placeholder and added Home root replacement that removes Splash/Auth from the typed back stack after auth success.
- Added focused auth/startup JVM tests with zero failures plus guarded Firebase Auth Emulator and app navigation instrumentation coverage.
- Verified the debug APK, full JVM regression suite, lint, and Android-test APK compilation.
- Removed Navigation 2 production/testing artifacts and updated navigation instrumentation coverage to inspect the app-owned typed back stack.
- Resolved all code-review findings and moved the story to done.
- Implemented the onboarding-first post-sign-up routing correction so sign-up now lands on onboarding, while login resolves to onboarding or home based on onboarding completion state.
- Kept onboarding completion separate from the level selector; the actual completion flag remains an onboarding-owned concern for OB-004.
- Extended auth/navigation tests to cover sign-up -> onboarding, login with completed onboarding -> home, and login with incomplete onboarding -> onboarding.

### File List

- `_bmad-output/implementation-artifacts/auth-001-firebase-auth-module-setup.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `gradle/libs.versions.toml`
- `app/build.gradle.kts`
- `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt`
- `app/src/androidTest/java/com/aml_sakr/fitlife/FitLifeAppNavigationTest.kt`
- `feature/auth/auth-data/build.gradle.kts`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/di/AuthModule.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseAuthDataSource.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseAuthExceptionMapper.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseAuthRemoteDataSource.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseAuthRepository.kt`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/startup/FirebaseAuthSessionReader.kt`
- `feature/auth/auth-data/src/test/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseAuthRepositoryTest.kt`
- `feature/auth/auth-data/src/test/java/com/aml_sakr/fitlife/feature/auth/data/startup/FirebaseAuthSessionReaderTest.kt`
- `feature/auth/auth-data/src/androidTest/java/com/aml_sakr/fitlife/feature/auth/data/FirebaseAuthEmulatorInstrumentedTest.kt`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/error/AuthError.kt`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/model/AuthUser.kt`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/repository/AuthRepository.kt`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/startup/AuthSession.kt`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/startup/DetermineStartupDestinationUseCase.kt`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/usecase/GetCurrentUserUseCase.kt`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/usecase/RefreshCurrentUserUseCase.kt`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/usecase/ResendEmailVerificationUseCase.kt`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/usecase/SignInUseCase.kt`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/usecase/SignOutUseCase.kt`
- `feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/usecase/SignUpUseCase.kt`
- `feature/auth/auth-domain/src/test/java/com/aml_sakr/fitlife/feature/auth/domain/startup/DetermineStartupDestinationUseCaseTest.kt`
- `feature/auth/auth-domain/src/test/java/com/aml_sakr/fitlife/feature/auth/domain/usecase/AuthUseCasesTest.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/ui/AuthAction.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/ui/AuthErrorMessageMapper.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/ui/AuthEvent.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/ui/AuthScreen.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/ui/AuthState.kt`
- `feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/ui/AuthViewModel.kt`
- `feature/auth/auth-ui/src/test/java/com/aml_sakr/fitlife/feature/auth/ui/AuthViewModelTest.kt`

## Change Log

- 2026-06-11: Created comprehensive AUTH-001 implementation context and marked the story ready for development.
- 2026-06-11: Implemented Firebase email/password auth, verification gating, MVI auth UI, startup integration, and automated coverage; moved story to review.
- 2026-06-14: Approved course correction migrated AUTH-001 post-auth routing and navigation coverage to Navigation 3.
- 2026-06-14: Applied all code-review patches, expanded verification, and marked AUTH-001 done.
- 2026-06-18: Applied the onboarding-first routing correction so sign-up goes to onboarding and login resolves onboarding/home through the onboarding completion contract.
