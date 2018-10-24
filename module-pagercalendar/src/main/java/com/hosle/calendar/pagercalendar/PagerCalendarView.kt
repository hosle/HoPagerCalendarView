package com.hosle.calendar.pagercalendar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.hosle.calendar.pagercalendar.util.dp2px
import kotlinx.android.synthetic.main.layout_pager_calendar_view.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashSet


/**
 * Created by tanjiahao on 2018/9/26
 * Original Project PagerCalendar
 */
class PagerCalendarView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var dateMonth: Array<Array<Int>> = arrayOf()

    private var firstVisibleItemPosition = 0

    private val monthLabelHeight = context.dp2px(20f) + 10

    init {
        initView(context)
    }

    private fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.layout_pager_calendar_view, this, true)
        header_day_of_week.setParams()
        initViewPager()
        initBtnTop()
    }

    private fun initBtnTop(){
        btn_left.setOnClickListener {
            view_pager_calendar.setCurrentItem(view_pager_calendar.currentItem - 1,true)
        }

        btn_right.setOnClickListener {
            view_pager_calendar.setCurrentItem(view_pager_calendar.currentItem + 1,true)
        }
    }

    private fun initViewPager(){
        view_pager_calendar.setOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                if (dateMonth.size > position) {
                    tv_title.text = "${dateMonth[position][0]}年${dateMonth[position][1]}月"
                }
            }

        })
    }

    private fun isGoingDown(dy: Int): Boolean {
        return dy < 0
    }

    /**
     * @param monthArrange month period to be shown
     * @param onDayClickListener click listener on day
     * @param operationForTaskCount check task count for each day
     * @param taskBitmap specific drawable bitmap for the day with task
     */
    fun setCalendarParams(monthArrange: Array<Array<Int>>, onDayClickListener: MonthView.OnDayClickListener?,
                          operationForTaskCount: ((Calendar) -> Int)? = null, taskBitmap: Bitmap? = null ) {

        dateMonth = getUniqueArray(monthArrange)

        if(dateMonth.isNotEmpty()) {
            tv_title.text = "${dateMonth[0][0]}年${dateMonth[0][1]}月"
        }

        setAdapter(CalendarAdapter(context, dateMonth, onDayClickListener,operationForTaskCount, taskBitmap))
    }

    private fun getUniqueArray(origin:Array<Array<Int>>):Array<Array<Int>>{
        val result = arrayListOf<Array<Int>>()
        val set = hashSetOf<String>()
        origin.forEach {
            val value = it[0].toString() + it[1].toString()
            if (!set.contains(value)) {
                result.add(it)
                set.add(value)
            }
        }

        return result.toTypedArray()
    }

    fun setOndayClickListener(onDayClickListener: MonthView.OnDayClickListener?) {
        (view_pager_calendar.adapter as CalendarAdapter).onDayClickListener = onDayClickListener
    }

    private fun setAdapter(_adapter: CalendarAdapter) {
        view_pager_calendar.adapter = _adapter
    }



    class CalendarAdapter(val context: Context, val monthArange: Array<Array<Int>>,
                          var onDayClickListener: MonthView.OnDayClickListener?,
                          val operationForTaskCount: ((Calendar) -> Int)? = null,
                          val taskBitmap: Bitmap? = null ) : PagerAdapter() {

        private val viewList = ArrayList<MonthView>()

        init {
            for(i in 0 until 4){
                val view = MonthView(context)
                setViewData(view,i)
                viewList.add(view)
            }
        }


        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getCount(): Int {
            return monthArange.size
        }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {

            val view: MonthView = viewList[position % 4]

            setViewData(view,position)

            if (view.parent == null) {
                container.addView(view)
            }
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as MonthView)
        }

        private fun setViewData(view: MonthView, position: Int) {
            if (position < monthArange.size) {
                view.setMonthParams(monthArange[position][1], monthArange[position][0],
                        operation = operationForTaskCount, taskBitmap = taskBitmap)

                view.setOnDayClickListener(onDayClickListener)
            }
        }

    }
}
