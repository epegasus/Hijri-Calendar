package com.pegasus.hijricalendar.presentation.state

import com.pegasus.hijricalendar.presentation.model.CalendarDayUiModel
import java.time.LocalDate
import java.time.YearMonth

internal data class CalendarUiState(
    val isLoading: Boolean = false,
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val monthTitle: String = "",
    val hijriMonthSubtitle: String = "",
    val locationText: String = "",
    val hijriFullDateText: String = "",
    val gregorianFullDateText: String = "",
    val days: List<CalendarDayUiModel> = emptyList()
)