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
