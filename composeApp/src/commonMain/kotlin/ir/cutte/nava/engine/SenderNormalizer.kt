package ir.cutte.nava.engine

object SenderNormalizer {

    fun convertDigitsToAscii(input: String): String {
        val stringBuilder = StringBuilder(input.length)
        for (char in input) {
            when (char) {
                in '\u06F0'..'\u06F9' -> stringBuilder.append((char.code - '\u06F0'.code + '0'.code).toChar())
                in '\u0660'..'\u0669' -> stringBuilder.append((char.code - '\u0660'.code + '0'.code).toChar())
                else -> stringBuilder.append(char)
            }
        }
        return stringBuilder.toString()
    }

    fun normalize(rawSender: String): String {
        val converted = convertDigitsToAscii(rawSender.trim())
        val hasLetter = converted.any { it.isLetter() }

        if (hasLetter) {
            return converted.filter { it.isLetterOrDigit() }.uppercase()
        }

        var cleaned = converted.filter { it.isDigit() || it == '+' }
        if (cleaned.isEmpty()) {
            return ""
        }

        if (cleaned.startsWith("+98")) {
            cleaned = cleaned.removePrefix("+98")
        } else if (cleaned.startsWith("0098")) {
            cleaned = cleaned.removePrefix("0098")
        } else if (cleaned.startsWith("98") && cleaned.length >= 12) {
            cleaned = cleaned.removePrefix("98")
        }

        if (cleaned.startsWith("+")) {
            cleaned = cleaned.removePrefix("+")
        }

        if (cleaned.startsWith("0")) {
            cleaned = cleaned.removePrefix("0")
        }

        return cleaned
    }
}
