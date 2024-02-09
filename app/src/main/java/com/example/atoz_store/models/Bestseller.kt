package com.example.atoz_store.models

data class Bestseller(
    val id:String?=null,
    val productType: String? = null,
    val productList: ArrayList<Product> ? = null
)
