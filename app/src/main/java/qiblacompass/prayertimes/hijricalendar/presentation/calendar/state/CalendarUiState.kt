package qiblacompass.prayertimes.hijricalendar.presentation.calendar.state

import qiblacompass.prayertimes.hijricalendar.presentation.calendar.model.CalendarDayUiModel
import java.time.LocalDate
import java.time.YearMonth

data class CalendarUiState(
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