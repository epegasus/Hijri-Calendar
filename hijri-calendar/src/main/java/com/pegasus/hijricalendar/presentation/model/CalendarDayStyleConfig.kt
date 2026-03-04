package com.pegasus.hijricalendar.presentation.model

/**
 * Configuration for calendar day cell appearance.
 * Holds drawable resource ids for backgrounds and color values for text,
 * keyed by state: DISABLED, UNSELECTED, SELECTED.
 */
internal data class CalendarDayStyleConfig(
    val disabledDayBackgroundRes: Int,
    val unselectedDayBackgroundRes: Int,
    val selectedDayBackgroundRes: Int,
    val disabledGregorianTextColor: Int,
    val disabledHijriTextColor: Int,
    val unselectedGregorianTextColor: Int,
    val unselectedHijriTextColor: Int,
    val selectedGregorianTextColor: Int,
    val selectedHijriTextColor: Int
)