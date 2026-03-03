package qiblacompass.prayertimes.hijricalendar

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import qiblacompass.prayertimes.hijricalendar.data.calendar.AndroidHijriDateConverter
import qiblacompass.prayertimes.hijricalendar.databinding.ActivityMainBinding
import qiblacompass.prayertimes.hijricalendar.domain.calendar.GenerateMonthCalendarUseCase
import qiblacompass.prayertimes.hijricalendar.presentation.calendar.adapter.pager.PagerAdapter
import qiblacompass.prayertimes.hijricalendar.presentation.calendar.fragment.PagerHostFragment
import qiblacompass.prayertimes.hijricalendar.presentation.calendar.state.CalendarUiState
import qiblacompass.prayertimes.hijricalendar.presentation.calendar.viewModel.CalendarViewModel
import qiblacompass.prayertimes.hijricalendar.presentation.calendar.viewModel.CalendarViewModelFactory

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    // MVVM
    private val dataSource by lazy { AndroidHijriDateConverter() }
    private val useCase by lazy { GenerateMonthCalendarUseCase(dataSource) }
    val viewModel: CalendarViewModel by viewModels { CalendarViewModelFactory(useCase) }

    private val pagerAdapter by lazy { PagerAdapter(pagerHostFragment) }

    private var currentPagerPosition: Int = PagerAdapter.INITIAL_POSITION

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load persisted Hijri adjustment and apply to converter
        val adjustment = readHijriAdjustment()
        dataSource.offsetDays = adjustment

        fullScreen()
        setupViewPager()
        initObserver()

        binding.topAppBar.setNavigationOnClickListener { }
        binding.topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_calendar_settings -> {
                    showAdjustHijriDialog()
                    true
                }

                else -> false
            }
        }
        binding.mbPreviousMonth.setOnClickListener { binding.vpCalendarMonths.currentItem -= 1 }
        binding.mbNextMonth.setOnClickListener { binding.vpCalendarMonths.currentItem += 1 }
    }

    private fun fullScreen() {
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupViewPager() {
        binding.vpCalendarMonths.apply {
            adapter = pagerAdapter
            offscreenPageLimit = 3
            setCurrentItem(currentPagerPosition, false)

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    currentPagerPosition = position
                    val offset = position - PagerAdapter.INITIAL_POSITION
                    viewModel.setCurrentPage(offset)
                }
            })
        }
    }

    private val pagerHostFragment: Fragment
        get() {
            val tag = "calendar_pager_host"
            val existing = supportFragmentManager.findFragmentByTag(tag)
            if (existing is PagerHostFragment) return existing

            val created = PagerHostFragment()
            supportFragmentManager.beginTransaction()
                .add(created, tag)
                .commitNow()
            return created
        }

    private fun showAdjustHijriDialog() {
        val options = intArrayOf(-3, -2, -1, 0, 1, 2, 3)
        val labels = options.map { it.toString() }.toTypedArray()
        val currentAdjustment = readHijriAdjustment()
        val currentIndex = options.indexOf(currentAdjustment).takeIf { it >= 0 } ?: 3 // default 0 at index 3
        var selectedIndex = currentIndex

        MaterialAlertDialogBuilder(this)
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
                val offset = currentPagerPosition - PagerAdapter.INITIAL_POSITION
                viewModel.setCurrentPage(offset)
            }
            .show()
    }

    private fun readHijriAdjustment(): Int {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return prefs.getInt(KEY_HIJRI_ADJUSTMENT, 0)
    }

    private fun saveHijriAdjustment(value: Int) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit { putInt(KEY_HIJRI_ADJUSTMENT, value) }
    }

    private fun initObserver() {
        lifecycleScope.launch { viewModel.uiState.collectLatest { render(it) } }
    }

    private fun render(state: CalendarUiState) {
        binding.apply {
            mtvMyLocation.text = state.locationText
            mtvHijriFullDate.text = state.hijriFullDateText
            mtvGregorianFullDate.text = state.gregorianFullDateText
            mtvTitleCalendarGregorianMonth.text = state.monthTitle
            mtvBodyCalendarHijriMonth.text = state.hijriMonthSubtitle
        }
    }

    companion object {
        private const val PREFS_NAME = "calendar_prefs"
        private const val KEY_HIJRI_ADJUSTMENT = "hijri_adjustment_days"
    }
}