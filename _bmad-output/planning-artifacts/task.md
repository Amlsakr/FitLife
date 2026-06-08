# Task Checklist - Room + Firestore Offline Sync Spike

- [ ] Add Room, WorkManager, and Firestore dependencies to [libs.versions.toml](file:///d:/LinkDevProject/FitLife/gradle/libs.versions.toml) and [:core:core-data](file:///d:/LinkDevProject/FitLife/core/core-data/build.gradle.kts)
- [ ] Implement local database models, DAO, and Database class under `:core:core-data`
- [ ] Implement connectivity and mock network simulator
- [ ] Implement remote Firestore database sync adapter (or abstract Firestore operations behind a sync client interface to keep it unit-testable)
- [ ] Implement WorkManager Sync Worker that triggers on network reconnection and handles conflict resolution (latest-wins)
- [ ] Create automated unit tests (for mapping, logic, and state transitions) and instrumented/local tests for database operations
- [ ] Run benchmark sync and produce spike report at `_bmad-output/implementation-artifacts/spike-room-firestore-offline-sync-report.md`
- [ ] Verify whole project build/test status with `./gradlew.bat test`
