package com.example.atoz_store.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.atoz_store.R
import com.example.atoz_store.Utils
import com.example.atoz_store.activity.AuthMainActivity
import com.example.atoz_store.databinding.AddressBookLayoutBinding
import com.example.atoz_store.databinding.FragmentProfileBinding
import com.example.atoz_store.viewModels.UserViewModel

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private val viewModel: UserViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        onBackButtonClicked()
        onOrdersLayoutClicked()
        funOnAddressLayoutClicked()
        onLogout()


        return binding.root
    }

    private fun onLogout() {
        binding.llLogout.setOnClickListener {
            val alertdialog=AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    viewModel.logout()
                    startActivity(Intent(requireContext(),AuthMainActivity::class.java))
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            alertdialog.show()
        }


    }

    private fun funOnAddressLayoutClicked() {
        binding.llAddress.setOnClickListener {

            val addressBookLayoutBinding =
                AddressBookLayoutBinding.inflate(LayoutInflater.from(requireContext()))

            viewModel.getUserAddress {
                addressBookLayoutBinding.etAddress.setText(it.toString())

            }
            val alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(addressBookLayoutBinding.root)
                .create()
            alertDialog.show()
            addressBookLayoutBinding.btnEdit.setOnClickListener {
                addressBookLayoutBinding.etAddress.isEnabled = true
                addressBookLayoutBinding.btnSave.isEnabled = true
            }
            addressBookLayoutBinding.btnSave.setOnClickListener {
                viewModel.saveAddress(addressBookLayoutBinding.etAddress.text.toString())
                alertDialog.dismiss()
                Utils.showToast(requireContext(), "Address saved successfully")
            }
        }


    }

    private fun onOrdersLayoutClicked() {
        binding.llOrders.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_ordersFragment)
        }

    }

    private fun onBackButtonClicked() {
        binding.tbProfileFragment.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_homeFragment)
        }
    }

}