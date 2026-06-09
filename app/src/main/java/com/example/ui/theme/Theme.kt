package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ClinicalSecondary,
    secondary = Teal80,
    tertiary = ClinicalTertiary,
    background = OffBlack,
    surface = SlateDark,
    onPrimary = OffWhite,
    onSecondary = OffWhite,
    onBackground = OffWhite,
    onSurface = OffWhite,
    error = MedicalAlertLow
)

private val LightColorScheme = lightColorScheme(
    primary = ClinicalTeal,
    secondary = ClinicalSecondary,
    tertiary = ClinicalTertiary,
    background = OffWhite,
    surface = SlateLight,
    onPrimary = OffWhite,
    onSecondary = TextDark,
    onBackground = TextDark,
    onSurface = TextDark,
    error = MedicalAlertLow
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep dynamic color true on newer systems to match Android 12 design style gracefully
    dynamicColor: Boolean = false, // Set to false to force our gorgeous custom clinical theme branding consistently
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
