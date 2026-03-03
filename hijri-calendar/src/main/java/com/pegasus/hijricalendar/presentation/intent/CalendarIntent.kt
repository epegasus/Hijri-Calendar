package com.pegasus.hijricalendar.presentation.intent

import java.time.LocalDate

internal sealed class CalendarIntent {
    data object Initialize : CalendarIntent()
    data object PreviousMonth : CalendarIntent()
    data object NextMonth : CalendarIntent()
    data class DaySelected(val date: LocalDate) : CalendarIntent()
}