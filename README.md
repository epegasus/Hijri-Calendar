[![](https://jitpack.io/v/epegasus/Hijri-Calendar.svg)](https://jitpack.io/#epegasus/Hijri-Calendar)

# Hijri Calendar

A reusable Android library that provides a **Hijri (Islamic) calendar** as a single custom view. It shows Gregorian and Hijri dates in a month grid with optional day-offset adjustment and Material 3–friendly styling.

---

## Features

- **`HijriCalendarView`** — Drop-in `FrameLayout` with month grid, week headers, and prev/next navigation
- **Dual dates** — Each cell shows both Hijri and Gregorian day
- **Configurable Hijri offset** — Adjust displayed Hijri date by -3 to +3 days (dialog or programmatic)
- **Customizable appearance** — XML attributes for day backgrounds and text colors (disabled / unselected / selected)
- **Public API** — Set date, get selected date, navigate months, listen for selection, get header text for your own UI
- **Material 3** — Default colors from theme; works with DayNight

---

## ScreenShot

<img width="360" height="800" alt="image" src="https://github.com/user-attachments/assets/9fa306c6-0537-4d40-9899-91dd713f32a3" />

---

## Requirements

- **minSdk:** 23  
- **compileSdk:** 36 (or match your app)  
- **Kotlin,** **Java 17**  
- Host must be a **`FragmentActivity`** (e.g. `AppCompatActivity`)

---

## Installation (JitPack)

**1. Add the JitPack repository** (project-level `settings.gradle` or `settings.gradle.kts`):

```gradle
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

**2. Add the dependency** (app-level `build.gradle` or `build.gradle.kts`): Use the latest version: [![](https://jitpack.io/v/epegasus/Hijri-Calendar.svg)](https://jitpack.io/#epegasus/Hijri-Calendar)


```gradle
dependencies {
    implementation("com.github.epegasus:Hijri-Calendar:1.0.1")
}
```

If your app uses **core library desugaring** for `java.time` on older devices, enable it in the app module:

```gradle
android {
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}
dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
}
```

---

## Quick Start

**1. Add the view in XML** (e.g. your Activity layout):

```xml
<com.pegasus.hijricalendar.view.HijriCalendarView
    android:id="@+id/hijriCalendar"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

**2. In your Activity/Fragment (Kotlin):**

```kotlin
binding.hijriCalendar.setOnDateSelectedListener(object : HijriCalendarListener {
    override fun onDateSelected(date: LocalDate) {
        // Handle date selection
    }
})

// Optional: show dialog to adjust Hijri date offset
binding.hijriCalendar.showHijriAdjustmentDialog { header ->
    // Update your header UI with header.hijriFullDateText, header.gregorianFullDateText
}

// Optional: get current header strings for your own header card
val header = binding.hijriCalendar.getHeader()
textViewHijri.text = header.hijriFullDateText
textViewGregorian.text = header.gregorianFullDateText
```

---

## Public API

| Method | Description |
|--------|-------------|
| `getHeader(): HijriCalendarHeader` | Returns current Hijri and Gregorian full-date strings for the header |
| `setDate(date: LocalDate)` | Sets the selected date and jumps the pager to that month |
| `nextMonth()` | Swipes to the next month |
| `previousMonth()` | Swipes to the previous month |
| `setOnDateSelectedListener(listener: HijriCalendarListener)` | Callback when the selected date changes |
| `getSelectedDate(): LocalDate` | Returns the currently selected Gregorian date |
| `showHijriAdjustmentDialog(onApplied: ((HijriCalendarHeader) -> Unit)?)` | Shows dialog to pick Hijri offset (-3..+3); optional callback with updated header |
| `setHijriAdjustmentDays(value: Int)` | Sets Hijri offset programmatically (e.g. -1, 0, 1) |

**Listener:**

```kotlin
interface HijriCalendarListener {
    fun onDateSelected(date: LocalDate)
}
```

---

## Customization (XML attributes)

You can style day cells with drawables and colors.

**Backgrounds (drawable references):**

| Attribute | Description |
|-----------|-------------|
| `app:disabledDayBackground` | Background for days outside the current month |
| `app:unselectedDayBackground` | Background for normal days in the month |
| `app:selectedDayBackground` | Background for the selected (e.g. today) day |

**Text colors (color or `@color` reference):**

| Attribute | Description |
|-----------|-------------|
| `app:disabledGregorianTextColor` | Gregorian text when disabled |
| `app:disabledHijriTextColor` | Hijri text when disabled |
| `app:unselectedGregorianTextColor` | Gregorian text when unselected |
| `app:unselectedHijriTextColor` | Hijri text when unselected |
| `app:selectedGregorianTextColor` | Gregorian text when selected |
| `app:selectedHijriTextColor` | Hijri text when selected |

**Example:**

```xml
<com.pegasus.hijricalendar.view.HijriCalendarView
    android:id="@+id/hijriCalendar"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:disabledDayBackground="@drawable/bg_calendar_day_empty"
    app:unselectedDayBackground="@drawable/bg_calendar_day_normal"
    app:selectedDayBackground="@drawable/bg_calendar_day_selected"
    app:disabledGregorianTextColor="@color/gray_400"
    app:disabledHijriTextColor="@color/gray_400"
    app:unselectedGregorianTextColor="@color/black"
    app:unselectedHijriTextColor="@color/gray_700"
    app:selectedGregorianTextColor="@color/white"
    app:selectedHijriTextColor="@color/white" />
```

If you omit these, the library uses Material 3 theme attributes and its default drawables.

---

## Project structure

- **`hijri-calendar`** — Android library module (the artifact you consume).
- **`app`** — Sample app that uses the library.

---

## License

[Specify your license here, e.g. MIT, Apache 2.0.]
