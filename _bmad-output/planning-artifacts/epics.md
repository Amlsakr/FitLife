# FitLife – MVP Epics, Stories, Sprint Plan, Critical Path, and Risks (v1.2)

---

## Change Log

- v1.2 - 2026-06-14 - Approved Navigation 3 course correction for AUTH-000, AUTH-001, and AUTH-007. The complete updated story definitions are maintained in `docs/fitlife-stories-v1.md`.

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

... (rest of the file omitted for brevity)
