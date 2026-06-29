# Story 4.6: Equipment Rerouting Bottom Sheet (Gemini API)

Status: review

## Story

As a FitLife user,
I want a one-tap unavailable option during a session so I can replace exercises when I lack equipment,
so that I can continue my workout without interruption.

## Acceptance Criteria

1. Session screen includes an "unavailable" action button for the current exercise.
2. Bottom sheet presents 3 Gemini-generated alternatives when "unavailable" is tapped.
3. Selecting an alternative replaces the current exercise and logs the reroute to analytics.
4. Fallback alternatives are loaded locally if Gemini API is unavailable or times out (5s limit).
5. Bottom sheet follows Material3 `ModalBottomSheet` pattern with three `AlternativeCard`s.
6. Each `AlternativeCard` shows exercise image, name, and "Select" CTA button.
7. Equipment rerouting event is logged to Firebase Analytics: `equipment_rerouted` with `original` and `alternative` parameters.
8. No regression on existing session functionality (pose detection, fatigue, lighting fallback, skeleton overlay).

## Tasks / Subtasks

- [x] Task 1: Extend ActiveSessionState for Equipment Rerouting (AC: 1, 2)
  - [x] Add `currentExerciseName: String?` to `ActiveSessionState`
  - [x] Add `alternatives: List<ExerciseAlternative>` to `ActiveSessionState`
  - [x] Add `isEquipmentSheetLoading: Boolean` to `ActiveSessionState`
  - [x] Add `isEquipmentSheetVisible: Boolean` to `ActiveSessionState`
  - [x] Create `ExerciseAlternative` data class in `:feature:session:session-domain`
- [x] Task 2: Create Equipment Rerouting Use Case (AC: 2, 4)
  - [x] Create `RerouteEquipmentUseCase` in `:feature:session:session-domain`
  - [x] Inject `IEquipmentReroutingRepository` interface
  - [x] Implement operator `invoke(exerciseName: String, availableEquipment: Set<String>): Result<List<ExerciseAlternative>, NetworkErrors>`
  - [x] Add 5-second timeout using `withTimeout(5000)`
  - [x] Implement local fallback: if Gemini fails, return cached alternatives from Room or hardcoded defaults
- [x] Task 3: Create Equipment Rerouting Repository Interface (AC: 2, 4)
  - [x] Define `IEquipmentReroutingRepository` in `:feature:session:session-domain`
  - [x] Method: `suspend fun fetchAlternatives(exerciseName: String, equipment: Set<String>): Result<List<ExerciseAlternative>, NetworkErrors>`
- [x] Task 4: Implement Gemini-based Equipment Rerouting Repository (AC: 2, 4)
  - [x] Create `GeminiEquipmentReroutingRepository` in `:feature:session:session-data`
  - [x] Reuse existing `GeminiApiService` from workout module via dependency injection
  - [x] Create `EquipmentGeminiApiService` interface in `:feature:session:session-data` that mirrors Gemini HTTP contract
  - [x] Create `EquipmentReroutingPromptBuilder` to generate structured JSON prompts for alternatives
  - [x] Parse Gemini response into `ExerciseAlternative` list
  - [x] Handle API failures with exponential backoff (1s, 2s, 4s) up to 3 attempts
- [x] Task 5: Create Equipment Rerouting DAO and Entity (AC: 4)
  - [x] Create `EquipmentReroutingEntity` Room entity in `:feature:session:session-data`
  - [x] Create `EquipmentReroutingDao` with methods: `insertAlternatives`, `getAlternativesForExercise`
  - [x] Cache Gemini responses locally for offline fallback
- [x] Task 6: Build Equipment Unavailable Bottom Sheet UI (AC: 1, 5, 6)
  - [x] Create `EquipmentUnavailableBottomSheet.kt` in `:feature:session:session-ui/src/main/java/.../components/`
  - [x] Use Material3 `ModalBottomSheet` composable
  - [x] Create `AlternativeCard` composable with elevation, primary border, image, name, and "Select" button
  - [x] Add "Equipment Unavailable" button to `ActiveSessionCameraRoute.kt`
  - [x] Wire button to trigger bottom sheet visibility
- [x] Task 7: Integrate with ActiveSessionViewModel (AC: 1, 2, 3, 7)
  - [x] Add `onEquipmentUnavailable` event to `ActiveSessionEvent`
  - [x] Add `onAlternativeSelected(alternative: ExerciseAlternative)` event
  - [x] Handle events in ViewModel: call `RerouteEquipmentUseCase`, update state
  - [x] Log `equipment_rerouted` analytics event with original and alternative names
- [x] Task 8: Add Analytics Event (AC: 7)
  - [x] Add `equipment_rerouted` event to analytics taxonomy with parameters: `original: String`, `alternative: String`
- [x] Task 9: Testing (AC: 8)
  - [x] Unit tests for `RerouteEquipmentUseCase`
  - [x] Unit tests for `EquipmentReroutingPromptBuilder`
  - [x] Integration test for `GeminiEquipmentReroutingRepository` with MockWebServer
  - [x] UI test for `EquipmentUnavailableBottomSheet` rendering
  - [x] Verify no regression on existing session tests

## Dev Notes

### Architecture Compliance

- **Module Structure**: New code goes in `:feature:session:session-domain` (use case, interface), `:feature:session:session-data` (repository implementation, Room), `:feature:session:session-ui` (bottom sheet, button)
- **MVI Pattern**: Extend `ActiveSessionState`, `ActiveSessionEvent`, `ActiveSessionAction` in `ActiveSessionContracts.kt`
- **Clean Architecture**: Domain layer defines `IEquipmentReroutingRepository` interface; data layer implements it
- **Gemini API Isolation**: Session modules CANNOT depend on workout modules per architecture dependency graph. Create `EquipmentGeminiApiService` interface in `:feature:session:session-data` that mirrors Gemini HTTP contract. Do NOT import from workout module.
- **Dependency Direction**: Session modules depend only on core modules and their own sibling layers

### ExerciseAlternative Data Class

```kotlin
data class ExerciseAlternative(
    val exerciseId: String,
    val name: String,
    val description: String,
    val muscleGroups: List<String>,
    val equipmentRequired: String,
    val difficulty: ExerciseDifficulty,
    val lottieAssetPath: String?,
    val defaultSets: Int,
    val defaultReps: Int
)
```

### Gemini Prompt Template

```json
{
  "exercise": "Barbell Squat",
  "equipment_needed": "Barbell, Squat Rack",
  "available_equipment": ["Dumbbells", "Resistance Bands", "Bodyweight"],
  "fitness_level": "INTERMEDIATE",
  "muscle_groups": ["Quadriceps", "Glutes", "Hamstrings"],
  "request": "Suggest 3 alternative exercises that target the same muscle groups using only the available equipment"
}
```

**Expected Gemini Response Schema:**
```json
{
  "alternatives": [
    {
      "name": "Goblet Squat",
      "description": "Squat holding a dumbbell at chest level",
      "equipment_required": "Dumbbell",
      "muscle_groups": ["Quadriceps", "Glutes"],
      "difficulty": "INTERMEDIATE"
    }
  ]
}
```

### Room Entity Schema

```kotlin
@Entity(tableName = "equipment_rerouting_cache")
data class EquipmentReroutingEntity(
    @PrimaryKey val exerciseName: String,
    val alternativesJson: String,  // JSON array of ExerciseAlternative
    val fetchedAt: Long,
    val expiresAt: Long  // fetchedAt + 24 hours
)
```

### Technical Guardrails

1. **Gemini API Isolation**: Session modules CANNOT depend on workout modules. Create `EquipmentGeminiApiService` interface in `:feature:session:session-data` that mirrors the HTTP contract:
   ```kotlin
   interface EquipmentGeminiApiService {
       suspend fun generateAlternatives(
           prompt: String,
           apiKey: String,
           configuration: GeminiConfiguration
       ): GeminiApiCallResult
   }
   ```
   Implement with `HttpURLConnection` similar to `HttpGeminiApiService` in workout module. Use BuildConfig for API key.

2. **Timeout Handling**: Use `withTimeout(5000)` for Gemini calls as per architecture doc Section 10. If timeout occurs, load local fallback alternatives.

3. **Local Fallback**: Create `assets/fallback_equipment_alternatives.json` at module root (`session-ui/src/main/assets/`) with hardcoded alternatives. Map exercises to alternatives based on equipment requirements.

4. **Bottom Sheet Pattern**: Use Material3 `ModalBottomSheet` from `androidx.compose.material3`. The UX spec describes: "ModalBottomSheet with three AlternativeCards (elevation, primary border). Each shows image, name, Select CTA."

5. **Analytics Logging**: Use existing `AnalyticsLogger` interface from `:core:core-data`. Log `equipment_rerouted` event with `original` and `alternative` parameters.

6. **State Updates**: When alternative is selected:
   - Update `currentExerciseName` in state
   - Clear `alternatives` list
   - Dismiss bottom sheet
   - Log analytics event
   - Emit `Announce` action with TTS message

7. **Current Exercise Tracking**: The `ActiveSessionState` must track which exercise is currently active. Add `currentExerciseIndex: Int = 0` and `currentExerciseName: String? = null`. Exercise progression is handled by the session flow (manual or rep-count based).

8. **Hilt Module**: Create `EquipmentReroutingModule.kt` in `session-data/src/main/java/.../di/` to bind:
   - `EquipmentGeminiApiService` → `HttpEquipmentGeminiApiService`
   - `IEquipmentReroutingRepository` → `GeminiEquipmentReroutingRepository`
   - `EquipmentReroutingDao` from Room database

### Previous Story Intelligence (from session-005)

**Apply these learnings to session-006:**

| Session-005 Finding | Application to Session-006 |
|---------------------|----------------------------|
| Hardcoded colors bypass theme | Use `MaterialTheme.colorScheme` for bottom sheet and card colors |
| Performance bottleneck: dp conversion in draw loop | Avoid recomposition allocations in `AlternativeCard` - pre-calculate dimensions |
| Division by zero risk | Validate equipment list is non-empty before API call |
| Conflated channel pattern | Consider using `Channel.CONFLATED` for equipment API responses |
| TTS Manager cleanup race | Ensure proper cleanup in bottom sheet dismiss callback |
| Review findings: mocked verification issues | Use precise mock verification in tests |

**Code Patterns Established:**
- Utility classes go in `session-ui/src/main/java/.../utils/`
- Use `kotlinx-coroutines-test` and `MainDispatcherRule` for ViewModel tests
- Use `Channel.CONFLATED` for preventing frame flooding

### File Structure Requirements

#### New Files
- `feature/session/session-domain/src/main/java/com/aml_sakr/fitlife/feature/session/domain/equipment/ExerciseAlternative.kt`
- `feature/session/session-domain/src/main/java/com/aml_sakr/fitlife/feature/session/domain/equipment/IEquipmentReroutingRepository.kt`
- `feature/session/session-domain/src/main/java/com/aml_sakr/fitlife/feature/session/domain/equipment/RerouteEquipmentUseCase.kt`
- `feature/session/session-data/src/main/java/com/aml_sakr/fitlife/feature/session/data/equipment/GeminiEquipmentReroutingRepository.kt`
- `feature/session/session-data/src/main/java/com/aml_sakr/fitlife/feature/session/data/equipment/EquipmentReroutingPromptBuilder.kt`
- `feature/session/session-data/src/main/java/com/aml_sakr/fitlife/feature/session/data/equipment/EquipmentReroutingEntity.kt`
- `feature/session/session-data/src/main/java/com/aml_sakr/fitlife/feature/session/data/equipment/EquipmentReroutingDao.kt`
- `feature/session/session-data/src/main/java/com/aml_sakr/fitlife/feature/session/data/equipment/EquipmentGeminiApiService.kt`
- `feature/session/session-data/src/main/java/com/aml_sakr/fitlife/feature/session/data/equipment/HttpEquipmentGeminiApiService.kt`
- `feature/session/session-data/src/main/java/com/aml_sakr/fitlife/feature/session/data/equipment/EquipmentReroutingModels.kt`
- `feature/session/session-data/src/main/java/com/aml_sakr/fitlife/feature/session/data/di/EquipmentReroutingModule.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/components/EquipmentUnavailableBottomSheet.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/components/AlternativeCard.kt`
- `feature/session/session-ui/src/main/assets/fallback_equipment_alternatives.json`

#### Updated Files
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/ActiveSessionContracts.kt` (add state fields, events)
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/ActiveSessionViewModel.kt` (handle new events)
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/preview/ActiveSessionCameraRoute.kt` (add button, bottom sheet)

### Testing Standards

- **Unit Tests**: JUnit4 with `kotlinx-coroutines-test` for coroutine testing
- **Repository Tests**: Use MockWebServer for Gemini API integration tests
- **UI Tests**: Use AndroidX Compose UI test APIs aligned through Compose BOM
- **Mocking**: Use Mockk or Mockito for mocking dependencies
- **Coverage**: Test happy path, timeout, API failure, local fallback scenarios

#### Required Test Files
- `RerouteEquipmentUseCaseTest.kt` - Unit tests for use case with timeout, failure, fallback
- `GeminiEquipmentReroutingRepositoryTest.kt` - Integration test with MockWebServer
- `EquipmentReroutingPromptBuilderTest.kt` - Unit tests for prompt generation
- `EquipmentUnavailableBottomSheetTest.kt` - Compose UI test for bottom sheet rendering
- `ActiveSessionViewModelEquipmentTest.kt` - ViewModel event handling tests

#### Test Scenarios
1. **Happy Path**: Gemini returns 3 alternatives, user selects one
2. **Timeout**: Gemini call exceeds 5s, fallback to local alternatives
3. **API Failure**: Gemini returns error, fallback to cached or local alternatives
4. **Exponential Backoff**: Verify retry logic (1s, 2s, 4s) up to 3 attempts
5. **Local Fallback**: No network, load from `fallback_equipment_alternatives.json`
6. **Bottom Sheet State**: Loading → Success/Error → Sheet visibility transitions
7. **Analytics Logging**: Verify `equipment_rerouted` event with correct parameters
8. **No Regression**: Existing session tests pass (pose, fatigue, lighting, skeleton)

### References

- [Source: _bmad-output/project-context.md] - Critical rules and patterns for AI agents
- [Source: _bmad-output/planning-artifacts/fitlife-prd-v1.md#5.12-dynamic-equipment-rerouting]
- [Source: _bmad-output/planning-artifacts/fitlife-architecture-v1.md#10-gemini-api-integration-flow]
- [Source: docs/fitlife-ux-spec-v1.md#179-equipment-unavailable-bottom-sheet]
- [Source: docs/fitlife-stories-v1.md#481-session-006]
- [Source: feature/workout/workout-data/src/main/java/.../gemini/HttpGeminiApiService.kt] - Reference for HTTP implementation pattern
- [Source: feature/session/session-ui/src/main/java/.../ActiveSessionContracts.kt]
- [Source: feature/session/session-ui/src/main/java/.../ActiveSessionViewModel.kt]

## Dev Agent Record

### Agent Model Used

### Debug Log References

### Completion Notes List

### File List