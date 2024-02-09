package com.example.atoz_store.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController

import com.example.atoz_store.R
import com.example.atoz_store.Utils
import com.example.atoz_store.activity.UsersMainActivity
import com.example.atoz_store.databinding.FragmentOTPBinding
import com.example.atoz_store.models.Users
import com.example.atoz_store.viewModels.AuthViewModel
import kotlinx.coroutines.launch


class OTPFragment : Fragment() {
   private lateinit var binding: FragmentOTPBinding
   private lateinit var userNumber: String
   private val viewModel: AuthViewModel =AuthViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentOTPBinding.inflate(layoutInflater)
        getUserNumber()
        customEnteringOTP()
        sendOTP()
        onLoginButtonClicked()
        onBackButtonClick()


        return binding.root
    }

    private fun onLoginButtonClicked() {
        binding.btnLogin.setOnClickListener {
            Utils.showDialog(requireContext(),"Verifying OTP...")
            val editTexts= arrayOf(binding.etOtp1,binding.etOtp2,binding.etOtp3,binding.etOtp4,binding.etOtp5,binding.etOtp6)
            val otp=editTexts.joinToString(separator = "") { it.text.toString() }
            if(otp.length<editTexts.size){
                Utils.showToast(requireContext(),"Please enter right OTP")
            }else{
                editTexts.forEach { it.text?.clear(); it.clearFocus() }
                verifyOtp(otp)
            }
        }
    }

    private fun verifyOtp(otp:String) {
        val user= Users(uid=null,userPhoneNumber= userNumber, userAddress = " ")
        viewModel.signInWithPhoneAuthCredential(otp,userNumber,user)

        lifecycleScope.launch {
            viewModel.isSignedInSuccessfully.collect {
                if (it==true){
                    Utils.hideDialog()
                    Utils.showToast(requireContext(),"Login Successfully")
                    startActivity(Intent(requireContext(), UsersMainActivity::class.java))
                    requireActivity().finish()
                   // findNavController().navigate(R.id.action_OTPFragment_to_homeFragment)
                }
            }
        }

    }

    private fun sendOTP() {
        Utils.showDialog(requireContext(),"Sending OTP...")
        viewModel.apply {
            sendOTP(userNumber,requireActivity())
            lifecycleScope.launch{
                otpSent.collect {
                    if (it==true){
                        Utils.hideDialog()
                        Utils.showToast(requireContext(),"OTP Sent")
//                        binding.btnVerifyOtp.setOnClickListener {
//                            val otp=binding.etOtp1.text.toString()+binding.etOtp2.text.toString()+binding.etOtp3.text.toString()+binding.etOtp4.text.toString()+binding.etOtp5.text.toString()+binding.etOtp6.text.toString()
//                            if (otp.length==6){
//                                Utils.showDialog(requireContext(),"Verifying OTP...")
//                                verifyOTP(otp)
//                            }
//                            else{
//                                Utils.showToast(requireContext(),"Please enter valid OTP")
//                            }
//                        }
                    }

                }

            }

        }

    }

    private fun onBackButtonClick() {
        binding.tbOtpFragment.setNavigationOnClickListener{
            findNavController().navigate(R.id.action_OTPFragment_to_signInFragment)
        }
    }

    private fun customEnteringOTP() {
        val editTexts= arrayOf(binding.etOtp1,binding.etOtp2,binding.etOtp3,binding.etOtp4,binding.etOtp5,binding.etOtp6)
        for (i in editTexts.indices){
            editTexts[i].addTextChangedListener(object :android.text.TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }
                override fun afterTextChanged(p0: android.text.Editable?) {
                    if (p0?.length==1){
                        if (i<editTexts.size-1){
                            editTexts[i+1].requestFocus()
                        }
                    }
                    else if (p0?.length==0){
                        if (i>0){
                            editTexts[i-1].requestFocus()
                        }
                    }
                }
            })
        }
    }

    private fun getUserNumber() {
        val bundle=arguments
        userNumber=bundle?.getString("number").toString()
        binding.tvUserNumber.text=userNumber
    }


}