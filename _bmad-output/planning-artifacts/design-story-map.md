# Design Story Map

This document maps Stitch UI designs to BMAD implementation stories before development begins. Stitch is the visual source of truth for the screens listed here, while implementation must still follow the FitLife architecture, PRD requirements, and `core-ui` theme tokens.

If a Stitch screen conflicts with architecture boundaries, PRD expectations, navigation rules, privacy requirements, or existing story acceptance criteria, run a BMAD Correct Course review before implementation.

## Story to Screen Map

| Story ID | Screen Name | Stitch Link / Export      | States | Notes | Status |
|----------| ----------- |---------------------------| ------ | ----- | ------ |
| auth-000 | Splash | _bmad-output/design/auth/splash.png | loading, route-to-login, route-to-onboarding, route-to-home | No camera permission | Linked |
| AUTH-001 | Sign In / Sign Up | TODO                      | empty, loading, error, success | Firebase Auth |

## Usage Workflow

1. Export or link the Stitch screen.
2. Update the corresponding row in this document.
3. Reference the design in the story before development.
4. Implement the story using the linked design.
5. Run Correct Course if new screens, flows, or states are discovered.
