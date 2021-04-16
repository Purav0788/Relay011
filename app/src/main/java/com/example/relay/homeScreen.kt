package com.example.relay

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_home_screen.*
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap


class homeScreen : AppCompatActivity() {

    var chatsList = LinkedList<myDataClass>()
    private lateinit var adapter: myCustomAdapter
    var user1: String = " "
    private val InitializeChat: Int = 2

    companion object {
        private const val SELECT_PHONE_NUMBER = 111
    }

    private val lastChatMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context?, intent: Intent?) {
            //get the item which is the user2 chatlist item and update that item and then put that item on
            //the first of this chatslist and datasetnotified
            val user2 = intent!!.getStringExtra("user2")!!
            val lastMessage = intent.getStringExtra("lastMessageText")!!
            val lastMessageTime = intent.getStringExtra("lastMessageTime")!!
            lateinit var user2Name:String
            val iterator = chatsList.listIterator()
            //assumption is that all the names are in chat and thus user2Name will always be populate, which
            //may not be true
            while(iterator.hasNext()){
                val it = iterator.next()
                if (it.getphoneNumber() == user2) {
                    user2Name = it.getName()
                    iterator.remove()
                }
            }
            val chatItem = myDataClass(LocalDateTime.parse(lastMessageTime), lastMessage, user2Name, user2)
            chatsList.add(0, chatItem)
            adapter.notifyDataSetChanged()
            Log.d("receiver","in my tempreceiver")
        }
    }

    private val businessNameChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            var businessName = intent!!.getStringExtra("newBusinessName")!!
            _yourBusinessName.setText(businessName)
        }
    }

    private val userNameChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
        var userName = intent!!.getStringExtra("newName")!!
            _yourName.setText(userName)
        }
    }

    private lateinit var originalAddContactNumber: String

//    var receiver2:BroadcastReceiver =object: BroadcastReceiver(){
//        override fun onReceive(context: Context?, intent: Intent?) {
//            // Unregister self right away
//            context!!.unregisterReceiver(this);
//
//            // Component will hold the package info of the app the user chose
//            var component = intent!!.getParcelableExtra<ComponentName>(Intent.EXTRA_CHOSEN_COMPONENT);
//            for (key in intent.extras!!.keySet()) {
//                Log.d(javaClass.simpleName, " " + intent.extras!![key])
//                Log.d("register","I am here")
//            }
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)
        user1 = intent.getStringExtra("phoneNumber")!!
        var user = findUserInDbAndRefreshData(user1)
        adapter = myCustomAdapter(this@homeScreen, chatsList)
        _listOfChats.setAdapter(adapter)
        _listOfChats.setOnItemClickListener { parent, view, id, position ->
            val user2ChatData = chatsList.get(position.toInt())
            val user1 = user1
            openChat(user1, user2ChatData.getphoneNumber())
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
//        applicationContext.registerReceiver(receiver2, IntentFilter("SHARE_ACTION"));
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(lastChatMessageReceiver, IntentFilter("lastMessageData"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(businessNameChangeReceiver, IntentFilter("changedBusinessName"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(userNameChangeReceiver, IntentFilter("changedUserName"))
    }

    override fun onDestroy() {
        super.onDestroy()
        //needed to use LocalBroadcastManager because otherwise with simple unregister the app crashes
        LocalBroadcastManager.getInstance(this).unregisterReceiver(lastChatMessageReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userNameChangeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(businessNameChangeReceiver);
    }


    override fun onResume() {
        super.onResume()
//        findUserInDbAndRefreshData2(user1)
//        loadAllPastChats(user1)
    }


    private fun findUserInDbAndRefreshData(phoneNumber: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val query: Query =
            reference.child("users").orderByChild("phone_number").equalTo(phoneNumber)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "users" node with all children with phone_number = phoneNumber
                    for (user in dataSnapshot.children) {
                        // do something with the individual "user"
                        refreshData(user)
                    }
                } else {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun refreshData(myUser: DataSnapshot) {
        _yourBusinessName.text = myUser.child("business_name").value.toString()
        _yourName.text = myUser.child("name").value.toString()
        _yourPhoneNumber.text = myUser.child("phone_number").value.toString()
        //because on resume even gets called when the activity starts for the first time, we dont need to load data here
        //because on resume is already loading the data here
        loadAllPastChats(user1)
    }

    public fun createChat(view: View) {
        val i = Intent(Intent.ACTION_PICK)
        i.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        startActivityForResult(i, SELECT_PHONE_NUMBER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_PHONE_NUMBER && resultCode == Activity.RESULT_OK) {
            val contactUri = data?.data ?: return
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )
            val cursor = contentResolver.query(
                contactUri, projection,
                null, null, null
            )

            if (cursor != null && cursor.moveToFirst()) {
                val nameIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val name = cursor.getString(nameIndex)
                var number1 = cursor.getString(numberIndex)
                //cleaning the string, removing all non numeric characters
                var number = number1
                originalAddContactNumber = number1
                number = number.replace("[^0-9]", "")
                //cutting the extra 91 or any 0 prefix from this number
                number = number.filter { !it.isWhitespace() }
                var extraPrefix: Int = number.length - 10
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
    private fun initChat(user1: String, user2: String) {

        //how to make user1_user2 entry
        var mDatabase = FirebaseDatabase.getInstance()
        var mDbRef = mDatabase.getReference("messages")


        val mHashmap: MutableMap<String, Any> = HashMap()

        mHashmap[user1 + "_" + user2] = " "
        mHashmap[user2 + "_" + user1] = " "

        mDbRef.updateChildren(mHashmap).addOnSuccessListener {
            //nested structure after user1_user2:
            var reference1 = FirebaseDatabase.getInstance().getReferenceFromUrl(
                "https://relay-28f2e-default-rtdb.firebaseio.com/messages/"
                        + user1.toString() + "_" + user2
            )

            var reference2 = FirebaseDatabase.getInstance().getReferenceFromUrl(
                "https://relay-28f2e-default-rtdb.firebaseio.com/messages/"
                        + user2.toString() + "_" + user1
            )

            val map: MutableMap<String, Any> = HashMap()
            map["message"] = " "
            map["user"] = user1
//need to change some stuff here, for fetching the name as the user initializes
            //basically loadAllpastchats , reinit the chatslist tehre and then add that stuff from loadall past chats
            //and here attach listener on reference1.push() then run the loadallpast chats
            var time = LocalDateTime.now()
            map["time"] = time
            reference1.push().setValue(map).addOnSuccessListener { loadAllPastChats(user1) }
            reference2.push().setValue(map)
        }.addOnFailureListener {}

        //these can and should be clubber together,they will never be called individually
        makeUser2InUser1Chat(user1, user2)
        makeUser1InUser2Chat(user1, user2)
        //add the newly initiated chat to the user chat
    }

    public fun openChat(user1: String, user2: String) {
        val intent = Intent(this@homeScreen, Chat::class.java)
        intent.putExtra("user1", this.user1)
        intent.putExtra("user2", user2)
        startActivity(intent)
    }


    public fun reloadChat(user1: String, user2: String) {
        openChat(user1, user2)
    }

    public fun loadAllPastChats(user1: String) {
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
                        ref.addListenerForSingleValueEvent(object : ValueEventListener {
                            @RequiresApi(Build.VERSION_CODES.O)
                            @Override
                            override fun onDataChange(snapshot: DataSnapshot) {
                                // dataSnapshot is the "users" node with all children with phone_number = phoneNumber
                                var lastMessageText: String = ""
                                lateinit var lastMessageTime: LocalDateTime
                                if (snapshot.exists()) {
                                    for (datasnapshot in snapshot.children) {
                                        // do something with the individual "user"
                                        var tempLastMessageTimeMap: HashMap<Any, Any> =
                                            datasnapshot.child("time").value as HashMap<Any, Any>
                                        //temp is a map so need to extract the relevant date values and then parse them to get the
                                        //LocalDateTime object
                                        //when db stores the custom objects like DateTime, it returns them as hashmaps only
                                        lastMessageTime = getLocalDateTime(tempLastMessageTimeMap)

                                        //message is not there , when its an order
                                        lastMessageText =
                                            if (datasnapshot.child("message").value == null) {
                                                "An Order"
                                            } else {
                                                datasnapshot.child("message").value as String
                                            }
                                    }
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

                                                    this@homeScreen.runOnUiThread({
                                                        updateChatsListInUIThread(
                                                            myDataClass(
                                                                lastMessageTime,
                                                                lastMessageText,
                                                                user2Name,
                                                                user2
                                                            )
                                                        ) //Your code to run in GUI thread here
                                                    } //public void run() {
                                                    )
                                                    //ava.lang.IllegalStateException: The content of the adapter has changed but ListView did
                                                    // //not receive a notification. Make sure the content of your adapter is
                                                    // not modified from a background thread, but only from the UI thread.
                                                    // Make sure your adapter calls notifyDataSetChanged() when its content changes.
                                                    // [in ListView(2131296270, class android.widget.ListView) with Adapter(class com.example.relay.
                                                    // myCustomAdapter)]
                                                    //need to add items and call adpater notify dataset
//                                                                changed in the UI thread otherwise throws the error(
//                                                                    illeagal state exception
//                                                                chatsList.add(
//                                                                    myDataClass(
//                                                                        lastMessageTime,
//                                                                        lastMessageText,
//                                                                        user2Name,
//                                                                        user2
////                                                                    )
////                                                                )
////                                                                chatsList.sortWith(Comparator{chatItem1:myDataClass, chatItem2:myDataClass -> if(chatItem1.getTime().isAfter(chatItem2.getTime())) 0 else 1
////                                                                Log.d("compare", "somenonsense")
////                                                                })
//                                                                _listOfChats.requestLayout()
//                                                                adapter.notifyDataSetChanged()
//                                                                _listOfChats.requestLayout()

                                                }
                                            } else {
                                            }
                                        }

                                        override fun onCancelled(databaseError: DatabaseError) {}
                                    })
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


    public fun makeUser2InUser1Chat(user1: String, user2: String) {
        Log.d("arpan", "i am here")
        val reference = FirebaseDatabase.getInstance().reference
        val chatRef = reference.child("chats")
        val map: MutableMap<String, String> = HashMap()
        map["username"] = user2
        map["time"] = "0"
        chatRef.child(user1).push().setValue(map).addOnSuccessListener { }
    }


    public fun isUser2InUser1sChat(user1: String, user2: String) {
        //whichever is not make that and update that
        val reference = FirebaseDatabase.getInstance().reference
        val query: Query = reference.child("chats/$user1").orderByChild("username").equalTo(user2)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    //if exists do nothing
                    //one has the others chat implies they both will have each others chat
                    Log.d("Chat", "I am here in isUser2InUser1sChat")
                    openChat(user1, user2)
                } else {
                    Log.d("Chat", "I am here in else of isUser2InUser1sChat")
                    initChat(user1, user2)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun isUser2InDb(user2: String) {
        val reference = FirebaseDatabase.getInstance().reference
        Log.d("Chat", user2)
        val query: Query = reference.child("users").orderByChild("phone_number").equalTo(user2)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    //user2 exists in db(user2 is registered)
                    //checking if user2 is already chat initiated with user1
                    Log.d("Chat", "I am here in isUser2InDb")
                    isUser2InUser1sChat(user1, user2)
                } else {
                    //user2 doesn't exist in db(user2 is not registered), so direct to whatsapp popup
                    Log.d("Chat", "I am here in else of isUser2InDb")
//                    onWhatsAppClick(originalAddContactNumber)
                    onWhatsAppClick(user2)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    public fun makeUser1InUser2Chat(user1: String, user2: String) {
        Log.d("arpan", "i am here")
        val reference = FirebaseDatabase.getInstance().reference
        val chatRef = reference.child("chats")
        val map: MutableMap<String, String> = HashMap()
        map["username"] = user1
        map["time"] = "0"
        chatRef.child(user2).push().setValue(map).addOnSuccessListener { }
    }

    private fun onWhatsAppClick(phone: String) {
        val pm = packageManager
        val message: String = "Install relay"
        try {
            val url =
                "https://api.whatsapp.com/send?phone=" + "91" + phone + "&text=" + URLEncoder.encode(
                    message,
                    "UTF-8"
                )
            val i = Intent(Intent.ACTION_VIEW)
//            i.setPackage("com.whatsapp")
            i.setData(Uri.parse(url))
            if (i.resolveActivity(packageManager) != null) {
                startActivity(i)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onSmsAppClick(phone: String) {

    }

//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
//    private fun sendInvite(){
//        // Set up the broadcast receiver (preferably as a class member)
//
//        var intent = Intent(Intent.ACTION_SEND);
//        intent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
//        intent.setType("text/plain");
//
//// Use custom action only for your app to receive the broadcast
//        val shareAction :String = "com.yourdomain.share.SHARE_ACTION";
//        var receiver:Intent = Intent(shareAction)
//        receiver.putExtra("test", "test");
//        var pendingIntent:PendingIntent = PendingIntent.getBroadcast(this, 0, receiver, PendingIntent.FLAG_UPDATE_CURRENT);
//        var chooser = Intent.createChooser(intent, "test", pendingIntent.intentSender);
//
//// Before firing chooser activity, register the receiver with our custom action from above so that we receive the chosen app
//
//
//        startActivity(chooser);
//    }

//    try {
//        Intent i = new Intent(Intent.ACTION_SEND);
//        i.setType("text/plain");
//        i.putExtra(Intent.EXTRA_SUBJECT, "My app name");
//        String strShareMessage = "\nLet me recommend you this application\n\n";
//        strShareMessage = strShareMessage + "https://play.google.com/store/apps/details?id=" + getPackageName();
//        Uri screenshotUri = Uri.parse("android.resource://packagename/drawable/image_name");
//        i.setType("image/png");
//        i.putExtra(Intent.EXTRA_STREAM, screenshotUri);
//        i.putExtra(Intent.EXTRA_TEXT, strShareMessage);
//        startActivity(Intent.createChooser(i, "Share via"));
//    } catch(Exception e) {
//        //e.toString();
//    }


    public fun more(view: View) {
        val intent = Intent(this@homeScreen, settings::class.java)
        intent.putExtra("user1", user1)
        startActivity(intent)
    }

    private fun findUserInDbAndRefreshData2(phoneNumber: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val query: Query =
            reference.child("users").orderByChild("phone_number").equalTo(phoneNumber)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "users" node with all children with phone_number = phoneNumber
                    for (user in dataSnapshot.children) {
                        // do something with the individual "user"
                        refreshData2(user)
                    }
                } else {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun refreshData2(myUser: DataSnapshot) {
        _yourBusinessName.text = myUser.child("business_name").value.toString()
        _yourName.text = myUser.child("name").value.toString()
        _yourPhoneNumber.text = myUser.child("phone_number").value.toString()
    }


    private fun setActionBar(phoneNumber: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val query: Query =
            reference.child("users").orderByChild("phone_number").equalTo(phoneNumber)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "users" node with all children with phone_number = phoneNumber
                    for (user in dataSnapshot.children) {
                        // do something with the individual "user"
                        val actionBar = supportActionBar
                        actionBar!!.title = user.child("business_name").value as String
                    }
                } else {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getLocalDateTime(tempLastMessageTimeMap: HashMap<Any, Any>): LocalDateTime {
        var date: String = tempLastMessageTimeMap["dayOfMonth"].toString()
        var month: String = tempLastMessageTimeMap["monthValue"].toString()
        var year: String = tempLastMessageTimeMap["year"].toString()
        var hour: String = tempLastMessageTimeMap["hour"].toString()
        var minute: String = tempLastMessageTimeMap["minute"].toString()
        var second: String = tempLastMessageTimeMap["second"].toString()
        //need to add these 0s as db sends 3 whereas expected is 03
        if (date.length == 1) {
            date = "0" + date
        }
        if (month.length == 1) {
            month = "0" + month
        }
        if (year.length == 1) {
            year = "0" + year
        }
        if (hour.length == 1) {
            hour = "0" + hour
        }
        if (minute.length == 1) {
            minute = "0" + minute
        }
        if (second.length == 1) {
            second = "0" + second
        }
        var fullDatetime: String =
            date + "/" + month + "/" + year + " " + hour + ":" + minute + ":" + second
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return LocalDateTime.parse(fullDatetime, formatter);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public fun updateChatsListInUIThread(myDataClass: myDataClass) {
        //hopefully this function gets called in the ui thread
        chatsList.add(0,myDataClass)
        adapter.notifyDataSetChanged()
    }
}                                                                                                                                                                                               