package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = EditorialAccentMedium,
    onPrimary = CharcoalDark,
    secondary = EditorialPurple,
    onSecondary = Color.White,
    tertiary = CollaborativeBlue,
    background = CharcoalDark,
    surface = CharcoalMedium,
    surfaceVariant = CharcoalMedium,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = EditorialPurple,
    onPrimary = Color.White,
    secondary = EditorialAccentLight,
    onSecondary = EditorialText,
    tertiary = CollaborativeBlue,
    background = EditorialBg,
    surface = Color.White,
    surfaceVariant = EditorialSurfaceVariant,
    onBackground = EditorialText,
    onSurface = EditorialText,
    outline = EditorialBorder
)

@Composable
fun MyApplicationTheme(
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
        typography = Typography,
        content = content
    )
}
