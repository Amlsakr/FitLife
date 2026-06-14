package com.aml_sakr.fitlife.feature.auth.splash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aml_sakr.fitlife.core.ui.theme.FitLifeDimens
import com.aml_sakr.fitlife.core.ui.theme.FitnessAppTheme

@Composable
fun SplashRoute(
    viewModel: SplashViewModel,
    onNavigateToAuth: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.actions.collect { action ->
            when (action) {
                SplashAction.NavigateToAuth -> onNavigateToAuth()
                SplashAction.NavigateToOnboarding -> onNavigateToOnboarding()
                SplashAction.NavigateToHome -> onNavigateToHome()
                SplashAction.ShowRetryableFallback -> Unit
            }
        }
    }

    SplashScreen(
        state = state,
        onRetry = { viewModel.onEvent(SplashEvent.RetryStartupRoute) },
        modifier = modifier
    )
}

@Composable
fun SplashScreen(
    state: SplashState,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val backgroundBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surface
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        DottedBackground(
            dotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = FitLifeDimens.SpaceXl, vertical = FitLifeDimens.SpaceXl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BrandLockup()

            Spacer(modifier = Modifier.height(FitLifeDimens.SpaceXl))

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                    strokeWidth = 4.dp
                )
            } else {
                Spacer(modifier = Modifier.height(48.dp))
            }

            Spacer(modifier = Modifier.height(FitLifeDimens.SpaceLg))

            Text(
                text = if (state.hasRetryableError) {
                    "We couldn't finish preparing FitLife."
                } else {
                    "Initializing Arctic AI focus environment..."
                },
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.semantics {
                    liveRegion = LiveRegionMode.Polite
                }
            )

            if (state.hasRetryableError) {
                Spacer(modifier = Modifier.height(FitLifeDimens.SpaceMd))
                OutlinedButton(
                    onClick = onRetry,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Try again")
                }
            }
        }
    }
}

@Composable
private fun BrandLockup() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier
                .size(120.dp)
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                ),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)
            )
        ) {
            Box(
                modifier = Modifier.padding(FitLifeDimens.SpaceMd),
                contentAlignment = Alignment.Center
            ) {
                FitLifeBrandMark(
                    color = MaterialTheme.colorScheme.secondary,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(FitLifeDimens.SpaceLg))

        Text(
            text = "FitLife",
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(FitLifeDimens.SpaceSm))

        Text(
            text = "PRECISION PERFORMANCE",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.64f),
            style = MaterialTheme.typography.labelLarge.copy(
                letterSpacing = MaterialTheme.typography.labelLarge.fontSize * 0.16f
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FitLifeBrandMark(
    color: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val centerX = size.width * 0.42f
        val headRadius = size.minDimension * 0.095f
        val stroke = size.minDimension * 0.055f

        drawCircle(
            color = color,
            radius = headRadius,
            center = Offset(centerX, size.height * 0.22f)
        )
        drawLine(
            color = color,
            start = Offset(centerX, size.height * 0.36f),
            end = Offset(size.width * 0.35f, size.height * 0.72f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.39f, size.height * 0.48f),
            end = Offset(size.width * 0.67f, size.height * 0.39f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.38f, size.height * 0.49f),
            end = Offset(size.width * 0.18f, size.height * 0.61f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.35f, size.height * 0.72f),
            end = Offset(size.width * 0.17f, size.height * 0.84f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.35f, size.height * 0.72f),
            end = Offset(size.width * 0.59f, size.height * 0.82f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        val networkCenter = Offset(size.width * 0.7f, size.height * 0.36f)
        val nodes = listOf(
            Offset(size.width * 0.78f, size.height * 0.18f),
            Offset(size.width * 0.86f, size.height * 0.31f),
            Offset(size.width * 0.82f, size.height * 0.48f),
            Offset(size.width * 0.66f, size.height * 0.2f)
        )
        nodes.forEach { node ->
            drawLine(
                color = accentColor,
                start = networkCenter,
                end = node,
                strokeWidth = stroke * 0.35f,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = accentColor,
                radius = stroke * 0.55f,
                center = node,
                style = Stroke(width = stroke * 0.3f)
            )
        }
        drawCircle(
            color = accentColor,
            radius = stroke * 0.65f,
            center = networkCenter
        )
    }
}

@Composable
private fun DottedBackground(
    dotColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawDotGrid(dotColor = dotColor)
    }
}

private fun DrawScope.drawDotGrid(dotColor: Color) {
    val spacing = 24.dp.toPx()
    val radius = 1.25.dp.toPx()
    var y = 24.dp.toPx()
    while (y < size.height) {
        var x = 24.dp.toPx()
        while (x < size.width) {
            drawCircle(
                color = dotColor,
                radius = radius,
                center = Offset(x, y)
            )
            x += spacing
        }
        y += spacing
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    FitnessAppTheme {
        SplashScreen(SplashState())
    }
}
