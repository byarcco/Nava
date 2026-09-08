package ir.cutte.nava.engine

import kotlin.test.Test
import kotlin.test.assertEquals

class OtpExtractorTest {

    @Test
    fun extractsCodeFromPersianText() {
        assertEquals("123456", OtpExtractor.extractCode("کد تایید شما: 123456"))
        assertEquals("123456", OtpExtractor.extractCode("کد تائید: ۱۲۳۴۵۶"))
        assertEquals("849201", OtpExtractor.extractCode("کد ورود به حساب کاربری: 849201"))
    }

    @Test
    fun extractsCodeWhenDirectNumberProvided() {
        assertEquals("123456", OtpExtractor.extractCode("123456"))
        assertEquals("654321", OtpExtractor.extractCode("۶۵۴۳۲۱"))
    }

    @Test
    fun extractsCodeFromRajaSms() {
        assertEquals("48192", OtpExtractor.extractCode("کد تایید رجا: 48192"))
    }
}
