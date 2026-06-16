## Deferred from: code review of setup-002-core-domain-core-data-core-ui-boilerplate (2026-06-03)

- Remove tracked generated build artifacts from version control. Current dirty state includes generated files under `core/core-ui/build/**`, while `_bmad-output/project-context.md` requires generated build artifacts and IDE files to stay out of commits.

## Deferred from: code review of auth-000-splash-screen-and-startup-routing (2026-06-11)

- Implement the real authenticated session and onboarding readers in AUTH-001. AUTH-000 remains in progress because its temporary runtime reader always reports no authenticated session.
