package com.rob729.quiethours.util

import ca.antonious.materialdaypicker.MaterialDayPicker

object Utils {
    fun selectedDays(d: List<Boolean>, materialDayPicker: MaterialDayPicker) {
        if (d[0])
            materialDayPicker.selectDay(MaterialDayPicker.Weekday.SUNDAY)
        if (d[1])
            materialDayPicker.selectDay(MaterialDayPicker.Weekday.MONDAY)
        if (d[2])
            materialDayPicker.selectDay(MaterialDayPicker.Weekday.TUESDAY)
        if (d[3])
            materialDayPicker.selectDay(MaterialDayPicker.Weekday.WEDNESDAY)
        if (d[4])
            materialDayPicker.selectDay(MaterialDayPicker.Weekday.THURSDAY)
        if (d[5])
            materialDayPicker.selectDay(MaterialDayPicker.Weekday.FRIDAY)
        if (d[6])
            materialDayPicker.selectDay(MaterialDayPicker.Weekday.SATURDAY)
    }
    fun setTimeString(i: Int): String {
        return if (i < 10) {
            "0$i"
        } else {
            "$i"
        }
    }
}