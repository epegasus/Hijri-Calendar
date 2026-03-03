package qiblacompass.prayertimes.hijricalendar.presentation.calendar.adapter.pager

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import qiblacompass.prayertimes.hijricalendar.presentation.calendar.fragment.MonthPageFragment

/**
 * ViewPager2 adapter backed by fragments.
 *
 * Each page is a [qiblacompass.prayertimes.hijricalendar.presentation.calendar.fragment.MonthPageFragment] that hosts the calendar grid.
 * The [fragment] passed to the constructor is the host fragment used
 * by FragmentStateAdapter for FragmentManager/Lifecycle.
 */
class PagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = Int.MAX_VALUE

    override fun createFragment(position: Int): Fragment = MonthPageFragment.newInstance(position)

    companion object {
        const val INITIAL_POSITION: Int = Int.MAX_VALUE / 2
    }
}