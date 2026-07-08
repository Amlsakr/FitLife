## Deferred from: code review of ob-004-onboarding-completion-flag-navigation-graph.md (2026-06-20)

- Global root back handling can empty the Navigation 3 back stack. `NavDisplay(onBack = { backStack.removeLastOrNull() })` predates OB-004 and affects any single-root destination, not just onboarding completion; handle as a shared navigation-shell hardening item.

## Deferred from: code review of ob-004-onboarding-completion-flag-navigation-graph.md (2026-06-21)

- Non-completion onboarding persistence paths still catch `Throwable` and swallow coroutine cancellation. Completion read/write paths were fixed for OB-004, but selected-level, draft, and remote-sync paths predate this completion-gate story and should be hardened in a broader repository cleanup.
- Selected level and draft preferences remain device-global instead of per-user. OB-004 keys the completion flag per user as required, but the older selected-level and draft keys can still be inherited across accounts and should be revisited separately.

## Deferred from: code review of setup-002-core-domain-core-data-core-ui-boilerplate (2026-06-03)

- Remove tracked generated build artifacts from version control. Current dirty state includes generated files under `core/core-ui/build/**`, while `_bmad-output/project-context.md` requires generated build artifacts and IDE files to stay out of commits.

## Deferred from: code review of auth-000-splash-screen-and-startup-routing (2026-06-11)

- Implement the real authenticated session and onboarding readers in AUTH-001. AUTH-000 remains in progress because its temporary runtime reader always reports no authenticated session.

## Deferred from: code review of auth-003-forgot-password-account-deletion.md (2026-06-19)

- Forgot-password tap falls back to reset-email flow instead of navigation. AuthNavigation currently keeps the existing auth-flow event path and does not expose the dedicated destination from the login route yet.
- Sign-up success no-ops because onboarding callback expects Splash on top. MainActivity’s onboarding replacement path still assumes the splash/root stack shape.
- Story docs now promise Home-only auth despite onboarding routing. The story narrative and the runtime onboarding flow are drifting apart and should be reconciled in a separate doc/update pass.
## Deferred from: code review of session-005-skeleton-overlay-canvas.md (2026-06-28)\n\n- Potential pose data leak in state [feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/ActiveSessionViewModel.kt] — deferred, pre-existing

## Deferred from: code review of session-006-equipment-rerouting-bottom-sheet-gemini-api.md (2026-06-30)

- `NetworkErrors` used as domain-layer error type — The domain use case returns `Result<..., NetworkErrors>`, coupling domain to network-specific errors. Pre-existing pattern in the codebase; refactor in a future story.
- `AudioOnlyOverlay` hardcodes `Color.Black` and `Color.White` — Same hardcoded colors bypass from session-005. Pre-existing issue, not introduced by this change.
- `LightingUseCase` emission timing — If `poseDataFlow` never emits, `PoseData.EMPTY` permanently biases the result. Pre-existing edge case in lighting module.
- `timeoutMillis` truncation risk — `Long.toInt()` could truncate if timeout exceeds `Int.MAX_VALUE`. Current default (5000L) is safe; defer until timeout becomes configurable.
- `isAudioOnlyMode` not persisted across config changes — Ephemeral state resets on ViewModel recreation. Pre-existing pattern; address with `SavedStateHandle` in a future story.

## Deferred from: code review of session-007-guided-session-ui-with-lottie-demos.md (2026-06-30)

- Fatigue detection without baseline guard [ActiveSessionViewModel.kt:handleRepCompleted] — The algorithm (Section 8 of Arch Doc) requires a baseline from the first two reps, but currently calls `analyzeRep` for every rep; deferred as pre-existing logic exposed here.
- Temporary Lottie Mapping Tech Debt [ActiveSessionViewModel.kt] — The hardcoded asset path logic is acknowledged as "temporary" in comments. This should be addressed in the planned Exercise Library migration; deferred as pre-existing pattern.
- Potential safety risk in fatigue warning suppression [ActiveSessionViewModel.kt] — The hardcoded 5-rep suppression after dismissal is pre-existing logic. While risky, it is out of scope for the Lottie UI story; deferred as pre-existing logic.

## Deferred from: code review of prog-003-progress-ui-metric-cards-history-list.md (2026-07-08)

- Hardcoded Placeholder for User ID [ProgressDashboardViewModel.kt] — deferred, pre-existing intent
- Potential Lifecycle Issue with AndroidView in LazyColumn [FitLifeLineChart.kt] — deferred, pre-existing component

## Deferred from: code review of prog-003-progress-ui-metric-cards-history-list.md (2026-07-08) - Round 2

- Timezone inconsistency in history formatting [SessionHistoryItem.kt:17] — deferred, project-wide pattern
