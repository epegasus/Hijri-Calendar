package qiblacompass.prayertimes.hijricalendar.presentation.calendar.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import qiblacompass.prayertimes.hijricalendar.domain.calendar.GenerateMonthCalendarUseCase
import qiblacompass.prayertimes.hijricalendar.domain.model.CalendarDay
import qiblacompass.prayertimes.hijricalendar.domain.model.MonthCalendar
import qiblacompass.prayertimes.hijricalendar.presentation.calendar.intent.CalendarIntent
import qiblacompass.prayertimes.hijricalendar.presentation.calendar.model.CalendarDayUiModel
import qiblacompass.prayertimes.hijricalendar.presentation.calendar.state.CalendarUiState
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class CalendarViewModel(private val generateMonthCalendar: GenerateMonthCalendarUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState(isLoading = true))
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    // Anchors for ViewPager2 page offsets (center page = baseMonth/baseSelectedDate)
    private var baseMonth: YearMonth = YearMonth.now()
    private var baseSelectedDate: LocalDate = LocalDate.now()

    init {
        processIntent(CalendarIntent.Initialize)
    }

    fun processIntent(intent: CalendarIntent) {
        when (intent) {
            CalendarIntent.Initialize -> loadInitial()
            CalendarIntent.NextMonth -> moveToAdjacentMonth(+1)
            CalendarIntent.PreviousMonth -> moveToAdjacentMonth(-1)
            is CalendarIntent.DaySelected -> selectDate(intent.date)
        }
    }

    private fun loadInitial() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val month = YearMonth.from(today)

            baseMonth = month
            baseSelectedDate = today

            val newState = buildStateFor(month, today)
            _uiState.value = newState
        }
    }

    /**
     * Build a complete UI state for "base month + offset".
     * Used by each ViewPager page to pre-fill its RecyclerView.
     */
    fun stateForOffset(offset: Int): CalendarUiState {
        val targetMonth = baseMonth.plusMonths(offset.toLong())
        val desiredDay = baseSelectedDate.dayOfMonth.coerceAtMost(targetMonth.lengthOfMonth())
        val selectedDate = targetMonth.atDay(desiredDay)
        return buildStateFor(targetMonth, selectedDate)
    }

    /**
     * Update the shared state when the primary ViewPager page changes.
     */
    fun setCurrentPage(offset: Int) {
        _uiState.value = stateForOffset(offset)
    }

    private fun moveToAdjacentMonth(offsetMonths: Long) {
        viewModelScope.launch {
            val current = _uiState.value
            val targetMonth = current.currentMonth.plusMonths(offsetMonths)
            val desiredDay = current.selectedDate.dayOfMonth
            val adjustedDay = desiredDay.coerceAtMost(targetMonth.lengthOfMonth())
            val selectedDate = targetMonth.atDay(adjustedDay)
            val newState = buildStateFor(targetMonth, selectedDate)
            _uiState.value = newState
        }
    }

    private fun selectDate(date: LocalDate) {
        viewModelScope.launch {
            val current = _uiState.value
            if (current.currentMonth != YearMonth.from(date)) {
                // If a day from another month is tapped, real apps might navigate; we ignore for now.
                return@launch
            }

            _uiState.update { state ->
                val updatedDays = state.days.map { uiModel ->
                    uiModel.copy(isSelected = uiModel.gregorianDate == date)
                }

                val monthCalendar = generateMonthCalendar(state.currentMonth)
                val hijriForSelected = monthCalendar.days
                    .firstOrNull { it.gregorianDate == date }
                    ?.hijriDate

                val hijriFullDateText = hijriForSelected?.let {
                    "${it.day} ${it.monthName}, ${it.year} ھـ"
                } ?: state.hijriFullDateText

                val gregorianFullDateText = GREGORIAN_FULL_DATE_FORMATTER.format(date)

                state.copy(
                    selectedDate = date,
                    days = updatedDays,
                    hijriFullDateText = hijriFullDateText,
                    gregorianFullDateText = gregorianFullDateText
                )
            }
        }
    }

    private fun buildStateFor(
        targetMonth: YearMonth,
        selectedDate: LocalDate
    ): CalendarUiState {
        val monthCalendar = generateMonthCalendar(targetMonth)
        val hijriForSelected = monthCalendar.days
            .firstOrNull { it.gregorianDate == selectedDate }
            ?.hijriDate

        val daysUiModels = monthCalendar.days.toUiModels(selectedDate)
        val monthTitle = MONTH_TITLE_FORMATTER.format(targetMonth.atDay(1))
        val hijriMonthSubtitle = buildHijriMonthSubtitle(monthCalendar)

        val hijriFullDateText = hijriForSelected?.let {
            "${it.day} ${it.monthName}, ${it.year} ھـ"
        } ?: ""
        val gregorianFullDateText = GREGORIAN_FULL_DATE_FORMATTER.format(selectedDate)

        return CalendarUiState(
            isLoading = false,
            currentMonth = targetMonth,
            selectedDate = selectedDate,
            monthTitle = monthTitle,
            hijriMonthSubtitle = hijriMonthSubtitle,
            locationText = "Bahria Town Phase 8, Rawalpindi",
            hijriFullDateText = hijriFullDateText,
            gregorianFullDateText = gregorianFullDateText,
            days = daysUiModels
        )
    }

    private fun List<CalendarDay>.toUiModels(selectedDate: LocalDate): List<CalendarDayUiModel> {
        return map { day ->
            if (day.isEmpty) {
                CalendarDayUiModel(
                    gregorianDate = null,
                    hijriDayLabel = "",
                    gregorianDayLabel = "",
                    isSelected = false,
                    isClickable = false
                )
            } else {
                val gregorian = requireNotNull(day.gregorianDate)
                val hijri = requireNotNull(day.hijriDate)
                CalendarDayUiModel(
                    gregorianDate = gregorian,
                    hijriDayLabel = "${hijri.day} ھـ",
                    gregorianDayLabel = gregorian.dayOfMonth.toString(),
                    isSelected = gregorian == selectedDate,
                    isClickable = true
                )
            }
        }
    }

    private fun buildHijriMonthSubtitle(monthCalendar: MonthCalendar): String {
        val distinctMonths = monthCalendar.days
            .filterNot(CalendarDay::isEmpty)
            .mapNotNull { it.hijriDate }
            .distinctBy { it.monthName to it.year }

        if (distinctMonths.isEmpty()) return ""

        val monthNames = distinctMonths.map { it.monthName }
        val years = distinctMonths.map { it.year }.distinct()

        val monthPart = monthNames.joinToString(separator = "/")
        val yearPart = when (years.size) {
            1 -> years.first().toString()
            else -> "${years.first()}-${years.last()}"
        }

        return "$monthPart $yearPart ھـ"
    }

    companion object {
        private val MONTH_TITLE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
        private val GREGORIAN_FULL_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM, yyyy", Locale.ENGLISH)
    }
}