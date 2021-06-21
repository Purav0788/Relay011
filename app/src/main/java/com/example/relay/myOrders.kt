package com.example.relay

//import android.R

//import android.R

//import android.R
//import android.R

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*


class myOrders : AppCompatActivity() {
    val myStartDateCalender: Calendar = Calendar.getInstance()
    val myEndDateCalender: Calendar = Calendar.getInstance()
    private lateinit var user1: String
    private lateinit var ordersList: List<myOrdersItem>
    private lateinit var myOrdersAdapter: myOrdersAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_orders)
        user1 = intent.getStringExtra("user1")!!
        setupDates()
        setUpOrdersLabelSpinner()
//        setUpMerchantsSpinner()
        fillOrdersList()
//        setUpRecyclerView()

    }

    private fun setupDates() {

        val startDateEditText = findViewById<View>(R.id.startDate) as EditText
        val startDate =
                OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    myStartDateCalender.set(Calendar.YEAR, year)
                    myStartDateCalender.set(Calendar.MONTH, monthOfYear)
                    myStartDateCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    updateStartDateLabel()
                }

        startDateEditText.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                DatePickerDialog(
                        this@myOrders, startDate, myEndDateCalender
                        .get(Calendar.YEAR), myEndDateCalender.get(Calendar.MONTH),
                        myStartDateCalender.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        })

        val endDateEditText = findViewById<View>(R.id.endDate) as EditText
        val endDate =
                OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    myEndDateCalender.set(Calendar.YEAR, year)
                    myEndDateCalender.set(Calendar.MONTH, monthOfYear)
                    myEndDateCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    updateEndDateLabel()
                }

        endDateEditText.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                DatePickerDialog(
                        this@myOrders, endDate, myEndDateCalender
                        .get(Calendar.YEAR), myEndDateCalender.get(Calendar.MONTH),
                        myEndDateCalender.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        })

    }

    private fun updateStartDateLabel() {
        val startDateEditText = findViewById<View>(R.id.startDate) as EditText
        val myFormat = "dd/MM/yy" //In which you need put here
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        startDateEditText.setText(sdf.format(myStartDateCalender.getTime()))
        //here call the function for the listener
        selectedItemsChanged()
    }

    private fun updateEndDateLabel() {
        val endDateEditText = findViewById<View>(R.id.endDate) as EditText
        val myFormat = "dd/MM/yy" //In which you need put here
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        endDateEditText.setText(sdf.format(myEndDateCalender.getTime()))
        //here call the function for hte listener
        selectedItemsChanged()
    }

    public fun startDateButton(view: View) {
        val startDateEditText = findViewById<View>(R.id.startDate) as EditText
        val startDate =
                OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    myStartDateCalender.set(Calendar.YEAR, year)
                    myStartDateCalender.set(Calendar.MONTH, monthOfYear)
                    myStartDateCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    updateStartDateLabel()
                }

        DatePickerDialog(
                this@myOrders, startDate, myEndDateCalender
                .get(Calendar.YEAR), myEndDateCalender.get(Calendar.MONTH),
                myStartDateCalender.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    public fun endDateButton(view: View) {
        val endDateEditText = findViewById<View>(R.id.endDate) as EditText
        val endDate =
                OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    myEndDateCalender.set(Calendar.YEAR, year)
                    myEndDateCalender.set(Calendar.MONTH, monthOfYear)
                    myEndDateCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    updateEndDateLabel()
                }

        DatePickerDialog(
                this@myOrders, endDate, myEndDateCalender
                .get(Calendar.YEAR), myEndDateCalender.get(Calendar.MONTH),
                myEndDateCalender.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setUpOrdersLabelSpinner() {

        val spinnerArray: MutableList<String> = ArrayList()
        spinnerArray.add("All Orders")
        spinnerArray.add("Orders Sent")
        spinnerArray.add("Orders Received")

        val adapter = ArrayAdapter(
                this, android.R.layout.simple_spinner_item, spinnerArray
        )
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        val sItems = findViewById<View>(R.id.ordersLabel) as Spinner
        sItems.adapter = adapter
        //here set the function for the listener

        sItems.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(
                    parentView: AdapterView<*>?,
                    selectedItemView: View,
                    position: Int,
                    id: Long
            ) {
                selectedItemsChanged()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                selectedItemsChanged()
            }
        })
    }


    public fun fillOrdersList() {
        ordersList = ArrayList<myOrdersItem>()

        var myReference1 = FirebaseDatabase.getInstance().getReferenceFromUrl(
                "https://relay-28f2e-default-rtdb.firebaseio.com/chats/"
        )
        myReference1.child(user1).addChildEventListener(object : ChildEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                if (dataSnapshot.exists()) {
                    //if someone else initiates a chat with this user
                    var user2 = dataSnapshot.child("user2").value.toString()

                    var reference = FirebaseDatabase.getInstance().getReferenceFromUrl(
                            "https://relay-28f2e-default-rtdb.firebaseio.com/messages/"
                                    + user1 + "_" + user2
                    )
                    Log.d("in users", user1 + "_" + user2)
                    val query: Query = reference
                    query.addChildEventListener(object : ChildEventListener {
                        override fun onCancelled(error: DatabaseError) {
                        }

                        override fun onChildMoved(
                                snapshot: DataSnapshot,
                                previousChildName: String?
                        ) {
                        }

                        override fun onChildChanged(
                                snapshot: DataSnapshot,
                                previousChildName: String?
                        ) {
                        }

                        override fun onChildAdded(
                                messageSnapshot: DataSnapshot,
                                previousChildName: String?
                        ) {
                            if (messageSnapshot.exists()) {
                                Log.d("myorders", "here3new")
                                //if someone else initiates a chat with this user
                                val map = messageSnapshot.value
                                Log.d("myMapinMyorders", map.toString())
                                if (messageSnapshot.child("orderID").value != null) {
                                    val orderID = messageSnapshot.child("orderID").value.toString()
                                    Log.d("here4new", "myOrders")
                                    var myOrdersReference1 = FirebaseDatabase.getInstance().getReferenceFromUrl(
                                            "https://relay-28f2e-default-rtdb.firebaseio.com/orders/$orderID"
                                    )

                                    val query: Query = myOrdersReference1
                                    Log.d("myorderssnaporderIDP", orderID)
                                    query.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onCancelled(error: DatabaseError) {

                                        }

                                        override fun onDataChange(orderSnapshot: DataSnapshot) {

                                            Log.d("orrderSnapshotMap", orderSnapshot.value.toString())
                                            var orderSnapshotItem = orderSnapshot
                                            var orderSnapshotItemValueMap = orderSnapshotItem.value as HashMap<String, Any>
                                            Log.d("orrderSnapshotMap2", orderSnapshotItem.value.toString())

                                            Log.d("myorderssnaporderID", orderID)
//                                                Log.d("myOrdersID", orderSnapshotItem.toString())


                                            lateinit var orderStatus: String
                                            lateinit var orderDate: String
                                            lateinit var orderUser2Name: String
                                            lateinit var totalPrice: String
                                            var noOfItems: Int = 0
                                            Log.d("myorderssnap", orderSnapshotItemValueMap.toString())
                                            Log.d("myorderssnapOrder", orderSnapshotItemValueMap["orderCancelled"].toString())
                                            if (orderSnapshotItemValueMap["orderCancelled"].toString() == "true") {
                                                orderStatus = "Order Cancelled"
                                            } else if (orderSnapshotItemValueMap["orderConfirmed"].toString() == "true") {
                                                orderStatus = "Order Confirmed"
                                            } else {
                                                orderStatus = "Awaiting Order Confirmation"
                                            }



                                            orderDate = (if ((orderSnapshotItemValueMap["deliveryDate"] == "") || (orderSnapshotItemValueMap["deliveryDate"] == null)) {
                                                "Not Given"
                                            } else {
                                                orderSnapshotItemValueMap["deliveryDate"].toString()
                                            }).toString()
//                                                    orderDate = "NOne"
//                                                    orderUser2Name = "none"

                                            if (orderStatus == "Order Cancelled" || orderStatus == "Order Confirmed") {
                                                totalPrice =
                                                        orderSnapshotItemValueMap["totalPrice"].toString()
                                            } else {
                                                totalPrice = "Not yet sent by the receiver"
                                            }
//                                                    totalPrice = "100"

                                            for (i in orderSnapshotItemValueMap["orderList"] as List<String>) {
                                                noOfItems++
                                            }

                                            Log.d("myorderssnaporderStatus", orderStatus)
                                            Log.d("myorderssnaporderDate", orderDate)
                                            Log.d("myorderssnaptotalPrice", totalPrice)
                                            Log.d("myorderssnapnoOfItems", noOfItems.toString())
                                            val query: Query =
                                                    FirebaseDatabase.getInstance().reference.child("users")
                                                            .orderByChild("phone_number").equalTo(user2)
                                            query.addListenerForSingleValueEvent(object :
                                                    ValueEventListener {
                                                override fun onCancelled(error: DatabaseError) {
                                                }

                                                override fun onDataChange(userSnapshot: DataSnapshot) {
                                                    if (userSnapshot.exists()) {
                                                        for (userSnapshotItem in userSnapshot.children) {
                                                            val user2Name =
                                                                    userSnapshotItem.child("name").value.toString()
                                                            orderUser2Name = user2Name
                                                            //handling the case that this should only show the final order
                                                            //which is either orderReceived1 or Awaiting Order Confirmed
                                                            //it is showing one for each order message for now
                                                            //whenever the orderStatus is higher than the message status
                                                            //then continue
                                                            if ((messageSnapshot.child("orderCancelled").value == null) && (orderStatus == "Order Cancelled")) {
                                                                continue

                                                            } else if ((messageSnapshot.child("orderConfirmed").value == null) && (orderStatus == "Order Confirmed")) {
                                                                continue
                                                            }
                                                            (ordersList as ArrayList<myOrdersItem>).add(
                                                                    myOrdersItem(
                                                                            orderStatus,
                                                                            orderDate,
                                                                            orderUser2Name,
                                                                            totalPrice,
                                                                            noOfItems.toString()
                                                                    ))
                                                        }
                                                        setUpRecyclerView()
                                                    }
                                                }

                                            })


                                        }

//
//


                                    })


                                }
                            }
                        }

                        override fun onChildRemoved(snapshot: DataSnapshot) {
                        }
                    })
                }
            }


            override fun onChildChanged(
                    dataSnapshot: DataSnapshot,
                    s: String?
            ) {
                //for now i dont think its needed as when i changed the status of an order, it still
                //got reflected in my orders
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


    private fun selectedItemsChanged() {
        val myFormat = "dd/MM/yyyy" //In which you need put here
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        //here i should check if the endDateTime is set or not, or maybe I should initialize it with some time
        val endDateTimeText = sdf.format(myEndDateCalender.time)
        val startDateTimeText = sdf.format(myStartDateCalender.time)
        val mySpinner = findViewById<View>(R.id.ordersLabel) as Spinner
        val spinnerText = mySpinner.selectedItem.toString()
        val filterText = "$endDateTimeText#$startDateTimeText#$spinnerText"
        if(this::myOrdersAdapter.isInitialized){
            //i think right at the start of the activity it was running this event
            myOrdersAdapter.filter.filter(filterText);
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        val inflater = menuInflater
//        inflater.inflate(R.menu.main_menu, menu)
//        val searchItem = menu.findItem(R.id.actionSearch)
//        val searchView = searchItem.actionView as SearchView
//        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                return false
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                myOrdersAdapter.getFilter().filter(newText)
//                return false
//            }
//        })
//        return true
//    }


    private fun setUpRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.myOrders)
        recyclerView.setHasFixedSize(true)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        myOrdersAdapter = myOrdersAdapter(ordersList as MutableList<myOrdersItem>)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = myOrdersAdapter
    }
}