package com.tech.young.ui.auth.login_flow

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.BindingUtils.setNavigationBarStyle
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showErrorToast
import com.tech.young.base.utils.showSuccessToast
import com.tech.young.data.api.Constants
import com.tech.young.data.model.Login
import com.tech.young.databinding.FragmentLoginBinding
import com.tech.young.ui.auth.AuthCommonVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    var token = ""

    companion object{
        var lat = 0.0
        var long = 0.0

    }

    override fun onCreateView(view: View) {
        initView()
        initOnClick()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_login
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }


    private fun initView() {

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful) {
                return@addOnCompleteListener
            }
            token = it.result
            Log.i("daadd", "initView: $token")
        }

        initObserver()
        BindingUtils.statusBarStyleBlack(requireActivity())
        BindingUtils.styleSystemBars(requireActivity(), requireActivity().getColor(R.color.white))

        setNavigationBarStyle(
            activity = requireActivity(),
            navigationBarColorResId = R.color.white,
            isLightIcons = true
        )
    }

    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity(), Observer {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }

                R.id.tvLoginButton -> {
                    try {
                        val username = binding.edtUsername.text?.toString()?.trim()
                        val password = binding.edtPassword.text?.toString()?.trim()
                        if (!isLoginInputValid()) return@Observer
                        val request = hashMapOf<String, Any>(
                            "email" to username!!,
                            "password" to password!!,
                            "deviceToken" to token,
                            "deviceType" to "2",
                            "latitude" to lat,
                            "longitude" to long
                        )
                        viewModel.login(request, Constants.LOGIN)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        showErrorToast("Something went wrong: ${e.message}")
                    }
                }


                R.id.tvForgetPass -> {
                    findNavController().navigate(R.id.navigateToForgetPasswordFragment)
                }

                R.id.tvRegister -> {
                    findNavController().navigate(R.id.navigateToSignupMainFragment)
                }
            }
        })

    }


    private fun isLoginInputValid(): Boolean {
        return when {
            binding.edtUsername.text.toString().trim().isBlank() -> {
                showErrorToast("Please enter username")
                false
            }

            binding.edtPassword.text.toString().trim().isBlank() -> {
                showErrorToast("Please enter password")
                false
            }

            else -> true
        }
    }


    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            when (it?.status) {
                Status.LOADING -> {
                    showLoading()
                }

                Status.SUCCESS -> {
                    hideLoading()
                    val result: Login? = BindingUtils.parseJson(it.data.toString())
                    showSuccessToast(result?.message.toString())
                   result!!.data?._id?.let { it1 -> sharedPrefManager.saveUserId(it1) }
                    if (result?.data?.is2FAEnabled == true) {
                        val bundle: Bundle = bundleOf("data" to result?.data)
                        findNavController().navigate(R.id.navigateToTowFactorAuthFragment, bundle)
//                        val intent = Intent(requireActivity(), HomeActivity::class.java)
//                        startActivity(intent)

                    } else {
                        findNavController().navigate(R.id.fragmentTwoFactorQr)
//                        val bundle: Bundle = bundleOf("data" to result?.data)
//                        findNavController().navigate(R.id.navigateToTowFactorAuthFragment, bundle)
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