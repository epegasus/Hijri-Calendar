package qiblacompass.prayertimes.hijricalendar.data.calendar

import android.icu.util.IslamicCalendar
import android.os.Build
import androidx.annotation.RequiresApi
import qiblacompass.prayertimes.hijricalendar.domain.calendar.HijriDateConverter
import qiblacompass.prayertimes.hijricalendar.domain.model.HijriDate
import java.time.LocalDate
import java.time.ZoneId
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.util.Date
import java.util.Locale

/**
 * Maps Gregorian dates to Hijri dates using android.icu IslamicCalendar on API 24+
 * and java.time HijrahDate as a safe fallback on lower API levels.
 */
class AndroidHijriDateConverter(private val locale: Locale = DEFAULT_LOCALE) : HijriDateConverter {

    /**
     * User-configurable offset, in days, used to adjust the Hijri date.
     * For example, -1 will shift the Hijri result back by one day.
     */
    var offsetDays: Int = 0

    override fun toHijri(date: LocalDate): HijriDate {
        val adjustedDate = date.plusDays(offsetDays.toLong())
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fromIslamicCalendar(adjustedDate)
        } else {
            fromHijrahDate(adjustedDate)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun fromIslamicCalendar(date: LocalDate): HijriDate {
        val islamicCalendar = IslamicCalendar(locale)
        val instant = date.atStartOfDay(ZoneId.systemDefault()).toInstant()
        islamicCalendar.time = Date.from(instant)

        val day = islamicCalendar.get(IslamicCalendar.DATE)
        val monthIndex = islamicCalendar.get(IslamicCalendar.MONTH) // 0-based
        val year = islamicCalendar.get(IslamicCalendar.YEAR)

        return HijriDate(
            day = day,
            month = monthIndex + 1,
            monthName = hijriMonthName(monthIndex),
            year = year
        )
    }

    private fun fromHijrahDate(date: LocalDate): HijriDate {
        val hijrahDate = HijrahDate.from(date)
        val day = hijrahDate[ChronoField.DAY_OF_MONTH]
        val month = hijrahDate[ChronoField.MONTH_OF_YEAR]
        val year = hijrahDate[ChronoField.YEAR]

        return HijriDate(
            day = day,
            month = month,
            monthName = hijriMonthName(month - 1),
            year = year
        )
    }

    private fun hijriMonthName(zeroBasedIndex: Int): String {
        val index = zeroBasedIndex.coerceIn(0, HIJRI_MONTHS.lastIndex)
        return HIJRI_MONTHS[index]
    }

    companion object {
        private val DEFAULT_LOCALE: Locale = Locale("ar", "SA")

        // 0-based to match IslamicCalendar
        private val HIJRI_MONTHS = listOf(
            "Muharram",
            "Safar",
            "Rabi' al-awwal",
            "Rabi' al-thani",
            "Jumada al-awwal",
            "Jumada al-thani",
            "Rajab",
            "Sha'ban",
            "Ramadan",
            "Shawwal",
            "Dhu al-Qadah",
            "Dhu al-Hijjah"
        )
    }
}