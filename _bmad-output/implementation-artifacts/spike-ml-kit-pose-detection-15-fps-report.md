# ML Kit Pose Detection 15 FPS Spike Report

Status: pass

Date: 2026-06-07
Story: SETUP-004 - Technical Spike: ML Kit Pose Detection at 15 FPS

## Decision

ML Kit Pose Detection passed the SETUP-004 performance spike on a real OPPO Reno 14-class device using CameraX `ImageAnalysis` and the base ML Kit Pose Detection SDK in stream mode.

Recommendation: keep pose feedback in v1.0 and promote the spike harness carefully through the production session stories. The result does not remove the need for later broader QA on lower-end Snapdragon 6xx-class devices, but this physical mid-range run satisfies the story's 15 FPS go/no-go threshold.

## Device And Environment

| Field | Value |
| --- | --- |
| Device id | `AIUWI7BUWWKFFQ8L` |
| Manufacturer | OPPO |
| Model | `CPH2737` |
| Market name | OPPO Reno 14, user-provided |
| Processor | MediaTek Dimensity 8350, user-provided |
| Android API level | 36 |
| Hardware | `mt6897` |
| Physical device | Yes |
| Representative mid-range device | Yes |
| Power | USB powered, charging |
| Battery after run | 31% |
| Battery temperature after run | 38.7 C |
| Thermal status after run | 2 |
| Current CPU/GPU temperature after run | 47.7 C |
| Current skin temperature after run | 40.7 C |
| Lighting | Good lighting, user-framed with preview |
| Subject visibility | Full-body pose detected in 97.6% of frames |

## Harness Configuration

| Field | Value |
| --- | --- |
| Camera API | CameraX `ImageAnalysis` |
| Camera lens | Back camera |
| Analysis resolution | 640x480 |
| Output format | YUV_420_888 |
| Backpressure strategy | `ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST` |
| ML Kit SDK | `com.google.mlkit:pose-detection:18.0.0-beta5` |
| ML Kit mode | `PoseDetectorOptions.STREAM_MODE` |
| CameraX version | 1.6.1 |
| Preview | Instrumentation-only `PreviewView` used for framing |

## Benchmark Result

Final successful run from device log:

```text
POSE_BENCHMARK_RESULT outcome=Pass elapsedMs=310106 processed=9008 avgFps=29.05 p50Ms=26 p95Ms=36 errors=0 poseDetectedRatio=0.976 avgLandmarks=32.2 reasons=Representative 5-minute run sustained at least 15 FPS without invalidating runtime conditions.
```

| Metric | Result |
| --- | --- |
| Outcome | Pass |
| Elapsed time | 310,106 ms |
| Required measured duration | At least 300,000 ms |
| Processed frames | 9,008 |
| Average processed FPS | 29.05 |
| Required FPS | >= 15 |
| p50 processing latency | 26 ms |
| p95 processing latency | 36 ms |
| Error count | 0 |
| Pose-detected frame ratio | 0.976 |
| Average visible landmarks | 32.2 |
| App crash | No |
| Analyzer backlog observed | No |
| Dropped/ignored frame behavior | CameraX was configured with `STRATEGY_KEEP_ONLY_LATEST`, so stale frames were dropped instead of queued when analysis could not keep up; no analyzer backlog was observed during the successful run. |
| Unclosed frames observed | No |
| Preview responsive | Yes |
| Thermal throttling invalidation | No |

## Earlier Invalid Runs

Two earlier physical runs processed frames successfully but were intentionally rejected as inconclusive because the camera was not framed on a full-body subject:

- Run 1: 22.40 FPS average, p50 36 ms, p95 66 ms, zero errors, pose-detected ratio 0.066.
- Run 2: 20.04 FPS average, p50 44 ms, p95 71 ms, zero errors, pose-detected ratio 0.000.

An instrumentation-only preview activity was then added so the device could be aimed correctly before the final run.

## Verification Completed

- `./gradlew.bat :feature:session:session-domain:test --no-daemon --console=plain` passed.
- `./gradlew.bat :feature:session:session-data:compileDebugKotlin :feature:session:session-data:assembleDebugAndroidTest --no-daemon --console=plain` passed.
- Physical-device instrumentation run on OPPO `CPH2737` passed.
- Initial parallel `test`/`lint` validation failed due Gradle/Kotlin cache locking and resource merge file contention, not code errors.
- `./gradlew.bat --stop; ./gradlew.bat test --no-daemon --console=plain; ./gradlew.bat lint --no-daemon --console=plain` passed sequentially.
