package qiblacompass.prayertimes.hijricalendar.domain.calendar

import qiblacompass.prayertimes.hijricalendar.domain.model.HijriDate
import java.time.LocalDate

/**
 * Abstraction for converting a Gregorian date to Hijri.
 * Implemented in the data layer to avoid Android dependencies in domain.
 */
interface HijriDateConverter {
    fun toHijri(date: LocalDate): HijriDate
}