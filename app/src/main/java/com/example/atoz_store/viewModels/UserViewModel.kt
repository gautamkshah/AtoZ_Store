package com.example.atoz_store.viewModels

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.atoz_store.Constants
import com.example.atoz_store.Utils
import com.example.atoz_store.api.ApiUtilities
import com.example.atoz_store.models.Bestseller
import com.example.atoz_store.models.Notification
import com.example.atoz_store.models.NotificationData
import com.example.atoz_store.models.Orders
import com.example.atoz_store.models.Product
import com.example.atoz_store.roomdb.CartProducts
import com.example.atoz_store.roomdb.CartProductsDatabase
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserViewModel(application: Application) : AndroidViewModel(application) {
    // initialization
    val sharedPreferences = application.getSharedPreferences("My_pref", MODE_PRIVATE)
    val cartProductDao = CartProductsDatabase.getDatabaseInstance(application).CartProductsDao()

    val _paymentStatus = MutableStateFlow<Boolean>(false)
    val paymentStatus = _paymentStatus

    //RoomDb
    suspend fun insertCartProduct(products: CartProducts) {
        cartProductDao.insertCartProduct(products)
    }

    fun getAll(): LiveData<List<CartProducts>> {
        return cartProductDao.getAllCartProduct()
    }

    suspend fun updateCartProduct(products: CartProducts) {
        cartProductDao.updateCartproduct(products)
    }

    suspend fun deleteCartProduct(productId: String) {
        cartProductDao.deleteCartProduct(productId)
    }


    //firebase

    fun saveUserAddress(address: String) {
        Firebase.database.getReference("AllUsers").child("Users").child(Utils.getCurrentUserId())
            .child("userAddress").setValue(address)


    }

    fun fetchAllTheProduct(): Flow<List<Product>> = callbackFlow {
        val db = FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts")

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<Product>()
                for (product in snapshot.children) {
                    val prod = product.getValue(Product::class.java)
                    products.add(prod!!)

                }
                trySend(products)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        db.addValueEventListener(eventListener)
        awaitClose {
            db.removeEventListener(eventListener)
        }
    }

    fun getCategoryProduct(category: String): Flow<List<Product>> = callbackFlow {

        val db = FirebaseDatabase.getInstance().getReference("Admins")
            .child("ProductCategory/${category}")
        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<Product>()
                for (product in snapshot.children) {
                    Log.d("catttegoy", "onDataChange: ${product.value}")
                    val prod = product.getValue(Product::class.java)
                    products.add(prod!!)
                }
                trySend(products)
            }


            override fun onCancelled(error: DatabaseError) {

            }

        }
        db.addValueEventListener(eventListener)
        awaitClose {
            db.removeEventListener(eventListener)
        }


    }

    suspend fun deleteCartProducts() {
        cartProductDao.deleteCartProducts()
    }


    fun updateItemCount(product: Product, itemCount: Int) {
        FirebaseDatabase.getInstance().getReference("Admins")
            .child("AllProducts/${product.productRandomId}").child("itemCount").setValue(itemCount)
        FirebaseDatabase.getInstance().getReference("Admins")
            .child("ProductCategory/${product.productCategory}/${product.productRandomId}")
            .child("itemCount").setValue(itemCount)
        FirebaseDatabase.getInstance().getReference("Admins")
            .child("ProductType/${product.productType}/${product.productRandomId}")
            .child("itemCount").setValue(itemCount)

    }

    fun saveProductsAfterorder(stock: Int, product: CartProducts) {
        FirebaseDatabase.getInstance().getReference("Admins")
            .child("AllProducts/${product.productId}").child("itemCount").setValue(0)
        FirebaseDatabase.getInstance().getReference("Admins")
            .child("ProductCategory/${product.productCategory}/${product.productId}")
            .child("itemCount").setValue(0)
        FirebaseDatabase.getInstance().getReference("Admins")
            .child("ProductType/${product.productType}/${product.productId}").child("itemCount")
            .setValue(0)


        FirebaseDatabase.getInstance().getReference("Admins")
            .child("AllProducts/${product.productId}").child("productStock").setValue(stock)
        FirebaseDatabase.getInstance().getReference("Admins")
            .child("ProductCategory/${product.productCategory}/${product.productId}")
            .child("productStock").setValue(stock)
        FirebaseDatabase.getInstance().getReference("Admins")
            .child("ProductType/${product.productType}/${product.productId}").child("productStock")
            .setValue(stock)


    }

    //shared preference
    fun savingCartItemCount(itemCount: Int) {
        sharedPreferences.edit().putInt("itemCount", itemCount).apply()

    }

    fun fetchTotalItemCart(): MutableLiveData<Int> {
        val totalItemCount = MutableLiveData<Int>()
        totalItemCount.value = sharedPreferences.getInt("itemCount", 0)
        return totalItemCount
    }

    fun saveAddressStatus() {
        sharedPreferences.edit().putBoolean("addressStatus", true).apply()

    }
    fun saveAddress(address: String){
        Firebase.database.getReference("AllUsers").child("Users")
            .child(Utils.getCurrentUserId()).child("userAddress").setValue(address)

    }
    fun logout(){
        FirebaseAuth.getInstance().signOut()
    }



    fun getAddressStatus(): MutableLiveData<Boolean> {
        val status = MutableLiveData<Boolean>()
        status.value = sharedPreferences.getBoolean("addressStatus", false)
        return status
    }


    suspend fun checkPayment(headers: Map<String, String>) {
        val res = ApiUtilities.statusApi.checkStatus(
            headers,
            Constants.MERCHANTID,
            Constants.merchantTransactionId
        )
        if (res.body() != null && res.body()!!.success) {
            _paymentStatus.value = true
        } else {
            _paymentStatus.value = false
        }

    }

    fun getUserAddress(callback: (String?) -> Unit) {
        val db = Firebase.database.getReference("AllUsers").child("Users")
            .child(Utils.getCurrentUserId()).child("userAddress")
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val address = snapshot.getValue(String::class.java)
                    callback(address)
                } else {

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun saveorderProduct(orders: Orders) {

        FirebaseDatabase.getInstance().getReference("Admins").child("Orders")
            .child(orders.orderId!!).setValue(orders)

    }


    fun getAllOrders(): Flow<List<Orders>> = callbackFlow {
        val db = FirebaseDatabase.getInstance().getReference("Admins").child("Orders")
            .orderByChild("orderStatus")
        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orderList = ArrayList<Orders>()
                for (orders in snapshot.children) {
                    val order = orders.getValue(Orders::class.java)
                    if (order?.orderingUserId == Utils.getCurrentUserId()) {
                        orderList.add(order)
                    }
                }
                // Utils.showToast(getApplication(),"No  Found${orderList.size}dn")
                trySend(orderList)
            }

            override fun onCancelled(error: DatabaseError) {


            }

        }
        db.addValueEventListener(eventListener)
        awaitClose() {
            db.removeEventListener(eventListener)
        }


    }




    fun fetchProductType() : Flow<List<Bestseller>> = callbackFlow {
        val db = FirebaseDatabase.getInstance().getReference("Admins").child("ProductType")
        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productTypeList = ArrayList<Bestseller>()
                for (productType in snapshot.children) {
                    val productTypeName=productType.key
                    val productList = ArrayList<Product>()
                    for (product in productType.children) {
                        val prod = product.getValue(Product::class.java)
                        productList.add(prod!!)
                    }
                    val bestseller= Bestseller(productType=productTypeName, productList = productList)
                    productTypeList.add(bestseller)

                }
                trySend(productTypeList)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        db.addValueEventListener(eventListener)
        awaitClose {
            db.removeEventListener(eventListener)
        }
    }

    fun getOrderedProducts(orderId: String): Flow<List<CartProducts>> = callbackFlow {
        val db = Firebase.database.getReference("Admins").child("Orders").child(orderId)
        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val order = snapshot.getValue(Orders::class.java)
                trySend(order?.orderList!!)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        }
        db.addValueEventListener(eventListener)
        awaitClose {
            db.removeEventListener(eventListener)
        }
    }

    suspend fun sendNotification(adminUid: String, title: String, message: String) {
        val gettoken =
            FirebaseDatabase.getInstance().getReference("Admins").child("AdminInfo").child(adminUid)
                .child("adminToken").get()
        Log.d("iam", "sendNotification: ${gettoken.toString()}")
        gettoken.addOnCompleteListener {
            val token = it.result.getValue(String::class.java)
            val notification = Notification(token, NotificationData(title, message))

            ApiUtilities.notificationApi.onSendNotification(notification).enqueue(object : Callback<Notification>{
                override fun onResponse(
                    call: Call<Notification>,
                    response: Response<Notification>,
                ) {
                    if (response.isSuccessful) {
                        Log.d("GG", "onResponse:")

                    }else{
                        Log.d("GGG", "nopResponse: ")
                    }
                }

                override fun onFailure(call: Call<Notification>, t: Throwable) {
                }

            })


        }


    }
}