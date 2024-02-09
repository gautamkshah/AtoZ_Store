package com.example.atoz_store.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.atoz_store.R
import com.example.atoz_store.adapters.AdapterCartProducts
import com.example.atoz_store.adapters.AdapterOrders
import com.example.atoz_store.databinding.FragmentOrderDetailBinding
import com.example.atoz_store.viewModels.UserViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class OrderDetailFragment : Fragment() {
    private lateinit var binding: FragmentOrderDetailBinding
    private var status = 0
    private var orderId = ""
    private val viewModel: UserViewModel by viewModels()
    private lateinit var adapterCartProducts: AdapterCartProducts


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentOrderDetailBinding.inflate(layoutInflater)
        getValues()
        settingStatus()
        lifecycleScope.launch {
            getOrderedProducts()

        }
        onBackButtonClick()

        return binding.root
    }

    private fun onBackButtonClick() {
        binding.tbOrderDetailFragment.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_orderDetailFragment_to_ordersFragment)
        }
    }

    suspend fun getOrderedProducts() {
        viewModel.getOrderedProducts(orderId).collect{
            adapterCartProducts = AdapterCartProducts()
            binding.rvProductsItems.adapter = adapterCartProducts
            adapterCartProducts.differ.submitList(it)

        }


    }

    private fun settingStatus() {
        val viewMap = mapOf(
            0 to listOf(binding.iv1),
            1 to listOf(binding.iv1, binding.iv2, binding.view1),
            2 to listOf(binding.iv1, binding.iv2, binding.view1, binding.iv3, binding.view2),
            3 to listOf(binding.iv1, binding.iv2, binding.view1, binding.iv3, binding.view2, binding.iv4, binding.view3)
        )

       val viewsTint=viewMap.getOrDefault(status, emptyList())

        for(it in viewsTint){
            it.backgroundTintList=ContextCompat.getColorStateList(requireContext(),R.color.blue)
        }

//        when (status) {
//            0 -> {
//                binding.iv1.backgroundTintList =
//                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
//
//            }
//
//            1 -> {
//                binding.iv1.backgroundTintList =
//                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
//                binding.iv2.backgroundTintList =
//                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
//                binding.view1.backgroundTintList =
//                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
//            }
//
//            2 -> {
//                binding.iv1.backgroundTintList =
//                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
//                binding.iv2.backgroundTintList =
//                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
//                binding.view1.backgroundTintList =
//                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
//                binding.iv3.backgroundTintList =
//                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
//                binding.view2.backgroundTintList =
//                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
//
//            }
//
//            3 -> {
//                binding.iv1.backgroundTintList =
//                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
//                binding.iv2.backgroundTintList =
//                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
//                binding.view1.backgroundTintList =
//                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
//                binding.iv3.backgroundTintList =
//                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
//                binding.view2.backgroundTintList =
//                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
//                binding.iv4.backgroundTintList =
//                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
//                binding.view3.backgroundTintList =
//                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
//
//            }
//        }




    }
    private fun getValues() {
        val bundle = arguments
        status = bundle?.getInt("status")!!
        orderId = bundle.getString("orderId").toString()

    }
}