package com.example.atoz_store.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import com.example.atoz_store.CartListner
import com.example.atoz_store.R
import com.example.atoz_store.adapters.AdapterCartProducts
import com.example.atoz_store.databinding.ActivityUsersMainBinding
import com.example.atoz_store.databinding.BsCartProductsBinding
import com.example.atoz_store.roomdb.CartProducts
import com.example.atoz_store.viewModels.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog

class UsersMainActivity : AppCompatActivity() ,CartListner{
    private lateinit var cartProductList: List<CartProducts>
    private val viewModel :UserViewModel by viewModels()
    private lateinit var adapterCartProducts: AdapterCartProducts
    private lateinit var binding: ActivityUsersMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityUsersMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getAllCartProducts()
        getTotalItemCountCart()
        onCartClicked()
        onNextButtonClicked()
    }

    private fun onNextButtonClicked() {
        binding.btnNext.setOnClickListener {
                startActivity(Intent(this,OrderPlaceActivity::class.java))

            }
    }

    private fun getAllCartProducts() {

        viewModel.getAll().observe(this){
            for (i in it){
                cartProductList=it
            }
        }


    }

    private fun onCartClicked() {
        binding.llItemCart.setOnClickListener {
            val bsCartProductsBinding = BsCartProductsBinding.inflate(LayoutInflater.from(this))
            val bs=BottomSheetDialog(this)

            bs.setContentView(bsCartProductsBinding.root)

            bsCartProductsBinding.tvNoOfProductCount.text=binding.tvNoOfProductCount.text
            bsCartProductsBinding.btnNext.setOnClickListener {
                startActivity(Intent(this,OrderPlaceActivity::class.java))

            }
            adapterCartProducts= AdapterCartProducts()
            bsCartProductsBinding.rvProductsItems.adapter=adapterCartProducts
            adapterCartProducts.differ.submitList(cartProductList)
            bs.show()




        }

    }

    private fun getTotalItemCountCart() {
        viewModel.fetchTotalItemCart().observe(this){
            if(it>0){
                binding.llCart.visibility=View.VISIBLE
                binding.tvNoOfProductCount.text=it.toString()

            }else{
                binding.llCart.visibility=View.GONE
                binding.tvNoOfProductCount.text="0"
            }

        }

    }

    override fun showcartLayout(itemCount:Int) {
        val previousCount=binding.tvNoOfProductCount.text.toString().toInt()
        val updateCount=previousCount+itemCount
        if(updateCount>0){
            binding.llCart.visibility=View.VISIBLE
            binding.tvNoOfProductCount.text=updateCount.toString()
        }
        else{
            binding.llCart.visibility= View.GONE
            binding.tvNoOfProductCount.text="0"

        }


    }
    override fun savingCartItemCount(itemCount: Int) {
       viewModel.fetchTotalItemCart().observe(this){
           viewModel.savingCartItemCount(it+itemCount)

       }


    }

    override fun hideCartLayout() {
        binding.llCart.visibility=View.GONE
        binding.tvNoOfProductCount.text="0"
    }


}