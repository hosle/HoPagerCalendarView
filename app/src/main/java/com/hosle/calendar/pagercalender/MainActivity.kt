package com.hosle.calendar.pagercalender

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import android.widget.Toast
import com.hosle.calendar.pagercalendar.MonthView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import android.graphics.drawable.Drawable



class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val todayCalendar =Calendar.getInstance()

        setParams(arrayOf(arrayOf(todayCalendar.get(Calendar.YEAR), todayCalendar.get(Calendar.MONTH)+1),
                arrayOf(todayCalendar.get(Calendar.YEAR), todayCalendar.get(Calendar.MONTH)+2)),
                object :MonthView.OnDayClickListener{
                    override fun onDayClick(view: MonthView, day: Calendar) {
                        Toast.makeText(view.context,"${day.get(Calendar.MONTH) + 1} - ${day.get(Calendar.DAY_OF_MONTH)}",Toast.LENGTH_SHORT).show()
                    }
                },
                { calendar -> if (calendar.get(Calendar.DAY_OF_MONTH) == 18) 1 else 0 },
                taskBitmap = getBitmap(R.drawable.shape_green_circle))
    }

    private fun setParams(monthArrange: Array<Array<Int>>, onDayClickListener: MonthView.OnDayClickListener? = null,
                  operationForTaskCount: ((Calendar) -> Int)? = null, taskBitmap: Bitmap? = null) {

        view_calendar.setCalendarParams(monthArrange, onDayClickListener,
                operationForTaskCount, taskBitmap)
    }

    private fun getBitmap(drawableRes: Int) : Bitmap{
        val drawable = resources.getDrawable(drawableRes)
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)

        return bitmap
    }
}
