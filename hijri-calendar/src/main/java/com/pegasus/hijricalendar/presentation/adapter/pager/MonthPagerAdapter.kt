package com.pegasus.hijricalendar.presentation.adapter.pager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pegasus.hijricalendar.R
import com.pegasus.hijricalendar.presentation.adapter.recyclerview.CalendarAdapter
import com.pegasus.hijricalendar.presentation.intent.CalendarIntent
import com.pegasus.hijricalendar.presentation.model.CalendarDayStyleConfig
import com.pegasus.hijricalendar.presentation.model.CalendarDayUiModel
import com.pegasus.hijricalendar.presentation.viewmodel.CalendarViewModel

internal class MonthPagerAdapter(
    private val viewModel: CalendarViewModel,
    private val dayStyleConfig: CalendarDayStyleConfig
) : RecyclerView.Adapter<MonthPagerAdapter.MonthViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_month_page, parent, false)
        return MonthViewHolder(view).apply {
            recyclerView.adapter = CalendarAdapter(dayStyleConfig) { item ->
                viewModel.processIntent(CalendarIntent.DaySelected(item.gregorianDate))
            }
        }
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        val offset = position - INITIAL_POSITION
        val initialState = viewModel.stateForOffset(offset)
        holder.submitDays(initialState.days)
    }

    override fun getItemCount(): Int = Int.MAX_VALUE

    fun submitDaysForPosition(position: Int, days: List<CalendarDayUiModel>) {
        // Find an attached ViewHolder for the given adapter position and update its list.
        // The RecyclerView that hosts this adapter (ViewPager2's internal RecyclerView)
        // is responsible for calling this method with a visible position.
        // The holder lookup is performed externally in HijriCalendarView.
    }

    class MonthViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recyclerView: RecyclerView = view.findViewById(R.id.rcvMonthDays)

        fun submitDays(days: List<CalendarDayUiModel>) {
            (recyclerView.adapter as? CalendarAdapter)?.submitList(days)
        }
    }

    companion object {
        const val INITIAL_POSITION: Int = Int.MAX_VALUE / 2
    }
}