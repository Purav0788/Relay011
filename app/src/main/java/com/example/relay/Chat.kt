package com.example.relay


import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseError
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.message_place.*


class Chat : AppCompatActivity() {

    lateinit var reference1: DatabaseReference
    lateinit var reference2: DatabaseReference
    private lateinit var user1:String //primary user, whose phone it is
    private lateinit var user2:String //secondary user,

    override fun onCreate(savedInstanceState: Bundle?) {
        //expecting intent with the username_usernameofchatter, this string is expected
        //expecting the same intent as UserDetails
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        user1 = intent.getStringExtra("user1")!!
        user2 = intent.getStringExtra("user2")!!

        reference1 = FirebaseDatabase.getInstance().getReferenceFromUrl(
            "https://relay-28f2e-default-rtdb.firebaseio.com/messages/"
                + user1.toString() + "_" + user2)

        reference2 = FirebaseDatabase.getInstance().getReferenceFromUrl(
            "https://relay-28f2e-default-rtdb.firebaseio.com/messages/"
                + user2.toString() + "_" + user1)


        reference1.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val map = dataSnapshot.value as Map<String, Any>
                val message = map["message"].toString()
                val userName = map["user"].toString()
                if (userName == user1) {
                    addMessageBox(message, 1)
                } else {
                    addMessageBox(message, 2)
                }
            }

            override fun onChildChanged(
                dataSnapshot: DataSnapshot,
                s: String?
            ) {
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(
                dataSnapshot: DataSnapshot,
                s: String?
            ) {
            }

            fun onCancelled(firebaseError: FirebaseError?) {}
        })
        reloadAllPastMessages(user1, user2)
    }

    fun addMessageBox(message: String?, type: Int) {
        val textView = TextView(this@Chat)
        textView.text = message
        val lp2 = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp2.weight = 7.0f
        if (type == 1) {
            lp2.gravity = Gravity.LEFT
//            textView.setBackgroundResource()
        } else {
            lp2.gravity = Gravity.RIGHT
//            textView.setBackgroundResource(R.drawable.bubble_out)
        }
        textView.layoutParams = lp2
        layout1!!.addView(textView)
        scrollView!!.fullScroll(View.FOCUS_DOWN)
    }

    public fun sendMessage(v: View?) {
        val messageText = messageArea.getText().toString()
        if (messageText != "") {
            val map: MutableMap<String, String> =
                HashMap()
            map["message"] = messageText
            map["user"] = user1
            reference1.push().setValue(map)
            reference2.push().setValue(map)
            messageArea.setText("")
        }
    }
    public fun reloadAllPastMessages(user1:String, user2:String){
        FirebaseDatabase.getInstance().reference.child("messages/"+ user1 +"_"+ user2)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (snapshot in dataSnapshot.children) {
                            val message = snapshot.getValue(Message::class.java)!!
//                            System.out.println(message.user)
                            if(message.user == user1){
                                addMessageBox(message.message,1)
                            }
                            else{
                                addMessageBox(message.message,2)
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {}
                })
    }
}