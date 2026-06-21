package com.aml_sakr.fitlife.feature.onboarding.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aml_sakr.fitlife.core.ui.theme.FitLifeBlue
import com.aml_sakr.fitlife.core.ui.theme.FitLifeCoral
import com.aml_sakr.fitlife.core.ui.theme.FitLifeDimens
import com.aml_sakr.fitlife.core.ui.theme.FitnessAppTheme
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessLevel

@Composable
fun WelcomeLevelRoute(
    viewModel: WelcomeLevelViewModel,
    onNavigateToBeginner: () -> Unit,
    onNavigateToIntermediate: () -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.actions.collect { action ->
            when (action) {
                WelcomeLevelAction.NavigateToBeginner -> onNavigateToBeginner()
                WelcomeLevelAction.NavigateToIntermediate -> onNavigateToIntermediate()
                is WelcomeLevelAction.ShowMessage -> Unit
            }
        }
    }

    WelcomeLevelScreen(
        state = state,
        onBack = onBack,
        onCardSelected = { viewModel.onEvent(WelcomeLevelEvent.SelectLevel(it)) },
        onContinue = { viewModel.onEvent(WelcomeLevelEvent.ContinuePressed) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeLevelScreen(
    state: WelcomeLevelState,
    onBack: () -> Unit,
    onCardSelected: (FitnessLevel) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isContinueEnabled =
        state.selectedLevel != null && state.isSelectionSaved && !state.isLoading

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "FitLife AI",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    Surface(
                        onClick = onBack,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        shadowElevation = 0.dp,
                        modifier = Modifier
                            .padding(start = FitLifeDimens.SpaceSm)
                            .size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "<",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
                tonalElevation = 4.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = FitLifeDimens.SpaceLg,
                            top = FitLifeDimens.SpaceMd,
                            end = FitLifeDimens.SpaceLg,
                            bottom = FitLifeDimens.SpaceLg
                        ),
                    verticalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceSm)
                ) {
                    Button(
                        onClick = onContinue,
                        enabled = isContinueEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 64.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.outlineVariant,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Continue",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = FitLifeDimens.SpaceLg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = FitLifeDimens.SpaceLg, bottom = 140.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceLg)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceSm),
                    modifier = Modifier.animateContentSize()
                ) {
                    Text(
                        text = "Welcome to FitLife",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Select your fitness level to personalize your journey.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                if (state.errorMessage != null) {
                    ErrorBanner(message = state.errorMessage)
                }

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val stacked = maxWidth < 600.dp
                    if (stacked) {
                        Column(verticalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceLg)) {
                            LevelCard(
                                level = FitnessLevel.Beginner,
                                title = "Beginner",
                                description = "New to fitness or returning after a long break. We'll start with the basics.",
                                isSelected = state.selectedLevel == FitnessLevel.Beginner,
                                onSelected = onCardSelected,
                                modifier = Modifier.fillMaxWidth()
                            )
                            LevelCard(
                                level = FitnessLevel.Intermediate,
                                title = "Intermediate",
                                description = "Regularly active and looking to level up performance with AI guidance.",
                                isSelected = state.selectedLevel == FitnessLevel.Intermediate,
                                onSelected = onCardSelected,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceLg)
                        ) {
                            LevelCard(
                                level = FitnessLevel.Beginner,
                                title = "Beginner",
                                description = "New to fitness or returning after a long break. We'll start with the basics.",
                                isSelected = state.selectedLevel == FitnessLevel.Beginner,
                                onSelected = onCardSelected,
                                modifier = Modifier.weight(1f)
                            )
                            LevelCard(
                                level = FitnessLevel.Intermediate,
                                title = "Intermediate",
                                description = "Regularly active and looking to level up performance with AI guidance.",
                                isSelected = state.selectedLevel == FitnessLevel.Intermediate,
                                onSelected = onCardSelected,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelCard(
    level: FitnessLevel,
    title: String,
    description: String,
    isSelected: Boolean,
    onSelected: (FitnessLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = when (level) {
        FitnessLevel.Beginner -> FitLifeBlue
        FitnessLevel.Intermediate -> FitLifeCoral
    }
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) accent else MaterialTheme.colorScheme.outlineVariant,
        label = "levelCardBorder"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) accent.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface,
        label = "levelCardBackground"
    )
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 6.dp else 1.dp,
        label = "levelCardElevation"
    )

    Card(
        modifier = modifier
            .heightIn(min = 430.dp)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = "Select $title"
                stateDescription = if (isSelected) "Selected" else "Not selected"
            }
            .clickable { onSelected(level) },
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(FitLifeDimens.SpaceLg),
            verticalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceLg)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                LevelBadge(
                    level = level,
                    modifier = Modifier.size(88.dp)
                )

                if (isSelected) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        tonalElevation = 0.dp,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = "\u2713",
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceSm)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            when (level) {
                FitnessLevel.Beginner -> BeginnerArtwork(modifier = Modifier.fillMaxWidth())
                FitnessLevel.Intermediate -> IntermediateArtwork(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun LevelBadge(
    level: FitnessLevel,
    modifier: Modifier = Modifier
) {
    Surface(
        color = when (level) {
            FitnessLevel.Beginner -> Color(0xFF5FD8F4)
            FitnessLevel.Intermediate -> Color(0xFFB3DBFF)
        },
        shape = CircleShape,
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = size.minDimension * 0.06f, cap = StrokeCap.Round)
            when (level) {
                FitnessLevel.Beginner -> {
                    drawCircle(
                        color = Color(0xFF0F5A6B),
                        radius = size.minDimension * 0.18f,
                        center = Offset(size.width * 0.54f, size.height * 0.42f),
                        style = stroke
                    )
                    drawLine(
                        color = Color(0xFF0F5A6B),
                        start = Offset(size.width * 0.50f, size.height * 0.63f),
                        end = Offset(size.width * 0.40f, size.height * 0.35f),
                        strokeWidth = stroke.width
                    )
                }

                FitnessLevel.Intermediate -> {
                    drawLine(
                        color = Color(0xFF0F5A6B),
                        start = Offset(size.width * 0.48f, size.height * 0.18f),
                        end = Offset(size.width * 0.30f, size.height * 0.52f),
                        strokeWidth = stroke.width
                    )
                    drawLine(
                        color = Color(0xFF0F5A6B),
                        start = Offset(size.width * 0.30f, size.height * 0.52f),
                        end = Offset(size.width * 0.54f, size.height * 0.52f),
                        strokeWidth = stroke.width
                    )
                    drawLine(
                        color = Color(0xFF0F5A6B),
                        start = Offset(size.width * 0.54f, size.height * 0.52f),
                        end = Offset(size.width * 0.42f, size.height * 0.82f),
                        strokeWidth = stroke.width
                    )
                }
            }
        }
    }
}

@Composable
private fun BeginnerArtwork(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(152.dp)
            .semantics { contentDescription = "Beginner illustration" }
            .heightIn(min = 152.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF8FAF7),
                        Color(0xFFE5EEEA)
                    )
                ),
                cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx())
            )

            drawRoundRect(
                color = Color.White.copy(alpha = 0.20f),
                topLeft = Offset(size.width * 0.09f, size.height * 0.10f),
                size = Size(size.width * 0.82f, size.height * 0.72f),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
            )

            val moundPath = Path().apply {
                fillType = PathFillType.NonZero
                moveTo(0f, size.height * 0.82f)
                cubicTo(
                    size.width * 0.18f, size.height * 0.66f,
                    size.width * 0.78f, size.height * 0.92f,
                    size.width, size.height * 0.73f
                )
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(moundPath, color = Color(0xFF9EB4BF))

            val stemX = size.width * 0.50f
            drawLine(
                color = Color(0xFF52A94A),
                start = Offset(stemX, size.height * 0.70f),
                end = Offset(stemX, size.height * 0.35f),
                strokeWidth = size.minDimension * 0.038f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF52A94A),
                start = Offset(stemX, size.height * 0.54f),
                end = Offset(size.width * 0.34f, size.height * 0.39f),
                strokeWidth = size.minDimension * 0.028f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF52A94A),
                start = Offset(stemX, size.height * 0.50f),
                end = Offset(size.width * 0.69f, size.height * 0.39f),
                strokeWidth = size.minDimension * 0.028f,
                cap = StrokeCap.Round
            )

            rotate(degrees = -22f, pivot = Offset(size.width * 0.38f, size.height * 0.33f)) {
                drawOval(
                    color = Color(0xFF73C15E),
                    topLeft = Offset(size.width * 0.29f, size.height * 0.22f),
                    size = Size(size.width * 0.18f, size.height * 0.27f)
                )
            }
            rotate(degrees = 24f, pivot = Offset(size.width * 0.66f, size.height * 0.30f)) {
                drawOval(
                    color = Color(0xFF73C15E),
                    topLeft = Offset(size.width * 0.58f, size.height * 0.20f),
                    size = Size(size.width * 0.18f, size.height * 0.25f)
                )
            }
            rotate(degrees = -18f, pivot = Offset(size.width * 0.50f, size.height * 0.50f)) {
                drawOval(
                    color = Color(0xFF69B84E),
                    topLeft = Offset(size.width * 0.45f, size.height * 0.41f),
                    size = Size(size.width * 0.12f, size.height * 0.18f)
                )
            }
        }
    }
}

@Composable
private fun IntermediateArtwork(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(152.dp)
            .semantics { contentDescription = "Intermediate illustration" }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF8FBFF),
                        Color(0xFFE7F2FF)
                    )
                ),
                cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx())
            )

            drawRoundRect(
                color = Color.White.copy(alpha = 0.22f),
                topLeft = Offset(size.width * 0.10f, size.height * 0.12f),
                size = Size(size.width * 0.80f, size.height * 0.70f),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
            )

            val streakBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF5AD8FF),
                    Color(0xFF0F73E2),
                    Color(0xFF5AD8FF)
                )
            )

            rotate(degrees = -24f, pivot = Offset(size.width * 0.55f, size.height * 0.50f)) {
                drawRoundRect(
                    brush = streakBrush,
                    topLeft = Offset(size.width * 0.22f, size.height * 0.23f),
                    size = Size(size.width * 0.68f, size.height * 0.14f),
                    cornerRadius = CornerRadius(18f, 18f)
                )
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2D90F5).copy(alpha = 0.90f),
                            Color(0xFF0F73E2).copy(alpha = 0.86f),
                            Color(0xFF2D90F5).copy(alpha = 0.90f)
                        )
                    ),
                    topLeft = Offset(size.width * 0.30f, size.height * 0.36f),
                    size = Size(size.width * 0.60f, size.height * 0.12f),
                    cornerRadius = CornerRadius(18f, 18f)
                )
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF5AD8FF).copy(alpha = 0.74f),
                            Color(0xFF1263D6).copy(alpha = 0.72f)
                        )
                    ),
                    topLeft = Offset(size.width * 0.36f, size.height * 0.49f),
                    size = Size(size.width * 0.52f, size.height * 0.10f),
                    cornerRadius = CornerRadius(18f, 18f)
                )
            }
        }
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(FitLifeDimens.SpaceMd),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomeLevelScreenPreview() {
    FitnessAppTheme {
        WelcomeLevelScreen(
            state = WelcomeLevelState(
                selectedLevel = FitnessLevel.Beginner,
                isSelectionSaved = true,
                isLoading = false,
                errorMessage = "Saved fitness level is invalid."
            ),
            onBack = {},
            onCardSelected = {},
            onContinue = {}
        )
    }
}
