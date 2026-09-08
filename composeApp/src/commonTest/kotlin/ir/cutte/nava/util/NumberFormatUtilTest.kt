package ir.cutte.nava.util

import kotlin.test.Test
import kotlin.test.assertEquals

class NumberFormatUtilTest {

    @Test
    fun formatsUnderOneMinute() {
        assertEquals("کمتر از یک دقیقه", formatActivityDuration(10_000L))
        assertEquals("کمتر از یک دقیقه", formatActivityDuration(59_999L))
    }

    @Test
    fun formatsMinutes() {
        assertEquals("۱ دقیقه", formatActivityDuration(60_000L))
        assertEquals("۵۰ دقیقه", formatActivityDuration(50 * 60 * 1000L))
    }

    @Test
    fun formatsHoursAndMinutes() {
        val millis = (12 * 60 + 24) * 60 * 1000L
        assertEquals("۱۲ ساعت و ۲۴ دقیقه", formatActivityDuration(millis))
    }

    @Test
    fun formatsDaysAndHours() {
        val millis = (2 * 24 + 13) * 60 * 60 * 1000L
        assertEquals("۲ روز و ۱۳ ساعت", formatActivityDuration(millis))
    }

    @Test
    fun formatsWeeksAndDays() {
        val millis = (1 * 7 + 4) * 24 * 60 * 60 * 1000L
        assertEquals("۱ هفته و ۴ روز", formatActivityDuration(millis))
    }
}
