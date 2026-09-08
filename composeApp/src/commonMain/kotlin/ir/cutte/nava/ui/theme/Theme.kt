package ir.cutte.nava.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NavaColorScheme = lightColorScheme(
    primary = NavaPrimary,
    onPrimary = NavaOnPrimary,
    primaryContainer = NavaPrimaryContainer,
    onPrimaryContainer = NavaOnPrimaryContainer,
    secondary = NavaSecondary,
    onSecondary = NavaOnSecondary,
    secondaryContainer = NavaSecondaryContainer,
    onSecondaryContainer = NavaOnSecondaryContainer,
    background = NavaBackground,
    onBackground = NavaOnBackground,
    surface = NavaSurface,
    onSurface = NavaOnSurface,
    surfaceVariant = NavaSurfaceVariant,
    onSurfaceVariant = NavaOnSurfaceVariant,
    outline = NavaOutline,
    outlineVariant = NavaOutlineVariant,
    error = NavaError,
    onError = NavaOnError,
    errorContainer = NavaErrorContainer,
    onErrorContainer = NavaOnErrorContainer
)

@Composable
fun NavaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NavaColorScheme,
        typography = getNavaTypography(),
        content = content
    )
}
