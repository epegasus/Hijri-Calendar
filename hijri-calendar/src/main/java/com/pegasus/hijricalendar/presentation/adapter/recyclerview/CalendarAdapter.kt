package com.pegasus.hijricalendar.presentation.adapter.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pegasus.hijricalendar.R
import com.pegasus.hijricalendar.databinding.ItemDayBinding
import com.pegasus.hijricalendar.presentation.model.CalendarDayUiModel

import com.pegasus.hijricalendar.presentation.model.CalendarColors

internal class CalendarAdapter(
    private val colors: CalendarColors,
    private val onDayClicked: (CalendarDayUiModel) -> Unit
) : ListAdapter<CalendarDayUiModel, CalendarAdapter.DayViewHolder>(DiffCallback) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        // Use Gregorian date epoch day as a stable identifier.
        return getItem(position).gregorianDate.toEpochDay()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDayBinding.inflate(inflater, parent, false)
        return DayViewHolder(binding, colors, onDayClicked)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DayViewHolder(
        private val binding: ItemDayBinding,
        private val colors: CalendarColors,
        private val onDayClicked: (CalendarDayUiModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CalendarDayUiModel) = with(binding) {
            mtvHijriDay.text = item.hijriDayLabel
            mtvGregorianDay.text = item.gregorianDayLabel

            if (item.isInCurrentMonth) {
                mtvGregorianDay.setTextColor(
                    if (item.isSelected) colors.selectedHijriTextColor else colors.activeGregorianTextColor
                )
                mtvHijriDay.setTextColor(
                    if (item.isSelected) colors.selectedHijriTextColor else colors.activeHijriTextColor
                )
            } else {
                mtvGregorianDay.setTextColor(colors.inactiveTextColor)
                mtvHijriDay.setTextColor(colors.inactiveTextColor)
            }

            root.setBackgroundResource(
                when {
                    !item.isClickable -> R.drawable.bg_calendar_day_empty
                    item.isSelected -> R.drawable.bg_calendar_day_selected
                    else -> R.drawable.bg_calendar_day_normal
                }
            )

            root.isEnabled = item.isClickable
            root.isClickable = false
            // Original code had click handling disabled; preserve behavior.
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<CalendarDayUiModel>() {
        override fun areItemsTheSame(oldItem: CalendarDayUiModel, newItem: CalendarDayUiModel): Boolean = oldItem.gregorianDate == newItem.gregorianDate
        override fun areContentsTheSame(oldItem: CalendarDayUiModel, newItem: CalendarDayUiModel): Boolean = oldItem == newItem
    }
}