package com.example.relay


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_order_list.*
import kotlinx.android.synthetic.main.activity_order_list.view.*
import kotlinx.android.synthetic.main.order_list_item.view.*
import java.util.*
import kotlin.collections.ArrayList


class orderList : AppCompatActivity() {
    private var layoutID = 1
    private var listOfOrders:ArrayList<kotlin.String> = ArrayList<kotlin.String>()
    private val LAUNCH_ORDER_DETAILS :Int = 1
    private lateinit var user1:String
    private lateinit var user2:String
    private lateinit var reference1:DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_list)
        user1 = intent.getStringExtra("user1")!!
        user2 = intent.getStringExtra("user2")!!


        loadLastPrevOrder()

        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

    }

    public fun addListItem(view:View = findViewById<View>(R.id.orderList)){
        var parent = findViewById<View>(R.id.orderList) as RelativeLayout
        val neu: View = layoutInflater.inflate(R.layout.order_list_item, parent, false)
        neu.id = layoutID
        var params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            params.addRule(RelativeLayout.BELOW, layoutID-1)

        neu.layoutParams = params
        setUpSpinner(neu.spinner)
        parent.addView(neu)
        neu.imageButton.setOnClickListener(View.OnClickListener {
                if(neu.orderQuantity.text.toString().toInt() == 0){
                    increaseTotalItems()
                }
                neu.orderQuantity.setText((neu.orderQuantity.text.toString().toInt()+1).toString())
            })
        neu.imageButton2.setOnClickListener(View.OnClickListener {
            if(neu.orderQuantity.text.toString().toInt() == 1){
                decreaseTotalItems()
            }
            if(neu.orderQuantity.text.toString().toInt()>0){
                neu.orderQuantity.setText((neu.orderQuantity.text.toString().toInt()-1).toString())
            }
        })
        layoutID++

    }

    private fun decreaseTotalItems() {
        var totalItems = findViewById<View>(R.id.totalItems)
        var quantity = totalItems.totalItems.text.toString().toInt()
        Log.d("quantity",quantity.toString())
        quantity = quantity - 1
        totalItems.totalItems.setText(quantity.toString())
    }

    private fun setUpSpinner(dropdown:AutoCompleteTextView){
        val dropdown = dropdown
        //create a list of items for the spinner.
        val items = arrayOf("kg", "gram", "packets", "bottles", "boxes")
        val adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, items)
        dropdown.setAdapter(adapter)
    }

    public fun goToOrderDetails(view:View){
        Log.d("orderlist", "I am here")
        listOfOrders.clear()
        for(i:Int in 1..layoutID-1){
            Log.d("orderlist", "I am in for loop")
            var order:String
            var newOrderItem:View = findViewById<View>(i)
            if( (newOrderItem.orderName.text.toString() == "") ||
                (newOrderItem.orderQuantity.text.toString().toInt() == 0) ||
                (newOrderItem.spinner.text.isEmpty())
            ){
                continue
            }
            order = newOrderItem.orderName.text.toString()
            order = order + "#"+ newOrderItem.orderQuantity.text.toString()
            order = order + "#" + newOrderItem.spinner.text.toString()
            Log.d("orderlist", order)
            listOfOrders.add(order)
            Log.d("orderlistindex", listOfOrders.elementAt(0).toString())
        }
        if(listOfOrders.size != 0){
            val intent = Intent(this@orderList, orderDetails::class.java)
            intent.putExtra("listOfOrders", listOfOrders)
            intent.putExtra("user1", user1)
            intent.putExtra("user2", user2)
            startActivityForResult(intent, LAUNCH_ORDER_DETAILS);
        }


    }

    private fun increaseTotalItems(){
        var totalItems = findViewById<View>(R.id.totalItems)
        var quantity = totalItems.totalItems.text.toString().toInt()
        Log.d("quantity",quantity.toString())
        quantity = quantity + 1
       totalItems.totalItems.setText(quantity.toString())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LAUNCH_ORDER_DETAILS) {
            if (resultCode == Activity.RESULT_OK) {
                setResult(Activity.RESULT_OK,data);
                finish();
            }
            if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }

    public fun loadLastPrevOrder(){
        var lastOrderMessageID: String? = null
        reference1 = FirebaseDatabase.getInstance().getReferenceFromUrl(
            "https://relay-28f2e-default-rtdb.firebaseio.com/messages/"
                    + user1 + "_" + user2)
        reference1.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(myItem in dataSnapshot.children){
                    if((myItem.child("orderID").value != null)&&(myItem.child("orderConfirmed").value == null)&&myItem.child("user").value==user1) {
                        lastOrderMessageID = myItem.child("orderID").value as String
                    }
                }
                if(lastOrderMessageID != null){
                    loadPrevOrder(lastOrderMessageID!!)
                }else{
                    addListItem()
                }
            }

        })

    }

    public fun loadPrevOrder(lastOrderMessageID :String){
        //add list items
        //then populate it , with the order data
        val reference1 = FirebaseDatabase.getInstance().getReferenceFromUrl(
            "https://relay-28f2e-default-rtdb.firebaseio.com/orders/$lastOrderMessageID"
        )
        reference1.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("confirm", " I am here")
                var orderList: DataSnapshot = snapshot.child("orderList")
                val delimiter = "#"
                var orderName = ""
                var quantity:Int
                var unit:String = ""
                var count = 0;
                for ( i in orderList.children){
                    if ( i!= null) {
                        var t = i.value as String
                        count++;
                        val splitString = t.split(delimiter).toTypedArray();
                        orderName =splitString[0]
                        quantity = splitString[1].toInt()
                        unit = splitString[2]
                        setUpItem(orderName, unit)
                    }
                }

            }
        })

    }

    public fun setUpItem(orderName:String, unit:String){
            addListItem()
            var newOrderItem:View = findViewById<View>(layoutID-1)
            newOrderItem.orderName.setText(orderName)
            newOrderItem.orderQuantity.setText("0")
//            Log.d("unit", unit)
//            val spinnerSelectedItemIndex:Int = arrayOf("kg", "gram", "packets", "bottles", "boxes").indexOf(unit)
            newOrderItem.spinner.setText(unit)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu_order_list, menu)

        val myActionMenuItem = menu!!.findItem(R.id.action_search)
        val searchView = myActionMenuItem.actionView as androidx.appcompat.widget.SearchView
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // Toast like print
                Toast.makeText(this@orderList, "SearchOnQueryTextSubmit: $query", Toast.LENGTH_SHORT).show()
                if (!searchView.isIconified()) {
                    searchView.setIconified(true)
                }
                myActionMenuItem.collapseActionView()
                return false
            }

            override fun onQueryTextChange(s: String?): Boolean {
                Toast.makeText(this@orderList, "onQueryTextChange: $s", Toast.LENGTH_SHORT).show()
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_info -> {
            Toast.makeText(this, "Info clicked", Toast.LENGTH_SHORT).show()
            true
        }

        android.R.id.home ->{
            onBackPressed()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    fun backPressed(view: View) {
        onBackPressed()
    }
}

