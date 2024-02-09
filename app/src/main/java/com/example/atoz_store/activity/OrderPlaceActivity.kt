package com.example.atoz_store.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.atoz_store.CartListner
import com.example.atoz_store.Constants
import com.example.atoz_store.Utils
import com.example.atoz_store.adapters.AdapterCartProducts
import com.example.atoz_store.databinding.ActivityOrderPlaceBinding
import com.example.atoz_store.databinding.AddressLayoutBinding
import com.example.atoz_store.models.Orders
import com.example.atoz_store.models.Users
import com.example.atoz_store.viewModels.UserViewModel
import com.phonepe.intent.sdk.api.B2BPGRequest
import com.phonepe.intent.sdk.api.B2BPGRequestBuilder
import com.phonepe.intent.sdk.api.PhonePe
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.nio.charset.Charset
import java.security.MessageDigest

class OrderPlaceActivity : AppCompatActivity() {
    private val viewModel: UserViewModel by viewModels()
    private var cartListener: CartListner? = null

    private lateinit var adapterCartProducts: AdapterCartProducts
    private lateinit var b2BPGRequest: B2BPGRequest

    private lateinit var binding: ActivityOrderPlaceBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        backToUserMainActivity()

        getAllCartProduct()
       onPlaceOrderClicked()
        initializePhonePay()

    }

    private fun initializePhonePay() {
        PhonePe.init(this, PhonePeEnvironment.UAT, Constants.MERCHANTID, "")
        val data= JSONObject()
        data.put("merchantId", Constants.MERCHANTID)
        data.put("merchantTransactionId", Constants.merchantTransactionId)
        data.put("merchantUserId",System.currentTimeMillis().toString())
        data.put("amount", 200)
        data.put("mobileNumber", 9450645829)
        data.put("callback","https://webhook.site/a492c8df-e206-4629-896a-90b407e75d89")

        val paymentInstrument = JSONObject()
        paymentInstrument.put("type", "PAY_PAGE")

        data.put("paymentInstrument", paymentInstrument)


        val payloadBase64 = android.util.Base64.encodeToString(
            data.toString().toByteArray(Charset.defaultCharset()), android.util.Base64.NO_WRAP
        )

        val checksum = sha256(payloadBase64 + Constants.apiEndPoint + Constants.SALT_KEY) + "###1";



        b2BPGRequest = B2BPGRequestBuilder()
            .setData(payloadBase64)
            .setChecksum(checksum)
            .setUrl(Constants.apiEndPoint)
            .build()


    }

    private fun sha256(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    private fun onPlaceOrderClicked() {
        binding.btnNext.setOnClickListener {
            viewModel.getAddressStatus().observe(this) { status ->
                if (status) {

                    getPaymentView()
                    //payment


                } else {
                    val addressLayoutBinding =
                        AddressLayoutBinding.inflate(LayoutInflater.from(this))
                    val alertDialog = AlertDialog.Builder(this)
                        .setView(addressLayoutBinding.root)
                        .create()
                    alertDialog.show()
                    addressLayoutBinding.btnAdd.setOnClickListener {
                        saveAddress(alertDialog, addressLayoutBinding)
                    }


                }
            }
        }
    }

//    val phonePayView = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//        if (it.resultCode == RESULT_OK) {
//            checkStatus()
//        }
//    }

//    private fun checkStatus() {
//        val xVerify =
//            sha256("/pg/v1/status/${Constants.MERCHANTID}/${Constants.merchantTransactionId}${Constants.SALT_KEY}") + "###1"
//
//        Log.d("phonepe", "onCreate  xverify : $xVerify")
//
//
//        val headers = mapOf(
//            "Content-Type" to "application/json",
//            "X-VERIFY" to xVerify,
//            "X-MERCHANT-ID" to Constants.MERCHANTID,
//        )
//
//        lifecycleScope.launch {
//            viewModel.checkPayment(headers)
//            viewModel.paymentStatus.collect() {
//                if (it) {
//                    Utils.showToast(this@OrderPlaceActivity, "PaymentComplete")
//
//                    saveOrder()
//                    viewModel.deleteCartProducts()
//                    viewModel.savingCartItemCount(0)
//                    cartListener?.hideCartLayout()
//
//
//                    Utils.hideDialog()
//                    startActivity(Intent(this@OrderPlaceActivity, UsersMainActivity::class.java))
//                    finish()
//                } else {
//                    Utils.showToast(this@OrderPlaceActivity, "Payment not done")
//                }
//            }
//        }
//    }


    private fun saveOrder() {

        viewModel.getAll().observe(this) { cartProductList ->
            if (cartProductList.isNotEmpty()) {
                viewModel.getUserAddress {
                    Utils.showToast(this,"geting user address")

                    val orders = Orders(
                        orderId = Utils.getRandomId(),
                        orderList = cartProductList,
                        userAddress = it,
                        orderStatus = 0,
                        orderDate = Utils.getCurrentDate(),
                        orderingUserId = Utils.getCurrentUserId(),

                        )
                    viewModel.saveorderProduct(orders)
                    lifecycleScope.launch {
                        viewModel.sendNotification(cartProductList[0].adminUid!!, "New Order", "New Order has been placed")

                    }

                }
                for (products in cartProductList) {

                    val count = products.productCount

                    val stock = products.productStock?.minus(count!!)
                    if (stock != null) {
                        viewModel.saveProductsAfterorder(stock, products)


                    }
                }

            }



        }
    }

    private fun getPaymentView() {
        try {
            startActivityForResult(PhonePe.getImplicitIntent(
                 this, b2BPGRequest," ")!!,1);
        } catch(e: Exception){
        }


    }

    private fun saveAddress(alertDialog: AlertDialog, addressLayoutBinding: AddressLayoutBinding) {
        Utils.showDialog(this, "processing")
        val userPinCode = addressLayoutBinding.etPin.text.toString()
        val userPhoneNumber = addressLayoutBinding.etPhoneNumber.text.toString()
        val userState = addressLayoutBinding.etState.text.toString()
        val userdistirct = addressLayoutBinding.etDistirct.text.toString()
        val userAddress = addressLayoutBinding.etPin.text.toString()

        val address = "$userPinCode, $userdistirct($userState), $userAddress, $userPhoneNumber"

        val users = Users(
            userAddress = address
        )
        lifecycleScope.launch {
            viewModel.saveUserAddress(address)
            viewModel.saveAddressStatus()
        }
        Utils.showToast(this, "Saved...")
        alertDialog.dismiss()

        getPaymentView()


    }

    private fun backToUserMainActivity() {
        binding.tbOrderFragment.setNavigationOnClickListener {
            startActivity(Intent(this, UsersMainActivity::class.java))
            finish()
        }
    }

    private fun getAllCartProduct() {

        viewModel.getAll().observe(this) { cartProductList ->
            adapterCartProducts = AdapterCartProducts()
            binding.rvProductsItems.adapter = adapterCartProducts
            adapterCartProducts.differ.submitList(cartProductList)

            var totalprice = 0;
            for (products in cartProductList) {
                val price = products.productPrice?.substring(1)?.toInt()
                val itemCount = products.productCount!!
                totalprice += (price?.times(itemCount)!!)
            }
            binding.tvSubTotal.text = totalprice.toString()
            if (totalprice < 100) {
                binding.tvDeliveryCharge.text = "15"
                totalprice += 15
            }
            binding.tvGrandTotal.text = totalprice.toString()

        }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        lifecycleScope.launch {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == 1) {
               // Utils.showToast(this,"check callback")
                saveOrder()
                viewModel.deleteCartProducts()
                viewModel.savingCartItemCount(0)
                cartListener?.hideCartLayout()



                startActivity(Intent(this@OrderPlaceActivity, UsersMainActivity::class.java))
                finish()



            }

        }

    }
}