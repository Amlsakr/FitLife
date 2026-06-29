# Story 4.5: Skeleton Overlay Canvas

Status: done

## Story

As a FitLife user,
I want a visual overlay showing my detected joints with confidence-based colors during a session,
so that I can see real-time feedback on how well the app is tracking my form.

## Acceptance Criteria

1. [x] `SkeletonOverlay` composable implemented in `:feature:session:session-ui` using `androidx.compose.foundation.Canvas`.
2. [x] Visualization draws dots (or small circles) for each joint detected in `PoseData`.
3. [x] Joint colors are mapped based on detection confidence:
    - **Cyan** (#00FFFF): High confidence (> 0.8) - "Good"
    - **Orange** (#FFA500): Medium confidence (0.5 < confidence <= 0.8) - "Uncertain"
    - **Red** (#FF0000): Low confidence (<= 0.5) - "Bad"
4. [x] Overlay updates in sync with the `PoseDetector` output (real-time stream).
5. [x] Coordinate Mapping: Joints are correctly mapped from the ML Kit image coordinate system to the Compose Canvas coordinate system, accounting for `FILL_CENTER` scaling and cropping used in `CameraPreview`.
6. [x] Isolation: The overlay only renders when the camera is active and NOT in audio-only mode.
7. [x] Performance: Drawing logic must be optimized to maintain ≥15 fps (using `drawCircle` and avoiding unnecessary allocations during recomposition).

## Tasks / Subtasks

- [x] Prepare Domain and Data for Mapping. (AC: 5)
  - [x] Update `PoseData` in `:feature:session:session-domain` to include source image dimensions (`sourceWidth`, `sourceHeight`).
  - [x] Update `MlKitPoseDetector` in `:feature:session:session-data` to populate these dimensions from `InputImage`.
- [x] Implement Skeleton UI Component. (AC: 1, 2, 3, 7)
  - [x] Create `SkeletonOverlay.kt` in `:feature:session:session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/components/`.
  - [x] Implement color mapping logic based on confidence.
  - [x] Implement `SkeletonOverlay` composable using `Canvas`.
- [x] Coordinate Mapping Logic. (AC: 5)
  - [x] Implement utility to map ML Kit coordinates (pixels) to Canvas coordinates (pixels) respecting `PreviewView.ScaleType.FILL_CENTER`.
- [x] Integrate with Active Session Screen. (AC: 4, 6)
  - [x] Update `ActiveSessionCameraRoute.kt` to include the `SkeletonOverlay` layer.
  - [x] Ensure overlay visibility is tied to `state.isAudioOnlyMode` and `state.isCameraActive`.
- [x] Testing.
  - [x] Verify joint colors correctly reflect confidence levels in manual testing.
  - [x] Verify skeleton aligns with the user's body in the camera preview across different screen aspect ratios.

### Review Findings (AI)

- [x] [Review][Patch] Missing Horizontal Mirroring for Front Camera — Add isMirrored flag to PoseCoordinateMapper.map() to flip X coordinate.
- [x] [Review][Patch] Hardcoded colors bypass FitnessAppTheme [feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/components/SkeletonOverlay.kt:43-47]
- [x] [Review][Patch] Performance bottleneck: dp conversion inside draw loop [feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/components/SkeletonOverlay.kt:53]
- [x] [Review][Patch] Division by zero risk in coordinate mapping [feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/utils/PoseCoordinateMapper.kt:23-26]
- [x] [Review][Patch] Logical error in PoseData.EMPTY confidence [feature/session/session-domain/src/main/java/com/aml_sakr/fitlife/feature/session/domain/pose/PoseData.kt:15]
- [x] [Review][Patch] TTS Manager cleanup race condition [feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/preview/ActiveSessionCameraRoute.kt:77]
- [x] [Review][Patch] Overly broad mock verification in tests [feature/session/session-ui/src/test/java/com/aml_sakr/fitlife/feature/session/ui/ActiveSessionViewModelTest.kt:41]
- [x] [Review][Patch] Redundant Canvas drawing for empty joints [feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/components/SkeletonOverlay.kt:25-26]
- [x] [Review][Defer] Potential pose data leak in state [feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/ActiveSessionViewModel.kt] — deferred, pre-existing

## Developer Context

### Architecture Compliance
- **Module**: `:feature:session:session-ui` for the UI component.
- **MVI Integration**: Uses `latestPoseData` from `ActiveSessionState`.
- **Clean Architecture**: `PoseData` update happens in `session-domain`.

### Technical Guardrails
- **Coordinate Mapping**: This is the most critical part. ML Kit returns coordinates in the image buffer's coordinate system (e.g., 640x480). `CameraPreview` displays this buffer using `FILL_CENTER` in a `Box` that fills the screen (e.g., 1080x2400).
  - You must calculate the scale factor: `max(screenWidth / imageWidth, screenHeight / imageHeight)`.
  - Calculate offsets to center the scaled image: `offsetX = (screenWidth - imageWidth * scale) / 2`, `offsetY = (screenHeight - imageHeight * scale) / 2`.
  - Map joint: `canvasX = imageX * scale + offsetX`, `canvasY = imageY * scale + offsetY`.
- **Mirroring**: If the front camera is used, ML Kit coordinates might need horizontal mirroring depending on how `CameraX` / `ML Kit` handles it. Currently, `CameraSelector.DEFAULT_BACK_CAMERA` is used in `CameraPreview.kt`, so mirroring is not required yet, but keep it in mind for future front-camera support.

### Dev Notes (from previous stories)
- `AnalyzePoseUseCase` now uses a conflated channel to prevent coroutine flooding (added in `ActiveSessionCameraRoute.kt`).
- The `CameraPreview` uses `ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST` and a target resolution of 640x480.

### File Structure Requirements
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/components/SkeletonOverlay.kt` (New)
- `feature/session/session-domain/src/main/java/com/aml_sakr/fitlife/feature/session/domain/pose/PoseData.kt` (Update)
- `feature/session/session-data/src/main/java/com/aml_sakr/fitlife/feature/session/data/pose/MlKitPoseDetector.kt` (Update)
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/preview/ActiveSessionCameraRoute.kt` (Update)
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/utils/PoseCoordinateMapper.kt` (New)

### References
- [Source: _bmad-output/planning-artifacts/fitlife-architecture-v1.md#7-ml-kit-pose-detection-pipeline]
- [Source: _bmad-output/planning-artifacts/epics.md#SESSION-005]
- [Source: feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/preview/CameraPreview.kt]
- [Source: feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/preview/ActiveSessionCameraRoute.kt]

## Dev Agent Record

### Agent Model Used
BMad Developer Agent (Gemini 2.0 Flash)

### Debug Log References
- [Target Resolution]: 640x480 (specified in `CameraPreview.kt`)
- [Pose Joint Enum]: `com.aml_sakr.fitlife.feature.session.domain.pose.PoseJoint`
- [Test Status]: Success (`PoseCoordinateMapperTest`, `ActiveSessionViewModelTest`)

### Completion Notes
- Implemented `SkeletonOverlay` using Compose `Canvas` for low-latency skeleton rendering.
- Created `PoseCoordinateMapper` utility to handle `FILL_CENTER` aspect-ratio scaling and centering.
- Updated `PoseData` and `MlKitPoseDetector` to propagate source image dimensions for accurate mapping.
- Integrated overlay into `ActiveSessionCameraRoute` with visibility logic tied to camera and audio-only states.
- Fixed a regression in `ActiveSessionViewModelTest` caused by missing dependencies in the constructor.

### File List
- `feature/session/session-domain/src/main/java/com/aml_sakr/fitlife/feature/session/domain/pose/PoseData.kt`
- `feature/session/session-data/src/main/java/com/aml_sakr/fitlife/feature/session/data/pose/MlKitPoseDetector.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/components/SkeletonOverlay.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/utils/PoseCoordinateMapper.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/preview/ActiveSessionCameraRoute.kt`
- `feature/session/session-ui/src/test/java/com/aml_sakr/fitlife/feature/session/ui/utils/PoseCoordinateMapperTest.kt`
- `feature/session/session-ui/src/test/java/com/aml_sakr/fitlife/feature/session/ui/ActiveSessionViewModelTest.kt`
