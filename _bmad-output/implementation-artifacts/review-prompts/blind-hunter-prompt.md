# Role: Blind Hunter (Adversarial Code Reviewer)

You are an elite adversarial code reviewer. You receive the diff of a change and must find bugs, security risks, architectural violations, and code smells. You have NO context about the project other than this diff.

## The Changes

```kotlin
// ActiveSessionContracts.kt
data class ActiveSessionState(
    val latestPoseData: PoseData? = null,
    val currentExerciseName: String? = null,
    val alternatives: List<ExerciseAlternative> = emptyList(),
    val isEquipmentSheetLoading: Boolean = false,
    val isEquipmentSheetVisible: Boolean = false,
    // ...
) : UIState

sealed interface ActiveSessionEvent : UIEvent {
    // ...
    data object OnEquipmentUnavailable : ActiveSessionEvent
    data class OnAlternativeSelected(val alternative: ExerciseAlternative) : ActiveSessionEvent
    data object DismissEquipmentSheet : ActiveSessionEvent
}

// ActiveSessionViewModel.kt
@HiltViewModel
class ActiveSessionViewModel @Inject constructor(
    // ...
    private val rerouteEquipmentUseCase: RerouteEquipmentUseCase
) : BaseMviViewModel<ActiveSessionState, ActiveSessionEvent, ActiveSessionAction>(
    ActiveSessionState()
) {
    // ...
    private fun handleEquipmentUnavailable() {
        setState { copy(isEquipmentSheetVisible = true, isEquipmentSheetLoading = true, alternatives = emptyList()) }
        viewModelScope.launch {
            val result = rerouteEquipmentUseCase(
                exerciseName = state.value.currentExerciseName ?: "Current Exercise",
                availableEquipment = emptySet()
            )
            when (result) {
                is Result.Success -> {
                    setState { copy(isEquipmentSheetLoading = false, alternatives = result.value) }
                }
                is Result.Failure -> {
                    setState { copy(isEquipmentSheetLoading = false, error = Exception(result.error.message)) }
                }
            }
        }
    }

    private fun handleAlternativeSelected(alternative: ExerciseAlternative) {
        val original = state.value.currentExerciseName ?: "Unknown"
        setState { 
            copy(
                currentExerciseName = alternative.name,
                isEquipmentSheetVisible = false,
                alternatives = emptyList()
            ) 
        }
        analyticsLogger.logEvent("equipment_rerouted", mapOf("original" to original, "alternative" to alternative.name))
        sendAction(ActiveSessionAction.Announce("Alternative selected: ${alternative.name}..."))
    }
}

// RerouteEquipmentUseCase.kt
class RerouteEquipmentUseCase @Inject constructor(
    private val repository: IEquipmentReroutingRepository
) {
    suspend operator fun invoke(
        exerciseName: String,
        availableEquipment: Set<String>
    ): Result<List<ExerciseAlternative>, NetworkErrors> {
        return try {
            withTimeout(5000) {
                repository.fetchAlternatives(exerciseName, availableEquipment)
            }
        } catch (e: TimeoutCancellationException) {
            Result.Failure(NetworkErrors.Timeout)
        } catch (e: Exception) {
            Result.Failure(NetworkErrors.UnknownApiError)
        }
    }
}

// GeminiEquipmentReroutingRepository.kt
class GeminiEquipmentReroutingRepository @Inject constructor(
    private val apiService: EquipmentGeminiApiService,
    private val promptBuilder: EquipmentReroutingPromptBuilder,
    private val dao: EquipmentReroutingDao,
    private val config: SessionGeminiConfiguration,
    private val gson: Gson
) : IEquipmentReroutingRepository {

    override suspend fun fetchAlternatives(
        exerciseName: String,
        equipment: Set<String>
    ): Result<List<ExerciseAlternative>, NetworkErrors> {
        val cached = dao.getAlternativesForExercise(exerciseName)
        if (cached != null && cached.expiresAt > System.currentTimeMillis()) {
            val type = object : TypeToken<List<ExerciseAlternative>>() {}.type
            val alternatives: List<ExerciseAlternative> = gson.fromJson(cached.alternativesJson, type)
            return Result.Success(alternatives)
        }

        val request = promptBuilder.buildPrompt(exerciseName, equipment)
        val apiConfig = EquipmentGeminiConfiguration(...)

        var lastError: NetworkErrors = NetworkErrors.UnknownApiError
        var retryDelay = 1000L

        repeat(3) { attempt ->
            val result = apiService.generateAlternatives(request, config.apiKey, apiConfig)
            if (result.httpStatusCode in 200..299) {
                val alternatives = parseAlternatives(result.responseBody)
                cacheAlternatives(exerciseName, alternatives)
                return Result.Success(alternatives)
            } else {
                lastError = mapHttpError(result.httpStatusCode)
                if (attempt < 2) {
                    delay(retryDelay)
                    retryDelay *= 2
                }
            }
        }

        if (cached != null) {
            val type = object : TypeToken<List<ExerciseAlternative>>() {}.type
            val alternatives: List<ExerciseAlternative> = gson.fromJson(cached.alternativesJson, type)
            return Result.Success(alternatives)
        }

        return Result.Failure(lastError)
    }

    private fun parseAlternatives(json: String): List<ExerciseAlternative> {
        return try {
            val response = gson.fromJson(json, GeminiAlternativesResponse::class.java)
            response.alternatives.map { draft ->
                ExerciseAlternative(
                    exerciseId = UUID.randomUUID().toString(),
                    name = draft.name,
                    description = draft.description,
                    muscleGroups = draft.muscle_groups,
                    equipmentRequired = draft.equipment_required,
                    difficulty = mapDifficulty(draft.difficulty),
                    lottieAssetPath = null,
                    defaultSets = 3,
                    defaultReps = 12
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

// HttpEquipmentGeminiApiService.kt
class HttpEquipmentGeminiApiService @Inject constructor(
    private val gson: Gson,
    private val baseUrl: String = "https://generativelanguage.googleapis.com"
) : EquipmentGeminiApiService {
    override suspend fun generateAlternatives(...) = withContext(Dispatchers.IO) {
        val url = URL(...)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = configuration.timeoutMillis.toInt()
            readTimeout = configuration.timeoutMillis.toInt()
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("x-goog-api-key", apiKey)
        }
        try {
            val body = gson.toJson(request).toByteArray(Charsets.UTF_8)
            connection.outputStream.use { it.write(body) }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader()?.use { it.readText() } ?: ""
            EquipmentGeminiApiCallResult(status, response, response.length)
        } catch (e: Exception) {
            EquipmentGeminiApiCallResult(-1, e.message ?: "Unknown", 0)
        } finally {
            connection.disconnect()
        }
    }
}

// EquipmentUnavailableBottomSheet.kt
@Composable
fun EquipmentUnavailableBottomSheet(
    alternatives: List<ExerciseAlternative>,
    isLoading: Boolean,
    onAlternativeSelected: (ExerciseAlternative) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 32.dp, start = 16.dp, end = 16.dp).fillMaxWidth()) {
            Text(text = "Equipment Unavailable?", style = MaterialTheme.typography.headlineSmall)
            // ...
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(alternatives) { alternative ->
                        AlternativeCard(alternative = alternative, onSelect = { onAlternativeSelected(alternative) })
                    }
                }
            }
        }
    }
}

// AlternativeCard.kt
@Composable
fun AlternativeCard(alternative: ExerciseAlternative, onSelect: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = alternative.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = "Equipment: ${alternative.equipmentRequired}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            Text(text = alternative.description, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
            Button(onClick = onSelect, modifier = Modifier.fillMaxWidth()) { Text("Select") }
        }
    }
}
```

## Instructions

1. Review the diff for bugs, logic errors, architectural violations, and code smells.
2. Focus on:
    - Thread safety and coroutine usage.
    - Resource management (HttpURLConnection).
    - Error handling (parsing, network failures).
    - UI performance (recomposition, layout).
    - Hardcoded values or "magic" strings.
3. Present your findings in a structured Markdown list. Indicate the file and line/block for each finding.
