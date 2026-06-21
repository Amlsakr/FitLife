# Story WP-002: GenerateWorkoutPlan Use-Case with Fallback Asset

Status: done

## Story

As a FitLife user,
I want my workout plan to be produced through one orchestration use case that prefers cached data and falls back to a local template when Gemini is slow or unavailable,
so that I still get a usable 7-day plan quickly even when the network or API quota is unreliable.

## Acceptance Criteria

1. A production `GenerateWorkoutPlanUseCase` exists in `:feature:workout:workout-domain` and is the single public entry point for workout-plan generation.
2. The use case accepts a pure workout-generation input model from the domain boundary, not onboarding, UI, Gemini, Retrofit, or Android types.
3. The use case checks for a fresh cached plan first and returns it without calling Gemini when the cached plan is still valid.
4. Cached plans older than 24 hours are treated as stale and do not block a remote refresh or fallback path.
5. When cache is missing or stale, the use case delegates to the workout data boundary for Gemini generation and keeps the existing 5-second timeout and bounded retry/backoff expectations from the spike and architecture notes.
6. Successful Gemini responses are mapped into a production `WorkoutPlan` domain model, persisted through repository hooks, and returned with fallback marked as `false`.
7. Any network, timeout, non-2xx, rate-limit, or parse failure falls back to `assets/fallback_workout_plans.json`; the selector picks the best matching plan for the user profile by fitness level, location, and equipment compatibility.
8. If no fallback template matches, the use case returns a safe domain error instead of crashing or leaking transport details.
9. Fallback usage is observable through the data boundary's logging or analytics hook, but API keys, raw HTTP bodies, and benchmark-only helpers never reach the UI or domain logs.
10. Automated tests cover cache hit, cache miss, remote success, timeout, non-2xx, parse failure, fallback selection, and no-match failure paths without calling the live Gemini API.
11. This story does not implement Room entities or DAOs, Firestore sync, or the home-screen UI. Those belong to WP-003 and later workout stories.

## Tasks / Subtasks

- [x] Add the production workout-plan domain model and use-case entry point in `:feature:workout:workout-domain`.
- [x] Define or tighten the workout repository contract so the use case can request cache, remote generation, persistence, and safe failures without seeing Gemini DTOs.
- [x] Implement the production fallback asset source in the data boundary and wire it to `assets/fallback_workout_plans.json`.
- [x] Add mapping from parsed Gemini or fallback drafts into the production `WorkoutPlan` model.
- [x] Emit fallback usage through the existing observability path, not from UI code.
- [x] Add unit tests for cache-first behavior, remote success, timeout/non-2xx classification, fallback selection, and no-match failure handling.
- [x] Verify the smallest relevant build or test surface after implementation.

### Review Findings

- [x] [Review][Patch] GenerateWorkoutPlanUseCase is not constructible from the production Hilt graph [feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/usecase/GenerateWorkoutPlanUseCase.kt:9]
- [x] [Review][Decision] Fallback equipment compatibility is ambiguous - resolved by treating `requiredEquipment` as exact required coverage so users are not assigned exercises needing unavailable gear.
- [x] [Review][Patch] Fallback asset parsing can still crash on a null or malformed catalog after remote failure [feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/fallback/WorkoutPlanFallbackSource.kt:21]
- [x] [Review][Patch] WorkoutGenerationRequest accepts non-7-day requests even though the parser and story require a 7-day plan [feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/model/WorkoutGenerationRequest.kt:9]
- [x] [Review][Patch] Cached plan restore does not validate that decoded cached data is structurally valid for the request before short-circuiting generation [feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/repository/WorkoutPlanRepositoryImpl.kt:52]
- [x] [Review][Patch] Fallback asset validation is still incomplete for nested template fields and 7-day structure [feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/fallback/WorkoutPlanFallbackSource.kt:34]
- [x] [Review][Patch] Production beginner-home fallback asset requires resistance bands, so common bodyweight/chair users cannot fall back [feature/workout/workout-data/src/main/assets/fallback_workout_plans.json:7]
- [x] [Review][Patch] Required WP-002 failure-path tests are incomplete for repository parse/non-2xx fallback behavior [feature/workout/workout-data/src/test/java/com/aml_sakr/fitlife/feature/workout/data/repository/WorkoutPlanRepositoryImplTest.kt:112]
- [x] [Review][Patch] Fallback usage logging includes a persistent user identifier [feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/di/WorkoutDataModule.kt:62]

## Implementation Contract

- WP-001 already owns the Gemini request builder, HTTP transport, response parser, and schema validation. Reuse those boundaries and do not duplicate prompt or schema logic in this story.
- The benchmark runner already records `assets/fallback_workout_plans.json` as the fallback path, but it does not load the production asset yet. WP-002 must close that gap.
- `feature/workout/workout-data/build.gradle.kts` currently uses Gson and coroutines with a direct `HttpURLConnection` transport. Do not introduce Retrofit just to satisfy this story.
- Keep Gemini DTOs, raw HTTP status handling, and transport exceptions in `:feature:workout:workout-data`; keep the use case and domain model platform-free.
- Prefer a small, explicit fallback selector contract over ad hoc asset parsing in a ViewModel or composable.
- If a shared asset-reader abstraction is introduced, keep it narrow and avoid leaking Android `Context` into domain APIs.
- Cache freshness should follow the architecture guidance: plans older than 24 hours are stale.
- Gemini structured output should remain JSON-only with `responseMimeType = application/json` and a schema inside the supported subset.
- Rate limiting is evaluated per project, not per API key. Retry and backoff should remain bounded, and fallback should still trigger when thresholds are exceeded.
- Keep model selection config-driven. Current official Gemini docs show `gemini-3.5-flash` in examples, but the orchestration layer should not hard-code a model name.

## Developer Notes

### Current State

- `GeminiWorkoutPromptBuilder` already builds a structured JSON-only request for a 7-day workout plan.
- `GeminiPlanResponseParser` already validates the 7-day shape, rejects malformed day layouts, and rejects duplicate days.
- `HttpGeminiApiService` already preserves HTTP status, response body, and response-size metadata, which is what this story needs for classification.
- `GeminiLatencyBenchmarkRunner` already records `fallbackUsed = true` and the fallback path `assets/fallback_workout_plans.json`, but that is benchmark metadata only.
- There is no production fallback asset loader yet.
- There is no production workout-plan use case yet.

### What This Story Changes

- Adds the production orchestration layer that ties cached data, Gemini generation, and fallback selection together.
- Introduces or finalizes the pure workout domain model that the app can render and persist without transport leakage.
- Makes the fallback asset a first-class production path rather than a benchmark-only label.
- Keeps all Gemini and Android details below the data boundary.

### What Must Be Preserved

- The existing Gemini request/response contract from WP-001.
- The existing non-2xx-before-parse behavior in the data layer.
- The existing 5-second timeout expectation from the spike and architecture notes.
- The existing benchmark output and file naming for `assets/fallback_workout_plans.json`.
- The Clean Architecture rule that feature domain code must not depend on Android, HTTP, or Gemini DTOs.

### Architecture Compliance

- Follow the workout module boundary in `_bmad-output/planning-artifacts/fitlife-architecture-v1.md`: domain owns the use case and pure models, data owns Gemini transport and asset loading.
- Keep the use case input narrow and pure. Do not bind it to onboarding storage, UI state, or `GeminiWorkoutProfile`, which is benchmark-only.
- If the implementation needs a new helper interface, keep it inside the workout data boundary unless there is a clear cross-feature reuse case.
- Do not move cache or fallback logic into UI code.
- Do not add feature-to-feature dependencies. If onboarding data must be transformed into a workout request, do that at the app or UI composition boundary.

### File Structure Requirements

Expected touch points for this story:

- `_bmad-output/implementation-artifacts/wp-002-generateworkoutplan-use-case-with-fallback-asset.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/...`
- `feature/workout/workout-domain/src/test/java/com/aml_sakr/fitlife/feature/workout/domain/...`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/...`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/fallback/...` or another narrow package for the fallback asset source
- `feature/workout/workout-data/src/main/assets/fallback_workout_plans.json`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/...` only if a reusable asset-reader abstraction is intentionally introduced
- `feature/workout/workout-data/src/test/java/com/aml_sakr/fitlife/feature/workout/data/...`

### Testing Requirements

- Unit tests must run offline and must not call live Gemini or real Firebase.
- Cache-first tests should verify that a valid cached plan short-circuits the remote call.
- Remote-success tests should verify that successful plans are saved and returned through the same production model.
- Failure tests should cover timeout, network, 429, 5xx, parse failure, and empty or error-body cases.
- Fallback tests should cover exact-match selection, best-match selection, and no-match safe failure.
- Keep tests deterministic by faking the repository, clock, and fallback asset source.

### Previous Story Intelligence

- WP-001 is already complete and established the Gemini contract in `:feature:workout:workout-data`.
- The existing workout prompt builder already requests JSON-only structured output for a 7-day plan.
- The existing parser already rejects malformed or duplicate-day responses, so WP-002 should not duplicate parser logic.
- The benchmark report showed all ten live Gemini calls timing out at 5 seconds and pointed to static fallback templates as the primary v1.0 safety net if latency stays high.
- The benchmark runner already records the fallback path name, which confirms the production fallback asset path the story should use.

### Latest Tech Notes

- Gemini structured outputs use JSON Schema with `responseMimeType = application/json`; keep the schema simple and within the supported subset.
- `responseSchema` and `responseJsonSchema` are alternatives, not additive. Do not define both.
- Gemini rate limits are evaluated per project across RPM, TPM, and RPD. A 429 should be treated as a project-level capacity signal, not an API-key-specific workaround.
- Current official Gemini docs use `gemini-3.5-flash` in examples; treat the model as config, not a hard-coded orchestration detail.

### Project Context Reference

- `docs/fitlife-stories-v1.md#WP-002`
- `_bmad-output/planning-artifacts/fitlife-prd-v1.md#5-2-AI-Workout-Plan-Generation-Gemini-API`
- `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#6-Use-Case-Implementations-Domain`
- `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#10-Gemini-API-Integration-Flow`
- `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#13-Open-Questions-Answered`
- `_bmad-output/implementation-artifacts/wp-001-gemini-api-service-prompt-builder.md`
- `_bmad-output/implementation-artifacts/spike-gemini-api-5-s-latency-report.md`
- `_bmad-output/project-context.md`
- Official Gemini docs: structured outputs, generateContent, and rate limits

## Change Log

- 2026-06-19: Created WP-002 story context for the workout generation use case, fallback asset selection, cache-first orchestration, and offline-safe testing.
- 2026-06-19: Implemented the workout generation orchestration, fallback asset source, mapper, failure classifier, and offline tests.
- 2026-06-19: Added a production workout-plan repository implementation, Hilt bindings, cache persistence, and bounded timeout/retry handling to resolve review findings.
- 2026-06-19: Tightened app-level workout-data wiring, fixed cache-key segregation and partial fallback matching, and split timeout test coverage into retryable transport timeouts and overall deadline timeouts.
- 2026-06-19: Hardened the workout path against missing Gemini model config and cache write failures so a usable plan still reaches the user through fallback or remote success.
- 2026-06-20: Aligned app BuildConfig Gemini key/model lookup with the workout data-layer configuration aliases.
- 2026-06-20: Resolved remaining WP-002 review findings by hardening fallback asset parsing, enforcing 7-day requests, validating restored cached plans, and verifying app Hilt wiring.
- 2026-06-20: Resolved final code-review findings for fallback template validation, production fallback asset coverage, failure-path tests, and privacy-safe fallback logging.

## Dev Agent Record

### Debug Log References

- Story context loaded from the WP-002 spec, current sprint status, workout Gemini source files, the benchmark report, architecture notes, PRD guidance, project context, and current official Gemini docs.
- Verified the existing `workout-data` Gemini transport and parser boundaries before adding the new domain orchestration and fallback source layer.
- Confirmed the repository already had no production fallback asset loader, so the new asset source, selector, mapper, and logger hook were added in `workout-data`.
- Ran `./gradlew.bat :feature:workout:workout-domain:test :feature:workout:workout-data:testDebugUnitTest --no-daemon --console=plain` and fixed the only failing fallback-source fixture by upgrading it to a full 7-day plan.
- Added a production workout-plan repository implementation with cache persistence, Gemini retry/backoff, timeout enforcement, and Hilt module wiring so the use case can be resolved at runtime.
- Re-ran the focused workout domain/data tests after the repository and binding changes; the build passed cleanly.
- Resolved the final repository timeout test mismatch by separating transport timeout retries from the overall 5-second deadline, then re-ran focused and wider verification successfully.
- Kept plan delivery resilient when cache persistence fails and ensured missing Gemini model configuration degrades safely into fallback behavior instead of crashing.
- Updated app BuildConfig wiring to honor `FITLIFE_GEMINI_API_KEY`/`GEMINI_API_KEY` and `FITLIFE_GEMINI_MODEL`/`GEMINI_MODEL_NAME` consistently.
- Added regression coverage for null/missing fallback catalogs, structurally invalid cached plans, request/profile cache mismatches, and non-7-day workout-generation requests.
- Ran `./gradlew.bat :feature:workout:workout-data:testDebugUnitTest --no-daemon --console=plain --rerun-tasks`, `./gradlew.bat :feature:workout:workout-domain:test --no-daemon --console=plain --rerun-tasks`, and `./gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain`; all passed.
- Added fallback asset structural validation for nested template fields and exact 7-day shape, updated the production beginner-home fallback to work with bodyweight/chair equipment, removed user identifiers from fallback logging, and expanded non-2xx/parse-failure generation tests.
- Ran `./gradlew.bat :feature:workout:workout-domain:test :feature:workout:workout-data:testDebugUnitTest --no-daemon --console=plain --rerun-tasks` and `./gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain`; both passed.

### Completion Notes List

- Added a pure workout domain model, repository contract, cache clock, and `GenerateWorkoutPlanUseCase` in `:feature:workout:workout-domain`.
- Added a fallback asset reader abstraction, Android asset reader, fallback selector, fallback source, logger hook, mapper, and remote failure classifier in `:feature:workout:workout-data`.
- Added a production workout-plan repository implementation plus Hilt bindings in `:feature:workout:workout-data` so the use case can be wired into the app graph.
- Added app-level workout bindings and BuildConfig-backed Gemini config wiring so the workout-data module is resolvable at runtime.
- Added `assets/fallback_workout_plans.json` with two 7-day templates for beginner home and intermediate gym scenarios.
- Added offline unit coverage for cache-first behavior, remote success, bounded retry/backoff, timeout handling, fallback success, no-match fallback failure, fallback selection, response classification, and mapping.
- Added tests for missing Gemini model configuration and cache persistence failure handling so resilience paths stay covered.
- Verified the focused workout tests and app compile passed after implementation.
- Resolved the remaining review findings: production Hilt graph construction is verified, fallback catalog parsing is defensive, request length is constrained to 7 days, and cached plans are structurally validated before reuse.
- Resolved the final code-review action items and verified WP-002 is ready to close as done.

### File List

- `_bmad-output/implementation-artifacts/wp-002-generateworkoutplan-use-case-with-fallback-asset.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `app/build.gradle.kts`
- `app/src/main/java/com/aml_sakr/fitlife/WorkoutBindingsModule.kt`
- `feature/workout/workout-domain/build.gradle.kts`
- `feature/workout/workout-data/build.gradle.kts`
- `feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/WorkoutPlanDefaults.kt`
- `feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/error/WorkoutGenerationError.kt`
- `feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/model/WorkoutFitnessLevel.kt`
- `feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/model/WorkoutGoal.kt`
- `feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/model/WorkoutLocation.kt`
- `feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/model/WorkoutGenerationRequest.kt`
- `feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/model/WorkoutExercise.kt`
- `feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/model/WorkoutDay.kt`
- `feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/model/WorkoutPlan.kt`
- `feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/repository/WorkoutPlanRepository.kt`
- `feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/usecase/GenerateWorkoutPlanUseCase.kt`
- `feature/workout/workout-domain/src/test/java/com/aml_sakr/fitlife/feature/workout/domain/usecase/GenerateWorkoutPlanUseCaseTest.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/fallback/WorkoutPlanFallbackModels.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/fallback/WorkoutPlanAssetReader.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/fallback/AndroidWorkoutPlanAssetReader.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/fallback/WorkoutPlanFallbackLogger.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/fallback/WorkoutPlanFallbackSelector.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/fallback/WorkoutPlanFallbackSource.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/WorkoutPlanMapper.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/WorkoutPlanFailureClassifier.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/WorkoutGeminiGatewayConfiguration.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/di/WorkoutDataModule.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/repository/WorkoutPlanRepositoryImpl.kt`
- `feature/workout/workout-data/src/main/assets/fallback_workout_plans.json`
- `feature/workout/workout-data/src/test/java/com/aml_sakr/fitlife/feature/workout/data/fallback/WorkoutPlanFallbackSelectorTest.kt`
- `feature/workout/workout-data/src/test/java/com/aml_sakr/fitlife/feature/workout/data/fallback/WorkoutPlanFallbackSourceTest.kt`
- `feature/workout/workout-data/src/test/java/com/aml_sakr/fitlife/feature/workout/data/gemini/WorkoutPlanFailureClassifierTest.kt`
- `feature/workout/workout-data/src/test/java/com/aml_sakr/fitlife/feature/workout/data/gemini/WorkoutPlanMapperTest.kt`
- `feature/workout/workout-data/src/test/java/com/aml_sakr/fitlife/feature/workout/data/repository/WorkoutPlanRepositoryImplTest.kt`

### Story Status

done
