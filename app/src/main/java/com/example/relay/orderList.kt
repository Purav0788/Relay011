package com.example.relay


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.order_list_item.view.*


class orderList : AppCompatActivity() {
    private var layoutID = 0
    private var listOfOrders:ArrayList<kotlin.String?>? = ArrayList<kotlin.String?>()
    private val LAUNCH_ORDER_DETAILS :Int = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_list)
    }

    public fun addListItem(view:View){
        var parent = findViewById<View>(R.id.orderList) as RelativeLayout
        val neu: View = layoutInflater.inflate(R.layout.order_list_item, parent, false)
        neu.id = layoutID
        var params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
            )
        params.addRule(RelativeLayout.BELOW, layoutID-1)
        neu.layoutParams = params
        setUpSpinner(neu.spinner)
        parent.addView(neu)
        neu.imageButton
            .setOnClickListener(View.OnClickListener {
                neu.orderQuantity.setText((neu.orderQuantity.toString().toInt()+1).toString())
            })
        neu.imageButton2.setOnClickListener(View.OnClickListener {
            neu.orderQuantity.setText((neu.orderQuantity.toString().toInt()-1).toString())
        })
        layoutID++
    }

    private fun setUpSpinner(dropdown:Spinner){
        val dropdown = dropdown
        //create a list of items for the spinner.
        val items = arrayOf("kg", "gram", "packets", "bottles", "boxes")
        val adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, items)
        dropdown.adapter = adapter
    }

    public fun next(view:View){
        for(i:Int in 0..layoutID-1){
            var order:String
            var neu:View = findViewById<View>(i)
            order = neu.orderName.toString()
            order = order + "#"+ neu.orderQuantity.toString()
            order = order + "#" + neu.spinner.selectedItem.toString()
            listOfOrders!!.add(order)
        }
        val intent = Intent(this@orderList, orderDetails::class.java)
        intent.putExtra("listOfOrders", listOfOrders)
        startActivityForResult(intent, LAUNCH_ORDER_DETAILS);
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
}

