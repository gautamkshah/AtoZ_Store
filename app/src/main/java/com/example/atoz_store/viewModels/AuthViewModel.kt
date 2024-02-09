package com.example.atoz_store.viewModels

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.example.atoz_store.Utils
import com.example.atoz_store.models.Users
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.TimeUnit

class AuthViewModel : ViewModel() {


    private val auth = Firebase.auth
    private val _verificationId = MutableStateFlow<String?>(null)
    private val _otpSent = MutableStateFlow<Boolean?>(false)
    val otpSent = _otpSent
    private val _isSignedInSuccessfully = MutableStateFlow<Boolean?>(false)

    val isSignedInSuccessfully = _isSignedInSuccessfully
    private val _isACurrentuser = MutableStateFlow(false)
    val isACurrentUser = _isACurrentuser

    init {
        Utils.getAuthInstance().addAuthStateListener {
            if (auth.currentUser != null) {
                _isACurrentuser.value = true
            } else {
                _isACurrentuser.value = false

            }
        }
    }

    fun sendOTP(userNumber: String, activity: Activity) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

            }

            override fun onVerificationFailed(e: FirebaseException) {

            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                _verificationId.value = verificationId
                _otpSent.value = true


            }
        }

        val options = PhoneAuthOptions.newBuilder(Utils.getAuthInstance())
            .setPhoneNumber("+91$userNumber") // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }

    fun signInWithPhoneAuthCredential(otp: String, userNumber: String, user: Users) {
        val credential = PhoneAuthProvider.getCredential(_verificationId.value.toString(), otp)

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            user.userToken=it.result
            Utils.getAuthInstance().signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    user.uid = Utils.getCurrentUserId()
                    if (task.isSuccessful) {
                        Firebase.database.getReference("AllUsers").child("Users").child(user.uid!!)
                            .setValue(user)
                        _isSignedInSuccessfully.value = true


                    } else {


                    }
                }

        }

    }
}