package com.example.atoz_store.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.atoz_store.R
import com.example.atoz_store.Utils
import com.example.atoz_store.adapters.AdapterOrders
import com.example.atoz_store.databinding.FragmentOrdersBinding
import com.example.atoz_store.models.OrderedItems
import com.example.atoz_store.viewModels.UserViewModel
import kotlinx.coroutines.launch
import java.lang.StringBuilder

class OrdersFragment : Fragment() {
   private lateinit var adapterOrders: AdapterOrders
   private lateinit var binding: FragmentOrdersBinding
   private val viewModel: UserViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding=FragmentOrdersBinding.inflate(layoutInflater)
        onBackButtonClicked()
        getAllOrders()
        return binding.root

    }

    private fun getAllOrders() {
        binding.shimmerViewContainer.visibility=View.VISIBLE
        lifecycleScope.launch {
            viewModel.getAllOrders().collect(){
                orderList->


                if(orderList.isNotEmpty()){

                    val orderedList=ArrayList<OrderedItems>()
                    for(orders in orderList){

                        val title=StringBuilder()
                        var totalprice=0
                        for (products in orders.orderList!!) {
                            val price = products.productPrice?.substring(1)?.toInt()
                            val itemCount = products.productCount!!
                            totalprice += (price?.times(itemCount)!!)

                           title.append("${products.productCategory}, ")
                        }
                        val orderedItems=OrderedItems(orders.orderId,orders.orderDate,orders.orderStatus,title.toString(),totalprice)
                        Utils.showToast(requireContext(),"${orderedItems.itemTitle}")
                        orderedList.add(orderedItems)

                    }
                    adapterOrders= AdapterOrders(requireContext(),::onOrderItemViewClick)
                    binding.rvOrders.adapter=adapterOrders
                    adapterOrders.differ.submitList(orderedList)
                    binding.shimmerViewContainer.visibility=View.VISIBLE

                }
                else{

                }

            }
        }

    }

    fun onOrderItemViewClick(orderedItems: OrderedItems){
        val bundle=Bundle()
        bundle.putInt("status", orderedItems.itemStatus!!)
        bundle.putString("orderId", orderedItems.orderId)
        findNavController().navigate(R.id.action_ordersFragment_to_orderDetailFragment,bundle)

    }

    private fun onBackButtonClicked() {
        binding.tbProfileFragment.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_ordersFragment_to_profileFragment)
        }
    }


}