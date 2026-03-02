package qiblacompass.prayertimes.hijricalendar.presentation.calendar.model

import java.time.LocalDate

data class CalendarDayUiModel(
    val gregorianDate: LocalDate?,
    val hijriDayLabel: String,
    val gregorianDayLabel: String,
    val isSelected: Boolean,
    val isClickable: Boolean
)