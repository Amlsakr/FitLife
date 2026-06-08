# Room + Firestore Offline Sync Spike Report

Generated: 2026-06-08
Story: SETUP-006

## Summary

The branch now compiles and the full `./gradlew.bat test --no-daemon --console=plain` suite passes.

## What Was Validated

- Room sync entities and DAO logic compile cleanly.
- The sync coordinator now treats partial failures as failures instead of reporting an all-green batch.
- Firestore read failures are no longer masked as missing documents.
- The worker now resolves dependencies through Hilt entry points instead of nullable global singletons.
- The unrelated Gemini fallback file was removed from the spike scope.
- An Android test harness now compiles under `:core:core-data` and uses in-memory Room, Firestore configured explicitly for the local emulator, and WorkManager test execution.
- Firestore writes include a `serverUpdatedAt` server timestamp while preserving the client `lastModified` timestamp used for latest-timestamp-wins reconciliation.
- Production Firestore wiring no longer defaults to the local emulator unless `fitlife.firestore.useEmulator=true` is explicitly set.
- The `RoomFirestoreWorkManagerSyncInstrumentedTest` ran successfully on a connected real device with the host Firestore emulator exposed through `adb reverse tcp:8080 tcp:8080`.

## Current Limitation

The JVM benchmark harness still uses fakes for fast coordinator coverage. Emulator-backed validation now lives in Android instrumentation and requires a running Android test target plus Firestore emulator on `10.0.2.2:8080`.

## Recommendation

Use the Android harness as the spike evidence path before promoting this pattern into the production sync story. Keep the fake JVM tests for deterministic conflict/state-transition regression coverage.
