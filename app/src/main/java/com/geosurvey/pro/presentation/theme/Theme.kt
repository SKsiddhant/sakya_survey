package com.geosurvey.pro.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SakyaRed,
    background = Color.Black, // True OLED Black
    surface = Color(0xFF121212),
    secondary = SakyaTeal,
    error = ErrorRed,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = SakyaRed,
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    secondary = SakyaTeal,
    error = ErrorRed,
    onPrimary = Color.White
)

@Composable
fun GeoSurveyProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val context = LocalContext.current
    SideEffect {
        val window = (context as? android.app.Activity)?.window
        window?.let {
            it.statusBarColor = colorScheme.background.toArgb()
            it.navigationBarColor = colorScheme.background.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
