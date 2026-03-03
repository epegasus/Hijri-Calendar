package qiblacompass.prayertimes.hijricalendar

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.pegasus.hijricalendar.presentation.model.HijriCalendarHeader
import com.pegasus.hijricalendar.presentation.listener.HijriCalendarListener
import qiblacompass.prayertimes.hijricalendar.databinding.ActivityMainBinding
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fullScreen()
        setUI()

        binding.topAppBar.setNavigationOnClickListener { }
        binding.topAppBar.setOnMenuItemClickListener { menuItemClick(it) }
        binding.hijriCalendar.setOnDateSelectedListener(dateSelectedListener)
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

    private fun setUI(header: HijriCalendarHeader = binding.hijriCalendar.getHeader()) {
        binding.mtvHijriFullDate.text = header.hijriFullDateText
        binding.mtvGregorianFullDate.text = header.gregorianFullDateText
    }

    private fun menuItemClick(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_calendar_settings -> {
            showAdjustDateDialog()
            true
        }

        else -> false
    }

    private fun showAdjustDateDialog() {
        binding.hijriCalendar.showHijriAdjustmentDialog { header ->
            setUI(header)
        }
    }

    private val dateSelectedListener = object : HijriCalendarListener {
        override fun onDateSelected(date: LocalDate) {
            // Host app can react to date selection here if needed.
        }
    }
}