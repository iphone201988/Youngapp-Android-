package com.tech.young.ui.auth.forget_flow

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.databinding.FragmentPasswordChangedBinding
import com.tech.young.ui.auth.AuthCommonVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PasswordChangedFragment : BaseFragment<FragmentPasswordChangedBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    override fun onCreateView(view: View) {
        initView()
        initOnClick()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_password_changed
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity(), Observer {
            when (it?.id) {
                R.id.tvDone -> {
                    val form = arguments?.getString("Form")
                    if (form != null && form == "AddPayment") {
                        val qrCodeUrl = arguments?.getString("qrCodeUrl")
                        val secret = arguments?.getString("secret")
                        val bundle = Bundle()
                        bundle.putString("qrCodeUrl", qrCodeUrl)
                        bundle.putString("secret", secret)
                        findNavController().navigate(R.id.navigateToTwoFactorQrFragment, bundle)
                    } else {

                        findNavController().navigate(
                            R.id.navigateToLoginFragment, null, NavOptions.Builder().setPopUpTo(
                                R.id.fragmentPasswordChanged, true
                            ) // Replace with your actual OTP fragment ID
                                .build()
                        )
                    }
                }

            }
        })

    }

    private fun initView() {
        val form = arguments?.getString("Form")
        if (form != null && form == "AddPayment") {
            binding.tvPassChanged.text = getString(R.string.account_created_successfully)
            binding.tvDes.text = getString(R.string.dis_created_successfully)
            binding.tvDone.text = getString(R.string.done)

        }


    }

}