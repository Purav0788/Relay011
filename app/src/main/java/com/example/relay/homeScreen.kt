package com.example.relay

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_home_screen.*
import java.net.URLEncoder
import java.time.LocalTime
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class homeScreen : AppCompatActivity() {

    var chatsList = ArrayList<myDataClass>()
    private lateinit var adapter: myCustomAdapter
    var user1:String = " "
    private val InitializeChat:Int = 2
    companion object {
        private const val SELECT_PHONE_NUMBER = 111
    }
    private lateinit var originalAddContactNumber: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)
        user1 = intent.getStringExtra("phoneNumber")!!
        var user = findUserInDbAndRefreshData(user1)
        adapter = myCustomAdapter(this@homeScreen, chatsList)
        _listOfChats.setAdapter(adapter)
        _listOfChats.setOnItemClickListener{parent,view,id,position->
            val user2ChatData = chatsList.get(position.toInt())
            val user1 = user1
            openChat(user1,user2ChatData.getphoneNumber())
        }

        //the idea is not to actually add the contact but rather to get the number
        // which the user wants to add to the app
        //my change starts from here, lets see if it works

//        try{
//            val reference = FirebaseDatabase.getInstance().reference
//            val query: Query = reference.child("users")
//                    .orderByChild("phone_number").equalTo(mobile)
//
//        } catch(e:FirebaseException){
//
//        }
        setActionBar(user1)
    }
    override fun onResume() {
        super.onResume()
        findUserInDbAndRefreshData2(user1)
    }


    private fun findUserInDbAndRefreshData(phoneNumber: String) {
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
        loadAllPastChats(user1)
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
                var number1 = cursor.getString(numberIndex)
                //cleaning the string, removing all non numeric characters
                var number = number1
                originalAddContactNumber = number1
                number = number.replace("[^0-9]","")
                //cutting the extra 91 or any 0 prefix from this number
                number = number.filter { !it.isWhitespace() }
                var extraPrefix : Int = number.length-10
                number = number.drop(extraPrefix)
                Log.d("Chat", number)
                //checking if user2 is registered in db, if not then the pop comes up to direct them to whatsapp
                //if user2 is registered then direct to initChat
                isUser2InDb(number)
            }
            cursor?.close()
        }
    }

    //initializing chat between user1 and user2 for the first time
    @RequiresApi(Build.VERSION_CODES.O)
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

            val map: MutableMap<String, Any> = HashMap()
            map["message"] = " "
            map["user"] = user1
//need to change some stuff here, for fetching the name as the user initializes
            //basically loadAllpastchats , reinit the chatslist tehre and then add that stuff from loadall past chats
            //and here attach listener on reference1.push() then run the loadallpast chats
            var time = LocalTime.now()
            map["time"] = time
            reference1.push().setValue(map).addOnSuccessListener { loadAllPastChats(user1) }
            reference2.push().setValue(map)
        }.addOnFailureListener {}

        //these can and should be clubber together,they will never be called individually
        makeUser2InUser1Chat(user1, user2)
        makeUser1InUser2Chat(user1, user2)
        //add the newly initiated chat to the user chat
    }

    public fun openChat(user1:String, user2:String){
        val intent = Intent(this@homeScreen, Chat::class.java)
        intent.putExtra("user1", this.user1)
        intent.putExtra("user2", user2)
        startActivity(intent)
    }


    public fun reloadChat(user1:String, user2:String){
        openChat(user1, user2)
    }

    public fun loadAllPastChats(user1:String){
        chatsList.clear()
        FirebaseDatabase.getInstance().reference.child("chats/$user1")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        Log.d("Chat", "I am in sfdsfs")

                        for (snapshot in dataSnapshot.children) {
                            val chat = snapshot.getValue(oneChat::class.java)!!
                            var user2 = chat.username as String
                            //case when username is "", not sure why a username can be blank,
                            // its there when user registers, not sure, i think the queries would still work, or maybe not
                            Log.d("user2", user2)
                            //note there are many users in this for loop
                            //get last message between user 1 and user2
                            //get name of user2
                            //get time of last message between user1 and user2
                            val ref = FirebaseDatabase
                                .getInstance().reference.child("messages/" + user1 + "_" + user2)
                            ref.orderByKey().limitToLast(1)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    @RequiresApi(Build.VERSION_CODES.O)
                                    @Override
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        // dataSnapshot is the "users" node with all children with phone_number = phoneNumber
                                        if (snapshot.exists()) {
                                            for (datasnapshot in snapshot.children) {
                                                // do something with the individual "user"
//                                                val time = datasnapshot.child("time").value as LocalTime
                                                val time = LocalTime.now()
//                                                val lastMessageText =
//                                                    datasnapshot.child("message").value as String
                                                val lastMessageText = "My last message"
                                                val reference =
                                                    FirebaseDatabase.getInstance().reference
                                                val query: Query = reference.child("users")
                                                    .orderByChild("phone_number").equalTo(user2)
                                                query.addListenerForSingleValueEvent(object :
                                                    ValueEventListener {
                                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                        if (dataSnapshot.exists()) {
                                                            for (mysnapshot in dataSnapshot.children) {
                                                                val user2Name =
                                                                    mysnapshot.child("name").value as String
//                                                                  val user2Name = "MyUser"
                                                                chatsList.add(
                                                                    myDataClass(
                                                                        time,
                                                                        lastMessageText,
                                                                        user2Name,
                                                                        user2
                                                                    )
                                                                )
                                                                adapter.notifyDataSetChanged()
                                                            }
                                                        } else {
                                                        }
                                                    }

                                                    override fun onCancelled(databaseError: DatabaseError) {}
                                                })
                                            }
                                        } else {
                                        }

                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })

                        }

                    }
                    override fun onCancelled(databaseError: DatabaseError) {}
                })
    }


    public fun makeUser2InUser1Chat(user1:String, user2:String){
        Log.d("arpan", "i am here")
        val reference = FirebaseDatabase.getInstance().reference
        val chatRef = reference.child("chats")
        val map: MutableMap<String, String> = HashMap()
        map["username"] = user2
        map["time"] = "0"
        chatRef.child(user1).push().setValue(map).addOnSuccessListener {  }
    }


    public fun isUser2InUser1sChat(user1:String, user2:String){
        //whichever is not make that and update that
        val reference = FirebaseDatabase.getInstance().reference
        val query: Query = reference.child("chats/$user1").orderByChild("username").equalTo(user2)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    //if exists do nothing
                    //one has the others chat implies they both will have each others chat
                    Log.d("Chat","I am here in isUser2InUser1sChat")
                    openChat(user1, user2)
                }
                else{
                    Log.d("Chat","I am here in else of isUser2InUser1sChat")
                    initChat(user1, user2)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun isUser2InDb(phoneNumber: String) {
        val reference = FirebaseDatabase.getInstance().reference
        Log.d("Chat", phoneNumber)
        val query: Query = reference.child("users").orderByChild("phone_number").equalTo(phoneNumber)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                   //user2 exists in db(user2 is registered)
                    //checking if user2 is already chat initiated with user1
                    Log.d("Chat","I am here in isUser2InDb")
                    isUser2InUser1sChat(user1, phoneNumber)
                }
                else{
                   //user2 doesn't exist in db(user2 is not registered), so direct to whatsapp popup
                    Log.d("Chat","I am here in else of isUser2InDb")
                    onWhatsAppClick(originalAddContactNumber)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    public fun makeUser1InUser2Chat(user1:String, user2:String){
        Log.d("arpan", "i am here")
        val reference = FirebaseDatabase.getInstance().reference
        val chatRef = reference.child("chats")
        val map: MutableMap<String, String> = HashMap()
        map["username"] = user1
        map["time"] = "0"
        chatRef.child(user2).push().setValue(map).addOnSuccessListener {  }
    }

    private fun onWhatsAppClick(phone:String) {
        val pm = packageManager
        val message:String = "Install relay"
        try {
            val url =
                "https://api.whatsapp.com/send?phone=" + phone + "&text=" + URLEncoder.encode(
                    message,
                    "UTF-8"
                )
            val i = Intent(Intent.ACTION_VIEW)
            i.setPackage("com.whatsapp")
            i.setData(Uri.parse(url))
            if (i.resolveActivity(packageManager) != null) {
                startActivity(i)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    public fun more(view:View){
        val intent = Intent(this@homeScreen, settings::class.java)
        intent.putExtra("user1", user1)
        startActivity(intent)
    }

    private fun findUserInDbAndRefreshData2(phoneNumber: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val query: Query = reference.child("users").orderByChild("phone_number").equalTo(phoneNumber)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "users" node with all children with phone_number = phoneNumber
                    for (user in dataSnapshot.children) {
                        // do something with the individual "user"
                        refreshData2(user)
                    }
                }
                else{
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun refreshData2(myUser:DataSnapshot){
        _yourBusinessName.text = myUser.child("business_name").value.toString()
        _yourName.text = myUser.child("name").value.toString()
        _yourPhoneNumber.text = myUser.child("phone_number").value.toString()
    }



    private fun setActionBar(phoneNumber: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val query: Query = reference.child("users").orderByChild("phone_number").equalTo(phoneNumber)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "users" node with all children with phone_number = phoneNumber
                    for (user in dataSnapshot.children) {
                        // do something with the individual "user"
                        val actionBar = supportActionBar
                        actionBar!!.title = user.child("business_name").value as String
                    }
                }
                else{
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}                                                                                                                                                                                               