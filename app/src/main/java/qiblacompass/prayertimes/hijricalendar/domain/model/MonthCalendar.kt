package qiblacompass.prayertimes.hijricalendar.domain.model

import java.time.YearMonth

data class MonthCalendar(
    val month: YearMonth,
    val days: List<CalendarDay>
)