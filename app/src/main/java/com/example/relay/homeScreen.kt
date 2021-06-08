package com.example.relay

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_home_screen.*
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap


class homeScreen : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    var chatsList = LinkedList<myDataClass>()
    private lateinit var adapter: myCustomAdapter
    var user1: String = " "
    private val InitializeChat: Int = 2

    companion object {
        private const val SELECT_PHONE_NUMBER = 111
    }

//    private val lastChatMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
//        @RequiresApi(Build.VERSION_CODES.O)
//        override fun onReceive(context: Context?, intent: Intent?) {
//            //get the item which is the user2 chatlist item and update that item and then put that item on
//            //the first of this chatslist and datasetnotified
//            val user2 = intent!!.getStringExtra("user2")!!
//            val lastMessage = intent.getStringExtra("lastMessageText")!!
//            val lastMessageTime = intent.getStringExtra("lastMessageTime")!!
//            lateinit var user2Name: String
//            val iterator = chatsList.listIterator()
//            //assumption is that all the names are in chat and thus user2Name will always be populate, which
//            //may not be true
//            while (iterator.hasNext()) {
//                val it = iterator.next()
//                if (it.getphoneNumber() == user2) {
//                    user2Name = it.getName()
//                    iterator.remove()
//                }
//            }
//            val chatItem =
//                    myDataClass(LocalDateTime.parse(lastMessageTime), lastMessage, user2Name, user2)
//            chatsList.add(0, chatItem)
//            adapter.notifyDataSetChanged()
//            Log.d("receiver", "in my tempreceiver")
//        }
//    }

    private val businessNameChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            var businessName = intent!!.getStringExtra("newBusinessName")!!
            _yourBusinessName.text = businessName
        }
    }

    private val userNameChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            var userName = intent!!.getStringExtra("newName")!!
//            _yourName.setText(userName)
        }
    }

//    private lateinit var originalAddContactNumber: String

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
        setSupportActionBar(_toolbar)
        supportActionBar?.title = ""
        // TODO: 6/8/2021 Uncomment this
/*
        user1 = intent.getStringExtra("user1")!!
        var user = findUserInDbAndRefreshData(user1)
*/
        adapter = myCustomAdapter(this@homeScreen, chatsList)
        _listOfChats.adapter = adapter
        _listOfChats.setOnItemClickListener { parent, view, id, position ->
            // TODO: 6/7/2021 Uncomment this
/*
            val user2ChatData = chatsList.get(position.toInt())
            val user1 = user1
            openChat(user1, user2ChatData.getphoneNumber())
*/
            // TODO: 6/7/2021 Remove this. This is just for testing purpose
            openChat("", "")
        }
        detectChangeAndUpdateChats()

        _bottom_nav.setOnNavigationItemSelectedListener(this)

        imageButtonSearch.setOnClickListener{
            viewAnimator.showNext()
        }
        tILSearchView.setStartIconOnClickListener {
            viewAnimator.showNext()
        }

        tILSearchView.setEndIconOnClickListener {
            Toast.makeText(this, "Search Clicked", Toast.LENGTH_SHORT).show()
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
//        LocalBroadcastManager.getInstance(this)
//                .registerReceiver(lastChatMessageReceiver, IntentFilter("lastMessageData"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(businessNameChangeReceiver, IntentFilter("changedBusinessName"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(userNameChangeReceiver, IntentFilter("changedUserName"))
    }

//    private fun hideSearchView() {
//        tILSearchView.visibility = View.GONE
//        imageButton4.visibility = View.VISIBLE
//        tvMyNetwork.visibility = View.VISIBLE
//    }
//
//    private fun showSearchView() {
//        imageButton4.visibility = View.VISIBLE
//        tvMyNetwork.visibility = View.VISIBLE
//    }


    override fun onDestroy() {
        super.onDestroy()
        //needed to use LocalBroadcastManager because otherwise with simple unregister the app crashes
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(lastChatMessageReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userNameChangeReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(businessNameChangeReceiver)
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
//        _yourName.text = myUser.child("name").value.toString()
//        _yourPhoneNumber.text = myUser.child("phone_number").value.toString()
        //because on resume even gets called when the activity starts for the first time, we dont need to load data here
        //because on resume is already loading the data here
//        loadAllPastChats(user1)
    }

    fun createChat(view: View) {
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
//                originalAddContactNumber = number1
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
            var time = LocalDateTime.now()
            val map: MutableMap<String, Any> = HashMap()
            map["time"] = time
            map["user"] = user1
            map["message"] = " "

            //whoever is the initializing user sends the first message
//need to change some stuff here, for fetching the name as the user initializes
            //basically loadAllpastchats , reinit the chatslist tehre and then add that stuff from loadall past chats
            //and here attach listener on reference1.push() then run the loadallpast chats

            //will need to make several of these requests atomic down the line like makeUser and these re
            //reference1 push etc

            reference1.push().setValue(map)
            reference2.push().setValue(map)
        }

        //these can and should be clubber together,they will never be called individually
        makeUser2InUser1Chat(user1, user2)
        makeUser1InUser2Chat(user1, user2)
        //add the newly initiated chat to the user chat

        //here if one removes the " " value , then it would mean chat has initialzied in the chats but
        //but there wont be any message or even the parent key in the messages
        //which is bad, while it will still work, but things can indeed crash and its just
        //bad stuff
    }

    fun openChat(user1: String, user2: String) {
        val intent = Intent(this@homeScreen, Chat::class.java)
        intent.putExtra("user1", this.user1)
        intent.putExtra("user2", user2)
//        intent.putExtra("user1Name", _yourName.text)
        startActivity(intent)
//        val reference = FirebaseDatabase.getInstance().reference
//        val query: Query = reference.child("users").child(user1)
//        query.addListenerForSingleValueEvent(object : ValueEventListener {
//            @RequiresApi(Build.VERSION_CODES.O)
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    var user1Name = dataSnapshot.child("name").value.toString()
//                    Log.d("user1Name", user1Name)
//
//                } else {}
//            }
//            override fun onCancelled(databaseError: DatabaseError) {}
//        })

    }


    fun reloadChat(user1: String, user2: String) {
        openChat(user1, user2)
    }

//    public fun loadAllPastChats(user1: String) {
//        //in my understanding this only runs once , and so when the other user sends a message, these listeners
//        //are not listening and thus dont update the chats, thus need to make an on child kind of listener in the
//        //on create and make that send a broadcast , whenever some other user messages us or some other user
//        //initiates a chat with us, that needs to update the chats UI
//
//        //changed these to valueEventListener so that they might load on their own, detecting those changes
//        //also notice that there are basically 100s of listeners here for each user2
//        //stemming from a single listener of chats/user1
//        //changing them back to the singlevalueevent Listeners, because as expected they work, but here it creates a mess
//        //in behaviour, will just attach explicit listeners for listening
//        chatsList.clear()
//        FirebaseDatabase.getInstance().reference.child("chats/$user1")
//                .addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(dataSnapshot: DataSnapshot) {
//                        Log.d("Chat", "I am in sfdsfs")
//
//                        for (snapshot in dataSnapshot.children) {
//                            val chat = snapshot.getValue(oneChat::class.java)!!
//                            var user2 = chat.username as String
//                            //case when username is "", not sure why a username can be blank,
//                            // its there when user registers, not sure, i think the queries would still work, or maybe not
//                            Log.d("user2", user2)
//                            //note there are many users in this for loop
//                            //get last message between user 1 and user2
//                            //get name of user2
//                            //get time of last message between user1 and user2
//                            val ref = FirebaseDatabase
//                                    .getInstance().reference.child("messages/" + user1 + "_" + user2)
//                            ref.addListenerForSingleValueEvent(object : ValueEventListener {
//                                @RequiresApi(Build.VERSION_CODES.O)
//                                @Override
//                                override fun onDataChange(snapshot: DataSnapshot) {
//                                    // dataSnapshot is the "users" node with all children with phone_number = phoneNumber
//                                    var lastMessageText: String = ""
//                                    lateinit var lastMessageTime: LocalDateTime
//                                    if (snapshot.exists()) {
//                                        for (datasnapshot in snapshot.children) {
//                                            // do something with the individual "user"
//                                            var tempLastMessageTimeMap: HashMap<Any, Any> =
//                                                    datasnapshot.child("time").value as HashMap<Any, Any>
//                                            //temp is a map so need to extract the relevant date values and then parse them to get the
//                                            //LocalDateTime object
//                                            //when db stores the custom objects like DateTime, it returns them as hashmaps only
//                                            lastMessageTime = getLocalDateTime(tempLastMessageTimeMap)
//
//                                            //message is not there , when its an order
//                                            lastMessageText =
//                                                    if (datasnapshot.child("message").value == null) {
//                                                        "An Order"
//                                                    } else {
//                                                        datasnapshot.child("message").value as String
//                                                    }
//                                        }
//                                        val reference =
//                                                FirebaseDatabase.getInstance().reference
//                                        val query: Query = reference.child("users")
//                                                .orderByChild("phone_number").equalTo(user2)
//                                        query.addListenerForSingleValueEvent(object :
//                                                ValueEventListener {
//                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                                                if (dataSnapshot.exists()) {
//                                                    for (mysnapshot in dataSnapshot.children) {
//                                                        val user2Name =
//                                                                mysnapshot.child("name").value as String
////                                                                  val user2Name = "MyUser"
//
//                                                        this@homeScreen.runOnUiThread({
//                                                            updateChatsListInUIThread(
//                                                                    myDataClass(
//                                                                            lastMessageTime,
//                                                                            lastMessageText,
//                                                                            user2Name,
//                                                                            user2
//                                                                    )
//                                                            ) //Your code to run in GUI thread here
//                                                        } //public void run() {
//                                                        )
//                                                        //ava.lang.IllegalStateException: The content of the adapter has changed but ListView did
//                                                        // //not receive a notification. Make sure the content of your adapter is
//                                                        // not modified from a background thread, but only from the UI thread.
//                                                        // Make sure your adapter calls notifyDataSetChanged() when its content changes.
//                                                        // [in ListView(2131296270, class android.widget.ListView) with Adapter(class com.example.relay.
//                                                        // myCustomAdapter)]
//                                                        //need to add items and call adpater notify dataset
////                                                                changed in the UI thread otherwise throws the error(
////                                                                    illeagal state exception
////                                                                chatsList.add(
////                                                                    myDataClass(
////                                                                        lastMessageTime,
////                                                                        lastMessageText,
////                                                                        user2Name,
////                                                                        user2
//////                                                                    )
//////                                                                )
//////                                                                chatsList.sortWith(Comparator{chatItem1:myDataClass, chatItem2:myDataClass -> if(chatItem1.getTime().isAfter(chatItem2.getTime())) 0 else 1
//////                                                                Log.d("compare", "somenonsense")
//////                                                                })
////                                                                _listOfChats.requestLayout()
////                                                                adapter.notifyDataSetChanged()
////                                                                _listOfChats.requestLayout()
//
//                                                    }
//                                                } else {
//                                                }
//                                            }
//
//                                            override fun onCancelled(databaseError: DatabaseError) {}
//                                        })
//                                    } else {
//                                    }
//                                }
//
//                                override fun onCancelled(error: DatabaseError) {}
//                            })
//                        }
//                    }
//
//                    override fun onCancelled(databaseError: DatabaseError) {}
//                })
//    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun makeUser2InUser1Chat(user1: String, user2: String) {
        Log.d("arpan", "i am here")
        val reference = FirebaseDatabase.getInstance().reference
        val myChatRef = reference.child("chats")
        val map: MutableMap<String, Any> = HashMap()
        map["user2"] = user2
        var time = LocalDateTime.now()
        val map1: MutableMap<String, Any> = HashMap()
        map1["sentByUser"] = user1
//        map1["sentByUserName"] = _yourName.text
        map1["lastSentOrReceivedMessage"] = " "
        map1["time"] = time
        map["lastMessageDetails"] = map1
//        map["lastMessageDetails"] = map1
        myChatRef.child(user1).child(user2).setValue(map)
//        myChatRef.child(user1).child(user2).child("lastMessageDetails").setValue(map1)
        //rather than
    }


    fun isUser2InUser1sChat(user1: String, user2: String) {
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
                    showConfirmDialog(user2)
//                    onWhatsAppClick(user2)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun makeUser1InUser2Chat(user1: String, user2: String) {
        Log.d("arpan", "i am here")
        val reference = FirebaseDatabase.getInstance().reference
        val chatRef = reference.child("chats")
        val map: MutableMap<String, Any> = HashMap()
        var time = LocalDateTime.now()
        val map1: MutableMap<String, Any> = HashMap()
        map1["sentByUser"] = user1
//        map1["sentByUserName"] = _yourName.text
        map1["lastSentOrReceivedMessage"] = " "
        map1["time"] = time
//        map["lastMessageDetails"] = map1
        map["user2"] = user1
        map["lastMessageDetails"] = map1
        chatRef.child(user2).child(user1).setValue(map)
//        chatRef.child(user2).child(user1).child("lastMessageDetails").setValue(map1)
//        myChatRef.child(user2).setValue(map)
//        myChatRef.child(user2).child("lastMessageDetails").setValue(map)

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
            i.data = Uri.parse(url)
            if (i.resolveActivity(packageManager) != null) {
                startActivity(i)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onSmsAppClick(phone: String) {

    }

    fun goToMyOrders() {
        val intent = Intent(this@homeScreen, myOrders::class.java)
        intent.putExtra("user1", user1)
        startActivity(intent)
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


    fun more() {
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
//        _yourName.text = myUser.child("name").value.toString()
//        _yourPhoneNumber.text = myUser.child("phone_number").value.toString()
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
                        _yourBusinessName.text = user.child("business_name").value as String
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
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        return LocalDateTime.parse(fullDatetime, formatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateChatsListInUIThread(myDataClass: myDataClass) {
        //hopefully this function gets called in the ui thread
        chatsList.add(0, myDataClass)
        adapter.notifyDataSetChanged()
    }

    private fun showConfirmDialog(user2: String) {
        val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    onWhatsAppClick(user2)
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                }
            }
        }

        val builder: AlertDialog.Builder = AlertDialog.Builder(this@homeScreen)
        builder.setMessage("Do you want to send an invite through whatsapp?")
            .setPositiveButton("Yes", dialogClickListener)
            .setNegativeButton("No", dialogClickListener).show()
    }

    //update chats whenever the last message sent by user2 is changed(not user 1 as its already detected(from broadcast)
    //update chats whenever the name of a user2 is changed(still keep the chats sorted according to the message time)
    //or some other user initiated a chat with user1, all three dimensions will need to detect
    //set that listener in teh oncreate, which strictly runs on such an operation(and doesnt detect the initial values, or set them on loadall past chats
    //but just detects changes
    //i can put those listeners in between the already implemented single value event listeners, but that will be a pain
    // should i change the db strucuture, nah lets not do that, its a pain to now update from other activities
    //need to change the db and the calls related to that, I can remove those 3 listeners


    private fun detectChangeAndUpdateChats() {
        //this guy is not reponsible to update the chat when this user sends a message, but rather it just listens t
        //to when the other user sends a message
        var myReference1 = FirebaseDatabase.getInstance().getReferenceFromUrl(
            "https://relay-28f2e-default-rtdb.firebaseio.com/chats/"
        )
        myReference1.child(user1).addChildEventListener(object : ChildEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                if (dataSnapshot.exists()) {
//                val map0 = dataSnapshot.value as Map<String, Any>
                    //checking if the message is actually an order:
                    //will need to fix makeUser1inUser2schat and vice versa as well
                    val map = dataSnapshot.value
                    Log.d("myMap", map.toString())
                    val map1 = dataSnapshot.child("lastMessageDetails").value as HashMap<Any, Any>
//                    datasnapshot.child("time").value as HashMap<Any, Any>
                    val user2 = dataSnapshot.child("user2").value.toString()
//                val user2Name = map["sentByUserName"].toString()
                    val timeHashMap = map1["time"] as HashMap<Any, Any>
                    val lastMessageTime = getLocalDateTime(timeHashMap)
                    val lastSentOrReceivedMessage = map1["lastSentOrReceivedMessage"].toString()


                    val reference = FirebaseDatabase.getInstance().reference
                    val query: Query = reference.child("users")
                        .orderByChild("phone_number").equalTo(user2)
                    query.addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {
                        }

                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (mysnapshot in dataSnapshot.children) {
                                    val user2Name =
                                        mysnapshot.child("name").value as String
//                                                                  val user2Name = "MyUser"
                                    this@homeScreen.runOnUiThread {
                                        updateChatsListInUIThread(
                                            myDataClass(
                                                lastMessageTime,
                                                lastSentOrReceivedMessage,
                                                user2Name,
                                                user2
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    })
                }
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onChildChanged(
                dataSnapshot: DataSnapshot,
                s: String?
            ) {
                val map = dataSnapshot.value as Map<String, Any>
                //checking if the message is actually an order:
                //will need to fix makeUser1inUser2schat and vice versa as well
                val map1 = map["lastMessageDetails"] as Map<String, Any>

                val user2 = map["user2"].toString()
//                val user2Name = map["sentByUserName"].toString()
                val timeHashMap = map1["time"] as HashMap<Any, Any>
                val lastMessageTime = getLocalDateTime(timeHashMap)
                val lastSentOrReceivedMessage = map1["lastSentOrReceivedMessage"].toString()

                sendMessageBroadCast(lastSentOrReceivedMessage, lastMessageTime, user2)
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onCancelled(error: DatabaseError) {
                //Should never happen
            }

            override fun onChildMoved(
                dataSnapshot: DataSnapshot,
                s: String?
            ) {
                //should never happen, dont even know what it means
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendMessageBroadCast(
        lastMessageText: String,
        lastMessageTime: LocalDateTime,
        user2: String
    ) {
        Log.d("receiverinChat", "hello")
        val intent = Intent("lastMessageData")
        intent.putExtra("lastMessageTime", lastMessageTime.toString())
        intent.putExtra("lastMessageText", lastMessageText)
        intent.putExtra("user2", user2)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_notifications -> {
            Toast.makeText(this@homeScreen, "Notifications clicked", Toast.LENGTH_SHORT).show()
            true
        }

        R.id.action_help -> {
            Toast.makeText(this@homeScreen, "Help clicked", Toast.LENGTH_SHORT).show()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_home -> {
                // Respond to navigation item 1 click
                true
            }
            R.id.nav_inbox -> {
                // Respond to navigation item 2 click
                goToMyOrders();
                false
            }
            R.id.nav_more -> {
                // Respond to navigation item 2 click
                more();
                false
            }
            else -> false
        };
    }
}