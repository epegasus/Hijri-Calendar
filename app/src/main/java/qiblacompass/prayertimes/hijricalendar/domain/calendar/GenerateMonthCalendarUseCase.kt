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

        val previousMonth = yearMonth.minusMonths(1)
        val nextMonth = yearMonth.plusMonths(1)
        val daysInPreviousMonth = previousMonth.lengthOfMonth()

        return buildList(capacity = totalCells) {
            for (cellIndex in 0 until totalCells) {
                val dayOfMonth = cellIndex - firstDayOfWeekIndex + 1

                val (gregorianDate, isInCurrentMonth) = when {
                    dayOfMonth in 1..daysInMonth -> {
                        yearMonth.atDay(dayOfMonth) to true
                    }

                    dayOfMonth < 1 -> {
                        // Leading days from previous month
                        val previousDay = daysInPreviousMonth + dayOfMonth
                        previousMonth.atDay(previousDay) to false
                    }

                    else -> {
                        // Trailing days from next month
                        val nextDay = dayOfMonth - daysInMonth
                        nextMonth.atDay(nextDay) to false
                    }
                }

                add(
                    CalendarDay(
                        gregorianDate = gregorianDate,
                        hijriDate = hijriDateConverter.toHijri(gregorianDate),
                        isInCurrentMonth = isInCurrentMonth
                    )
                )
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