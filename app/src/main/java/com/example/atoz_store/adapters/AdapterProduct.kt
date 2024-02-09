package com.example.atoz_store.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.denzcoskun.imageslider.models.SlideModel
import com.example.atoz_store.FilteringProduct
import com.example.atoz_store.Utils
import com.example.atoz_store.databinding.ItemViewProductBinding
import com.example.atoz_store.models.Product


class AdapterProduct(
    val onAddButtonClicked: (Product, ItemViewProductBinding) -> Unit,
    val onIncreamentButtonClicked: (Product, ItemViewProductBinding) -> Unit,
    val onDecreamentButtonClicked: (Product, ItemViewProductBinding) -> Unit,
) : RecyclerView.Adapter<AdapterProduct.ProductViewHolder>(), Filterable {
    class ProductViewHolder(val binding: ItemViewProductBinding) :
        RecyclerView.ViewHolder(binding.root) {



    }


    val diffutil = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.productRandomId == newItem.productRandomId
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, diffutil)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder(
            ItemViewProductBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = differ.currentList[position] ?: return
        holder.binding.apply {
            val imageList = ArrayList<SlideModel>()
            val productImage = product.productImageUris
            for (i in 0 until productImage?.size!!) {
                imageList.add(SlideModel(product.productImageUris!![i].toString()))
            }
            ivImageSlider.setImageList(imageList)
            tvProductTitle.text = product.productTitle
            tvProductPrice.text = "₹" + product.productPrice
            val quantity = product.productQuantity.toString() + product.productUnit
            tvProductQuantity.text = quantity

            if (product.itemCount!! > 0) {
                tvProductCount.text = product.itemCount.toString()
                tvAdd.visibility = View.GONE
                lLProductCount.visibility = View.VISIBLE
            }

            tvAdd.setOnClickListener {
                onAddButtonClicked(product, this)
            }
            tvIncrementCount.setOnClickListener {
                onIncreamentButtonClicked(product, this)
            }
            tvDecrementCount.setOnClickListener {
                onDecreamentButtonClicked(product, this)
            }
        }
    }


   private val filter: FilteringProduct? = null
    var originalList = ArrayList<Product>()
    override fun getFilter(): Filter {
        if (filter == null){
          //  Log.d("filter", "getFilter: ")
            return FilteringProduct(this, originalList)
        }
        return filter
    }


}
