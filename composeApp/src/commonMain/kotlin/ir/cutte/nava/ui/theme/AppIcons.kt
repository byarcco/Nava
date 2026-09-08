package ir.cutte.nava.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object AppIcons {
    val Settings: ImageVector = ImageVector.Builder(
        name = "Settings",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(12f, 15f)
        arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 9f)
        arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 15f)
        close()
        moveTo(19.4f, 15f)
        arcTo(1.65f, 1.65f, 0f, false, false, 20f, 16.34f)
        lineTo(20f, 17f)
        arcTo(2f, 2f, 0f, false, true, 18f, 19f)
        lineTo(17.66f, 19f)
        arcTo(1.65f, 1.65f, 0f, false, false, 16.34f, 20f)
        lineTo(15.7f, 20.66f)
        arcTo(2f, 2f, 0f, false, true, 13.7f, 22f)
        lineTo(10.3f, 22f)
        arcTo(2f, 2f, 0f, false, true, 8.3f, 20.66f)
        lineTo(7.66f, 20f)
        arcTo(1.65f, 1.65f, 0f, false, false, 6.34f, 19f)
        lineTo(6f, 19f)
        arcTo(2f, 2f, 0f, false, true, 4f, 17f)
        lineTo(4f, 16.34f)
        arcTo(1.65f, 1.65f, 0f, false, false, 3.4f, 15f)
        lineTo(2.74f, 14.36f)
        arcTo(2f, 2f, 0f, false, true, 2f, 12.36f)
        lineTo(2f, 11.64f)
        arcTo(2f, 2f, 0f, false, true, 2.74f, 9.64f)
        lineTo(3.4f, 9f)
        arcTo(1.65f, 1.65f, 0f, false, false, 4f, 7.66f)
        lineTo(4f, 7f)
        arcTo(2f, 2f, 0f, false, true, 6f, 5f)
        lineTo(6.34f, 5f)
        arcTo(1.65f, 1.65f, 0f, false, false, 7.66f, 4f)
        lineTo(8.3f, 3.34f)
        arcTo(2f, 2f, 0f, false, true, 10.3f, 2f)
        lineTo(13.7f, 2f)
        arcTo(2f, 2f, 0f, false, true, 15.7f, 3.34f)
        lineTo(16.34f, 4f)
        arcTo(1.65f, 1.65f, 0f, false, false, 17.66f, 5f)
        lineTo(18f, 5f)
        arcTo(2f, 2f, 0f, false, true, 20f, 7f)
        lineTo(20f, 7.66f)
        arcTo(1.65f, 1.65f, 0f, false, false, 20.66f, 9f)
        lineTo(21.26f, 9.64f)
        arcTo(2f, 2f, 0f, false, true, 22f, 11.64f)
        lineTo(22f, 12.36f)
        arcTo(2f, 2f, 0f, false, true, 21.26f, 14.36f)
        close()
    }.build()

    val ArrowForward: ImageVector = ImageVector.Builder(
        name = "ArrowForward",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(5f, 12f)
        lineTo(19f, 12f)
        moveTo(13f, 6f)
        lineTo(19f, 12f)
        lineTo(13f, 18f)
    }.build()

    val Close: ImageVector = ImageVector.Builder(
        name = "Close",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(18f, 6f)
        lineTo(6f, 18f)
        moveTo(6f, 6f)
        lineTo(18f, 18f)
    }.build()
}
