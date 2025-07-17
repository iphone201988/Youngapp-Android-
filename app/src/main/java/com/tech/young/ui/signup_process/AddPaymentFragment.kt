package com.tech.young.ui.signup_process

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.databinding.FragmentAddPaymentBinding
import com.tech.young.ui.auth.AuthCommonVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddPaymentFragment : BaseFragment<FragmentAddPaymentBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    override fun onCreateView(view: View) {
        initView()
        initOnClick()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_add_payment
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity(), Observer {
            when (it?.id) {
                R.id.ivBack -> {
                    findNavController().popBackStack()
                }

                R.id.tvAdd -> {
                    val bundle = Bundle()
                    bundle.putString("Form", "AddPayment")
                    findNavController().navigate(R.id.navigateToPasswordChangedFragment, bundle)
                }

                R.id.tvSkip -> {
                    val bundle = Bundle()
                    bundle.putString("Form", "AddPayment")
                    findNavController().navigate(R.id.navigateToPasswordChangedFragment, bundle)
                }

            }
        })

    }

    private fun initView() {

    }
}
