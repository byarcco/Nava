package ir.cutte.nava.engine

import kotlin.test.Test
import kotlin.test.assertEquals

class SenderNormalizerTest {

    @Test
    fun convertsPersianAndArabicDigitsToAscii() {
        val persian = "۰۱۲۳۴۵۶۷۸۹"
        val arabic = "٠١٢٣٤٥٦٧٨٩"
        assertEquals("0123456789", SenderNormalizer.convertDigitsToAscii(persian))
        assertEquals("0123456789", SenderNormalizer.convertDigitsToAscii(arabic))
    }

    @Test
    fun normalizesIranianMobileVariantsToIdenticalSubscriberNumber() {
        val expected = "9123456789"
        assertEquals(expected, SenderNormalizer.normalize("+989123456789"))
        assertEquals(expected, SenderNormalizer.normalize("00989123456789"))
        assertEquals(expected, SenderNormalizer.normalize("09123456789"))
        assertEquals(expected, SenderNormalizer.normalize("989123456789"))
        assertEquals(expected, SenderNormalizer.normalize("9123456789"))
        assertEquals(expected, SenderNormalizer.normalize("۰۹۱۲۳۴۵۶۷۸۹"))
        assertEquals(expected, SenderNormalizer.normalize("٠٩١٢٣٤٥٦٧٨٩"))
        assertEquals(expected, SenderNormalizer.normalize("+98 (912) 345-6789"))
    }

    @Test
    fun normalizesAlphanumericSendersIgnoringCaseAndPunctuation() {
        assertEquals("BANKMELLI", SenderNormalizer.normalize("BANKMELLI"))
        assertEquals("BANKMELLI", SenderNormalizer.normalize("Bank Melli"))
        assertEquals("BANKMELLI", SenderNormalizer.normalize("bank-melli"))
        assertEquals("SNAPP", SenderNormalizer.normalize("Snapp"))
        assertEquals("SNAPPBOX", SenderNormalizer.normalize("Snapp Box!"))
    }

    @Test
    fun normalizesShortcodesAndLandlines() {
        assertEquals("10008585", SenderNormalizer.normalize("10008585"))
        assertEquals("10008585", SenderNormalizer.normalize("+9810008585"))
        assertEquals("2188888888", SenderNormalizer.normalize("02188888888"))
        assertEquals("2188888888", SenderNormalizer.normalize("+982188888888"))
    }
}
