package qiblacompass.prayertimes.hijricalendar.domain.calendar

import qiblacompass.prayertimes.hijricalendar.domain.model.CalendarDay
import qiblacompass.prayertimes.hijricalendar.domain.model.MonthCalendar
import java.time.DayOfWeek
import java.time.YearMonth

/**
 * Generates a full month calendar with leading and trailing empty cells,
 * enriched with Hijri date information.
 */
class GenerateMonthCalendarUseCase(private val hijriDateConverter: HijriDateConverter) {

    operator fun invoke(yearMonth: YearMonth): MonthCalendar {
        val days = buildDays(yearMonth)
        return MonthCalendar(month = yearMonth, days = days)
    }

    private fun buildDays(yearMonth: YearMonth): List<CalendarDay> {
        val firstOfMonth = yearMonth.atDay(1)
        val daysInMonth = yearMonth.lengthOfMonth()

        // Grid starts on Sunday, java.time.DayOfWeek starts with Monday (1) ... Sunday (7)
        val firstDayOfWeekIndex = dayOfWeekIndex(firstOfMonth.dayOfWeek)
        val totalCells = calculateTotalCells(firstDayOfWeekIndex, daysInMonth)

        return buildList(capacity = totalCells) {
            for (cellIndex in 0 until totalCells) {
                val dayOfMonth = cellIndex - firstDayOfWeekIndex + 1
                if (dayOfMonth in 1..daysInMonth) {
                    val gregorian = yearMonth.atDay(dayOfMonth)
                    add(CalendarDay(gregorianDate = gregorian, hijriDate = hijriDateConverter.toHijri(gregorian)))
                } else {
                    add(CalendarDay(gregorianDate = null, hijriDate = null))
                }
            }
        }
    }

    private fun dayOfWeekIndex(dayOfWeek: DayOfWeek): Int {
        // Map Monday(1)..Sunday(7) to Sunday first grid index 0..6
        val javaIndex = dayOfWeek.value // Monday = 1 ... Sunday = 7
        return javaIndex % 7 // Sunday -> 0, Monday -> 1, ..., Saturday -> 6
    }

    private fun calculateTotalCells(leadingEmptyCells: Int, daysInMonth: Int): Int {
        val usedCells = leadingEmptyCells + daysInMonth
        val fullWeeks = (usedCells + DAYS_IN_WEEK - 1) / DAYS_IN_WEEK
        return fullWeeks * DAYS_IN_WEEK
    }

    companion object {
        private const val DAYS_IN_WEEK = 7
    }
}