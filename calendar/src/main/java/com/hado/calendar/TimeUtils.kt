package com.hado.calendar

import android.text.format.Time
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by DoanNH on 8/1/2017.
 */
object TimeUtils {
    fun getWeekSinceJulianDay(firstWeekMonday: Boolean = true): Int = getWeeksSinceEpochFromJulianDay(getJulianDay(), if (firstWeekMonday) 1 else 0)

    fun getDaysOfWeek(week: Int, currentWeek: Int): ArrayList<Date> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val betweenNumber = week - currentWeek
        calendar.add(Calendar.DAY_OF_MONTH, betweenNumber * Calendar.DAY_OF_WEEK)
        val daysOfWeek = ArrayList<Date>()

        for (i in 0..6) {
            daysOfWeek.add(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return daysOfWeek
    }

    fun getJulianDay(): Int {
        val calendar = Calendar.getInstance()

        val month = calendar.get(Calendar.MONTH) + 1
        val a = (14 - month) / 12
        val y = calendar.get(Calendar.YEAR) + 4800 - a
        val m = month + 12 * a - 3

        return calendar.get(Calendar.DATE) + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
    }

    fun getWeeksSinceEpochFromJulianDay(julianDay: Int, firstDayOfWeek: Int): Int {
        var diff = Time.THURSDAY - firstDayOfWeek
        if (diff < 0) {
            diff += 7
        }
        val refDay = Time.EPOCH_JULIAN_DAY - diff
        return (julianDay - refDay) / 7
    }
}