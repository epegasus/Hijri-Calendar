package qiblacompass.prayertimes.hijricalendar.presentation.calendar.fragment

import androidx.fragment.app.Fragment

/**
 * Headless fragment used only to provide a FragmentManager / Lifecycle
 * owner for the ViewPager2's FragmentStateAdapter.
 *
 * This fragment does not have a UI; it is attached to the Activity
 * purely as an adapter host.
 */
class PagerHostFragment : Fragment()