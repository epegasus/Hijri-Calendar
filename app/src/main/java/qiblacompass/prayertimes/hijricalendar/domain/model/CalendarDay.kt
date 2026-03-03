package qiblacompass.prayertimes.hijricalendar.domain.model

import java.time.LocalDate

data class CalendarDay(
    val gregorianDate: LocalDate,
    val hijriDate: HijriDate,
    val isInCurrentMonth: Boolean
)