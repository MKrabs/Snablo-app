package de.mkrabs.snablo.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Snablo Farben
private val SnabloPrimary = Color(0xFF2E7D32) // Grün
private val SnabloSecondary = Color(0xFF558B2F)
private val SnabloTertiary = Color(0xFF8BC34A)

private val LightColorScheme = lightColorScheme(
    primary = SnabloPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF1B5E20),
    secondary = SnabloSecondary,
    onSecondary = Color.White,
    tertiary = SnabloTertiary,
    background = Color(0xFFFAFAFA),
    surface = Color.White,
    error = Color(0xFFD32F2F)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF1B5E20),
    primaryContainer = Color(0xFF2E7D32),
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = Color(0xFFA5D6A7),
    onSecondary = Color(0xFF33691E),
    tertiary = Color(0xFFDCEDC8),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    error = Color(0xFFEF5350)
)

@Composable
fun SnabloTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
