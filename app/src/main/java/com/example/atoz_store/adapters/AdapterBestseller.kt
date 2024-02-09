package com.example.atoz_store.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.atoz_store.databinding.ItemViewBestsellerBinding
import com.example.atoz_store.models.Bestseller


class AdapterBestseller(val onSeeAllBestSellerClick: (Bestseller) -> Unit) :RecyclerView.Adapter<AdapterBestseller.BestsellerViewHolder>(){
    class BestsellerViewHolder (val binding: ItemViewBestsellerBinding) : RecyclerView.ViewHolder(binding.root)

    val diffUtil=object: DiffUtil.ItemCallback<Bestseller>(){
        override fun areItemsTheSame(oldItem: Bestseller, newItem: Bestseller): Boolean {
            return oldItem.id== newItem.id

        }

        override fun areContentsTheSame(oldItem: Bestseller, newItem: Bestseller): Boolean {
            return oldItem==newItem
        }

    }
    val differ= AsyncListDiffer(this,diffUtil)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BestsellerViewHolder {

        return BestsellerViewHolder(ItemViewBestsellerBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: BestsellerViewHolder, position: Int) {
        val productType=differ.currentList[position]
        holder.binding.apply {
            tvCategory.text=productType.productType
            tvTotalProducts.text=productType.productList?.size.toString()
            val listOfIv= listOf(ivProduct1,ivProduct2,ivProduct3)
            var minSize=minOf(listOfIv.size, productType.productList?.size!!)
            for (i in 0 until minSize){
                Glide.with(holder.itemView).load(productType.productList[i].productImageUris?.get(i)).into(listOfIv[i])
                listOfIv[i].visibility=View.VISIBLE
            }
            if(productType.productList.size>3){
                tvProductCount.visibility= View.VISIBLE
                tvProductCount.text="+${productType.productList.size-3}"
            }

        }
        holder.itemView.setOnClickListener {
            onSeeAllBestSellerClick(productType)
        }

    }

}