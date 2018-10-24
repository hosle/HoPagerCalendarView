package com.hosle.calendar.pagercalendar

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.hosle.calendar.pagercalendar.util.dp2px
import java.text.NumberFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * Created by tanjiahao on 2018/9/26
 * Original Project PagerCalendar
 */


class MonthView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val DAYS_IN_WEEK = 7
    private val MAX_WEEKS_IN_MONTH = 6

    private val calendar = Calendar.getInstance()
    private var daysInMonth = 0
    private var month = 0
    private var year = 0
    private var dayOfWeekStart = 0
    private var weekStart = 0
    private var today = -1

    private var paddedWidth = 0
    private var paddedHeight = 0
    private var cellWidth = 0
    private var cellHeight = 0
    private var monthHeight = context.dp2px(0f)
    private var monthLabelPaddingRight = context.dp2px(10f)

    private val paintCell = TextPaint()
    private val paintTaskTag = Paint()
    private val paintHighLightDay = Paint()
    private val dayFormatter: NumberFormat

    private var highLightDay:Int = 0

    private var onDayClickListener: OnDayClickListener? = null

    private var operationForTaskCount: ((Calendar) -> Int)? = null
    private var taskBitmap:Bitmap? = null

    private val colorCell = Color.parseColor("#000000")
    private val colorCell2 = Color.parseColor("#8cb4ff")
    private val colorCellTextWithTask = Color.parseColor("#ffffff")
    private val colorPressHeighLight = Color.parseColor("#999999")

    init {
        calendar.firstDayOfWeek = Calendar.SUNDAY

        val locale = context.resources.configuration.locale
        dayFormatter = NumberFormat.getIntegerInstance(locale)

        initPaints()

    }

    interface OnDayClickListener {
        fun onDayClick(view: MonthView, day: Calendar)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val x = (event!!.x + 0.5f).toInt()
        val y = (event.y + 0.5f).toInt()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val touchItem = getDayAtLocation(x, y)
                highLightDay = touchItem
            }
            MotionEvent.ACTION_UP -> {
                val clickedDay = getDayAtLocation(x, y)
                highLightDay = -1
                onDayClicked(clickedDay)
            }
            MotionEvent.ACTION_CANCEL -> {
                highLightDay = -1
            }
        }
        invalidate()

        return true
    }

    fun setMonthParams(_month: Int, _year: Int, _weekStart: Int = Calendar.SUNDAY,
                       operation: ((Calendar) -> Int)? = null,
                       taskBitmap: Bitmap? = null) {

        operationForTaskCount = operation
        this.taskBitmap = taskBitmap

        if (isValidMonth(_month - 1)) {
            month = _month - 1
        }
        year = _year

        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        daysInMonth = getDaysInMonth(month, year)
        dayOfWeekStart = calendar.get(Calendar.DAY_OF_WEEK)

        weekStart = if (isValidDayOfWeek(_weekStart)) {
            _weekStart
        } else {
            calendar.firstDayOfWeek
        }

        today = -1
        for (i in 0 until daysInMonth) {
            val day = i + 1
            if (sameDay(day, Calendar.getInstance())) {
                today = day
            }
        }

        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val paddedWidth = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        cellWidth = paddedWidth / DAYS_IN_WEEK
        cellHeight = cellWidth

        val preferredHeight = (cellHeight * MAX_WEEKS_IN_MONTH
                + monthHeight
                + paddingTop + paddingBottom)
        val resolvedHeight = View.resolveSize(preferredHeight, heightMeasureSpec)
        setMeasuredDimension(widthMeasureSpec, resolvedHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val w = right - left
        val h = bottom - top
        val paddedRight = w - paddingRight
        val paddedBottom = h - paddingBottom
        val paddedWidth = paddedRight - paddingLeft
        val paddedHeight = paddedBottom - paddingTop
        if (paddedWidth == this.paddedWidth || paddedHeight == this.paddedHeight) {
            return
        }

        this.paddedWidth = paddedWidth
        this.paddedHeight = paddedHeight

        cellWidth = paddedWidth / DAYS_IN_WEEK
        cellHeight = cellWidth

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawDays(canvas!!)

    }

    fun setOnDayClickListener(listener: OnDayClickListener?) {
        onDayClickListener = listener
    }

    fun getMonth(): Int {
        return month + 1
    }

    fun getYear():Int{
        return year
    }

    private fun onDayClicked(day: Int): Boolean {
        if (!isValidDayOfMonth(day) /*|| !isDayEnabled(day)*/) {
            return false
        }

        onDayClickListener?.run {
            val date = Calendar.getInstance()
            date.set(year, month, day)
            this.onDayClick(this@MonthView, date)
        }

        // This is a no-op if accessibility is turned off.
//        mTouchHelper.sendEventForVirtualView(day, AccessibilityEvent.TYPE_VIEW_CLICKED)
        return true
    }


    private fun getDayAtLocation(x: Int, y: Int): Int {
        val paddedX = x - paddingLeft
        if (paddedX < 0 || paddedX >= paddedWidth) {
            return -1
        }

        val headerHeight = monthHeight /*+ dayOfWeekHeight*/
        val paddedY = y - paddingTop
        if (paddedY < headerHeight || paddedY >= paddedHeight) {
            return -1
        }


        val row = (paddedY - headerHeight) / cellHeight
        val col = paddedX * DAYS_IN_WEEK / paddedWidth
        val index = col + row * DAYS_IN_WEEK
        val day = index + 1 - findDayOffset()
        return if (!isValidDayOfMonth(day)) {
            -1
        } else day

    }

    private fun dayBeforeToday(dayOfMonth: Int): Boolean {
        val today = Calendar.getInstance()

        return when {
            year == today.get(Calendar.YEAR) -> {
                val targetDay = Calendar.getInstance()
                targetDay.set(Calendar.YEAR, year)
                targetDay.set(Calendar.MONTH, month)
                targetDay.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                today.get(Calendar.DAY_OF_YEAR) - targetDay.get(Calendar.DAY_OF_YEAR) > 0
            }
            year < today.get(Calendar.YEAR) -> true
            else -> false
        }
    }

    private fun dayAfterToday(dayOfMonth: Int): Boolean {

        if (!sameDay(dayOfMonth, Calendar.getInstance())) {
            return !dayBeforeToday(dayOfMonth)
        }
        return false

    }

    private fun sameDay(day: Int, today: Calendar): Boolean {
        return (year == today.get(Calendar.YEAR) && month == today.get(Calendar.MONTH)
                && day == today.get(Calendar.DAY_OF_MONTH))
    }

    private fun isValidDayOfMonth(day: Int): Boolean {
        return day in 1..daysInMonth
    }

    private fun drawDays(canvas: Canvas) {
        val paintText = paintCell
        val paintTag = paintTaskTag
        val paintHighLight = paintHighLightDay

        val colWidth = cellWidth

        var col = findDayOffset()
        val rowHeight = cellHeight
        var rowCenter = rowHeight / 2 + monthHeight
        val halfLineHeight = (paintText.ascent() + paintText.descent()) / 2f


        for (day in 1..daysInMonth) {
            val cellStartX = colWidth * col
            val cellStartY = rowCenter - rowHeight / 2
            val colCenter = cellStartX + colWidth / 2
            val colCenterRtl = colCenter

            val isDayHighLight = highLightDay ==day

            if(isDayHighLight){
                canvas.drawCircle(colCenterRtl.toFloat(), rowCenter.toFloat(), cellWidth / 3f, paintHighLight)
            }

            val dayString = day.toString()

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)

            val taskCount: Int = operationForTaskCount?.invoke(calendar) ?: 0

            drawTaskTag(canvas, cellStartX.toFloat(), cellStartY.toFloat(), calendar, taskCount, paintTag)
            drawText(canvas, dayString, colCenterRtl.toFloat(), rowCenter - halfLineHeight,
                    calendar, taskCount, paintText)

            if (++col == DAYS_IN_WEEK) {
                col = 0
                rowCenter += rowHeight
            }
        }

    }

    private fun drawText(canvas: Canvas, dayString:String,x: Float, y: Float,
                         calendar: Calendar,taskCount:Int, paint: Paint){

        if (taskCount > 0) {
            paint.color = colorCellTextWithTask
        } else when (calendar.get(Calendar.DAY_OF_WEEK)) {
            1, 7 -> {
                paint.color = colorCell2
            }
            else -> {
                paint.color = colorCell
            }
        }

        canvas.drawText(dayString, x, y, paint)
    }

    private fun drawTaskTag(canvas: Canvas, x: Float, y: Float, calendar: Calendar,
                            taskCount:Int, paint: Paint) {
        val bitmap = taskBitmap
        if (bitmap != null) {
            val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
            val destRect = Rect(x.roundToInt(), y.roundToInt(), x.roundToInt() + cellWidth, y.roundToInt() + cellHeight)

            when (taskCount) {
                0 -> {
                }
                else -> {
                    canvas.drawBitmap(bitmap, srcRect, destRect, paint)
                }
            }
        }
    }

    private fun findDayOffset(): Int {
        val offset = dayOfWeekStart - weekStart
        return if (dayOfWeekStart < weekStart) {
            offset + DAYS_IN_WEEK
        } else offset
    }

    private fun isValidDayOfWeek(day: Int): Boolean {
        return day >= Calendar.SUNDAY && day <= Calendar.SATURDAY
    }

    private fun getDaysInMonth(month: Int, year: Int): Int {
        return when (month) {
            Calendar.JANUARY, Calendar.MARCH, Calendar.MAY, Calendar.JULY, Calendar.AUGUST, Calendar.OCTOBER, Calendar.DECEMBER -> 31
            Calendar.APRIL, Calendar.JUNE, Calendar.SEPTEMBER, Calendar.NOVEMBER -> 30
            Calendar.FEBRUARY -> if (year % 4 == 0) 29 else 28
            else -> throw IllegalArgumentException("Invalid Month")
        }
    }

    private fun isValidMonth(month: Int): Boolean {
        return month >= Calendar.JANUARY && month <= Calendar.DECEMBER
    }

    private fun initPaints() {

        paintCell.isAntiAlias = true
        paintCell.color = colorCell
        paintCell.textSize = context.dp2px(15f).toFloat()
        paintCell.textAlign = Paint.Align.CENTER

        paintTaskTag.isFilterBitmap = true
        paintTaskTag.isAntiAlias = true
        paintTaskTag.isDither = true

        paintHighLightDay.color = colorPressHeighLight
        paintHighLightDay.isAntiAlias = true
    }


}