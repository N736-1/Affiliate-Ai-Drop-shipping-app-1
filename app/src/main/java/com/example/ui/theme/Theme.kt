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
  primary = OmniPrimary,
  onPrimary = Color(0xFF381E72),
  secondary = OmniMuted,
  onSecondary = Color(0xFF1C1B1F),
  tertiary = OmniLiveGreen,
  background = OmniBg,
  surface = OmniCardBg,
  onBackground = OmniText,
  onSurface = OmniText,
  outline = OmniBorder
)

private val LightColorScheme = DarkColorScheme // Replicated to guarantee style integrity regardless of setting

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // We override to enforce high-density dark mode
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve exact high-density specification
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme
  
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
