package com.tech.young.ui.signup_process

import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.utils.showErrorToast
import com.tech.young.databinding.FragmentCrdNumderVerificationBinding
import com.tech.young.ui.auth.AuthCommonVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CRDNumberVerificationFragment : BaseFragment<FragmentCrdNumderVerificationBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    override fun onCreateView(view: View) {
        initView()
        initOnClick()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_crd_numder_verification
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity(), Observer {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }

                R.id.tvNext -> {
                    if (binding.edtCRDNumber.text.toString().trim().isNotEmpty()) {
                        findNavController().navigate(R.id.navigateToAddYourPersonalInfoFragment)
                    } else {
                        showErrorToast("Please enter CRD number")
                    }
                }
            }
        })

    }

    private fun initView() {
        binding.edtCRDNumber.doAfterTextChanged {
            RegistrationDataHolder.crdNumber = it.toString()
        }

    }

}