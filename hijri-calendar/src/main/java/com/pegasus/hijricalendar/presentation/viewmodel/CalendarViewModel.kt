package com.pegasus.hijricalendar.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pegasus.hijricalendar.domain.calendar.GenerateMonthCalendarUseCase
import com.pegasus.hijricalendar.domain.model.CalendarDay
import com.pegasus.hijricalendar.domain.model.MonthCalendar
import com.pegasus.hijricalendar.presentation.intent.CalendarIntent
import com.pegasus.hijricalendar.presentation.model.CalendarDayUiModel
import com.pegasus.hijricalendar.presentation.state.CalendarUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

internal class CalendarViewModel(
    private val generateMonthCalendar: GenerateMonthCalendarUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState(isLoading = true))
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    // Today anchor (used for selection highlight)
    private val today: LocalDate = LocalDate.now()
    private val todayMonth: YearMonth = YearMonth.from(today)

    // Anchors for ViewPager2 page offsets (center page = baseMonth/baseSelectedDate)
    private var baseMonth: YearMonth = todayMonth
    private var baseSelectedDate: LocalDate = today

    // Header dates (Hijri / Gregorian) shown in the location card.
    // These are anchored to "today" and do NOT change when paging months.
    private var headerHijriFullDateText: String = ""
    private var headerGregorianFullDateText: String = ""

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
            baseMonth = todayMonth
            baseSelectedDate = today

            // Compute header dates once, anchored to "today"
            val monthCalendar = generateMonthCalendar(todayMonth)
            val hijriToday = monthCalendar.days
                .firstOrNull { it.gregorianDate == today }
                ?.hijriDate

            headerHijriFullDateText = hijriToday?.let {
                "${it.day} ${it.monthName}, ${it.year} ھـ"
            } ?: ""
            headerGregorianFullDateText = GREGORIAN_FULL_DATE_FORMATTER.format(today)

            val initialState = buildStateFor(todayMonth, today)
            _uiState.value = initialState
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

    /**
     * Recalculate header Hijri/Gregorian full date texts based on the
     * current Hijri converter configuration (e.g. after user adjusts
     * the Hijri date offset) and today's date.
     */
    fun refreshHeaderForToday() {
        viewModelScope.launch {
            val monthCalendar = generateMonthCalendar(todayMonth)
            val hijriToday = monthCalendar.days
                .firstOrNull { it.gregorianDate == today }
                ?.hijriDate

            headerHijriFullDateText = hijriToday?.let {
                "${it.day} ${it.monthName}, ${it.year} ھـ"
            } ?: ""
            headerGregorianFullDateText = GREGORIAN_FULL_DATE_FORMATTER.format(today)

            _uiState.update { state ->
                state.copy(
                    hijriFullDateText = headerHijriFullDateText,
                    gregorianFullDateText = headerGregorianFullDateText
                )
            }
        }
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

                state.copy(
                    selectedDate = date,
                    days = updatedDays,
                    // Do NOT touch headerHijriFullDateText / headerGregorianFullDateText here.
                    hijriFullDateText = state.hijriFullDateText,
                    gregorianFullDateText = state.gregorianFullDateText
                )
            }
        }
    }

    private fun buildStateFor(
        targetMonth: YearMonth,
        selectedDate: LocalDate
    ): CalendarUiState {
        val monthCalendar = generateMonthCalendar(targetMonth)
        val daysUiModels = monthCalendar.days.toUiModels(selectedDate)
        val monthTitle = MONTH_TITLE_FORMATTER.format(targetMonth.atDay(1))
        val hijriMonthSubtitle = buildHijriMonthSubtitle(monthCalendar)

        return CalendarUiState(
            isLoading = false,
            currentMonth = targetMonth,
            selectedDate = selectedDate,
            monthTitle = monthTitle,
            hijriMonthSubtitle = hijriMonthSubtitle,
            locationText = "Bahria Town Phase 8, Rawalpindi",
            hijriFullDateText = headerHijriFullDateText,
            gregorianFullDateText = headerGregorianFullDateText,
            days = daysUiModels
        )
    }

    private fun List<CalendarDay>.toUiModels(selectedDate: LocalDate): List<CalendarDayUiModel> {
        return map { day ->
            val gregorian = day.gregorianDate
            val hijri = day.hijriDate
            val isCurrent = day.isInCurrentMonth
            val isToday = isCurrent && gregorian == today && currentMonthOf(gregorian) == todayMonth

            CalendarDayUiModel(
                gregorianDate = gregorian,
                hijriDayLabel = "${hijri.day} ھـ",
                gregorianDayLabel = gregorian.dayOfMonth.toString(),
                isSelected = isToday,
                isClickable = isCurrent,
                isInCurrentMonth = isCurrent
            )
        }
    }

    private fun currentMonthOf(date: LocalDate): YearMonth = YearMonth.from(date)

    private fun buildHijriMonthSubtitle(monthCalendar: MonthCalendar): String {
        val distinctMonths = monthCalendar.days
            .filter { it.isInCurrentMonth }
            .map { it.hijriDate }
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

    internal fun setExternalSelectedDate(date: LocalDate) {
        // External API hook: align base anchors and rebuild state around the given date.
        baseMonth = YearMonth.from(date)
        baseSelectedDate = date
        _uiState.value = buildStateFor(baseMonth, baseSelectedDate)
    }

    companion object {
        private val MONTH_TITLE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
        private val GREGORIAN_FULL_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM, yyyy", Locale.ENGLISH)
    }
}