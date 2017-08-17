package com.hado.calendar

import android.text.format.Time
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by DoanNH on 8/1/2017.
 */
object TimeUtils {
    fun getWeekSinceJulianDay(firstWeekMonday: Boolean = true, calendar: Calendar): Int = getWeeksSinceEpochFromJulianDay(getJulianDay(calendar), if (firstWeekMonday) 1 else 0)

    fun getDaysOfWeek(week: Int, currentWeek: Int, firstDayOfWeek: Int, calendar: Calendar): ArrayList<Date> {
        val backupTime = calendar.time //need backup time because the calendar is changed time, and we want to keep it as it came

        calendar.firstDayOfWeek = if (firstDayOfWeek == 0) Calendar.SUNDAY else Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, if (firstDayOfWeek == 0) Calendar.SUNDAY else Calendar.MONDAY)

        val betweenNumber = week - currentWeek

        calendar.add(Calendar.DAY_OF_MONTH, betweenNumber * Calendar.DAY_OF_WEEK)
        val daysOfWeek = ArrayList<Date>()

        for (i in 0..6) {
            daysOfWeek.add(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        calendar.time = backupTime
        return daysOfWeek
    }

    fun getJulianDay(calendar: Calendar): Int {
        val backupTime = calendar.time //need backup time because the calendar is changed time, and we want to keep it as it came

        val month = calendar.get(Calendar.MONTH) + 1
        val a = (14 - month) / 12
        val y = calendar.get(Calendar.YEAR) + 4800 - a
        val m = month + 12 * a - 3

        calendar.time = backupTime
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

    fun getDateNumber(calendar: Calendar, date: Date): String {
        val backupTime = calendar.time //need backup time because the calendar is changed time, and we want to keep it as it came
        calendar.time = date
        val dateNumber = calendar.get(Calendar.DATE)
        calendar.time = backupTime
        if (dateNumber == 1) {
            return SimpleDateFormat("MMM d").format(date) //TODO: move string format to strings.xml to become flexible in difference language
        }
        return dateNumber.toString()
    }
}

fun Date.isSameDay(calendar: Calendar, secondDate: Date): Boolean {
    val backupTime = calendar.time //need backup time because the calendar is changed time, and we want to keep it as it came
    calendar.time = this

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val date = calendar.get(Calendar.DATE)

    calendar.time = secondDate
    val secondYear = calendar.get(Calendar.YEAR)
    val secondMonth = calendar.get(Calendar.MONTH)
    val secondDay = calendar.get(Calendar.DATE)

    calendar.time = backupTime

    if (year == secondYear && month == secondMonth && date == secondDay) return true

    return false
}