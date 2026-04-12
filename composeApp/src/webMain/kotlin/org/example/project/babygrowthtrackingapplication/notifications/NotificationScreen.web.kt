package org.example.project.babygrowthtrackingapplication.notifications
//NotificationScreen.webMain.kt
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
actual fun getCurrentDate(): String =
    kotlin.time.Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date.toString()

@OptIn(ExperimentalTime::class)
actual fun getYesterdayDate(): String {
    val today = kotlin.time.Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    return today.minus(1, DateTimeUnit.DAY).toString()
}

actual fun formatDisplayDate(iso: String): String {
    return try {
        val date  = LocalDate.parse(iso)
        val month = date.month.name
            .lowercase()
            .replaceFirstChar { it.uppercase() }
            .take(3)
        "${date.day} $month ${date.year}"
    } catch (e: Exception) { iso }
}

@OptIn(ExperimentalTime::class)
actual fun formatRelativeTime(isoDateTime: String, strings: RelativeTimeStrings): String {
    return try {
        val then  = Instant.parse(isoDateTime.take(19) + "Z")
        val now   = kotlin.time.Clock.System.now()
        val diff  = now - then
        val mins  = diff.inWholeMinutes
        val hours = diff.inWholeHours
        val days  = diff.inWholeDays
        when {
            mins  < 1  -> strings.justNow
            mins  < 60 -> strings.minsAgo.replace("%1\$d", mins.toString())
            hours < 24 -> strings.hoursAgo.replace("%1\$d", hours.toString())
            days  < 7  -> strings.daysAgo.replace("%1\$d", days.toString())
            else       -> formatDisplayDate(isoDateTime.take(10))
        }
    } catch (e: Exception) { isoDateTime.take(10) }
}