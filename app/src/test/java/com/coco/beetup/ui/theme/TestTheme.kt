package com.coco.beetup.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val testLightColorScheme =
    lightColorScheme(
        primary = Color(0xFF7F5700),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFF5AD18),
        onPrimaryContainer = Color(0xFF654400),
        secondary = Color(0xFF7B0047),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFF9E1C60),
        onSecondaryContainer = Color(0xFFFFB4CF),
        tertiary = Color(0xFF566500),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFAFC541),
        onTertiaryContainer = Color(0xFF445000),
        background = Color(0xFFFFF8F3),
        onBackground = Color(0xFF211B11),
        surface = Color(0xFFFFF8F3),
        onSurface = Color(0xFF211B11),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF))

@Composable
fun TestAppTheme(content: @Composable () -> Unit) {
  MaterialTheme(colorScheme = testLightColorScheme, typography = AppTypography, content = content)
}
