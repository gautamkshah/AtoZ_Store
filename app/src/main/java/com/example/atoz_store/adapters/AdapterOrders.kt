package com.example.atoz_store.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.atoz_store.Utils
import com.example.atoz_store.databinding.ItemViewOrdersBinding
import com.example.atoz_store.models.OrderedItems


class AdapterOrders(requireContext: Context, val onOrderItemViewClick: (OrderedItems) -> Unit) : RecyclerView.Adapter<AdapterOrders.OrdersViewHolder>() {
    class OrdersViewHolder(val binding: ItemViewOrdersBinding) : ViewHolder(binding.root)

    val diffUtil = object : DiffUtil.ItemCallback<OrderedItems>() {
        override fun areItemsTheSame(oldItem: OrderedItems, newItem: OrderedItems): Boolean {
            return oldItem.orderId == newItem.orderId
        }

        override fun areContentsTheSame(oldItem: OrderedItems, newItem: OrderedItems): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder {
        return OrdersViewHolder(
            ItemViewOrdersBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )


    }

    override fun getItemCount(): Int {
        Log.d("AdapterOrders", "getItemCount: ${differ.currentList.size}")
        return differ.currentList.size

    }

    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        val order = differ.currentList[position]
        holder.binding.apply {
            Utils.showToast(root.context, "Order Id: ${order.orderId}")
            tvOrderTitles.text = order.itemTitle
            tvOrderDate.text = order.itemDate
            tvOrderAmount.text = "₹${order.itemPrice.toString()}"
            when (order.itemStatus) {
                0 -> {
                    tvOrderStatus.text = "Ordered"
                }

                1 -> {
                    tvOrderStatus.text = "Recieved"
                }

                2 -> {
                    tvOrderStatus.text = "Dispatched"
                }

                3 -> {
                    tvOrderStatus.text = "Delivered"
                }

            }


        }
        holder.itemView.setOnClickListener {
            onOrderItemViewClick(order)
        }

    }
}