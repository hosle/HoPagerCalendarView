package com.hosle.calendar.pagercalendar.util

import android.content.Context

/**
 * Created by tanjiahao on 2018/9/26
 * Original Project PagerCalendar
 */
fun Context.dp2px(dpValue: Float): Int {
    val scale = resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}