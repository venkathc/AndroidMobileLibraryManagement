package com.venkateshgowda.personallibrary.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF14532D),
    secondary = Color(0xFF0F766E),
    tertiary = Color(0xFF9A6700),
    background = Color(0xFFFFFBF5),
    surface = Color(0xFFFFFBF5)
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFF86EFAC),
    secondary = Color(0xFF5EEAD4),
    tertiary = Color(0xFFFACC15),
    background = Color(0xFF102019),
    surface = Color(0xFF102019)
)

@Composable
fun LibraryTheme(theme: String = "System", content: @Composable () -> Unit) {
    val dark = when (theme) { "Light" -> false; "Dark" -> true; else -> isSystemInDarkTheme() }
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}