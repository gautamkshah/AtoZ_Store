package com.example.atoz_store

interface CartListner {
    fun showcartLayout(itemCount:Int)
    fun savingCartItemCount(itemCount: Int)
    fun hideCartLayout()


}