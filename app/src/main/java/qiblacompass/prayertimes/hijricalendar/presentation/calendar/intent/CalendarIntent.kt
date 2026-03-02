package qiblacompass.prayertimes.hijricalendar.presentation.calendar.intent

import java.time.LocalDate

sealed class CalendarIntent {
    data object Initialize : CalendarIntent()
    data object PreviousMonth : CalendarIntent()
    data object NextMonth : CalendarIntent()
    data class DaySelected(val date: LocalDate) : CalendarIntent()
}