package ir.cutte.nava.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import ir.cutte.nava.R

@Composable
actual fun getVazirmatnFontFamily(): FontFamily {
    return FontFamily(
        Font(R.font.vazirmatn, FontWeight.Normal),
        Font(R.font.vazirmatn, FontWeight.Medium),
        Font(R.font.vazirmatn, FontWeight.SemiBold),
        Font(R.font.vazirmatn, FontWeight.Bold)
    )
}
