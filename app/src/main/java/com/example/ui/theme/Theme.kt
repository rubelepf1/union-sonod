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

private val LightColorScheme = lightColorScheme(
    primary = BdGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = BdGreenContainer,
    onPrimaryContainer = BdOnGreenContainer,
    secondary = BdRedAccent,
    onSecondary = Color.White,
    secondaryContainer = BdRedContainer,
    onSecondaryContainer = BdOnRedContainer,
    tertiary = GoldAccent,
    onTertiary = Color.White,
    tertiaryContainer = GoldContainer,
    background = SurfaceLight,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF0F4F1),
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = BdGreenLight,
    onPrimary = Color(0xFF003827),
    primaryContainer = BdGreenContainerDark,
    onPrimaryContainer = Color(0xFFA6F2D2),
    secondary = BdRedLight,
    onSecondary = Color(0xFF680010),
    secondaryContainer = Color(0xFF91001C),
    onSecondaryContainer = Color(0xFFFFDAD6),
    tertiary = Color(0xFFFBBF24),
    onTertiary = Color(0xFF452B00),
    background = SurfaceDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceCardDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF26332C),
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0xFF435149)
)

@Composable
fun UpSonodTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve authentic Bangladesh Green & Red branding
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
        typography = AppTypography,
        content = content
    )
}
