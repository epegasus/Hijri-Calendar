package qiblacompass.prayertimes.hijricalendar.presentation.calendar.adapter.recyclerView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import qiblacompass.prayertimes.hijricalendar.R
import qiblacompass.prayertimes.hijricalendar.databinding.ItemDayBinding
import qiblacompass.prayertimes.hijricalendar.presentation.calendar.model.CalendarDayUiModel

class CalendarAdapter(private val onDayClicked: (CalendarDayUiModel) -> Unit) : ListAdapter<CalendarDayUiModel, CalendarAdapter.DayViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDayBinding.inflate(inflater, parent, false)
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DayViewHolder(private val binding: ItemDayBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CalendarDayUiModel) = with(binding) {
            mtvHijriDay.text = item.hijriDayLabel
            mtvGregorianDay.text = item.gregorianDayLabel

            mtvHijriDay.isVisible = item.isClickable
            mtvGregorianDay.isVisible = item.isClickable

            root.setBackgroundResource(
                when {
                    !item.isClickable -> R.drawable.bg_calendar_day_empty
                    item.isSelected -> R.drawable.bg_calendar_day_selected
                    else -> R.drawable.bg_calendar_day_normal
                }
            )

            root.isEnabled = item.isClickable
            root.isClickable = item.isClickable

            root.setOnClickListener {
                if (item.isClickable) {
                    onDayClicked(item)
                }
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<CalendarDayUiModel>() {
        override fun areItemsTheSame(oldItem: CalendarDayUiModel, newItem: CalendarDayUiModel): Boolean = oldItem.gregorianDate == newItem.gregorianDate
        override fun areContentsTheSame(oldItem: CalendarDayUiModel, newItem: CalendarDayUiModel): Boolean = oldItem == newItem
    }
}