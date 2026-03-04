package com.pegasus.hijricalendar.presentation.adapter.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pegasus.hijricalendar.databinding.ItemDayBinding
import com.pegasus.hijricalendar.presentation.model.CalendarDayStyleConfig
import com.pegasus.hijricalendar.presentation.model.CalendarDayUiModel

internal class CalendarAdapter(
    private val styleConfig: CalendarDayStyleConfig,
    private val onDayClicked: (CalendarDayUiModel) -> Unit
) : ListAdapter<CalendarDayUiModel, CalendarAdapter.DayViewHolder>(DiffCallback) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).gregorianDate.toEpochDay()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayViewHolder(binding, styleConfig, onDayClicked)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DayViewHolder(
        private val binding: ItemDayBinding,
        private val styleConfig: CalendarDayStyleConfig,
        private val onDayClicked: (CalendarDayUiModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CalendarDayUiModel) = with(binding) {
            mtvHijriDay.text = item.hijriDayLabel
            mtvGregorianDay.text = item.gregorianDayLabel

            val state = when {
                !item.isClickable -> DayCellState.DISABLED
                item.isSelected -> DayCellState.SELECTED
                else -> DayCellState.UNSELECTED
            }

            root.setBackgroundResource(styleConfig.backgroundResFor(state))
            mtvGregorianDay.setTextColor(styleConfig.gregorianTextColorFor(state))
            mtvHijriDay.setTextColor(styleConfig.hijriTextColorFor(state))

            root.isEnabled = item.isClickable
            root.isClickable = false
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<CalendarDayUiModel>() {
        override fun areItemsTheSame(oldItem: CalendarDayUiModel, newItem: CalendarDayUiModel): Boolean =
            oldItem.gregorianDate == newItem.gregorianDate
        override fun areContentsTheSame(oldItem: CalendarDayUiModel, newItem: CalendarDayUiModel): Boolean =
            oldItem == newItem
    }
}

private enum class DayCellState { DISABLED, UNSELECTED, SELECTED }

private fun CalendarDayStyleConfig.backgroundResFor(state: DayCellState): Int = when (state) {
    DayCellState.DISABLED -> disabledDayBackgroundRes
    DayCellState.UNSELECTED -> unselectedDayBackgroundRes
    DayCellState.SELECTED -> selectedDayBackgroundRes
}

private fun CalendarDayStyleConfig.gregorianTextColorFor(state: DayCellState): Int = when (state) {
    DayCellState.DISABLED -> disabledGregorianTextColor
    DayCellState.UNSELECTED -> unselectedGregorianTextColor
    DayCellState.SELECTED -> selectedGregorianTextColor
}

private fun CalendarDayStyleConfig.hijriTextColorFor(state: DayCellState): Int = when (state) {
    DayCellState.DISABLED -> disabledHijriTextColor
    DayCellState.UNSELECTED -> unselectedHijriTextColor
    DayCellState.SELECTED -> selectedHijriTextColor
}
