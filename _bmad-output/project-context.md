---
project_name: 'FitnessApp'
user_name: 'Amal.Sakr'
date: '2026-06-01'
sections_completed: ['technology_stack', 'language_specific_rules', 'framework_specific_rules', 'testing_rules', 'code_quality_style_rules', 'development_workflow_rules', 'critical_dont_miss_rules']
existing_patterns_found: 6
status: 'complete'
rule_count: 76
optimized_for_llm: true
---

# Project Context for AI Agents

_This file contains critical rules and patterns that AI agents must follow when implementing code in this project. Focus on unobvious details that agents might otherwise miss._

---

## Technology Stack & Versions

- Android application using Kotlin, Gradle Kotlin DSL, and Jetpack Compose Material3.
- Current Gradle state is a single module: `:app`; target architecture is the multi-module Clean Architecture graph in `docs/fitlife-architecture-v1.md`.
- New feature work should move toward the documented multi-module structure instead of expanding `:app` as a monolith; add modules to `settings.gradle.kts` when they are created.
- Build files are authoritative for versions: `settings.gradle.kts`, root `build.gradle.kts`, `app/build.gradle.kts`, and `gradle/libs.versions.toml`.
- Do not invent, normalize, or upgrade versions from planning docs. If this context conflicts with Gradle files, inspect and preserve the current Gradle values.
- Current Gradle catalog versions:
  - Android Gradle Plugin: `9.2.1`
  - Kotlin Compose plugin: `2.2.10`
  - Foojay resolver convention: `1.0.0`
  - AndroidX Core KTX: `1.18.0`
  - Lifecycle Runtime KTX: `2.10.0`
  - Activity Compose: `1.13.0`
  - Navigation 3 runtime/UI: `1.1.2`
  - Compose BOM: `2026.02.01`
  - JUnit: `4.13.2`
  - AndroidX JUnit: `1.3.0`
  - Espresso: `3.7.0`
- `app/build.gradle.kts` currently declares `minSdk = 30`, `targetSdk = 36`, and compile SDK using Android Gradle extension notation: `version = release(36) { minorApiLevel = 1 }`.
- Java compatibility is currently scoped to `sourceCompatibility = JavaVersion.VERSION_11` and `targetCompatibility = JavaVersion.VERSION_11`; do not introduce Java 17-only APIs unless the Gradle configuration is intentionally upgraded.
- Architecture docs approve future use of Room, Firebase Auth/Firestore, Hilt, Retrofit, Gemini API, ML Kit Pose Detection, CameraX, WorkManager, and MPAndroidChart, but these are planned dependencies unless already present in Gradle.
- Add planned libraries only when the implementing story requires them, through the version catalog where possible, with matching test dependencies and test strategy.
- MPAndroidChart is View-based; in Compose screens use `AndroidView` interop unless the project deliberately selects a Compose-native chart library.

## Critical Implementation Rules

### Language-Specific Rules

- Use Kotlin official style; `gradle.properties` sets `kotlin.code.style=official`.
- Keep application code under package `com.aml_sakr.fitlife` unless a new module/package boundary is intentionally introduced.
- Prefer `suspend` functions and coroutines for async work; avoid callback-style APIs leaking into domain or UI layers.
- Domain use cases should expose a single `suspend operator fun invoke(...)` pattern as shown in the architecture document.
- Repository interfaces in domain layers should return explicit `Result<Success, Error>`-style outcomes, not throw framework exceptions across layer boundaries.
- Do not introduce Java-only APIs or Java 17 language/runtime requirements while Gradle source/target compatibility remains Java 11.
- Keep Android framework types out of domain models and use cases; domain code should remain platform-independent where possible.
- Use sealed classes/interfaces for MVI events and one-time actions so `when` handling stays exhaustive.
- Do not hardcode API keys, Firebase credentials, or Gemini secrets in Kotlin source; keep secrets in local configuration/build config mechanisms.

### Framework-Specific Rules

- Use the multi-module Clean Architecture structure defined in `docs/fitlife-architecture-v1.md` as the target architecture.
- Current repo scaffolding may still be single-module, but new feature work should move toward the documented module graph instead of growing `:app` as a monolith.
- Planned modules are:
  - `:core:core-data`, `:core:core-domain`, `:core:core-ui`
  - `:feature:auth:auth-data`, `:feature:auth:auth-domain`, `:feature:auth:auth-ui`
  - `:feature:onboarding:onboarding-data`, `:feature:onboarding:onboarding-domain`, `:feature:onboarding:onboarding-ui`
  - `:feature:workout:workout-data`, `:feature:workout:workout-domain`, `:feature:workout:workout-ui`
  - `:feature:session:session-data`, `:feature:session:session-domain`, `:feature:session:session-ui`
  - `:feature:progress:progress-data`, `:feature:progress:progress-domain`, `:feature:progress:progress-ui`
- Core modules must not depend on feature modules.
- Feature modules may depend only on core modules and their own sibling layers.
- Use MVI + Clean Architecture as the default app pattern; the PRD explicitly says no MVVM.
- Keep unidirectional flow: Compose UI -> Event/Intent -> ViewModel -> State -> Compose UI.
- Model each feature with state, event, and one-time action types; use actions for navigation, snackbars, fatigue warnings, and other non-persistent effects.
- Compose screens should render from immutable state and send events upward; avoid embedding business logic in composables.
- `MainActivity` remains the single Activity host with Compose `setContent`; navigation uses Navigation 3 `NavDisplay`, typed serializable `NavKey` destinations, and app-owned saveable back stacks.
- Do not introduce `NavController`, `NavHost`, string routes, `navigation-compose`, or `navigation-testing`.
- Feature UI modules contribute typed keys and entry-provider registrations. Screens emit callbacks or MVI actions instead of receiving app navigation state.
- Use saveable-state and ViewModel-store entry decorators when a destination owns Compose state or a ViewModel.
- Splash, auth, sign-out, and onboarding completion must replace the navigation root atomically so obsolete protected-flow entries cannot be revisited.
- Use `FitnessAppTheme` and Material3 for app UI; avoid bypassing the theme with ad hoc colors unless adding deliberate design tokens.
- Room is the planned local source of truth. Write local data first, then sync to Firestore through background work.
- Gemini plan generation must use cache-first behavior, a 5-second timeout, bounded retry/backoff, and local fallback plans on failure.
- ML Kit pose detection should run on-device through CameraX analysis; do not block UI composition with frame analysis work.
- Lighting fallback uses pose confidence below `0.6`, low brightness/lux threshold around `10`, and a 2-second debounce before switching modes.
- Fatigue detection compares current rep joint angles against first-two-rep baselines and warns after 3 consecutive low-quality reps.

### Testing Rules

- Keep default test stack as JUnit4 unless the project deliberately migrates.
- Unit tests belong in `src/test`; Android/instrumented and Compose UI tests belong in `src/androidTest`.
- Add test dependencies with the production dependency that requires them; do not add broad test tooling without a concrete feature need.
- Coroutine-based ViewModel/use-case tests should use `kotlinx-coroutines-test` and a `MainDispatcherRule` once coroutine testing is introduced.
- Compose UI tests should use AndroidX Compose UI test APIs aligned through the Compose BOM.
- Repository and DAO tests should prefer in-memory Room databases.
- Retrofit/Gemini integration boundaries should use MockWebServer or equivalent fakes; do not call real Gemini APIs from automated tests.
- Firebase Auth/Firestore integration tests should use emulator-first workflows; CI must not depend on real cloud data.
- WorkManager sync behavior should use WorkManager test utilities once WorkManager is introduced.
- CameraX/ML Kit tests should use fake analyzer inputs or golden sample frames; automated tests must not require a physical camera.
- MVI tests should verify state transitions and one-time actions separately.
- Navigation 3 Compose tests should assert rendered destinations and the owned typed back stack directly; do not recreate Navigation 2 controller tests.

### Code Quality & Style Rules

- Keep Gradle dependencies centralized in `gradle/libs.versions.toml`; avoid hardcoded dependency coordinates in module build files when adding new libraries.
- Use Gradle Kotlin DSL consistently; do not add Groovy Gradle scripts.
- Keep module boundaries clean: `*-domain` owns domain models, repository interfaces, and use cases; `*-data` owns Room/Retrofit/Firebase implementations; `*-ui` owns Compose screens and ViewModels.
- Do not let UI modules depend directly on data implementations; route through domain use cases/repository interfaces.
- Name MVI types consistently by feature: `{Feature}State`, `{Feature}Event`, `{Feature}Action`, `{Feature}ViewModel`, `{Verb}{Noun}UseCase`.
- Keep composables small and state-driven; use previews for reusable UI components where practical.
- Put shared theme/design tokens in `core-ui` once that module exists; avoid duplicating color/type values across feature UI modules.
- Use explicit error models for network, auth, validation, and sync failures; avoid passing raw exception messages into UI state.
- Keep comments sparse and useful; document non-obvious algorithms such as fatigue thresholds, pose-angle calculations, sync conflict handling, and Gemini fallback behavior.
- Prefer immutable data classes for UI/domain state and copy-based updates.

### Development Workflow Rules

- Treat `docs/fitlife-architecture-v1.md` and `docs/fitlife-prd-v1.md` as the planning source for intended architecture and MVP behavior.
- Treat Gradle files and source code as the implementation source of truth when versions or current module state differ from docs.
- When creating the multi-module structure, update `settings.gradle.kts` and add each module's Gradle file in the same change.
- Add dependencies only for the story/feature being implemented; avoid preloading every planned library.
- Before changing shared architecture, module graph, dependency versions, package names, or min/target SDK, update the relevant planning/context docs or call out the mismatch.
- Navigation keys must remain serializable and free of Android/Firebase types so back-stack state can survive recreation.
- Run the smallest relevant Gradle verification after changes, such as `./gradlew test`, `./gradlew connectedAndroidTest`, or module-specific test tasks when modules exist.
- Keep generated build artifacts and IDE files out of commits; respect the existing `.gitignore`.
- Do not remove or rewrite existing planning docs unless the user explicitly asks; amend them when architecture decisions change.

### Critical Don't-Miss Rules

- Do not implement MVVM; project direction is MVI + Clean Architecture.
- Do not keep adding production features directly to `:app` once the relevant feature/core module should exist.
- Do not make feature modules depend on other feature modules; share cross-feature code through core modules.
- Do not let Room entities, Retrofit DTOs, Firebase documents, or Android framework classes leak into domain APIs.
- Do not call Gemini from UI/ViewModel code directly; route through repository/use-case layers with timeout, cache, retry, and fallback behavior.
- Do not require live network, real Firebase, real Gemini, or physical camera access in automated tests.
- Do not block the main thread with pose detection, frame analysis, database access, network calls, or sync work.
- Do not emit repeated lighting/fatigue warnings without debounce/consecutive-rep guards; follow documented thresholds.
- Do not store sensitive health/profile data or API secrets in logs, hardcoded strings, screenshots, or test fixtures.
- Do not assume planned dependencies are already installed; check Gradle files first and add only what the current feature requires.
- Do not introduce architecture/docs drift silently; if implementation differs from the PRD or architecture doc, document the decision.

---

## Usage Guidelines

**For AI Agents:**

- Read this file before implementing code.
- Follow all rules as documented.
- When Gradle/source files conflict with planning docs, treat implementation files as current truth and call out the mismatch.
- Update this file when new architecture, testing, or dependency patterns become established.

**For Humans:**

- Keep this file lean and focused on agent needs.
- Update it when the technology stack, module graph, or architecture decisions change.
- Review periodically for outdated rules.
- Remove rules that become obvious or obsolete over time.

Last Updated: 2026-06-14
