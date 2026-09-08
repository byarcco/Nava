package ir.cutte.nava.engine

object OtpExtractor {
    private val codeRegex = Regex("""(?:\b|[^0-9])([0-9]{4,8})(?:\b|[^0-9])""")

    fun extractCode(body: String): String {
        val asciiBody = SenderNormalizer.convertDigitsToAscii(body.trim())
        val match = codeRegex.find(asciiBody)
        if (match != null) {
            return match.groupValues[1]
        }
        val digitsOnly = asciiBody.filter { it.isDigit() }
        if (digitsOnly.length in 4..8) {
            return digitsOnly
        }
        return digitsOnly.ifEmpty { asciiBody }
    }
}
