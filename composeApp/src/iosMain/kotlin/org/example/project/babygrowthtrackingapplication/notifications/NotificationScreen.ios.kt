
@file:OptIn(ExperimentalTime::class)
//NotificationScreen.ios.kt
package org.example.project.babygrowthtrackingapplication.notifications

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

actual fun getCurrentDate(): String =
    Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date.toString()

actual fun getYesterdayDate(): String {
    val today = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    return today.minus(1, DateTimeUnit.DAY).toString()
}

actual fun formatDisplayDate(iso: String): String {
    var result = iso
    try {
        val date  = LocalDate.parse(iso)
        val month = date.month.name
            .lowercase()
            .replaceFirstChar { it.uppercase() }
            .take(3)
        result = "${date.dayOfMonth} $month ${date.year}"
    } catch (e: Exception) {
        // fallback to raw iso
    }
    return result
}

actual fun formatRelativeTime(isoDateTime: String, strings: RelativeTimeStrings): String {
    var result = isoDateTime.take(10)
    try {
        val then  = Instant.parse(isoDateTime.take(19) + "Z")
        val now   = Clock.System.now()
        val diff  = now - then
        val mins  = diff.inWholeMinutes
        val hours = diff.inWholeHours
        val days  = diff.inWholeMinutes / (60L * 24L)

        result = when {
            mins  < 1L  -> strings.justNow
            mins  < 60L -> strings.minsAgo.replace("%1\$d", mins.toString())
            hours < 24L -> strings.hoursAgo.replace("%1\$d", hours.toString())
            days  < 7L  -> strings.daysAgo.replace("%1\$d", days.toString())
            else        -> formatDisplayDate(isoDateTime.take(10))
        }
    } catch (e: Exception) {
        // fallback already set
    }
    return result
}