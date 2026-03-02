package qiblacompass.prayertimes.hijricalendar.presentation.calendar.adapter.pager

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import qiblacompass.prayertimes.hijricalendar.MainActivity
import qiblacompass.prayertimes.hijricalendar.R

/**
 * Single ViewPager2 page that shows the month grid.
 *
 * It reuses the shared CalendarAdapter exposed by [MainActivity],
 * so all pages always render the current month from the ViewModel.
 */
class MonthPageFragment : Fragment(R.layout.item_month_page) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.rcvMonthDays)
        val hostActivity = requireActivity() as MainActivity

        if (recyclerView.layoutManager == null) {
            recyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
        }
        if (recyclerView.adapter == null) {
            recyclerView.adapter = hostActivity.adapter
        }
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = null
    }
}

