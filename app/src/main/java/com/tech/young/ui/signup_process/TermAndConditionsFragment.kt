package com.tech.young.ui.signup_process

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showErrorToast
import com.tech.young.base.utils.showSuccessToast
import com.tech.young.data.api.Constants
import com.tech.young.data.model.RegistrationCompleted
import com.tech.young.databinding.FragmentTermAndConditionsBinding
import com.tech.young.ui.auth.AuthCommonVM
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@AndroidEntryPoint
class TermAndConditionsFragment : BaseFragment<FragmentTermAndConditionsBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    override fun onCreateView(view: View) {
        initView()
        initOnClick()
        initObserver()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_term_and_conditions
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

                R.id.tvNext -> {
                    findNavController().navigate(R.id.navigateToAddPaymentFragment)

                }


            }
        })

    }

    private fun initView() {
        val fullText = "I agree with the terms and conditions"
        val spannable = SpannableString(fullText)

        val start = fullText.indexOf("terms")
        val end = fullText.length

        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireActivity(), R.color.colorPrimary)),
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvTerms.text = spannable
    }

    private fun initObserver() {

    }
}
