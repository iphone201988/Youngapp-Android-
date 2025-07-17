package com.tech.young.ui.auth.login_flow

import android.content.Intent
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showErrorToast
import com.tech.young.data.api.Constants
import com.tech.young.data.model.Login
import com.tech.young.data.model.Verification2faApiResponse
import com.tech.young.databinding.FragmentTwoFactorAuthBinding
import com.tech.young.ui.auth.AuthCommonVM
import com.tech.young.ui.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TwoFactorAuthFragment : BaseFragment<FragmentTwoFactorAuthBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    override fun onCreateView(view: View) {
        initView()
        initObserver()
        initOnClick()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_two_factor_auth
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

                R.id.tvCancel -> {
                    findNavController().popBackStack()
                }

                R.id.tvLoginButton -> {
                    val loginData = arguments?.getParcelable<Login.Data>("data")
                    try {
                        val request = hashMapOf<String, Any>(
                            "userId" to loginData?._id.toString(),
                            "otp" to binding.edtUsername.text.toString(),
                        )
                        viewModel.verify2FA(request, Constants.VERIFY_2FA)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        showErrorToast("Something went wrong: ${e.message}")
                    }
                }

            }
        })
    }

    private fun initView() {


    }

    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            when (it?.status) {
                Status.LOADING -> {
                    showLoading()
                }

                Status.SUCCESS -> {
                    hideLoading()
                    when (it.message) {
                        "Verify2FA" -> {
                            val myDataModel : Verification2faApiResponse ?= BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    sharedPrefManager.setLoginData(myDataModel.data!!)
                                    val intent = Intent(requireActivity(), HomeActivity::class.java)
                                    startActivity(intent)
                                }
                            }
//                            it.data.let {
//                                showSuccessToast(it?.get("message").toString())
//                                val intent = Intent(requireActivity(), HomeActivity::class.java)
//                                startActivity(intent)
//                            }
                        }
                    }


                }

                Status.ERROR -> {
                    hideLoading()
                    showErrorToast(it.message.toString())
                }

                else -> {}
            }
        })
    }

}