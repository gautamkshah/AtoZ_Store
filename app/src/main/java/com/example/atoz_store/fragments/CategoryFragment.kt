package com.example.atoz_store.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.atoz_store.CartListner
import com.example.atoz_store.R
import com.example.atoz_store.Utils
import com.example.atoz_store.adapters.AdapterProduct
import com.example.atoz_store.databinding.FragmentCategoryBinding
import com.example.atoz_store.databinding.ItemViewProductBinding
import com.example.atoz_store.models.Product
import com.example.atoz_store.roomdb.CartProducts
import com.example.atoz_store.viewModels.UserViewModel
import kotlinx.coroutines.launch



class CategoryFragment : Fragment() {
    private val viewModel: UserViewModel by viewModels()
    private lateinit var binding: FragmentCategoryBinding
    private var category: String? = null
    private lateinit var adapterProduct: AdapterProduct
    private var cartListener: CartListner? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentCategoryBinding.inflate(layoutInflater)

        getProductCategory()
        setToolBarTitle()
        onSearchMenuClick()
        onNavigationIconClicked()

        fetchCateogryProduct()
        return binding.root
    }

    private fun onNavigationIconClicked() {
        binding.tbSearchFragment.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_categoryFragment_to_homeFragment)
        }
    }

    private fun onSearchMenuClick() {
        binding.tbSearchFragment.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.searchMenu -> {
                    findNavController().navigate(R.id.action_categoryFragment_to_searchBlankFragment)
                    true
                }

                else -> false
            }
        }
    }

    private fun fetchCateogryProduct() {
        Log.d("categyyory", "getProductCategory: $category")
        binding.shimmerViewContainer.visibility = View.VISIBLE
        Log.d("feyttch", "enter: ")
        lifecycleScope.launch {
            category?.let {
                viewModel.getCategoryProduct(it).collect {
                    if (it.isEmpty()) {
                        binding.rvProducts.visibility = View.GONE
                        binding.tvtext.visibility = View.VISIBLE

                    } else {

                        binding.rvProducts.visibility = View.VISIBLE
                        binding.tvtext.visibility = View.GONE
                    }
                    Log.d("feh", "fetchCateogryProduct: ${it.size}")
                    adapterProduct = AdapterProduct(
                        ::onAddButtonClicked,
                        ::onIncreamentButtonClicked,
                        ::onDecreamentButtonClicked
                    )
                    binding.rvProducts.adapter = adapterProduct
                    adapterProduct.differ.submitList(it)
                    adapterProduct.originalList = it as ArrayList<Product>
                    binding.shimmerViewContainer.visibility = View.GONE
                }
            }
        }
    }

    private fun setToolBarTitle() {
        binding.tbSearchFragment.title = category
    }

    private fun getProductCategory() {
        val bundle = arguments
        category = bundle?.getString("category")

    }

    private fun onAddButtonClicked(product: Product, productBinding: ItemViewProductBinding) {
        productBinding.tvAdd.visibility = View.GONE
        productBinding.lLProductCount.visibility = View.VISIBLE


        var itemCount = productBinding.tvProductCount.text.toString().toInt()
        itemCount++
        productBinding.tvProductCount.text = itemCount.toString()
        cartListener?.showcartLayout(1)


        // step2
        product.itemCount = itemCount
        lifecycleScope.launch {
            cartListener?.savingCartItemCount(1)
            saveProductInRoomDb(product)
            viewModel.updateItemCount(product, itemCount)
        }


    }
    private fun onIncreamentButtonClicked(
        product: Product,
        productBinding: ItemViewProductBinding,
    ) {
        var itemCountIncrement = productBinding.tvProductCount.text.toString().toInt()
        itemCountIncrement++

        if (product.productStock!! + 1 > itemCountIncrement) {
            productBinding.tvProductCount.text = itemCountIncrement.toString()
            cartListener?.showcartLayout(1)
//step2
            product.itemCount = itemCountIncrement
            lifecycleScope.launch {
                cartListener?.savingCartItemCount(1)
                saveProductInRoomDb(product)
                viewModel.updateItemCount(product, itemCountIncrement)

            }
        }else{
            Utils.showToast(requireContext(),"Stock Over")
        }
    }

    fun onDecreamentButtonClicked(product: Product, productBinding: ItemViewProductBinding) {
        var itemCountDecrement = productBinding.tvProductCount.text.toString().toInt()
        itemCountDecrement--

        product.itemCount = itemCountDecrement
        lifecycleScope.launch {
            cartListener?.savingCartItemCount(-1)
            saveProductInRoomDb(product)
            viewModel.updateItemCount(product, itemCountDecrement)

        }

        if (itemCountDecrement <= 0) {
            lifecycleScope.launch {
                viewModel.deleteCartProduct(product.productRandomId!!)
            }
            productBinding.tvAdd.visibility = View.VISIBLE
            productBinding.lLProductCount.visibility = View.GONE
            productBinding.tvProductCount.text = "0"
        } else {

            productBinding.tvProductCount.text = itemCountDecrement.toString()

        }
        cartListener?.showcartLayout(-1)

        //step2
//        product.itemCount=itemCountDecrement
//        lifecycleScope.launch {
//            cartListener?.savingCartItemCount(-1)
//            saveProductInRoomDb(product)
//        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is CartListner) {
            cartListener = context
        } else {
            throw ClassCastException("must implement CartListner")
        }
    }

    private fun saveProductInRoomDb(product: Product) {
        val cartProduct = CartProducts(
            productId = product.productRandomId!!,
            productTitle = product.productTitle,
            productQuantity = product.productQuantity.toString() + product.productUnit.toString(),
            productPrice = "₹" + "${product.productPrice}",
            productCount = product.itemCount,
            productStock = product.productStock,
            productImage = product.productImageUris?.get(0)!!,
            productCategory = product.productCategory,
            adminUid = product.adminUid,
            productType = product.productType

        )
        lifecycleScope.launch {
            viewModel.insertCartProduct(cartProduct)
        }

    }


}