package ir.cutte.nava.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MessageMatcherTest {

    private val defaultKeywords = setOf("کد ورود", "کد تایید", "کد تائید", "OTP", "verification")
    private val defaultWhitelist = setOf("09123456789", "Bank Melli", "10008585", "Raja.ir")

    @Test
    fun matchesWhenBodyContainsConfiguredKeyword() {
        val body = "سلام، کد ورود شما ۱۲۳۴۵۶ می باشد."
        val result = MessageMatcher.match(
            rawSender = "UNKNOWN",
            body = body,
            whitelist = emptySet(),
            keywords = defaultKeywords
        )
        assertTrue(result.isMatched)
        assertEquals("کد ورود", result.matchedKeyword)
    }

    @Test
    fun matchesWhenBodyContainsNewPersianKeywords() {
        val body1 = "کد تایید شما: 849201"
        val result1 = MessageMatcher.match(
            rawSender = "UNKNOWN",
            body = body1,
            whitelist = emptySet(),
            keywords = defaultKeywords
        )
        assertTrue(result1.isMatched)
        assertEquals("کد تایید", result1.matchedKeyword)

        val body2 = "کد تائید ورود به سیستم: 123456"
        val result2 = MessageMatcher.match(
            rawSender = "UNKNOWN",
            body = body2,
            whitelist = emptySet(),
            keywords = defaultKeywords
        )
        assertTrue(result2.isMatched)
        assertEquals("کد تائید", result2.matchedKeyword)
    }

    @Test
    fun matchesWhenSenderIsRajaCaseInsensitive() {
        val result1 = MessageMatcher.match(
            rawSender = "raja.ir",
            body = "بلیط قطار شما صادر شد",
            whitelist = defaultWhitelist,
            keywords = defaultKeywords
        )
        assertTrue(result1.isMatched)

        val result2 = MessageMatcher.match(
            rawSender = "RAJA",
            body = "اطلاعیه سفر",
            whitelist = defaultWhitelist,
            keywords = defaultKeywords
        )
        assertTrue(result2.isMatched)
    }

    @Test
    fun matchesWhenBodyContainsCaseInsensitiveEnglishKeyword() {
        val body = "Your otp code is 987654"
        val result = MessageMatcher.match(
            rawSender = "UNKNOWN",
            body = body,
            whitelist = emptySet(),
            keywords = defaultKeywords
        )
        assertTrue(result.isMatched)
        assertEquals("OTP", result.matchedKeyword)
    }

    @Test
    fun matchesWhenSenderIsWhitelistedUnderDifferentFormatting() {
        val result = MessageMatcher.match(
            rawSender = "+989123456789",
            body = "Unrelated notification content",
            whitelist = defaultWhitelist,
            keywords = defaultKeywords
        )
        assertTrue(result.isMatched)
        assertEquals("WHITELIST:9123456789", result.matchedKeyword)
    }

    @Test
    fun matchesWhenAlphanumericSenderIsWhitelisted() {
        val result = MessageMatcher.match(
            rawSender = "BANKMELLI",
            body = "Unrelated transaction details",
            whitelist = defaultWhitelist,
            keywords = defaultKeywords
        )
        assertTrue(result.isMatched)
        assertEquals("WHITELIST:BANKMELLI", result.matchedKeyword)
    }

    @Test
    fun rejectsWhenNeitherSenderNorKeywordsMatch() {
        val result = MessageMatcher.match(
            rawSender = "09350000000",
            body = "Hello this is a regular chat message with no secret tokens",
            whitelist = defaultWhitelist,
            keywords = defaultKeywords
        )
        assertFalse(result.isMatched)
        assertEquals(null, result.matchedKeyword)
    }
}
