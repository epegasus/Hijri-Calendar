package com.pegasus.hijricalendar.presentation.listener

import java.time.LocalDate

interface HijriCalendarListener {
    fun onDateSelected(date: LocalDate)
}