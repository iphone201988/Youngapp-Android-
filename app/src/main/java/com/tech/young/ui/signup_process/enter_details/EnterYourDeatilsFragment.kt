package com.tech.young.ui.signup_process.enter_details

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.BindingUtils.setNavigationBarStyle
import com.tech.young.base.utils.showToast
import com.tech.young.databinding.FragmentEnterYourDetailsBinding
import com.tech.young.ui.signup_process.SignUpVm
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EnterYourDeatilsFragment : BaseFragment<FragmentEnterYourDetailsBinding>() {
    private val viewModel: SignUpVm by viewModels()
    var type = ""
    var countryCode = "+91"
    override fun onCreateView(view: View) {
        initView()
        initOnClick()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_enter_your_details
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }


    private fun initView() {
        BindingUtils.statusBarStyleBlack(requireActivity())
        BindingUtils.styleSystemBars(requireActivity(), requireActivity().getColor(R.color.white))
        countryCode = binding.countryCodePicker.selectedCountryCode
        binding.countryCodePicker.setOnCountryChangeListener {
            countryCode = binding.countryCodePicker.selectedCountryCode
        }
        setNavigationBarStyle(
            activity = requireActivity(),
            navigationBarColorResId = R.color.white,
            isLightIcons = true
        )

        type = arguments?.getString("accountType").toString()
        if (type == "General Member") {
            binding.edtCompany.visibility = View.GONE
        } else {
            binding.edtCompany.visibility = View.VISIBLE
        }


    }

    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity(), Observer {
            when (it?.id) {
                R.id.ivBack -> {
                    findNavController().popBackStack()
                }

                R.id.tvNext -> {
                    if (isEmptyField()){
                        val bundle = Bundle().apply {
                            putString("role", type)
                            putString("firstName", binding.edtFirstName.text.toString().trim())
                            putString("lastName", binding.edtLastName.text.toString().trim())
                            if (type != "General Member") {
                                putString("company", binding.edtCompany.text.toString().trim())
                            }
                            putString("username", binding.edtUsername.text.toString().trim())
                            putString("email", binding.edtEmail.text.toString().trim())
                            putString("countryCode", countryCode)
                            putString("phone", binding.edtPhone.text.toString().trim())

                        }
                        findNavController().navigate(R.id.navigateToSetupPasswordFragment, bundle)
                    }

                }
            }
        })

    }

    private fun isEmptyField() : Boolean {
        val firstName = binding.edtFirstName.text.toString().trim()
        val lastName = binding.edtLastName.text.toString().trim()


       if (TextUtils.isEmpty(binding.edtFirstName.text.toString().trim())){
           showToast("Please enter first name")
           return false
       }
        if (!firstName[0].isUpperCase()) {
            showToast("First name must start with a capital letter")
            return false
        }


        if (TextUtils.isEmpty(binding.edtLastName.text.toString().trim())){
            showToast("Please enter last name")
            return false
        }

        if (!lastName[0].isUpperCase()) {
            showToast("Last name must start with a capital letter")
            return false
        }

        if (TextUtils.isEmpty(binding.edtEmail.text.toString().trim())){
            showToast("Please enter email")
            return false
        }
        if (TextUtils.isEmpty(binding.edtPhone.text.toString().trim())){
            showToast("Please enter phone number")
            return false
        }
        if (type != "General Member"){
            if (TextUtils.isEmpty(binding.edtCompany.text.toString().trim())){
                showToast("Please enter company name")
                return false
            }
        }
        return true
    }
}