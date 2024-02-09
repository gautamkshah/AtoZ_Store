package com.example.atoz_store.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.atoz_store.CartListner
import com.example.atoz_store.Constants
import com.example.atoz_store.R
import com.example.atoz_store.Utils
import com.example.atoz_store.adapters.AdapterBestseller
import com.example.atoz_store.adapters.AdapterCategory
import com.example.atoz_store.adapters.AdapterProduct
import com.example.atoz_store.databinding.BottomSheetallBinding
import com.example.atoz_store.databinding.FragmentHomeBinding
import com.example.atoz_store.databinding.ItemViewProductBinding
import com.example.atoz_store.models.Bestseller
import com.example.atoz_store.models.Category
import com.example.atoz_store.models.Product
import com.example.atoz_store.roomdb.CartProducts
import com.example.atoz_store.viewModels.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var cartListener: CartListner? = null


    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapterProduct: AdapterProduct
    private lateinit var adapterBestseller: AdapterBestseller
    private val viewModel: UserViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        setStatusBarColor()
        setAllCategories()
        navigatingToSearchFragment()
        onProfileClick()
        fetchBestSeller()


        return binding.root
    }

    fun onSeeAllBestSellerClick(productType: Bestseller) {
        val bottomSheetallBinding =
            BottomSheetallBinding.inflate(LayoutInflater.from(requireContext()))
        val bs = BottomSheetDialog(requireContext())
        bs.setContentView(bottomSheetallBinding.root)


        adapterProduct = AdapterProduct(
            ::onAddButtonClicked, ::onIncreamentButtonClicked, ::onDecreamentButtonClicked
        )
        bottomSheetallBinding.rvProducts.adapter = adapterProduct
        adapterProduct.differ.submitList(productType.productList)
        bs.show()


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
        } else {
            Utils.showToast(requireContext(), "Stock Over")
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

    }


    private fun fetchBestSeller() {
        lifecycleScope.launch {
            viewModel.fetchProductType().collect() {
                adapterBestseller = AdapterBestseller(::onSeeAllBestSellerClick)
                binding.rvBestsellers.adapter = adapterBestseller
                adapterBestseller.differ.submitList(it)
            }
        }
    }

    private fun onProfileClick() {
        binding.ivProfile.setOnClickListener {
            Log.d("HomeFragment", "onProfileClick: ")
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }
    }


    private fun navigatingToSearchFragment() {
        binding.searchCv.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchBlankFragment)
        }
    }

    private fun setAllCategories() {
        val categoryList = ArrayList<Category>()
        for (i in 0 until Constants.allProductsCategoryIcon.size) {
            categoryList.add(
                Category(
                    Constants.allProductsCategory[i], Constants.allProductsCategoryIcon[i]
                )
            )
        }
        binding.rvCategories.adapter = AdapterCategory(categoryList, ::onCategoryIconClicked)

    }

    fun onCategoryIconClicked(category: Category) {
        val bundle = Bundle()
        bundle.putString("category", category.title)
        findNavController().navigate(R.id.action_homeFragment_to_categoryFragment, bundle)
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


    private fun setStatusBarColor() {
        activity?.window?.apply {
            val statusBarColors = ContextCompat.getColor(context, R.color.orange)
            statusBarColor = statusBarColors
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is CartListner) {
            cartListener = context
        } else {
            throw ClassCastException("must implement CartListner")
        }
    }


}