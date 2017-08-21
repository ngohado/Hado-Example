package com.hado.calendar

import android.text.format.Time
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by DoanNH on 8/1/2017.
 */
object TimeUtils {
    fun getWeekSinceJulianDay(firstDayOfWeek: Int): Int = getWeeksSinceEpochFromJulianDay(getJulianDay(), firstDayOfWeek)

    fun getDaysOfWeek(week: Int, currentWeek: Int, firstDayOfWeek: Int): ArrayList<Date> {
        val calendar = Calendar.getInstance()
        if (firstDayOfWeek == 1) {
            calendar.add(Calendar.DAY_OF_MONTH, -1)
        }
        calendar.set(Calendar.DAY_OF_WEEK, if (firstDayOfWeek == 0) Calendar.SUNDAY else Calendar.MONDAY)

        val betweenNumber = week - currentWeek

        calendar.add(Calendar.DAY_OF_MONTH, betweenNumber * Calendar.DAY_OF_WEEK)
        val daysOfWeek = ArrayList<Date>()

        for (i in 0..6) {
            daysOfWeek.add(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return daysOfWeek
    }

    private fun getJulianDay(): Int {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1
        val a = (14 - month) / 12
        val y = calendar.get(Calendar.YEAR) + 4800 - a
        val m = month + 12 * a - 3

        return calendar.get(Calendar.DATE) + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
    }

    private fun getWeeksSinceEpochFromJulianDay(julianDay: Int, firstDayOfWeek: Int): Int {
        var diff = Time.THURSDAY - firstDayOfWeek
        if (diff < 0) {
            diff += 7
        }
        val refDay = Time.EPOCH_JULIAN_DAY - diff
        return (julianDay - refDay) / 7
    }

    fun getDateNumber(calendar: Calendar, date: Date): String {
        calendar.time = date
        val dateNumber = calendar.get(Calendar.DATE)
        if (dateNumber == 1) {
            return SimpleDateFormat("MMM d").format(date) //TODO: move string format to strings.xml to become flexible in difference language
        }
        return dateNumber.toString()
    }
}

//fun Date.isSameDay(calendar: Calendar, secondDate: Date): Boolean {
//    val backupTime = calendar.time //need backup time because the calendar is changed time, and we want to keep it as it came
//    calendar.time = this
//
//    val year = calendar.get(Calendar.YEAR)
//    val month = calendar.get(Calendar.MONTH)
//    val date = calendar.get(Calendar.DATE)
//
//    calendar.time = secondDate
//    val secondYear = calendar.get(Calendar.YEAR)
//    val secondMonth = calendar.get(Calendar.MONTH)
//    val secondDay = calendar.get(Calendar.DATE)
//
//    calendar.time = backupTime
//
//    if (year == secondYear && month == secondMonth && date == secondDay) return true
//
//    return false
//}