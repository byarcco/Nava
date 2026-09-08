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

fun formatActivityDuration(elapsedMillis: Long): String {
    if (elapsedMillis < 60_000L) {
        return "کمتر از یک دقیقه"
    }

    val totalMinutes = elapsedMillis / (60 * 1000L)
    val totalHours = totalMinutes / 60
    val totalDays = totalHours / 24
    val totalWeeks = totalDays / 7
    val totalMonths = totalDays / 30
    val totalYears = totalDays / 365

    return when {
        totalYears >= 1 -> {
            val remainingMonths = (totalDays % 365) / 30
            if (remainingMonths > 0) {
                "${totalYears.toPersianDigits()} سال و ${remainingMonths.toPersianDigits()} ماه"
            } else {
                "${totalYears.toPersianDigits()} سال"
            }
        }
        totalMonths >= 1 -> {
            val remainingDays = totalDays % 30
            if (remainingDays > 0) {
                "${totalMonths.toPersianDigits()} ماه و ${remainingDays.toPersianDigits()} روز"
            } else {
                "${totalMonths.toPersianDigits()} ماه"
            }
        }
        totalWeeks >= 1 -> {
            val remainingDays = totalDays % 7
            if (remainingDays > 0) {
                "${totalWeeks.toPersianDigits()} هفته و ${remainingDays.toPersianDigits()} روز"
            } else {
                "${totalWeeks.toPersianDigits()} هفته"
            }
        }
        totalDays >= 1 -> {
            val remainingHours = totalHours % 24
            if (remainingHours > 0) {
                "${totalDays.toPersianDigits()} روز و ${remainingHours.toPersianDigits()} ساعت"
            } else {
                "${totalDays.toPersianDigits()} روز"
            }
        }
        totalHours >= 1 -> {
            val remainingMinutes = totalMinutes % 60
            if (remainingMinutes > 0) {
                "${totalHours.toPersianDigits()} ساعت و ${remainingMinutes.toPersianDigits()} دقیقه"
            } else {
                "${totalHours.toPersianDigits()} ساعت"
            }
        }
        else -> {
            "${totalMinutes.toPersianDigits()} دقیقه"
        }
    }
}
