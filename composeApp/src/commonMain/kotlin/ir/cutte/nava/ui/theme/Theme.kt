package ir.cutte.nava.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NavaColorScheme = lightColorScheme(
    primary = NavaPrimary,
    onPrimary = NavaOnPrimary,
    secondary = NavaSecondary,
    onSecondary = NavaOnSecondary,
    tertiary = NavaTertiary,
    onTertiary = NavaOnTertiary,
    background = NavaBackground,
    onBackground = NavaOnBackground,
    surface = NavaSurface,
    onSurface = NavaOnSurface,
    surfaceVariant = NavaSurfaceVariant,
    onSurfaceVariant = NavaOnSurfaceVariant,
    outline = NavaOutline,
    error = NavaError
)

@Composable
fun NavaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NavaColorScheme,
        content = content
    )
}
