# Story SETUP-002: Core Domain, Core Data, and Core UI Boilerplate

Status: done

Completion Note: Ultimate context engine analysis completed - comprehensive developer guide created.

## Story

As the FitLife developer,
I want the core domain, data, and UI libraries to expose shared contracts, utilities, MVI foundations, DI wiring, and theme resources,
so that feature modules can depend on a stable Clean Architecture foundation without duplicating boilerplate.

## Acceptance Criteria

1. `:core:core-domain` contains shared domain contracts required by the epic: `Error.kt`, `Result.kt`, `NetworkErrors.kt`, and `IBaseRepository.kt`, while preserving or safely adapting the existing `DomainError` and `DomainResult` contracts from SETUP-001.
2. `:core:core-data` contains reusable data-layer foundations: `BaseRepository`, safe API/data-call helpers, response-to-result mapping, connectivity utilities, a preferences data source, and core DI modules.
3. `:core:core-ui` contains shared MVI UI foundations: `UIState`, `Event`, `OneTimeAction`, and a base MVI ViewModel, while preserving or safely adapting the existing `UIEvent` and `BaseMviViewModel` behavior from SETUP-001.
4. `:core:core-ui` owns shared FitLife theme resources for Compose: Inter font family wiring where feasible, colors, typography, spacing/dimens, and a reusable `FitnessAppTheme` or equivalent theme API available to `:app` and future feature UI modules.
5. The Hilt application class remains defined and registered; core DI modules compile without introducing feature-specific bindings or credentials.
6. Gradle dependency changes are centralized in `gradle/libs.versions.toml`, use pinned versions, and add only dependencies needed for this core boilerplate.
7. `./gradlew.bat test` passes, with focused unit tests for domain result/error behavior, data safe-call mapping, preferences abstraction or connectivity utility behavior where practical, and MVI base ViewModel behavior.

## Tasks / Subtasks

- [x] Normalize core-domain contracts. (AC: 1)
  - [x] Add or rename wrappers so the epic-required file names exist under `core/core-domain/src/main/java/com/aml_sakr/fitlife/core/domain/`.
  - [x] Preserve binary/source intent of existing `DomainError` and `DomainResult`; do not break existing tests unless updating them to the finalized API.
  - [x] Add `NetworkErrors` as explicit domain-level error variants for common network states such as no connection, timeout, unauthorized, server error, serialization error, and unknown API error.
  - [x] Add `IBaseRepository` as a marker or shared contract only if it adds value to repository typing; do not put Android, Retrofit, Room, or Firebase types in domain APIs.
- [x] Build reusable core-data utilities. (AC: 2, 5, 6)
  - [x] Update `core/core-data/build.gradle.kts` with only required dependencies for Hilt annotations, DataStore Preferences if used, coroutines, and AndroidX core APIs.
  - [x] Add safe-call helpers that catch data-source exceptions and return domain `Result`/`NetworkErrors` instead of throwing framework exceptions across layer boundaries.
  - [x] Add response-to-result mapping for future network calls without introducing Retrofit service implementations before feature stories need them.
  - [x] Add connectivity abstraction with an Android implementation that can be faked in tests.
  - [x] Add preferences data source abstraction and implementation for simple persisted app/user flags; prefer Preferences DataStore over SharedPreferences for new async key-value persistence.
  - [x] Add core Hilt module(s), such as `CoreDataModule`, for core-only dependencies. Avoid `AuthModule`, `WorkoutModule`, `SessionModule`, `OnboardingModule`, or `ProgressModule` in this story.
- [x] Consolidate core-ui MVI and theme. (AC: 3, 4)
  - [x] Keep the existing `BaseMviViewModel` unidirectional flow: Compose UI -> event/intent -> ViewModel -> immutable state -> Compose UI, with one-time actions emitted separately.
  - [x] Align naming with architecture examples. If both `Event` and `UIEvent` are supported, provide a compatibility path or clear alias so future feature stories do not fork naming.
  - [x] Move shared theme code from `app/src/main/java/com/aml_sakr/fitlife/ui/theme` into `core-ui`, then update `MainActivity` imports to consume the core theme.
  - [x] Add FitLife color, typography, and spacing/dimens tokens in `core-ui`; avoid keeping the default one-note purple starter palette as the long-term app palette.
  - [x] Add Inter font resource wiring if font files are available locally or can be added without licensing ambiguity. If physical font files are not present, document the pending asset and wire typography with the closest safe fallback.
- [x] Preserve app shell and Hilt setup. (AC: 5)
  - [x] Keep `app/src/main/java/com/aml_sakr/fitlife/FitnessApplication.kt` annotated with `@HiltAndroidApp`.
  - [x] Keep `android:name=".FitnessApplication"` in `app/src/main/AndroidManifest.xml`.
  - [x] Keep `MainActivity` as the single Activity Compose host; only update theme imports/usages needed by the core-ui migration.
- [x] Add focused verification. (AC: 7)
  - [x] Keep or update `BaseMviViewModelTest` to match finalized event/action naming.
  - [x] Add domain tests for success/failure and error typing.
  - [x] Add data tests for safe-call result mapping; use fakes and avoid real network/Firebase.
  - [x] Run `./gradlew.bat test` and record the result in the Dev Agent Record.

### Review Findings

- [x] [Review][Patch] Re-throw non-timeout coroutine cancellation in SafeCall [core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/repository/SafeCall.kt:30]
- [x] [Review][Patch] Support successful no-body HTTP responses in result mapping [core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/repository/ResponseToResult.kt:13]
- [x] [Review][Patch] Handle DataStore read IOException in preference flows [core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/preferences/DataStorePreferencesDataSource.kt:16]
- [x] [Review][Defer] Remove tracked generated build artifacts from version control [core/core-ui/build/outputs/logs/manifest-merger-debug-report.txt:1] -- deferred, pre-existing

## Dev Notes

### Current State

- SETUP-001 is complete and already created the multi-module Gradle scaffold, Hilt app setup, core domain result contracts, and core UI MVI contracts. Reuse these; do not recreate competing equivalents. [Source: `_bmad-output/implementation-artifacts/setup-001-create-multi-module-gradle-project.md`]
- `settings.gradle.kts` includes `:app`, the three core modules, and auth/onboarding/workout/session/progress feature modules. It does not currently include `:feature:widget:widget-ui`, even though the epic file still mentions a widget module. Treat this as a known planning variance and do not solve widget scaffolding inside SETUP-002 unless explicitly directed. [Source: `settings.gradle.kts`] [Source: `_bmad-output/planning-artifacts/epics.md#EPIC-0-PROJECT-SETUP`]
- Current Gradle catalog versions include AGP `9.2.1`, Kotlin `2.2.10`, Compose BOM `2026.02.01`, Hilt `2.59.2`, KSP `2.3.4`, and coroutines `1.11.0`. Do not upgrade versions opportunistically. [Source: `gradle/libs.versions.toml`]
- `:app` currently depends on `:core:core-ui`, applies Hilt and KSP, and registers `FitnessApplication`. Keep this foundation intact. [Source: `app/build.gradle.kts`] [Source: `app/src/main/java/com/aml_sakr/fitlife/FitnessApplication.kt`] [Source: `app/src/main/AndroidManifest.xml`]

### Existing Files To Read Before Editing

- `core/core-domain/src/main/java/com/aml_sakr/fitlife/core/domain/DomainError.kt`: currently an interface with `code` and `message`.
- `core/core-domain/src/main/java/com/aml_sakr/fitlife/core/domain/DomainResult.kt`: currently a sealed interface with `Success(value)` and `Failure(error)`.
- `core/core-ui/src/main/java/com/aml_sakr/fitlife/core/ui/mvi/MviContracts.kt`: currently defines `UIState`, `UIEvent`, and `OneTimeAction`.
- `core/core-ui/src/main/java/com/aml_sakr/fitlife/core/ui/mvi/BaseMviViewModel.kt`: currently owns `StateFlow` state, receives events through `onEvent`, and emits one-time actions through a buffered channel.
- `app/src/main/java/com/aml_sakr/fitlife/ui/theme/Color.kt`, `Theme.kt`, and `Type.kt`: starter theme files that should migrate into `core-ui` or be replaced by core-ui theme APIs.
- `core/core-data/build.gradle.kts`: currently has no source files and only depends on `:core:core-domain`.

### Architecture Compliance

- Core modules must not depend on feature modules. Feature modules may depend on core modules and their own sibling layers only. [Source: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#1-module-structure--gradle-dependency-graph`]
- `core-domain` must stay platform-independent: domain models, error/result contracts, repository interfaces, and use-case contracts only. Keep Android framework, DataStore, Retrofit, Room, Firebase, and Hilt implementation types out of domain contracts. [Source: `_bmad-output/project-context.md#Critical-Implementation-Rules`]
- `core-data` may depend on `core-domain` and Android/data infrastructure libraries. It should provide reusable base implementations and abstractions, not feature-specific repositories.
- `core-ui` may contain Compose, Material3, lifecycle ViewModel, MVI contracts, theme tokens, and shared UI primitives. It must not depend on data implementations or feature modules.
- Use MVI + Clean Architecture. Do not drift into MVVM naming or two-way state mutation. [Source: `_bmad-output/planning-artifacts/fitlife-prd-v1.md#10-open-questions-for-architect-agent`]

### Library and Framework Requirements

- Use the existing version catalog for all new dependencies; add aliases such as DataStore Preferences or Hilt artifacts only if the implementation needs them.
- Official Android Hilt guidance requires `@HiltAndroidApp` on the application class and supports KSP-based setup; this project already follows that in `:app`. Add Hilt plugin/KSP to `:core:core-data` only if Hilt modules there require annotation processing. [Source: Android Hilt docs, last updated 2026-03-06: https://developer.android.com/training/dependency-injection/hilt-android]
- Official Android DataStore guidance positions Preferences DataStore for key-value persistence without a predefined schema and warns that multiple active DataStores for the same file in one process can throw. Implement a singleton-injected data source rather than creating ad hoc DataStore instances. [Source: Android DataStore docs: https://developer.android.com/topic/libraries/architecture/datastore]
- Compose Material3 remains the UI system. Use MaterialTheme-based color and typography tokens in `core-ui`; do not introduce XML View-based UI architecture. [Source: Compose Material3 docs: https://developer.android.com/jetpack/androidx/releases/compose-material3]

### Regression and Scope Guardrails

- Do not add Room, Retrofit, Firebase Auth, Firestore, Gemini API, ML Kit, CameraX, WorkManager, MPAndroidChart, or feature-specific implementations in this story unless they are strictly needed for the shared boilerplate contract.
- Do not create auth/workout/session/onboarding/progress repository implementations here. This story creates reusable base patterns that later feature stories extend.
- Do not hardcode Firebase, Gemini, or API secrets in Kotlin source, Gradle files, resources, logs, or tests.
- Do not remove starter app behavior unless replacing it with an equivalent smoke-test Compose screen that still proves the app launches.
- Do not silently change `minSdk = 30`, `targetSdk = 36`, Java 11 compatibility, or package root `com.aml_sakr.fitlife`.

### Testing Requirements

- Required command: `./gradlew.bat test`.
- Use JVM unit tests for domain contracts and MVI base behavior.
- For `core-data`, prefer fake interfaces and deterministic coroutine tests. Do not require real network, real Firebase, emulator, or device camera access.
- If DataStore behavior is tested, isolate storage to a temporary test scope and avoid sharing the same file name across tests.

### Previous Story Intelligence

- SETUP-001 established `DomainError`, `DomainResult`, `UIState`, `UIEvent`, `OneTimeAction`, `BaseMviViewModel`, Hilt application setup, and module Gradle files. Build on these exact locations instead of adding duplicate packages or app-local copies.
- SETUP-001 ran `./gradlew.bat test` successfully and added `BaseMviViewModelTest`. Keep this coverage green while extending the core boilerplate.
- SETUP-001 completion kept `minSdk = 30` despite the PRD's Android 26+ note. Preserve that decision unless a separate architecture/product update changes it.

### Git Intelligence

- Recent commits are focused on SETUP-001 modular setup and story tracking. The current working tree was clean before this story file was created.
- Commit `23398ae` renamed the SETUP-001 story file and updated sprint tracking; commit `7a2b77c` adjusted the SETUP-001 story status only. There is no deeper feature implementation history yet.

### References

- Epics: `_bmad-output/planning-artifacts/epics.md`
- Architecture: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md`
- PRD: `_bmad-output/planning-artifacts/fitlife-prd-v1.md`
- Project context: `_bmad-output/project-context.md`
- Previous story: `_bmad-output/implementation-artifacts/setup-001-create-multi-module-gradle-project.md`
- Sprint status: `_bmad-output/implementation-artifacts/sprint-status.yaml`
- Current Gradle settings: `settings.gradle.kts`
- Version catalog: `gradle/libs.versions.toml`
- Core domain build: `core/core-domain/build.gradle.kts`
- Core data build: `core/core-data/build.gradle.kts`
- Core UI build: `core/core-ui/build.gradle.kts`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-06-03: Loaded BMad dev-story workflow, project context, sprint status, and SETUP-002 story context.
- 2026-06-03: Red phase confirmed missing SETUP-002 domain/data APIs via `./gradlew.bat :core:core-domain:test :core:core-data:test`; compile failed on missing `Result`, `NetworkErrors`, `IBaseRepository`, `SafeCall`, response mapping, and preferences abstractions.
- 2026-06-03: Implemented core-domain contracts, core-data safe-call/response/preferences/connectivity/DI utilities, and verified `./gradlew.bat :core:core-domain:test :core:core-data:test --no-daemon` passed.
- 2026-06-03: Red phase confirmed missing `Event` compatibility alias via `./gradlew.bat :core:core-ui:test --no-daemon`; compile failed on unresolved `Event`.
- 2026-06-03: Implemented `core-ui` event compatibility and shared theme tokens, moved app theme ownership to `core-ui`, and verified `./gradlew.bat :core:core-ui:test --no-daemon` passed.
- 2026-06-03: Ran full regression suite. Initial `./gradlew.bat test --no-daemon` hit tool timeout; rerun with `--console=plain` passed. Final `./gradlew.bat test --no-daemon --console=plain` passed with `BUILD SUCCESSFUL in 1m 45s`.
- 2026-06-03: Ran lint. First `./gradlew.bat lint --no-daemon --console=plain` failed on missing `ACCESS_NETWORK_STATE`; added `core-data` manifest permission. Final lint run passed with `BUILD SUCCESSFUL in 3m 34s`.
- 2026-06-03: Applied code-review fixes for coroutine cancellation propagation, no-body HTTP success mapping, and DataStore read IO fallback. Verified `./gradlew.bat :core:core-data:test --no-daemon --console=plain`, `./gradlew.bat :core:core-data:compileDebugKotlin :core:core-data:testDebugUnitTest --rerun-tasks --no-daemon --console=plain`, `./gradlew.bat test --no-daemon --console=plain`, and `./gradlew.bat lint --no-daemon --console=plain` passed.

### Completion Notes List

- Added canonical `Result`, `Error`, `NetworkErrors`, and `IBaseRepository` contracts in `core-domain`; retained `DomainError` and `DomainResult` compatibility via typealiases.
- Added reusable `core-data` foundations: `BaseRepository`, `SafeCall`, `NetworkResponse.toResult`, connectivity abstractions, DataStore-backed preferences data source, in-memory preferences test double, and Hilt core data modules.
- Added `ACCESS_NETWORK_STATE` to `core-data` manifest so Android connectivity checks pass lint and merge correctly when the module is consumed.
- Moved shared Compose theme ownership from `:app` to `:core:core-ui`, added FitLife color/typography/dimens tokens, and updated `MainActivity` to use the core theme.
- Added `Event` compatibility alias while preserving existing `UIEvent`, `UIState`, `OneTimeAction`, and `BaseMviViewModel` behavior.
- Inter font files were not present in the repository; `core-ui` now wires a named `FitLifeFontFamily` fallback to `FontFamily.Default` so physical Inter assets can be added later without changing callers.
- Added focused tests for domain contracts, safe-call and response mapping behavior, preferences abstraction behavior, and MVI event compatibility.
- Addressed code-review findings by preserving coroutine cancellation, adding an explicit empty-body response mapping path for `Unit`/ack-style operations, and making DataStore preference reads fall back to defaults on `IOException`.

### File List

- `_bmad-output/implementation-artifacts/setup-002-core-domain-core-data-core-ui-boilerplate.md`
- `_bmad-output/implementation-artifacts/deferred-work.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `gradle/libs.versions.toml`
- `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt`
- `app/src/main/java/com/aml_sakr/fitlife/ui/theme/Color.kt` (deleted)
- `app/src/main/java/com/aml_sakr/fitlife/ui/theme/Theme.kt` (deleted)
- `app/src/main/java/com/aml_sakr/fitlife/ui/theme/Type.kt` (deleted)
- `core/core-domain/src/main/java/com/aml_sakr/fitlife/core/domain/DomainResult.kt`
- `core/core-domain/src/main/java/com/aml_sakr/fitlife/core/domain/Error.kt`
- `core/core-domain/src/main/java/com/aml_sakr/fitlife/core/domain/IBaseRepository.kt`
- `core/core-domain/src/main/java/com/aml_sakr/fitlife/core/domain/NetworkErrors.kt`
- `core/core-domain/src/main/java/com/aml_sakr/fitlife/core/domain/Result.kt`
- `core/core-domain/src/test/java/com/aml_sakr/fitlife/core/domain/DomainContractsTest.kt`
- `core/core-data/build.gradle.kts`
- `core/core-data/src/main/AndroidManifest.xml`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/connectivity/AndroidConnectivityMonitor.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/connectivity/ConnectivityMonitor.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/di/CoreDataModule.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/preferences/DataStorePreferencesDataSource.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/preferences/InMemoryPreferencesDataSource.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/preferences/PreferencesDataSource.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/repository/BaseRepository.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/repository/ResponseToResult.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/repository/SafeCall.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/repository/StaticConnectivityMonitor.kt`
- `core/core-data/src/test/java/com/aml_sakr/fitlife/core/data/preferences/InMemoryPreferencesDataSourceTest.kt`
- `core/core-data/src/test/java/com/aml_sakr/fitlife/core/data/repository/SafeCallTest.kt`
- `core/core-ui/build.gradle.kts`
- `core/core-ui/src/main/java/com/aml_sakr/fitlife/core/ui/mvi/Event.kt`
- `core/core-ui/src/main/java/com/aml_sakr/fitlife/core/ui/theme/Color.kt`
- `core/core-ui/src/main/java/com/aml_sakr/fitlife/core/ui/theme/Dimens.kt`
- `core/core-ui/src/main/java/com/aml_sakr/fitlife/core/ui/theme/Theme.kt`
- `core/core-ui/src/main/java/com/aml_sakr/fitlife/core/ui/theme/Type.kt`
- `core/core-ui/src/test/java/com/aml_sakr/fitlife/core/ui/mvi/MviContractsTest.kt`

### Change Log

- 2026-06-03: Created comprehensive SETUP-002 story context and marked story ready for development.
- 2026-06-03: Implemented SETUP-002 core-domain, core-data, and core-ui boilerplate; passed tests and lint; moved story to review.
