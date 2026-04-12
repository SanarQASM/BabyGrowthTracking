package org.example.project.babygrowthtrackingapplication.notifications
//NotificationScreen.android.kt
import android.os.Build
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale

actual fun getCurrentDate(): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        LocalDate.now().toString()
    } else {
        val cal = Calendar.getInstance()
        "%04d-%02d-%02d".format(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

actual fun getYesterdayDate(): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        LocalDate.now().minusDays(1).toString()
    } else {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, -1)
        "%04d-%02d-%02d".format(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

actual fun formatDisplayDate(iso: String): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val date      = LocalDate.parse(iso)
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
        date.format(formatter)
    } else {
        try {
            val sdf  = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val date = sdf.parse(iso) ?: return iso
            val out  = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            out.format(date)
        } catch (e: Exception) { iso }
    }

actual fun formatRelativeTime(isoDateTime: String, strings: RelativeTimeStrings): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        try {
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
    } else {
        try {
            val sdf  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
            val then = sdf.parse(isoDateTime.take(19)) ?: return isoDateTime.take(10)
            val diff  = System.currentTimeMillis() - then.time
            val mins  = diff / 60_000
            val hours = diff / 3_600_000
            val days  = diff / 86_400_000
            when {
                mins  < 1  -> strings.justNow
                mins  < 60 -> strings.minsAgo.format(mins)
                hours < 24 -> strings.hoursAgo.format(hours)
                days  < 7  -> strings.daysAgo.format(days)
                else       -> formatDisplayDate(isoDateTime.take(10))
            }
        } catch (e: Exception) { isoDateTime.take(10) }
    }