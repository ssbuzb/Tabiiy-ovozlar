package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimaryDark,
    onPrimary = OnEmeraldPrimaryDark,
    secondary = BlueSecondaryDark,
    onSecondary = OnBlueSecondaryDark,
    tertiary = LinenTertiaryDark,
    onTertiary = OnLinenTertiaryDark,
    background = NatureBackgroundDark,
    onBackground = NatureOnSurfaceDark,
    surface = NatureSurfaceDark,
    onSurface = NatureOnSurfaceDark,
    surfaceVariant = NatureSurfaceVariantDark,
    onSurfaceVariant = NatureOnSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimaryLight,
    onPrimary = OnEmeraldPrimaryLight,
    secondary = BlueSecondaryLight,
    onSecondary = OnBlueSecondaryLight,
    tertiary = LinenTertiaryLight,
    onTertiary = OnLinenTertiaryLight,
    background = NatureBackgroundLight,
    onBackground = NatureOnSurfaceLight,
    surface = NatureSurfaceLight,
    onSurface = NatureOnSurfaceLight,
    surfaceVariant = NatureSurfaceVariantLight,
    onSurfaceVariant = NatureOnSurfaceVariantLight
)

val NatureShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun NatureCalmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our handcrafted nature palette by default
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
        shapes = NatureShapes,
        content = content
    )
}
