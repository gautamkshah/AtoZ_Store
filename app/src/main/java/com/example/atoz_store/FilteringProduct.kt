package com.example.atoz_store

import android.util.Log
import android.widget.Filter
import com.example.atoz_store.adapters.AdapterProduct
import com.example.atoz_store.models.Product

import com.google.common.base.Strings.isNullOrEmpty
import java.util.Locale


class FilteringProduct(
    val adapter: AdapterProduct,
    val filter:ArrayList<Product>
) : Filter(){
    override fun performFiltering(constraint: CharSequence?): FilterResults {
        val result =FilterResults()
        if(!constraint.isNullOrEmpty()){
            val filteredList=ArrayList<Product>()
            val query=constraint.toString().trim().uppercase(Locale.getDefault()).split(" ")

            for (products in filter){
                if(query.any{
                        products.productTitle?.uppercase(Locale.getDefault())?.contains(it)==true
                                || products.productCategory?.uppercase(Locale.getDefault())?.contains(it)==true || products.productType?.uppercase(Locale.getDefault())?.contains(it)==true}

                ){
                    filteredList.add(products)
                }else{

                }

            }
            result.values=filteredList
            result.count=filteredList.size

        }else{
            result.values=filter
            result.count=filter.size
        }



        return result
    }

    override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
           adapter.differ.submitList(p1?.values as ArrayList<Product>?)
    }


}