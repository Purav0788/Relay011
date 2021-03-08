package com.example.relay

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_home_screen.*


class homeScreen : AppCompatActivity() {

    private var arrayList: MutableList<String>? = null
    private var adapter: ArrayAdapter<String>? = null
    private var list: ListView? = null
    var mobile:String = " "
    companion object {
        private const val SELECT_PHONE_NUMBER = 111
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)
        mobile = intent.getStringExtra("phoneNumber")!!
        var user = findUserInDb(mobile!!)
        if(user == null){
            _yourBusinessName.text = ""
            _yourName.text = ""
            _yourPhoneNumber.text = ""
        }

//        FirebaseAuth.getInstance().signOut()
        list =  _listOfChats
        arrayList = ArrayList<String>()

        adapter = ArrayAdapter<String>(this@homeScreen, android.R.layout.simple_spinner_item, arrayList!!)
        list!!.setAdapter(adapter)
        _listOfChats.setOnItemClickListener{parent,view,id,position->
            val user2 = (view as TextView).text.toString()
            val user1 = mobile
            openChat(user1,user2)
        }

        //the intent is not to actually add the contact but rather to get the number
        // which the user wants to add to the app

    }

    private fun findUserInDb(phoneNumber: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val query: Query = reference.child("users").orderByChild("phone_number").equalTo(phoneNumber)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "users" node with all children with phone_number = phoneNumber
                    for (user in dataSnapshot.children) {
                        // do something with the individual "user"
                        refreshData(user)
                    }
                }
                else{
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun refreshData(myUser:DataSnapshot){
        _yourBusinessName.text = myUser.child("business_name").value.toString()
        _yourName.text = myUser.child("name").value.toString()
        _yourPhoneNumber.text = myUser.child("phone_number").value.toString()
        loadAllPastChats(/*myUser.child("name").value.toString()*/ mobile)
    }

    public fun createChat(view:View){
        val i = Intent(Intent.ACTION_PICK)
        i.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        startActivityForResult(i, SELECT_PHONE_NUMBER)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_PHONE_NUMBER && resultCode == Activity.RESULT_OK) {
            val contactUri = data?.data ?: return
            val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER)
            val cursor = contentResolver.query(contactUri, projection,
                    null, null, null)

            if (cursor != null && cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val name = cursor.getString(nameIndex)
                val number = cursor.getString(numberIndex)

                initChat(mobile, number)
            }
            cursor?.close()
        }
    }

    //initializing chat between user1 and user2 for the first time
    private fun initChat(user1:String, user2: String){

        //how to make user1_user2 entry
       var mDatabase = FirebaseDatabase.getInstance()
       var mDbRef = mDatabase.getReference("messages")


        val mHashmap: MutableMap<String, Any> = HashMap()

        mHashmap[user1 +"_"+ user2] = " "
        mHashmap[user2 +"_"+ user1] = " "

        mDbRef.updateChildren(mHashmap).addOnSuccessListener {
            //nested structure after user1_user2:
            var reference1 = FirebaseDatabase.getInstance().getReferenceFromUrl(
                "https://relay-28f2e-default-rtdb.firebaseio.com/messages/"
                        + user1.toString() + "_" + user2)

            var reference2 = FirebaseDatabase.getInstance().getReferenceFromUrl(
                "https://relay-28f2e-default-rtdb.firebaseio.com/messages/"
                        + user2.toString() + "_" + user1)

            val map: MutableMap<String, String> = HashMap()
            map["message"] = " "
            map["user"] = user1
            reference1.push().setValue(map).addOnSuccessListener {                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
                reference2.push().setValue(map).addOnSuccessListener {
                    Log.d("chat","i am just before starting new activity")
                    openChat(user1, user2)
                }.addOnFailureListener {}
            }.addOnFailureListener {}
        }.addOnFailureListener {}

        //add the newly initiated chat to the user chat
        addUser2ToChat(user1, user2)
    }

    public fun openChat(user1:String, user2:String){
        val intent = Intent(this@homeScreen, Chat::class.java)
        intent.putExtra("user1", mobile)
        intent.putExtra("user2", user2)
        startActivity(intent)
    }

    public fun reloadChat(user1:String, user2:String){
        openChat(user1, user2)
    }

    public fun loadAllPastChats(user1:String){
        Log.d("home", "I am in load")
        FirebaseDatabase.getInstance().reference.child("chats/$user1")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        Log.d("home", "chats/"+user1)
                        for (snapshot in dataSnapshot.children) {
                            val chat = snapshot.getValue(oneChat::class.java)!!
                            var name = chat.username
                            Log.d("home", "$name")
                            (arrayList as ArrayList<String>).add("$name")
                            adapter!!.notifyDataSetChanged()
                        }

                    }
                    override fun onCancelled(databaseError: DatabaseError) {}
                })
    }


    public fun makeUser2InUser1Chat(user1:String, user2:String){
        val reference = FirebaseDatabase.getInstance().reference
        val chatRef = reference.child("chats")
        val map: MutableMap<String, String> = HashMap()
        map["username"] = user2
        map["time"] = "0"
        chatRef.child(user1).push().setValue(map).addOnSuccessListener {  }
    }

    public fun makeUser1InChat(user1:String, user2:String){
        val reference = FirebaseDatabase.getInstance().reference
        val chatRef = reference.child("chats")
        val map: MutableMap<String, String> = HashMap()
        map["username"] = user2
        map["time"] = "0"
        chatRef.child("$user1").setValue(" ").addOnSuccessListener {
            //making user 2
            chatRef.child("$user1").push().setValue(map).addOnSuccessListener {  }
        }
    }

    public fun addUser2ToChat(user1: String, user2: String){
        //check if the user1 is already there in chat
        isUser1InChat(user1, user2)
        //it also checks if the user2 is in chat and makes one if its not
    }

    public fun isUser1InChat(user1:String, user2:String){
        val reference = FirebaseDatabase.getInstance().reference
        val chatRef = reference.child("chats")
        chatRef.orderByKey().equalTo(user1).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    //Key exists
                    //check if user2 exists there
                    isUser2InUser1sChat(user1, user2)
                } else {
                   makeUser1InChat(user1, user2)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
    public fun isUser2InUser1sChat(user1:String, user2:String){
        //whichever is not make that and update that
        val reference = FirebaseDatabase.getInstance().reference
        val query: Query = reference.child("chats/$user1").orderByChild("username").equalTo(user2)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    //if exists do nothing
                    //this should never happen, as they are called upon their first chat init
                }
                else{
                    makeUser2InUser1Chat(user1, user2)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}                                                                                                                                                                                               