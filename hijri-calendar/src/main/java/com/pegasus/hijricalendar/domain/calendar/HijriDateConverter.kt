package com.pegasus.hijricalendar.domain.calendar

import com.pegasus.hijricalendar.domain.model.HijriDate
import java.time.LocalDate

internal interface HijriDateConverter {
    fun toHijri(date: LocalDate): HijriDate
}