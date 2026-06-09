# FitLife – MVP Epics, Stories, Sprint Plan, Critical Path, and Risks (v1.1)

---

## Change Log

- v1.1 — 2026-05-31 — Confirmed WhatsApp badge sharing, smart reminders, home screen widget, and dynamic equipment rerouting as v1.0 scope; aligned story module names to architecture; added Firebase Security Rules, GDPR deletion, and camera privacy stories; fixed fatigue and lighting edge cases.

## EPIC 0: PROJECT SETUP (Week 1)

**Story ID**: SETUP-001
**Title**: Create Multi‑module Gradle Project
**Note**: An empty Android Studio project has been created as a baseline.
**User Story**: *As a developer, I need a multi‑module Gradle project scaffold so that the codebase follows the Clean Architecture layout.*
**Acceptance Criteria**:
- 20 modules generated as per `fitlife-architecture-v1.md`, including `:feature:widget:widget-ui`.
- Build passes with `./gradlew assembleDebug`.
- Each module applies the version catalog `libs.versions.toml`.
**Technical Tasks**:
- Run `./gradlew init` with Kotlin DSL.
- Add `settings.gradle.kts` with `include` for all modules.
- Publish version catalog in `gradle/libs.versions.toml`.
- Verify compilation of empty modules.
**Module(s) Affected**: All 20 modules from `fitlife-architecture-v1.md`.
**Dependencies**: None.
**Size**: L (4–8h)

**Story ID**: SETUP-002
**Title**: Core‑Domain Core‑Data Core‑UI Boilerplate
**User Story**: *As a developer, I want core libraries (Domain, Data, UI) with shared entities and utilities so that feature modules can depend on a stable foundation.*
**Acceptance Criteria**:
- `Error.kt`, `Result.kt`, `NetworkErrors.kt`, `IBaseRepository.kt` present in `:core:core-domain`.
- `BaseRepository`, `SafeCall`, `ResponseToResult`, connection utils, preferences data source, DI modules in `:core:core-data`.
- UIState, Event, MVI base ViewModel, theme resources (Inter font, colors, typography, dimens) in `:core:core-ui`.
- Hilt application class defined.
**Technical Tasks**:
- Add Kotlin files per spec.
- Add `res/font` Inter font files.
- Add theme XML and Compose theming.
- Configure Hilt modules.
**Modules Affected**: `:core:core-domain`, `:core:core-data`, `:core:core-ui`.
**Dependencies**: SETUP-001.
**Size**: XL (>8h)

**Story ID**: SETUP-003
**Title**: Firebase & Crashlytics Integration
**User Story**: *As a developer, I need Firebase services connected so that the app can store user data and capture crashes.*
**Acceptance Criteria**:
- `google-services.json` added and `apply plugin: 'com.google.gms.google-services'`.
- Crashlytics initialized in Application class.
- Analytics events can be logged.
**Technical Tasks**:
- Create Firebase project, download config file.
- Add dependencies via version catalog.
- Initialize in `App.kt`.
**Modules Affected**: `:app`, `:core:core-data`.
**Dependencies**: SETUP-001.
**Size**: M (2–4h)

**Story ID**: SETUP-004
**Title**: Technical Spike – ML Kit Pose Detection ≥15 fps
**User Story**: *As a developer, I need to verify that ML Kit pose detection runs at ≥15 fps on Snapdragon 6xx devices so we can decide whether to include real‑time feedback in MVP.*
**Acceptance Criteria**:
- Demo app measures FPS on target device.
- Report shows ≥15 fps average.
- If fails, fallback plan recorded.
**Technical Tasks**:
- Build simple CameraX + ML Kit demo.
- Record FPS using `System.nanoTime`.
- Document results.
**Modules Affected**: `:feature:session:session-ui`.
**Dependencies**: SETUP-001.
**Size**: S (<2h)

**Story ID**: SETUP-005
**Title**: Technical Spike – Gemini API ≤5 s latency
**User Story**: *As a developer, I need to confirm Gemini API response time under free tier so that AI‑generated plans are viable.*
**Acceptance Criteria**:
- 10 sample requests measured.
- Average latency ≤5 s.
- If exceeds, fallback to static templates.
**Technical Tasks**:
- Write small script using Retrofit.
- Log timings.
**Modules Affected**: `:feature:workout:workout-data`.
**Dependencies**: SETUP-001.
**Size**: S

**Story ID**: SETUP-006
**Title**: Technical Spike – Room + Firestore Offline Sync
**User Story**: *As a developer, I need to verify that Room data can be synced to Firestore when offline and later reconciled.*
**Acceptance Criteria**:
- Create sample entity, write locally, lose network, reconnect, verify remote update.
- Document any conflict resolution strategy.
**Technical Tasks**:
- Implement simple sync worker.
- Test with network toggle.
**Modules Affected**: `:core:core-data`.
**Dependencies**: SETUP-001.
**Size**: S

---

## EPIC 1: AUTH (Weeks 1‑2)

**Story ID**: AUTH-000
**Title**: Splash Screen and Startup Routing
**User Story**: *As a user, I want FitLife to open with a branded loading screen and route me to the right next screen so the app starts smoothly and predictably.*
**Acceptance Criteria**:
- Splash screen displays the FitLife brand mark or wordmark centered on a fullscreen branded background.
- Loading state is visible while startup routing checks complete.
- Authenticated users with completed onboarding route to the main/home flow.
- Authenticated users without completed onboarding route to onboarding.
- Unauthenticated users route to the login/sign-in flow.
- The splash screen has no blocking user interactions and avoids requesting camera permission at launch.
**Technical Tasks**:
- Create a Compose splash screen in `:feature:auth:auth-ui`.
- Add startup routing state/events using the existing MVI pattern.
- Read auth session state through the auth domain boundary.
- Read onboarding completion state through the onboarding domain boundary or a temporary interface until onboarding data is implemented.
- Wire the splash destination as the app's startup destination in the Compose navigation graph.
**Modules Affected**: `:app`, `:feature:auth:auth-ui`, `:feature:auth:auth-domain`, `:feature:onboarding:onboarding-domain`.
**Dependencies**: SETUP-001, SETUP-002, SETUP-003.
**Size**: S

**Story ID**: AUTH-001
**Title**: Firebase Auth Module Setup
**User Story**: *As a new user, I want to create an account using email/password or Google so that my data is securely stored.*
**Acceptance Criteria**:
- Sign‑up, sign‑in, sign‑out flows work.
- Email verification required before first workout.
- Errors displayed for invalid credentials.
**Technical Tasks**:
- Add `firebase-auth-ktx` dependency.
- Create `AuthRepository` implementing sign‑up/in/out.
- Expose use‑cases.
**Modules Affected**: `:feature:auth:auth-data`, `:feature:auth:auth-domain`, `:feature:auth:auth-ui`.
**Dependencies**: SETUP-001, SETUP-003.
**Size**: M

**Story ID**: AUTH-002
**Title**: Google Sign‑In Integration
**User Story**: *As a user, I want to sign in with my Google account so I don’t have to remember another password.*
**Acceptance Criteria**:
- Google Sign‑In button appears.
- After success, user record created in Firestore.
- Handles cancellation gracefully.
**Technical Tasks**:
- Add `play-services-auth`.
- Configure Google Services JSON.
- Implement `GoogleSignInUseCase`.
**Modules Affected**: `:feature:auth:auth-data`, `:feature:auth:auth-domain`, `:feature:auth:auth-ui`.
**Dependencies**: AUTH-001.
**Size**: M

**Story ID**: AUTH-003
**Title**: Forgot Password & Account Deletion
**User Story**: *As a user, I want to recover my password and delete my account if I wish, ensuring control over my data.*
**Acceptance Criteria**:
- “Forgot password” sends reset email.
- Account deletion removes Auth user and related Firestore docs.
- Confirmation dialog required.
**Technical Tasks**:
- Implement `resetPassword` use‑case.
- Implement `deleteAccount` with cascade.
**Modules Affected**: `:feature:auth:auth-data`, `:feature:auth:auth-domain`, `:feature:auth:auth-ui`.
**Dependencies**: AUTH-001.
**Size**: M

**Story ID**: AUTH-004
**Title**: Firebase Security Rules
**User Story**: *As a user, I want my data to be private so only I can read and write my own records.*
**Acceptance Criteria**:
- Firestore rule: users can only read/write documents where userId == request.auth.uid.
- Rule covers: users/, workoutPlans/, sessions/, progress/ collections.
- Rules tested with Firebase emulator.
- No document readable without auth.
**Technical Tasks**:
- Write firestore.rules file.
- Test with Firebase emulator.
- Deploy rules before any data write.
**Modules Affected**: `:app`
**Dependencies**: AUTH-001.
**Size**: M

**Story ID**: AUTH-005
**Title**: GDPR Account Deletion
**User Story**: *As a user, I want to permanently delete my account and all my data so I have full control over my privacy.*
**Acceptance Criteria**:
- Confirmation dialog before deletion.
- Deletes: Firebase Auth account, all Firestore documents for userId, all Room database records for userId.
- Deletion completes within 30 seconds.
- User signed out and redirected to login screen after deletion.
- Cannot be undone.
**Technical Tasks**:
- DeleteAccountUseCase implementation.
- Firestore batch delete for userId.
- Room cascade delete via userId FK.
- Firebase Auth account deletion.
- Confirmation dialog in profile UI.
**Modules Affected**: `:feature:auth:auth-domain`, `:feature:auth:auth-data`, `:feature:auth:auth-ui`
**Dependencies**: AUTH-001.
**Size**: L

**Story ID**: AUTH-006
**Title**: Camera Permission Privacy Disclosure
**User Story**: *As a user, I want to understand why FitLife needs camera access before I grant permission so I can trust the app.*
**Acceptance Criteria**:
- Permission NOT requested at app launch.
- Permission requested only when user starts first workout session.
- Rationale shown BEFORE system dialog: "FitLife uses your camera to analyze your form in real time. All processing happens on your device — nothing is uploaded or stored."
- If denied: show audio-only mode option (never block the workout entirely).
- Rationale screen matches Play Store camera permission policy requirements.
**Technical Tasks**:
- Compose permission rationale screen.
- `rememberLauncherForActivityResult` for camera permission.
- Handle granted/denied states.
- Audio-only fallback if denied.
**Modules Affected**: `:feature:session:session-ui`
**Dependencies**: SESSION-001.
**Size**: M

**Story ID**: AUTH-007
**Title**: Auth Navigation Graph
**User Story**: *As a developer, I need a dedicated navigation graph for auth screens so navigation is isolated from main flow.*
**Acceptance Criteria**:
- NavHost includes SignIn, SignUp, ForgotPassword, Splash.
- Navigation actions defined.
**Technical Tasks**:
- Create `auth_nav_graph.xml` (or Compose NavGraph).
**Modules Affected**: `:feature:auth:auth-ui`.
**Dependencies**: AUTH-001.
**Size**: S

---

## EPIC 2: ONBOARDING (Week 2)

**Story ID**: OB-001
**Title**: Welcome & Level Selector Screen
**User Story**: *As a new user, I want to select my fitness level (Beginner or Intermediate) right after auth so the app can tailor content.*
**Acceptance Criteria**:
- Screen shows two large selectable cards.
- Selection persisted in `PreferencesDataSource`.
- Navigation proceeds to level‑specific flow.
**Technical Tasks**:
- Compose screen with Inter font.
- Store selection via DataStore.
**Modules Affected**: `:feature:onboarding:onboarding-data`, `:feature:onboarding:onboarding-domain`, `:feature:onboarding:onboarding-ui`.
**Dependencies**: AUTH-001.
**Size**: S

**Story ID**: OB-002
**Title**: Beginner Path – Goals, Equipment, Frequency
**User Story**: *As a Beginner, I want to answer three short questionnaires so the app knows my objectives.*
**Acceptance Criteria**:
- Three screens collect: fitness goal, available equipment, workout frequency.
- Data saved locally and in Firestore.
- Validation ensures non‑empty.
**Technical Tasks**:
- Create three composable screens.
- Hook up to `OnboardingUseCase`.
**Modules Affected**: `:feature:onboarding:onboarding-data`, `:feature:onboarding:onboarding-domain`, `:feature:onboarding:onboarding-ui`.
**Dependencies**: OB-001.
**Size**: M

**Story ID**: OB-003
**Title**: Intermediate Path – Split, Goals, Optional 1RM
**User Story**: *As an Intermediate user, I want to define my current split routine and optional 1RM so recommendations match my experience.*
**Acceptance Criteria**:
- Screens for split selection, goals, optional 1RM input.
- 1RM field optional; validation if entered.
- Persisted same as Beginner.
**Technical Tasks**:
- Similar to OB-002 but with extra fields.
**Modules Affected**: `:feature:onboarding:onboarding-data`, `:feature:onboarding:onboarding-domain`, `:feature:onboarding:onboarding-ui`.
**Dependencies**: OB-001.
**Size**: M

**Story ID**: OB-004
**Title**: Onboarding Completion Flag & Navigation Graph
**User Story**: *As a developer, I need a flag indicating onboarding finished and a dedicated graph so main app flows only after completion.*
**Acceptance Criteria**:
- Flag stored in `PreferencesDataSource`.
- NavGraph checks flag on launch.
- Onboarding screens removed after flag set.
**Technical Tasks**:
- Add `isOnboardingComplete` boolean.
- Update splash logic.
**Modules Affected**: `:feature:onboarding:onboarding-data`, `:feature:onboarding:onboarding-domain`, `:feature:onboarding:onboarding-ui`.
**Dependencies**: OB-002, OB-003.
**Size**: S

---

## EPIC 3: WORKOUT PLAN (Weeks 3‑4)

**Story ID**: WP-001
**Title**: Gemini API Service & Prompt Builder
**User Story**: *As a backend developer, I need a Retrofit service to call Gemini with a structured JSON prompt so the app can obtain a workout plan.*
**Acceptance Criteria**:
- `GeminiApiService` with POST `/v1beta/models/gemini-pro:generateContent`.
- Prompt builder creates JSON from user profile.
- Handles HTTP 200/400 with proper error mapping.
**Technical Tasks**:
- Add Retrofit dependency.
- Write data classes for request/response.
- Write `PromptBuilder` utility.
**Modules Affected**: `:feature:workout:workout-data`, `:feature:workout:workout-domain`.
**Dependencies**: SETUP-005 (Gemini spike success).
**Size**: M

**Story ID**: WP-002
**Title**: GenerateWorkoutPlan Use‑Case with Fallback Asset
**User Story**: *As a user, I want a workout plan generated quickly, and if the API fails, I receive a local fallback plan.*
**Acceptance Criteria**:
- Use‑case calls Gemini service.
- On success, stores plan in Room and Firestore.
- On failure (network or latency), loads `fallback_workout_plans.json`.
- Logs fallback usage.
**Technical Tasks**:
- Implement `GenerateWorkoutPlanUseCase`.
- Add fallback JSON asset.
- Write repository logic.
**Modules Affected**: `:feature:workout:workout-data`, `:feature:workout:workout-domain`, `:core:core-data`.
**Dependencies**: WP-001.
**Size**: M

**Story ID**: WP-003
**Title**: WorkoutPlan Room Entities & DAOs
**User Story**: *As a developer, I need local persistence for generated plans so the app works offline.*
**Acceptance Criteria**:
- `WorkoutPlanEntity` with days, exercises.
- DAO with `insert`, `getLatest`, `clearOld`.
- Migration scripts.
**Technical Tasks**:
- Define entity, DAO, TypeConverters.
**Modules Affected**: `:core:core-data`.
**Dependencies**: SETUP-002.
**Size**: S

**Story ID**: WP-004
**Title**: Home Screen UI – Plan States
**User Story**: *As a user, I want to see my weekly plan on the home screen with clear states (loading, success, empty, error).*
**Acceptance Criteria**:
- Four composable states implemented.
- Refresh button triggers `GenerateWorkoutPlanUseCase`.
- Empty state shows “Generate a plan”.
**Technical Tasks**:
- Create `HomeScreen` composable.
- Wire ViewModel to repository.
**Modules Affected**: `:feature:workout:workout-ui`, `:feature:workout:workout-domain`.
**Dependencies**: WP-002.
**Size**: M

**Story ID**: WP-005
**Title**: Weekly Overview Component
**User Story**: *As a user, I want a concise overview of each day’s workouts so I can plan my week at a glance.*
**Acceptance Criteria**:
- Horizontal scroll list of day cards.
- Each card shows total reps, duration.
- Tap navigates to day detail.
**Technical Tasks**:
- Build `WeeklyOverview` composable.
- Connect to ViewModel data.
**Modules Affected**: `:feature:workout:workout-ui`.
**Dependencies**: WP-004.
**Size**: S

---

## EPIC 4: SESSION (Weeks 5‑6)

**Story ID**: SESSION-001
**Title**: CameraX Preview Composable
**User Story**: *As a user, I want to see a live camera preview during a session so I can follow along.*
**Acceptance Criteria**:
- CameraX preview displayed full‑screen.
- Handles permission request flow.
- Switches to audio fallback if permission denied.
**Technical Tasks**:
- Add `camera-core` dependency.
- Create `CameraPreview` composable.
**Modules Affected**: `:feature:session:session-ui`.
**Dependencies**: SETUP-004, SETUP-001.
**Size**: M

**Story ID**: SESSION-002
**Title**: ML Kit PoseDetector Integration
**User Story**: *As a user, I want real‑time pose detection so the app can analyze my form.*
**Acceptance Criteria**:
- Pose detector initialized.
- Provides key joint coordinates at ≥15 fps (verified by spike).
- Emits `PoseData` stream.
**Technical Tasks**:
- Add `mlkit-pose-detection` dependency.
- Wrap in `PoseDetectorUseCase`.
**Modules Affected**: `:feature:session:session-data`, `:feature:session:session-domain`, `:feature:session:session-ui`.
**Dependencies**: SETUP-004, SETUP-001, SETUP-004 (pose spike success).
**Size**: M

**Story ID**: SESSION-003
**Title**: Fatigue Detection Use‑Case
**User Story**: *As a user, I want the app to warn me when I’m fatigued based on pose stability.*
**Acceptance Criteria**:
- Detects deviation >15° over three consecutive reps.
- Shows animated warning banner.
- Logs event to analytics.
- User can dismiss fatigue warning with "I feel fine — continue" button.
- Dismissal logged as `fatigue_dismissed` Firebase Analytics event.
- After dismissal, fatigue re-triggers only if detected in next 5 reps, not immediately.
- Audio alert plays even when phone screen is locked during session using Foreground Service audio.
- Detection latency ≤ 2 seconds from 3rd consecutive bad rep to warning.
**Technical Tasks**:
- Implement `DetectFatigueUseCase` reading PoseData.
- Add `AnimatedVisibility` banner composable.
**Modules Affected**: `:feature:session:session-domain`, `:feature:session:session-ui`.
**Dependencies**: SESSION-002.
**Size**: M

**Story ID**: SESSION-004
**Title**: Lighting Condition Use‑Case & Audio Fallback
**User Story**: *As a user in low‑light, the app should automatically switch to audio‑only mode.*
**Acceptance Criteria**:
- 2 seconds of sustained low confidence or brightness < 10 lux triggers audio-only mode.
- 3 seconds of stable brightness > 10 lux and pose confidence > 0.6 required before reverting to visual mode.
- Manual toggle overrides auto‑mode.
**Technical Tasks**:
- Use Camera2 `getSensorBrightness`.
- Create `LightingUseCase`.
- Implement audio UI (dark screen + TTS).
**Modules Affected**: `:feature:session:session-domain`, `:feature:session:session-ui`.
**Dependencies**: SESSION-001.
**Size**: M

**Story ID**: SESSION-005
**Title**: Skeleton Overlay Canvas
**User Story**: *As a user, I want a visual overlay showing joint confidence colors so I can see feedback.*
**Acceptance Criteria**:
- Canvas draws joints with cyan (good), orange (uncertain), red (bad).
- Updates in sync with PoseDetector.
**Technical Tasks**:
- Create `SkeletonOverlay` composable.
- Map confidence to colors.
**Modules Affected**: `:feature:session:session-ui`.
**Dependencies**: SESSION-002.
**Size**: S

**Story ID**: SESSION-006
**Title**: Equipment Rerouting Bottom Sheet (Gemini API)
**User Story**: *As a user, if I lack equipment, I want alternative suggestions so I can continue the workout.*
**Acceptance Criteria**:
- Bottom sheet shows up to 3 alternatives fetched via Gemini.
- One-tap "unavailable" button is available during the active session.
- Selecting alternative updates current exercise.
**Technical Tasks**:
- Implement bottom sheet UI.
- Reuse Gemini service.
- Provide local fallback alternatives if Gemini is unavailable.
**Modules Affected**: `:feature:session:session-ui`, `:feature:workout:workout-data`.
**Dependencies**: SESSION-002, WP-001.
**Size**: M

**Story ID**: SESSION-007
**Title**: Guided Session UI with Lottie Demos
**User Story**: *As a user, I want animated exercise demos during a session to follow proper form.*
**Acceptance Criteria**:
- Lottie animation plays at start of each exercise.
- Syncs with rep count.
**Technical Tasks**:
- Add `lottie-compose` dependency.
- Create `ExerciseDemo` composable.
**Modules Affected**: `:feature:session:session-ui`.
**Dependencies**: SESSION-001.
**Size**: M

**Story ID**: SESSION-008
**Title**: Session Persistence & Summary Screen
**User Story**: *As a user, I want my session data saved and a summary after finishing so I can review performance.*
**Acceptance Criteria**:
- `SessionEntity` stored in Room.
- Summary shows calories, fatigue events, reps.
- Share image card via Android share sheet (WhatsApp).
**Technical Tasks**:
- Define entity, DAO.
- Build `SessionSummaryScreen`.
- Generate share image card and implement share intent.
**Modules Affected**: `:feature:session:session-data`, `:feature:session:session-domain`, `:feature:session:session-ui`, `:core:core-data`.
**Dependencies**: SESSION-003.
**Size**: M

---

## EPIC 5: PROGRESS (Weeks 6‑7)

**Story ID**: PROG-001
**Title**: Progress Room Queries & Use‑Cases
**User Story**: *As a user, I want to see my weekly stats so I can track improvement.*
**Acceptance Criteria**:
- Queries for total sessions, calories, fatigue events.
- Use‑cases return data structures for UI.
**Technical Tasks**:
- Add DAO methods.
- Write `GetProgressAnalyticsUseCase`.
**Modules Affected**: `:feature:progress:progress-data`, `:feature:progress:progress-domain`, `:core:core-data`.
**Dependencies**: SESSION-008.
**Size**: S

**Story ID**: PROG-002
**Title**: MPAndroidChart Integration
**User Story**: *As a user, I want bar charts visualizing my weekly progress.*
**Acceptance Criteria**:
- Chart renders within 1 s.
- Supports zoom/pan.
**Technical Tasks**:
- Add `MPAndroidChart` dependency.
- Create `ProgressChart` composable.
**Modules Affected**: `:feature:progress:progress-ui`.
**Dependencies**: PROG-001.
**Size**: M

**Story ID**: PROG-003
**Title**: Progress UI – Metric Cards & History List
**User Story**: *As a user, I want a dashboard with metric cards and a session history list.*
**Acceptance Criteria**:
- Four cards (sessions, calories, fatigue, avg duration).
- History list with empty state.
**Technical Tasks**:
- Build `ProgressDashboard` composable.
- Connect to use‑cases.
**Modules Affected**: `:feature:progress:progress-ui`, `:feature:progress:progress-domain`.
**Dependencies**: PROG-001.
**Size**: M

---

## EPIC 6: INFRASTRUCTURE (Weeks 7‑8)

**Story ID**: INFRA-001
**Title**: WorkManager Sync Worker (Room → Firestore)
**User Story**: *As a developer, I need background sync so user data stays consistent across devices.*
**Acceptance Criteria**:
- Worker runs every 6 h (or on network change).
- Syncs unsynced entities.
- Retries with exponential back‑off.
**Technical Tasks**:
- Add WorkManager dependency.
- Implement `SyncWorker`.
**Modules Affected**: `:core:core-data`.
**Dependencies**: SETUP-006.
**Size**: M

**Story ID**: INFRA-002
**Title**: Smart Reminder Worker
**User Story**: *As a user, I want push reminders for upcoming workouts so I stay consistent.*
**Acceptance Criteria**:
- Reminder scheduled based on user’s preferred days.
- Adaptive timing based on user's historical workout times.
**Technical Tasks**:
- Create `ReminderWorker`.
- Store schedule in DataStore.
- Notification channel and permission handling live in `:app`.
**Modules Affected**: `:app`.
**Dependencies**: INFRA-001.
**Size**: S

**Story ID**: INFRA-003
**Title**: Home Screen Widget (Glance)
**User Story**: *As a user, I want a home‑screen widget showing today’s plan for quick glance.*
**Acceptance Criteria**:
- 2x2 widget displays today's workout and current streak.
- Tapping opens app to Home.
**Technical Tasks**:
- Add `androidx.glance` dependency.
- Implement `FitLifeWidget`.
**Modules Affected**: `:feature:widget:widget-ui`.
**Dependencies**: WP-004.
**Size**: M

**Story ID**: INFRA-004
**Title**: Performance Testing for Pose Detection
**User Story**: *As a developer, I need benchmark results for pose detection FPS to guarantee performance.*
**Acceptance Criteria**:
- Automated benchmark script runs on CI.
- Publishes report with ≥15 fps average or fails CI below 15 fps.
**Technical Tasks**:
- Write instrumentation test.
- Integrate into CI pipeline.
**Modules Affected**: `:feature:session:session-ui`.
**Dependencies**: SESSION-002.
**Size**: S

---

## 8‑Week Sprint Plan

| Week | Epic(s) / Stories Planned |
|------|---------------------------|
| **1** | SETUP‑001, SETUP‑002, SETUP‑003, SETUP‑004, SETUP‑005, SETUP‑006, AUTH‑001, AUTH‑002 |
| **2** | AUTH‑003, AUTH‑004, AUTH‑005, AUTH‑006, AUTH‑007, OB‑001, OB‑002, OB‑003, OB‑004 |
| **3** | WP‑001, WP‑002, WP‑003, WP‑004 |
| **4** | WP‑005, SESSION‑001 (initial integration) |
| **5** | SESSION‑002, SESSION‑003, SESSION‑004, SESSION‑005, SESSION‑006 |
| **6** | SESSION‑007, SESSION‑008, PROG‑001, PROG‑002 |
| **7** | PROG‑003, INFRA‑001, INFRA‑002, INFRA‑003 |
| **8** | INFRA‑004, final QA, bug‑fixes, beta release preparation |

*Notes*: Technical spikes (SETUP‑004, SETUP‑005, SETUP‑006) are completed in Week 1; their results dictate whether dependent stories proceed.

---

## Critical Path (Blocking Stories)
- **SETUP‑001** (project scaffold) → all other setup.
- **SETUP‑004** (ML Kit pose spike) → SESSION‑002 onward.
- **SETUP‑005** (Gemini latency spike) → WP‑001 onward.
- **AUTH‑001** → OB‑001 onward.
- **AUTH‑004** (Firebase Security Rules) → any Firestore data write.
- **AUTH‑006** (Camera Permission Privacy Disclosure) → SESSION‑001 camera permission flow.
- **OB‑001** → all onboarding stories.
- **WP‑002** (Generate plan use‑case) → SESSION‑001 (requires plan).
- **SESSION‑002** (pose detector) → SESSION‑003, SESSION‑006, SESSION‑008.
- **PROG‑001** → PROG‑002, PROG‑003.

---

## High‑Risk Stories
| Story ID | Risk Reason |
|----------|------------|
| SETUP‑004 | Pose detection may not meet 15 fps → fallback to audio only.
| SETUP‑005 | Gemini API latency or quota limits → static templates.
| SESSION‑002 | ML Kit accuracy on mid‑range devices.
| SESSION‑003 | Fatigue detection thresholds may cause false alarms.
| INFRA‑001 | Background sync reliability on intermittent network.
| INFRA‑004 | Performance benchmark may reveal sub‑optimal FPS.

---

*End of document.*
