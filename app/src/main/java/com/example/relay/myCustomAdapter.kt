package com.example.relay

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.annotation.RequiresApi
import java.util.*
import kotlin.Comparator

class myCustomAdapter(private var context: Context,
                      private var arrayList: LinkedList<myDataClass>
) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var myView = convertView
        myView = LayoutInflater.from(context).inflate(R.layout.home_screen_chat_box, parent, false)

        var lastMessageView:TextView = myView.findViewById(R.id.lastMessage)
        var lastMessageTimeView:TextView = myView.findViewById(R.id.lastMessageTime)
        var chatUserView:TextView = myView.findViewById(R.id.chatUser)

        lastMessageView.setText((arrayList.get(position)).getLastMessage().toString())
        lastMessageTimeView.setText((arrayList.get(position).getTime()).toString())
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
        val comparator = Comparator{
            o1:myDataClass,o2:myDataClass->
            return@Comparator o2.getTime().compareTo(o1.getTime())
        }
        arrayList.sortWith(comparator)
        super.notifyDataSetChanged()
    }

}