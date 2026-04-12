package org.example.project.babygrowthtrackingapplication.notifications
//NotificationScreen.desktop.kt
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

actual fun getCurrentDate(): String = LocalDate.now().toString()

actual fun getYesterdayDate(): String = LocalDate.now().minusDays(1).toString()

actual fun formatDisplayDate(iso: String): String {
    return try {
        val date      = LocalDate.parse(iso)
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
        date.format(formatter)
    } catch (e: Exception) { iso }
}

actual fun formatRelativeTime(isoDateTime: String, strings: RelativeTimeStrings): String {
    return try {
        val then  = LocalDateTime.parse(isoDateTime.take(19))
        val now   = LocalDateTime.now()
        val mins  = ChronoUnit.MINUTES.between(then, now)
        val hours = ChronoUnit.HOURS.between(then, now)
        val days  = ChronoUnit.DAYS.between(then, now)
        when {
            mins  < 1  -> strings.justNow
            mins  < 60 -> strings.minsAgo.format(mins)
            hours < 24 -> strings.hoursAgo.format(hours)
            days  < 7  -> strings.daysAgo.format(days)
            else       -> formatDisplayDate(isoDateTime.take(10))
        }
    } catch (e: Exception) { isoDateTime.take(10) }
}