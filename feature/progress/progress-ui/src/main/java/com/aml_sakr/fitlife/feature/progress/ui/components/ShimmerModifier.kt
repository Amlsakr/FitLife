package com.aml_sakr.fitlife.feature.progress.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun rememberShimmerBrush(): Brush {
    val baseColor = MaterialTheme.colorScheme.onSurface
    val shimmerColors = listOf(
        baseColor.copy(alpha = 0.1f),
        baseColor.copy(alpha = 0.05f),
        baseColor.copy(alpha = 0.1f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnimation.value, y = translateAnimation.value)
    )
}
