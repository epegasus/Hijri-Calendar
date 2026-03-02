package qiblacompass.prayertimes.hijricalendar.presentation.calendar.adapter.pager

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * ViewPager2 adapter backed by fragments.
 *
 * Each page is a [MonthPageFragment] that hosts the shared calendar grid.
 * The [fragment] passed to the constructor is a lifecycle owner that
 * provides the FragmentManager for the pager (typically a host fragment).
 */
class PagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = Int.MAX_VALUE

    override fun createFragment(position: Int): Fragment = MonthPageFragment()

    companion object {
        const val INITIAL_POSITION: Int = Int.MAX_VALUE / 2
    }
}