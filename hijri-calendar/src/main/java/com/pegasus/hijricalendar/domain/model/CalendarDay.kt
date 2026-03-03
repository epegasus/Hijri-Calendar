package com.pegasus.hijricalendar.domain.model

import java.time.LocalDate

internal data class CalendarDay(
    val gregorianDate: LocalDate,
    val hijriDate: HijriDate,
    val isInCurrentMonth: Boolean
)