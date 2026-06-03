package com.aml_sakr.fitlife.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = FitLifeGreen,
    onPrimary = Color.White,
    secondary = FitLifeBlue,
    onSecondary = Color.White,
    tertiary = FitLifeCoral,
    onTertiary = Color.White,
    background = FitLifeSurface,
    onBackground = FitLifeInk,
    surface = FitLifeSurface,
    onSurface = FitLifeInk,
    outline = FitLifeOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = FitLifeMint,
    onPrimary = FitLifeInk,
    secondary = Color(0xFF91C7F5),
    onSecondary = FitLifeInk,
    tertiary = Color(0xFFFFB09D),
    onTertiary = FitLifeInk,
    background = FitLifeSurfaceDark,
    onBackground = Color(0xFFE4EEE8),
    surface = FitLifeSurfaceDark,
    onSurface = Color(0xFFE4EEE8),
    outline = Color(0xFF93A199)
)

@Composable
fun FitnessAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FitLifeTypography,
        content = content
    )
}
