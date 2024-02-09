package com.example.atoz_store.api


import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Path
import com.example.atoz_store.models.CheckStatusModel
import com.example.atoz_store.models.Notification
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiInterface {
    @GET("apis/pg-sandbox/pg/v1/status/{merchantId}/{transactionId}")
    suspend fun checkStatus(
        @HeaderMap headers: Map<String, String>,
        @Path("merchantId") merchantId: String,
        @Path("transactionId") transactionId: String,
        ): Response<CheckStatusModel>

    @Headers(
        "Content-Type: application/json",
        "Authorization: key=cpx76gJXSeihHaDSsEceBl:APA91bGXVUQO0dzuGioI640FRa5aldN_TPNeJ_7gq-KsGmW1JMt3UVzNmw0WbEjCKYtF3le3NONIBT8yxQDtQvFbIEXDNZCHmtLGIg5pkpQZMo0TVgJw4-DSbvB7sJew5bNHSEqhGder"

    )
    @POST("fcm/send")
    fun onSendNotification(@Body notification: Notification): Call<Notification >


}