# Story WP-001: Gemini API Service & Prompt Builder

Status: done
Design Status: Not required. This is a data-contract story; there is no dedicated design export.

## Story

As a backend developer,
I need a Gemini API service and structured prompt builder for workout-plan requests,
so that the app can request a 7-day workout plan without leaking Gemini transport details into domain or UI code.

## Acceptance Criteria

1. A prompt builder creates a structured Gemini `generateContent` request from the workout-plan input model/profile and always requests JSON output.
2. The request schema describes a 7-day workout plan with required `days`, `day`, `title`, `durationMinutes`, and exercise fields needed for later mapping.
3. The API service executes the request against Gemini and preserves HTTP status, raw response body, and response-size metadata for downstream parsing and error handling.
4. Non-2xx responses are classified distinctly from parse and transport failures so later stories can decide whether to retry, surface an error, or fall back.
5. Gemini request/response DTOs, prompt logic, and transport code stay inside `:feature:workout:workout-data`; domain code remains free of Gemini DTOs, HTTP clients, Android types, and API-key handling.
6. Automated tests cover request construction, schema shape, success and error classification, and valid/invalid response parsing without calling live Gemini.
7. This story does not add cache-first behavior, Room persistence, Firestore sync, fallback assets, or UI work. Those belong to later workout stories.

## Tasks / Subtasks

- [x] Stabilize the workout-plan Gemini request contract in `:feature:workout:workout-data`. Keep the request model explicit, JSON-only, and aligned to the 7-day plan shape used by later mapping logic. (AC: 1, 2, 5)
  - [x] Ensure the prompt text clearly instructs Gemini to return only structured JSON, not prose or markdown fences.
  - [x] Keep the schema shallow and limited to Gemini-supported structured-output types.
  - [x] Do not leak benchmark-only configuration or prompt details into UI code.
- [x] Implement the Gemini API service boundary in `:feature:workout:workout-data`. Route requests to the current Gemini `generateContent` endpoint and preserve status/body metadata for the parser. (AC: 3, 4, 5)
  - [x] Keep the transport implementation isolated to the data layer.
  - [x] Preserve raw response details so later stories can classify success, rate limit, HTTP error, network error, or parse failure correctly.
  - [x] Keep API-key handling out of logs, test fixtures, and `toString()` output.
- [x] Keep the domain layer clean. If any supporting config or request model is required, keep it behind the data boundary or in a narrowly scoped contract that does not expose HTTP/Gemini types. (AC: 5, 7)
  - [x] Do not move transport concerns into `:feature:workout:workout-domain`.
  - [x] Do not add cache, fallback, or persistence responsibilities here.
- [x] Add focused tests for the Gemini contract. Cover prompt contents, schema shape, 200 vs non-200 handling, parser validation, and secret redaction. (AC: 6)
  - [x] Verify malformed payloads and duplicate-day responses are rejected.
  - [x] Use fakes or local test doubles; do not require a live Gemini API key or network access.
  - [x] Keep tests scoped to the data boundary and avoid UI or Room setup.
- [x] Verify the smallest relevant build surface after implementation. Prefer module unit tests first, then any required data-layer compile checks. (AC: 6, 7)

## Dev Notes

### Current State

- The Gemini spike already introduced production-shaped helpers in `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/`, including `GeminiApiService`, `GeminiWorkoutPromptBuilder`, `GeminiGenerateContentModels`, `HttpGeminiApiService`, and `GeminiPlanResponseParser`.
- The workout domain module already contains spike-only benchmark helpers such as `GeminiBenchmarkConfiguration`, `GeminiWorkoutProfile`, benchmark samples, and summarizer/decision logic from SETUP-005.
- `feature/workout/workout-data/build.gradle.kts` currently depends on Gson and coroutines, not Retrofit or OkHttp.
- The live Gemini spike failed the 5-second free-tier target on `models/gemini-2.5-flash-lite`, so this story should not claim production plan generation success, caching, or fallback behavior.
- The older story-map text still mentions Retrofit and `gemini-pro`, but the spike code and current project context are newer. Do not resurrect the old shorthand or hard-code a stale model name.
- `wp-002-generateworkoutplan-use-case-with-fallback-asset` owns cache-first behavior, fallback asset loading, and the user-facing generation path. `wp-003` owns Room persistence.

### What This Story Changes

- Finalizes the Gemini request/response contract that later workout stories can depend on.
- Makes the prompt builder explicitly structured and JSON-only.
- Keeps transport metadata available for later retry/error handling without entangling the domain layer.
- Reinforces the boundary between the current spike scaffolding and production contract code.

### What Must Be Preserved

- Existing Clean Architecture boundaries: data owns Gemini DTOs and transport, domain stays platform-free.
- Existing test philosophy: no live Gemini calls in automated tests.
- Existing spike learnings: response parsing must validate the 7-day shape and reject malformed day numbers.
- Existing project guardrails: do not block on network work in UI, do not expose API keys, and do not add planned dependencies unless the story truly needs them.

### Architecture Compliance

- Follow the workout module boundaries in `_bmad-output/planning-artifacts/fitlife-architecture-v1.md`: data layer for Gemini transport/prompting, domain layer for pure business logic, and no feature-to-feature coupling.
- Keep structured-output usage within the Gemini-supported schema subset. Use `responseMimeType = "application/json"` and a compatible schema definition.
- Record the exact model name in config rather than hard-coding the old `gemini-pro` path. The current official docs list `gemini-3.5-flash` as the stable Flash model and support structured outputs on `gemini-3.5-flash`, `gemini-2.5-flash`, and `gemini-2.5-flash-lite`.
- Rate limits are per project and measured across RPM, TPM, and RPD, so do not assume per-key limits or free-tier behavior from the spike run.
- If a transport library swap is ever needed, keep it inside `:feature:workout:workout-data` and add dependencies through the version catalog deliberately.

### Latest Gemini API Notes

- Gemini structured outputs support a subset of JSON Schema. Supported types include `string`, `number`, `integer`, `boolean`, `object`, `array`, and `null`.
- `responseSchema` requires `responseMimeType = "application/json"`. `responseJsonSchema` is an alternative, but both should stay within supported schema features.
- The official docs note that structured-output schemas should stay reasonably small and simple. Keep the workout schema shallow so the parser remains predictable.
- Current Gemini docs show `gemini-3.5-flash` as the stable Flash model and `gemini-2.5-flash` / `gemini-2.5-flash-lite` as also supporting structured outputs. Choose the exact model through config, not a hard-coded string hidden in code paths.

### File Structure Requirements

Expected touch points for this story:

- `_bmad-output/implementation-artifacts/wp-001-gemini-api-service-prompt-builder.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `feature/workout/workout-data/build.gradle.kts`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/GeminiApiService.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/GeminiWorkoutPromptBuilder.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/GeminiGenerateContentModels.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/HttpGeminiApiService.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/GeminiPlanResponseParser.kt`
- `feature/workout/workout-data/src/test/java/com/aml_sakr/fitlife/feature/workout/data/gemini/`
- `feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/gemini/` only if a narrowly scoped production contract is needed
- `gradle/libs.versions.toml` only if a new transport dependency is intentionally introduced

### Testing Requirements

- Unit tests should validate the prompt contents and schema shape without network access.
- Service boundary tests should use fakes or local doubles and classify non-2xx responses before parsing.
- Parser tests should reject malformed output, duplicate day numbers, missing required exercise fields, and non-JSON text.
- Do not call the live Gemini API from unit tests or CI.
- Keep coverage focused on the data contract; `wp-002` will own fallback-path behavior and `wp-003` will own persistence tests.

### Project Structure Notes

- The current spike code already uses a lightweight direct HTTP client in `HttpGeminiApiService` to avoid premature transport dependencies.
- This story should reuse that boundary or replace it deliberately, but it should not duplicate the request builder or parser in another package.
- Keep all Gemini-specific source under `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/` and matching test packages.
- Do not expand the UI module or add navigation work for this story.

### References

- Story source: `docs/fitlife-stories-v1.md#L308-L320`
- Architecture: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#L234-L247`
- Architecture: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#L308-L316`
- Spike story: `_bmad-output/implementation-artifacts/setup-005-technical-spike-gemini-api-5-s-latency.md`
- Spike report: `_bmad-output/implementation-artifacts/spike-gemini-api-5-s-latency-report.md`
- Project context: `_bmad-output/project-context.md`
- Gemini GenerateContent API: https://ai.google.dev/api/generate-content
- Gemini structured outputs: https://ai.google.dev/gemini-api/docs/structured-output
- Gemini rate limits: https://ai.google.dev/gemini-api/docs/rate-limits
- Gemini 3.5 Flash model docs: https://ai.google.dev/gemini-api/docs/models/gemini-3.5-flash

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-06-19: Created WP-001 story context from sprint status, the maintained workout story map, the architecture doc, the SETUP-005 Gemini spike artifacts, the project context file, and current official Gemini API docs.

### Completion Notes List

- Gemini request and response handling are already implemented in `:feature:workout:workout-data` with a structured JSON prompt builder, direct Gemini transport, and response parser.
- The existing spike code already preserves request/response metadata and rejects malformed workout-plan payloads, including duplicate day values.
- Verified the focused workout contract with `./gradlew.bat :feature:workout:workout-domain:test :feature:workout:workout-data:testDebugUnitTest --no-daemon --console=plain`.
- No code changes were required for this pass because the repository already contained the completed Gemini contract work that satisfies the story.

### File List

- `_bmad-output/implementation-artifacts/wp-001-gemini-api-service-prompt-builder.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `feature/workout/workout-data/build.gradle.kts`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/GeminiApiService.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/GeminiWorkoutPromptBuilder.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/GeminiGenerateContentModels.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/HttpGeminiApiService.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/GeminiPlanResponseParser.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/GeminiLatencyBenchmarkRunner.kt`
- `feature/workout/workout-data/src/test/java/com/aml_sakr/fitlife/feature/workout/data/gemini/GeminiLatencyBenchmarkRunnerTest.kt`
- `feature/workout/workout-data/src/test/java/com/aml_sakr/fitlife/feature/workout/data/gemini/GeminiLiveLatencyBenchmarkManualTest.kt`
- `feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/gemini/GeminiBenchmarkModels.kt`
- `feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/gemini/GeminiBenchmarkSummarizer.kt`
- `feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/gemini/GeminiBenchmarkDecisionMaker.kt`
- `feature/workout/workout-domain/src/test/java/com/aml_sakr/fitlife/feature/workout/domain/gemini/GeminiBenchmarkMetricsTest.kt`

### Change Log

- 2026-06-19: Confirmed the workout Gemini contract already satisfies WP-001, verified the focused workout tests, and marked the story complete.
