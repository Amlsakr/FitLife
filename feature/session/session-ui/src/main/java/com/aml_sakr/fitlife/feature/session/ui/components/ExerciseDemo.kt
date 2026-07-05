package com.aml_sakr.fitlife.feature.session.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun ExerciseDemo(
    lottiePath: String?,
    totalReps: Int,
    modifier: Modifier = Modifier
) {
    var lastRepCount by remember { mutableIntStateOf(totalReps) }
    var pulseTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(totalReps) {
        if (totalReps > lastRepCount) {
            pulseTrigger++
        }
        lastRepCount = totalReps
    }

    val scale by animateFloatAsState(
        targetValue = if (pulseTrigger > 0) 1.2f else 1f,
        animationSpec = spring(dampingRatio = 0.5f),
        finishedListener = { if (pulseTrigger > 0) pulseTrigger = 0 },
        label = "PulseScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!lottiePath.isNullOrBlank()) {
            val compositionResult = rememberLottieComposition(LottieCompositionSpec.Asset(lottiePath))
            val composition by compositionResult
            val progress by animateLottieCompositionAsState(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                isPlaying = true
            )
            
            if (composition != null) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                )
            } else if (compositionResult.isFailure) {
                Placeholder()
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Placeholder()
        }
    }
}

@Composable
private fun Placeholder() {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
        contentDescription = "Exercise Placeholder",
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(48.dp)
    )
}

@Preview
@Composable
private fun ExerciseDemoPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ExerciseDemo(
                lottiePath = null,
                totalReps = 0
            )
        }
    }
}
