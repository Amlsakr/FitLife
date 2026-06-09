package com.aml_sakr.fitlife.feature.auth.ui.splash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aml_sakr.fitlife.core.ui.theme.FitLifeBlue
import com.aml_sakr.fitlife.core.ui.theme.FitLifeDimens
import com.aml_sakr.fitlife.core.ui.theme.FitnessAppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

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
        modifier = modifier
    )
}

@Composable
fun SplashScreen(
    state: SplashState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DottedBackground(
            dotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = FitLifeDimens.SpaceXl, vertical = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            BrandLockup()

            Spacer(modifier = Modifier.weight(1.25f))

            CircularProgressIndicator(
                modifier = Modifier.size(56.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                strokeWidth = 5.dp
            )

            Spacer(modifier = Modifier.height(FitLifeDimens.SpaceXl))

            Text(
                text = if (state.hasRetryableError) {
                    "Still preparing your startup route..."
                } else {
                    "Initializing FitLife focus environment..."
                },
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
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
                .size(116.dp)
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                ),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "FL",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(FitLifeDimens.SpaceXl))

        Text(
            text = "FitLife",
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(FitLifeDimens.SpaceSm))

        Text(
            text = "PRECISION PERFORMANCE",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.64f),
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center
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
    val spacing = 48.dp.toPx()
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
