package ir.cutte.nava.util

fun Number.toPersianDigits(): String {
    return this.toString().toPersianDigits()
}

fun String.toPersianDigits(): String {
    val builder = StringBuilder(this.length)
    for (char in this) {
        when (char) {
            in '0'..'9' -> builder.append((char.code - '0'.code + '۰'.code).toChar())
            else -> builder.append(char)
        }
    }
    return builder.toString()
}
