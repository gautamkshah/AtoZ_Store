package com.example.atoz_store.auth

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.atoz_store.R
import com.example.atoz_store.activity.UsersMainActivity
import com.example.atoz_store.databinding.FragmentSplashBinding
import com.example.atoz_store.viewModels.AuthViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch


class SplashFragment : Fragment() {
   private val viewModel: AuthViewModel by viewModels()
    private lateinit var binding: FragmentSplashBinding
    private lateinit var auth : FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSplashBinding.inflate(layoutInflater)
        setStatusBarColor()
        auth= Firebase.auth
         Log.d("SplashFragment", "onCreateView: ${auth.currentUser}")
        Handler(Looper.getMainLooper()).postDelayed({
            lifecycleScope.launch {
                viewModel.isACurrentUser.collect{
                    if(it){
                        startActivity(Intent(requireContext(), UsersMainActivity::class.java))
                        requireActivity().finish()
                    }else{
                        findNavController().navigate(R.id.action_splashFragment_to_signInFragment)
                    }
                }
            }

        },3000)
        return binding.root
    }
    private fun setStatusBarColor(){
        activity?.window?.apply{
            val statusBarColors= ContextCompat.getColor(context, R.color.yellow)
            statusBarColor=statusBarColors
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }


}