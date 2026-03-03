package qiblacompass.prayertimes.hijricalendar.presentation.calendar.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import qiblacompass.prayertimes.hijricalendar.MainActivity
import qiblacompass.prayertimes.hijricalendar.R
import qiblacompass.prayertimes.hijricalendar.presentation.calendar.adapter.pager.PagerAdapter
import qiblacompass.prayertimes.hijricalendar.presentation.calendar.adapter.recyclerView.CalendarAdapter
import qiblacompass.prayertimes.hijricalendar.presentation.calendar.intent.CalendarIntent
import qiblacompass.prayertimes.hijricalendar.presentation.calendar.viewModel.CalendarViewModel

/**
 * Single ViewPager2 page that shows the month grid.
 *
 * Each page owns its own RecyclerView adapter but observes the
 * shared [CalendarViewModel] so that swipes and button presses
 * update all visible pages smoothly.
 */
class MonthPageFragment : Fragment(R.layout.item_month_page) {

    private val sharedViewModel: CalendarViewModel get() = (requireActivity() as MainActivity).viewModel

    private val calendarAdapter by lazy {
        CalendarAdapter { item ->
            item.gregorianDate?.let { date ->
                sharedViewModel.processIntent(CalendarIntent.DaySelected(date))
            }
        }
    }

    private val pageOffset: Int by lazy {
        val position = arguments?.getInt(ARG_POSITION) ?: 0
        position - PagerAdapter.INITIAL_POSITION
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.rcvMonthDays)
        recyclerView.adapter = calendarAdapter
        recyclerView.setHasFixedSize(true)

        // Pre-fill this page's month grid based on its offset
        val initialState = sharedViewModel.stateForOffset(pageOffset)
        calendarAdapter.submitList(initialState.days)

        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.uiState.collectLatest { state ->
                // Only update if this page represents the current visible month
                if (state.currentMonth == initialState.currentMonth) {
                    calendarAdapter.submitList(state.days)
                }
            }
        }
    }

    companion object {
        private const val ARG_POSITION = "arg_position"

        fun newInstance(position: Int): MonthPageFragment = MonthPageFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_POSITION, position)
            }
        }
    }
}