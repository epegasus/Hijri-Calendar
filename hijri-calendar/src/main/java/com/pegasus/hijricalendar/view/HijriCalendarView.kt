package com.pegasus.hijricalendar.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.edit
import androidx.core.content.withStyledAttributes
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pegasus.hijricalendar.R
import com.pegasus.hijricalendar.data.AndroidHijriDateConverter
import com.pegasus.hijricalendar.databinding.ViewHijriCalendarBinding
import com.pegasus.hijricalendar.domain.calendar.GenerateMonthCalendarUseCase
import com.pegasus.hijricalendar.presentation.adapter.pager.MonthPagerAdapter
import com.pegasus.hijricalendar.presentation.listener.HijriCalendarListener
import com.pegasus.hijricalendar.presentation.model.CalendarDayStyleConfig
import com.pegasus.hijricalendar.presentation.model.HijriCalendarHeader
import com.pegasus.hijricalendar.presentation.state.CalendarUiState
import com.pegasus.hijricalendar.presentation.viewmodel.CalendarViewModel
import com.pegasus.hijricalendar.presentation.viewmodel.CalendarViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

class HijriCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewHijriCalendarBinding = ViewHijriCalendarBinding.inflate(LayoutInflater.from(context), this, true)

    private val dayStyleConfig: CalendarDayStyleConfig

    private val dataSource = AndroidHijriDateConverter()
    private val useCase = GenerateMonthCalendarUseCase(dataSource)
    private val viewModel: CalendarViewModel by lazy {
        val activity = (context as? FragmentActivity) ?: error("HijriCalendarView must be hosted in a FragmentActivity")
        val factory = CalendarViewModelFactory(useCase)
        ViewModelProvider(activity, factory)[CalendarViewModel::class.java]
    }

    private val monthPagerAdapter by lazy { MonthPagerAdapter(viewModel, dayStyleConfig) }

    private var currentPagerPosition: Int = MonthPagerAdapter.INITIAL_POSITION
    private var listener: HijriCalendarListener? = null
    private var lastEmittedSelectedDate: LocalDate? = null
    private var observerStarted: Boolean = false

    init {
        // Default drawable and color values (preserve existing behavior when attrs not set)
        val defaultDisabledBg = R.drawable.bg_calendar_day_empty
        val defaultUnselectedBg = R.drawable.bg_calendar_day_normal
        val defaultSelectedBg = R.drawable.bg_calendar_day_selected
            val onSurface = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface)
        val onSurfaceVariant = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceVariant)
        val onPrimary = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnPrimary)

        var disabledDayBg = defaultDisabledBg
        var unselectedDayBg = defaultUnselectedBg
        var selectedDayBg = defaultSelectedBg
        var disabledGregorian = onSurfaceVariant
        var disabledHijri = onSurfaceVariant
        var unselectedGregorian = onSurface
        var unselectedHijri = onSurfaceVariant
        var selectedGregorian = onPrimary
        var selectedHijri = onPrimary

        if (attrs != null) {
            context.withStyledAttributes(attrs, R.styleable.HijriCalendarView, defStyleAttr, 0) {
                disabledDayBg = getResourceId(R.styleable.HijriCalendarView_disabledDayBackground, defaultDisabledBg)
                unselectedDayBg = getResourceId(R.styleable.HijriCalendarView_unselectedDayBackground, defaultUnselectedBg)
                selectedDayBg = getResourceId(R.styleable.HijriCalendarView_selectedDayBackground, defaultSelectedBg)
                disabledGregorian = getColor(R.styleable.HijriCalendarView_disabledGregorianTextColor, onSurfaceVariant)
                disabledHijri = getColor(R.styleable.HijriCalendarView_disabledHijriTextColor, onSurfaceVariant)
                unselectedGregorian = getColor(R.styleable.HijriCalendarView_unselectedGregorianTextColor, onSurface)
                unselectedHijri = getColor(R.styleable.HijriCalendarView_unselectedHijriTextColor, onSurfaceVariant)
                selectedGregorian = getColor(R.styleable.HijriCalendarView_selectedGregorianTextColor, onPrimary)
                selectedHijri = getColor(R.styleable.HijriCalendarView_selectedHijriTextColor, onPrimary)
            }
        }

        dayStyleConfig = CalendarDayStyleConfig(
            disabledDayBackgroundRes = disabledDayBg,
            unselectedDayBackgroundRes = unselectedDayBg,
            selectedDayBackgroundRes = selectedDayBg,
            disabledGregorianTextColor = disabledGregorian,
            disabledHijriTextColor = disabledHijri,
            unselectedGregorianTextColor = unselectedGregorian,
            unselectedHijriTextColor = unselectedHijri,
            selectedGregorianTextColor = selectedGregorian,
            selectedHijriTextColor = selectedHijri
        )

        // Load persisted Hijri adjustment and apply to converter
        val adjustment = readHijriAdjustment()
        dataSource.offsetDays = adjustment

        setupViewPager()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!observerStarted) {
            initObserver()
            observerStarted = true
        }
    }

    private fun setupViewPager() {
        binding.vpCalendarMonths.apply {
            adapter = monthPagerAdapter
            offscreenPageLimit = 3
            setCurrentItem(currentPagerPosition, false)

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    currentPagerPosition = position
                    val offset = position - MonthPagerAdapter.INITIAL_POSITION
                    viewModel.setCurrentPage(offset)
                }
            })
        }

        binding.mbPreviousMonth.setOnClickListener { previousMonth() }
        binding.mbNextMonth.setOnClickListener { nextMonth() }
    }

    private fun initObserver() {
        val lifecycleOwner = findViewTreeLifecycleOwner() ?: return

        lifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                render(state)
                notifySelectionIfChanged(state.selectedDate)

                // Update the currently visible month page's grid.
                val viewPagerRecyclerView = binding.vpCalendarMonths.getChildAt(0) as? androidx.recyclerview.widget.RecyclerView
                val holder = viewPagerRecyclerView
                    ?.findViewHolderForAdapterPosition(currentPagerPosition) as? MonthPagerAdapter.MonthViewHolder
                holder?.submitDays(state.days)
            }
        }
    }

    private fun render(state: CalendarUiState) {
        binding.apply {
            mtvTitleCalendarGregorianMonth.text = state.monthTitle
            mtvBodyCalendarHijriMonth.text = state.hijriMonthSubtitle
        }
    }

    private fun notifySelectionIfChanged(date: LocalDate) {
        if (lastEmittedSelectedDate != date) {
            lastEmittedSelectedDate = date
            listener?.onDateSelected(date)
        }
    }

    // region Public API

    fun getHeader(): HijriCalendarHeader {
        val state = viewModel.uiState.value
        return HijriCalendarHeader(
            hijriFullDateText = state.hijriFullDateText,
            gregorianFullDateText = state.gregorianFullDateText
        )
    }

    fun setDate(date: LocalDate) {
        viewModel.setExternalSelectedDate(date)
        // Move ViewPager to the page representing the selected date.
        currentPagerPosition = MonthPagerAdapter.INITIAL_POSITION
        binding.vpCalendarMonths.setCurrentItem(currentPagerPosition, false)
    }

    fun nextMonth() {
        binding.vpCalendarMonths.currentItem += 1
    }

    fun previousMonth() {
        binding.vpCalendarMonths.currentItem -= 1
    }

    fun setOnDateSelectedListener(listener: HijriCalendarListener) {
        this.listener = listener
    }

    fun getSelectedDate(): LocalDate = viewModel.uiState.value.selectedDate

    /**
     * Show the adjustment dialog for Hijri date offset. This preserves the original
     * behavior previously owned by MainActivity.
     */
    fun showHijriAdjustmentDialog(onApplied: ((HijriCalendarHeader) -> Unit)? = null) {
        val options = intArrayOf(-3, -2, -1, 0, 1, 2, 3)
        val labels = options.map { it.toString() }.toTypedArray()
        val currentAdjustment = readHijriAdjustment()
        val currentIndex = options.indexOf(currentAdjustment).takeIf { it >= 0 } ?: 3 // default 0 at index 3
        var selectedIndex = currentIndex

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.dialog_adjust_hijri_title)
            .setSingleChoiceItems(labels, currentIndex) { _, which ->
                selectedIndex = which
            }
            .setNegativeButton(R.string.dialog_adjust_hijri_cancel, null)
            .setPositiveButton(R.string.dialog_adjust_hijri_save) { _, _ ->
                val newAdjustment = options[selectedIndex]
                saveHijriAdjustment(newAdjustment)
                dataSource.offsetDays = newAdjustment
                // Refresh header full date (today) and rebuild state with updated Hijri mapping
                viewModel.refreshHeaderForToday()
                val offset = currentPagerPosition - MonthPagerAdapter.INITIAL_POSITION
                viewModel.setCurrentPage(offset)
                onApplied?.invoke(getHeader())
            }
            .show()
    }

    /**
     * Programmatically set the Hijri date adjustment in days.
     *
     * This mirrors the behavior of confirming a value in the adjustment dialog:
     * the value is persisted, the converter offset is updated, and the
     * calendar/header state is recomputed for the current page.
     *
     * @param value Number of days to offset Hijri dates
     * (typically in the range -3..3, where positive values move the Hijri date forward).
     */
    fun setHijriAdjustmentDays(value: Int) {
        saveHijriAdjustment(value)
        dataSource.offsetDays = value
        viewModel.refreshHeaderForToday()
        val offset = currentPagerPosition - MonthPagerAdapter.INITIAL_POSITION
        viewModel.setCurrentPage(offset)
    }

    // endregion

    // region Persistence

    private fun readHijriAdjustment(): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_HIJRI_ADJUSTMENT, 0)
    }

    private fun saveHijriAdjustment(value: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putInt(KEY_HIJRI_ADJUSTMENT, value) }
    }

    // endregion

    private companion object {
        private const val PREFS_NAME = "calendar_prefs"
        private const val KEY_HIJRI_ADJUSTMENT = "hijri_adjustment_days"
    }
}