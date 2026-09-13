package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.AppThemeMode

private val SakuraObsidianColorScheme = darkColorScheme(
    primary = SakuraLightPink,
    onPrimary = Color(0xFF1E0213),
    primaryContainer = Color(0xFF4A1030),
    onPrimaryContainer = SakuraPinkSoft,
    secondary = SakuraNeonLavender,
    onSecondary = Color(0xFF22083A),
    secondaryContainer = Color(0xFF3B185F),
    onSecondaryContainer = Color(0xFFE9D5FF),
    tertiary = SakuraGlintAqua,
    background = ObsidianNightBg,
    onBackground = TextWhite,
    surface = ObsidianSurface,
    onSurface = TextWhite,
    surfaceVariant = ObsidianSurfaceAlt,
    onSurfaceVariant = TextMuted,
    outline = ObsidianBorder
)

private val CyberRoseColorScheme = darkColorScheme(
    primary = CyberRosePink,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4E0325),
    onPrimaryContainer = Color(0xFFFFB3D2),
    secondary = CyberCyan,
    onSecondary = Color(0xFF00222E),
    secondaryContainer = Color(0xFF00384D),
    onSecondaryContainer = Color(0xFFB5F4FF),
    tertiary = Color(0xFF8B5CF6),
    background = CyberBg,
    onBackground = TextWhite,
    surface = CyberSurface,
    onSurface = TextWhite,
    surfaceVariant = Color(0xFF111D3B),
    onSurfaceVariant = TextMuted,
    outline = CyberBorder
)

private val PeachSunsetColorScheme = darkColorScheme(
    primary = PeachBlossom,
    onPrimary = Color(0xFF2E0903),
    primaryContainer = Color(0xFF5A1E12),
    onPrimaryContainer = Color(0xFFFFCCBD),
    secondary = PeachLavender,
    onSecondary = Color(0xFF21103D),
    secondaryContainer = Color(0xFF381F63),
    onSecondaryContainer = Color(0xFFE5D9FC),
    tertiary = Color(0xFFFBBF24),
    background = PeachBg,
    onBackground = TextWhite,
    surface = PeachSurface,
    onSurface = TextWhite,
    surfaceVariant = Color(0xFF26193D),
    onSurfaceVariant = TextMuted,
    outline = PeachBorder
)

private val CottonCandyColorScheme = darkColorScheme(
    primary = CottonPink,
    onPrimary = Color(0xFF240317),
    primaryContainer = Color(0xFF4B1032),
    onPrimaryContainer = Color(0xFFFFD4EA),
    secondary = CottonSkyBlue,
    onSecondary = Color(0xFF042036),
    secondaryContainer = Color(0xFF0D3B5C),
    onSecondaryContainer = Color(0xFFC7ECFF),
    tertiary = Color(0xFFE879F9),
    background = CottonBg,
    onBackground = TextWhite,
    surface = CottonSurface,
    onSurface = TextWhite,
    surfaceVariant = Color(0xFF1B2844),
    onSurfaceVariant = TextMuted,
    outline = CottonBorder
)

@Composable
fun SakuraStreamTheme(
    themeMode: AppThemeMode = AppThemeMode.SAKURA_OBSIDIAN,
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme = when (themeMode) {
        AppThemeMode.SAKURA_OBSIDIAN -> SakuraObsidianColorScheme
        AppThemeMode.CYBER_ROSE -> CyberRoseColorScheme
        AppThemeMode.PEACH_SUNSET -> PeachSunsetColorScheme
        AppThemeMode.COTTON_CANDY -> CottonCandyColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Keep backwards-compatibility alias for template tests
@Composable
fun MyApplicationTheme(content: @Composable () -> Unit) {
    SakuraStreamTheme(content = content)
}
