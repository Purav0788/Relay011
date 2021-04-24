package com.example.relay

//import android.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class myOrdersAdapter internal constructor(myOrdersList: MutableList<myOrdersItem>) :
    RecyclerView.Adapter<myOrdersAdapter.myOrdersViewHolder>(), Filterable {
    private var ordersList: List<myOrdersItem> = myOrdersList
    private var ordersListFull: List<myOrdersItem> = myOrdersList

    inner class myOrdersViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        lateinit var orderDate: TextView
        lateinit var orderStatus: TextView
        lateinit var user2Name: TextView
        lateinit var totalPrice: TextView
        lateinit var noOfItems: TextView

        init {
            orderDate = itemView.findViewById(R.id.orderDate)
            orderStatus = itemView.findViewById(R.id.orderStatus)
            user2Name = itemView.findViewById(R.id.user2Name)
            totalPrice = itemView.findViewById(R.id.totalPrice)
            noOfItems = itemView.findViewById(R.id.noOfItems)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): myOrdersViewHolder {
        val v: View =
            LayoutInflater.from(parent.context).inflate(R.layout.my_orders_list_item, parent, false)
        return myOrdersViewHolder(v)
    }

    override fun onBindViewHolder(
        holder: myOrdersViewHolder,
        position: Int
    ) {
        val currentItem: myOrdersItem = ordersList[position]
        holder.orderDate.setText(currentItem.orderDate)
        holder.user2Name.setText(currentItem.user2Name)
        holder.orderStatus.setText(currentItem.orderStatus)
        holder.noOfItems.setText(currentItem.noOfItems)
        holder.totalPrice.setText(currentItem.totalPrice)
    }

    override fun getItemCount(): Int {
        return ordersList.size
    }


    override fun getFilter(): Filter {
        return merchantFilter
    }

    private val merchantFilter: Filter = object : Filter() {
        protected override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList: MutableList<myOrdersItem> = ArrayList()
            if (constraint == null || constraint.length == 0) {
                filteredList.addAll(ordersListFull)
            } else {
                val filterPattern =
                    constraint.toString().toLowerCase().trim { it <= ' ' }
                for (item in ordersListFull) {
                    if (item.user2Name.toLowerCase().contains(filterPattern)) {
                        filteredList.add(item)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        protected override fun publishResults(
            constraint: CharSequence?,
            results: FilterResults
        ) {
            myOrdersList.clear()
            myOrdersList.addAll((results.values as List<myOrdersItem>))
            notifyDataSetChanged()
        }
    }

    init {
        this.ordersList = myOrdersList
        ordersListFull = ArrayList(myOrdersList)
    }


}