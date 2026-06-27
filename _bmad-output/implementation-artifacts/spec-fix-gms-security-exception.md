---
title: 'Fix GMS SecurityException in API 36'
type: 'bugfix'
created: '2026-06-21'
status: 'done'
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** The app crashes with `java.lang.SecurityException: Unknown calling package name 'com.google.android.gms'` when attempting to use Google Play Services (like ML Kit Pose Detection or Firestore) while targeting Android 16 (API 36).

**Approach:** Add the `<queries>` element to the `AndroidManifest.xml` to explicitly declare visibility for the `com.google.android.gms` package, ensuring the Google Play Services broker can correctly identify the calling package.

## Boundaries & Constraints

**Always:**
- Keep the manifest clean and follow existing formatting.
- Place the `<queries>` block as a direct child of `<manifest>`, before `<application>`.

**Ask First:**
- If the crash persists after this change, ask before attempting to downgrade the `targetSdk`.

**Never:**
- Do not add unnecessary queries for other packages.
- Do not modify `google-services.json`.

</frozen-after-approval>

## Code Map

- `app/src/main/AndroidManifest.xml` -- Main application manifest where package visibility rules are defined.

## Tasks & Acceptance

**Execution:**
- [ ] `app/src/main/AndroidManifest.xml` -- Add `<queries>` block for `com.google.android.gms` -- Restores package visibility for Google Play Services.

**Acceptance Criteria:**
- Given the app targets API 36, when a Google Play Services feature (e.g., ML Kit Pose Detection) is initialized, then the app no longer throws `SecurityException: Unknown calling package name 'com.google.android.gms'`.

## Verification

**Commands:**
- `./gradlew :app:assembleDebug` -- expected: Successful build.

**Manual checks (if no CLI):**
- Deploy to an API 36 device/emulator and verify that features using GMS (Pose Detection, Firebase) no longer crash on startup or first use.
