package com.example.relay

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.Comparator

class myCustomAdapter(
    private var context: Context,
    private var arrayList: LinkedList<myDataClass>
) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var myView = convertView
        myView = LayoutInflater.from(context).inflate(R.layout.home_screen_chat_box, parent, false)

        var lastMessageView: TextView = myView.findViewById(R.id.lastMessage)
        var lastMessageTimeView: TextView = myView.findViewById(R.id.lastMessageTime)
        var chatUserView: TextView = myView.findViewById(R.id.chatUser)

        lastMessageView.setText((arrayList.get(position)).getLastMessage().toString())
        val timeText: String = getTextForTime((arrayList.get(position).getTime()))
        lastMessageTimeView.setText(timeText/*((arrayList.get(position).getTime()).toString()*/)
        chatUserView.setText((arrayList.get(position).getName()))

        return myView
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return arrayList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun notifyDataSetChanged() {
        val comparator = Comparator { o1: myDataClass, o2: myDataClass ->
            return@Comparator o2.getTime().compareTo(o1.getTime())
        }
        arrayList.sortWith(comparator)
        super.notifyDataSetChanged()
    }

    private fun getTextForTime(time: LocalDateTime): String {
        val fromDateTime = time//time when message was sent

        val toDateTime = LocalDate.now(ZoneId.of( "Asia/Kolkata" ))
            .atStartOfDay(ZoneId.of( "Asia/Kolkata" ))
        // Determines the first moment of the day as seen on that
        // date in that time zone. Not all days start at 00:00!

        var tempDateTime = LocalDateTime.from(fromDateTime)

        val years = tempDateTime.until(toDateTime, ChronoUnit.YEARS)
        tempDateTime = tempDateTime.plusYears(years)

        val months = tempDateTime.until(toDateTime, ChronoUnit.MONTHS)
        tempDateTime = tempDateTime.plusMonths(months)

        val days = tempDateTime.until(toDateTime, ChronoUnit.DAYS)
        tempDateTime = tempDateTime.plusDays(days)

        val hours = tempDateTime.until(toDateTime, ChronoUnit.HOURS)
        tempDateTime = tempDateTime.plusHours(hours)

        val minutes = tempDateTime.until(toDateTime, ChronoUnit.MINUTES)
        tempDateTime = tempDateTime.plusMinutes(minutes)

        val seconds = tempDateTime.until(toDateTime, ChronoUnit.SECONDS)

        var timeText: String
        Log.d("adaptertm", years.toString() + "/" + months.toString() + "/" + days.toString()+"hours" +hours.toString() +"ninutes"+minutes.toString())
        if ((years.toString().equals("0")) &&
            (months.toString().equals("0")) &&
            (days.toString().equals("0")) &&
            ((hours.toInt() < 0)||(minutes.toInt()<0)||(seconds.toInt()<0))
            //when the hours and minutes etc is = 0
        )
         {
            //show the time
            timeText = convertToAmPm(time.hour, time.minute)
            Log.d("adaptertim", "in the if")
            return timeText
        } else if ((years.toString().equals("0")) &&
            (months.toString().equals("0")) &&
            (days.toString().equals("0")) &&
            (hours.toInt()>0)
        ) {
            //show Yesterday
            timeText = "Yesterday"
        } else {
            //show the date
            var yearValue: String = time.year.toString()
            var dayValue:String = time.dayOfMonth.toString()
            yearValue = yearValue.substring(2, yearValue.length)
            if(time.dayOfMonth.toString().length == 1){
                dayValue = "0"+ dayValue
            }
            timeText = (dayValue +"/"+ time.monthValue.toString()+"/" + yearValue)
        }
        return timeText
    }

    private fun convertToAmPm(hour: Int, minute: Int): String {
        val messageTimeHour = hour
        val messageTimeMinute = minute
        lateinit var hourAndMinute: String
        if (messageTimeHour > 12) {
            hourAndMinute =
                (messageTimeHour.toInt() % 12).toString() + ":" + messageTimeMinute + " p.m"
        } else if (messageTimeHour == 12) {
            hourAndMinute =
                messageTimeHour.toString() + ":" + messageTimeMinute.toString() + " p.m."
        } else {
            hourAndMinute =
                messageTimeHour.toString() + ":" + messageTimeMinute.toString() + " a.m."
        }
        return hourAndMinute
    }

}