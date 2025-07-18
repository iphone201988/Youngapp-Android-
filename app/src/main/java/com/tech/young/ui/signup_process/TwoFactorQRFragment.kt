package com.tech.young.ui.signup_process

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
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
import com.tech.young.base.utils.showSuccessToast
import com.tech.young.base.utils.showToast
import com.tech.young.data.api.Constants
import com.tech.young.data.model.Verification2faApiResponse
import com.tech.young.databinding.FragmentTwoFactorQrBinding
import com.tech.young.ui.auth.AuthCommonVM
import com.tech.young.ui.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TwoFactorQRFragment : BaseFragment<FragmentTwoFactorQrBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    override fun onCreateView(view: View) {
        initView()
        initOnClick()
        initObserver()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_two_factor_qr
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

                R.id.tvContinue -> {

                    try {
                        val request = hashMapOf<String, Any>(
                            "userId" to RegistrationDataHolder.userId.toString(),
                            "otp" to binding.edtSecrete.text.toString(),
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
                            val myDataModel : Verification2faApiResponse?= BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    showToast(myDataModel.message.toString())
                                    sharedPrefManager.setLoginData(myDataModel.data!!)
                                val intent = Intent(requireContext() ,HomeActivity::class.java )
                                startActivity(intent)
                                requireActivity().finishAffinity()
                                }
                            }

//                            it.data.let {
//                                showSuccessToast(it?.get("message").toString())
//                                val intent = Intent(requireContext() ,HomeActivity::class.java )
//                                startActivity(intent)
//                                requireActivity().finishAffinity()
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

    private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val pureBase64 = base64Str.substringAfter(",") // remove data:image/png;base64,
            val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun initView() {
        val qrCodeUrl = arguments?.getString("qrCodeUrl")
        val secretCode = arguments?.getString("secret")
        binding.tvSecrete.text = secretCode
        val qrBitmap = decodeBase64ToBitmap(qrCodeUrl.toString())
        qrBitmap?.let {
            binding.ivQRCode.setImageBitmap(it)
        }
    }
}
