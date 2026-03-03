package com.pegasus.hijricalendar.presentation.model

import java.time.LocalDate

internal data class CalendarDayUiModel(
    val gregorianDate: LocalDate,
    val hijriDayLabel: String,
    val gregorianDayLabel: String,
    val isSelected: Boolean,
    val isClickable: Boolean,
    val isInCurrentMonth: Boolean
)