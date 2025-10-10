package com.tech.young.ui.auth.forget_flow

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
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
import com.tech.young.data.model.VerifyOtpApiResponse
import com.tech.young.databinding.FragmentOtpBinding
import com.tech.young.ui.auth.AuthCommonVM
import com.tech.young.ui.signup_process.RegistrationDataHolder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OtpFragment : BaseFragment<FragmentOtpBinding>() {

    private var email : String ?= null
    private val viewModel: AuthCommonVM by viewModels()
    override fun onCreateView(view: View) {
        initView()
        initOnClick()
        initObserver()
        setupResendTimer()
        setupOtpInputs()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_otp
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

                R.id.tvVerify -> {
                    val userId = arguments?.getString("userId")
                    val screen = arguments?.getString("screen")
                    Log.i("Fsdfdsfds", "initOnClick: $userId , $screen")
                    RegistrationDataHolder.userId = userId.toString()
                    try {
                        if (!isPasswordInputValid()) return@Observer
                        if (screen == "forgot"){
                            val request = hashMapOf<String, Any>(
                                "userId" to userId.toString(),
                                "otp" to binding.edtOtpOne.text.toString() + binding.edtOtpTwo.text.toString() + binding.edtOtpThree.text.toString() + binding.edtOtpFour.text.toString(),
                                "type" to "1",
                            )
                            viewModel.verifyOtp(request, Constants.VERIFY_OTP)
                        }else{
                            val request = hashMapOf<String, Any>(
                                "userId" to userId.toString(),
                                "otp" to binding.edtOtpOne.text.toString() + binding.edtOtpTwo.text.toString() + binding.edtOtpThree.text.toString() + binding.edtOtpFour.text.toString(),
                                "type" to "3",
                            )
                            viewModel.verifyOtp(request, Constants.VERIFY_OTP)
                        }


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
                        "VerifyOtp" -> {
                            val myDataModel : VerifyOtpApiResponse? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    val type = arguments?.getString("from")
                                    if (type == "SignUpProcess") {
                                        findNavController().navigate(R.id.navigateToAddLicenseFragment)
                                    } else {
                                        val bundle = Bundle().apply {
                                            putString("userId", myDataModel.data?._id)
                                        }
                                        findNavController().navigate(R.id.navigateForNewPasswordFragment,bundle)
                                    }
                                }
                            }
//                            it.data.let {
//                                showSuccessToast(it?.get("message").toString())
//                                val type = arguments?.getString("from")
//                                if (type == "SignUpProcess") {
//                                    findNavController().navigate(R.id.navigateToAddLicenseFragment)
//                                } else {
//                                    findNavController().navigate(R.id.navigateForNewPasswordFragment)
//                                }
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

    private fun initView() {
        val type = arguments?.getString("from")
        email = arguments?.getString("email")

        if (type == "SignUpProcess") {
            binding.tvVerify.setText(getString(R.string.next))
        } else {
            binding.tvVerify.setText(getString(R.string.verify))
        }
    }

    private fun setupOtpInputs() {
        val otpFields = listOf(
            binding.edtOtpOne, binding.edtOtpTwo, binding.edtOtpThree, binding.edtOtpFour
        )

        otpFields.forEachIndexed { index, editText ->

            // Move to next on text input
            editText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && index < otpFields.size - 1) {
                        otpFields[index + 1].requestFocus()
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            // Handle backspace to move focus to previous and clear
            editText.setOnKeyListener { v, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (editText.text?.isEmpty() == true && index > 0) {
                        otpFields[index - 1].apply {
                            setText("")
                            requestFocus()
                        }
                    }
                }
                false
            }
        }
    }


    private fun isPasswordInputValid(): Boolean {
        return when {
            binding.edtOtpOne.text.toString().trim().isBlank() -> {
                showErrorToast("Please enter complete OTP")
                false
            }

            binding.edtOtpTwo.text.toString().trim().isBlank() -> {
                showErrorToast("Please enter complete OTP")
                false
            }

            binding.edtOtpThree.text.toString().trim().isBlank() -> {
                showErrorToast("Please enter complete OTP")
                false
            }

            binding.edtOtpFour.text.toString().trim().isBlank() -> {
                showErrorToast("Please enter complete OTP")
                false
            }


            else -> true
        }
    }

    private var countDownTimer: CountDownTimer? = null

    private fun setupResendTimer() {
        binding.tvSeconds.text = "30"
        binding.tvSeconds.isClickable = false
        binding.tvSeconds.setTextColor(
            ContextCompat.getColor(
                requireActivity(), R.color.gray
            )
        ) // Disabled color

        startCountdown()
    }

    private fun startCountdown() {
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.tvSeconds.text = seconds.toString()
            }

            override fun onFinish() {
                binding.tvSeconds.text = "Resend"
                binding.tvSeconds.isClickable = true
                binding.tvSeconds.setTextColor(
                    ContextCompat.getColor(
                        requireActivity(), R.color.colorPrimary
                    )
                ) // Your active color

                // Set click listener only when timer finishes
                binding.tvSeconds.setOnClickListener {

                    val data = HashMap<String,Any>()
                    data["email"] = email.toString()
                    data["type"] =  1
                    viewModel.sendOtp(data,com.tech.young.data.api.Constants.SENT_OTP)

                    // Restart the countdown when clicked
                    binding.tvSeconds.isClickable = false
                    binding.tvSeconds.setTextColor(
                        ContextCompat.getColor(
                            requireActivity(), R.color.gray
                        )
                    )
                    startCountdown()

                    // Add your resend logic here (API call, etc.)
                    // resendVerificationCode()
                }
            }
        }.start()
    }

    // Don't forget to clean up
    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }

}