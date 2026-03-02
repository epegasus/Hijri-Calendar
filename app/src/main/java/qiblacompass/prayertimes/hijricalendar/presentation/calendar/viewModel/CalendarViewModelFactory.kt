package qiblacompass.prayertimes.hijricalendar.presentation.calendar.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import qiblacompass.prayertimes.hijricalendar.domain.calendar.GenerateMonthCalendarUseCase

class CalendarViewModelFactory(private val generateMonthCalendarUseCase: GenerateMonthCalendarUseCase) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            return CalendarViewModel(generateMonthCalendar = generateMonthCalendarUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}