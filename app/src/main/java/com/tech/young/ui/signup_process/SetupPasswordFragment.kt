package com.tech.young.ui.signup_process

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
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
import com.tech.young.base.utils.showToast
import com.tech.young.data.api.Constants
import com.tech.young.data.model.Signup
import com.tech.young.databinding.FragmentSetupPasswordBinding
import com.tech.young.ui.auth.AuthCommonVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SetupPasswordFragment : BaseFragment<FragmentSetupPasswordBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    companion object{
        var lat = 0.0
        var long = 0.0

    }
    var token = ""
    var role: String? = ""
    var firstName: String? = ""
    var lastName: String? = ""
    var company: String? = ""
    var username: String? = ""
    var email: String? = ""
    var countryCode: String? = ""
    var phone: String? = ""
    override fun onCreateView(view: View) {
        initView()
        initOnClick()
        initObserver()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_setup_password
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            when (it?.status) {
                Status.LOADING -> {
                    showLoading()
                }

                Status.SUCCESS -> {
                    hideLoading()
                    val result: Signup? = BindingUtils.parseJson(it.data.toString())
                    sharedPrefManager.saveUserId(result?.data?._id.toString())
                    showSuccessToast(result?.message.toString())
                    val bundle = Bundle().apply {
                        putString("from", "SignUpProcess")
                        putString("userId", result?.data?._id.toString())
                    }
                    findNavController().navigate(R.id.navigateToOtpFragment, bundle)
                }

                Status.ERROR -> {
                    hideLoading()
                    showToast(it.message.toString())
                }

                else -> {}
            }
        })
    }


    private fun initView() {

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful) {
                return@addOnCompleteListener
            }
            token = it.result
            Log.i("daadd", "initView: $token")
        }


        role = arguments?.getString("role")
        when (role) {
            "General Member" -> {
                role = "general_member"
            }

            "Financial Advisor" -> {
                role = "financial_advisor"
            }

            "Financial Firm" -> {
                role = "financial_firm"
            }

            "Small Business" -> {
                role = "small_business"
            }

            "Startup" -> {
                role = "startup"
            }

            "Investor/ VC" -> {
                role = "investor"
            }

        }
        firstName = arguments?.getString("firstName")
        lastName = arguments?.getString("lastName")
        company = arguments?.getString("company")
        username = arguments?.getString("username")
        email = arguments?.getString("email")
        countryCode = arguments?.getString("countryCode")
        phone = arguments?.getString("phone")
        RegistrationDataHolder.role = role

        BindingUtils.statusBarStyleBlack(requireActivity())
        BindingUtils.styleSystemBars(
            requireActivity(), requireActivity().getColor(R.color.white)
        )

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

                R.id.ivPassword ->{
                    signUpShowHidePassword()
                }
                R.id.ivConfirmPassword ->{
                    signUpShowHideConfirmPassword()
                }
                R.id.tvNext -> {
                    try {
                        val oldPassword = binding.edtOldPassword.text?.toString()?.trim()
                        val confirmPassword = binding.edtConfirmPassword.text?.toString()?.trim()
                        if (!isPasswordInputValid()) return@Observer

                        val request = hashMapOf<String, Any>(
                            "firstName" to firstName.toString(),
                            "lastName" to lastName.toString(),
                            "role" to role.toString(),
                            "username" to username.toString(),
                            "email" to email.toString(),
                            "countryCode" to countryCode.toString(),
                            "company" to company.toString(),
                            "phone" to phone.toString(),
                            "password" to confirmPassword.toString(),
                            "deviceToken" to token,
                            "deviceType" to "2"
                        )

                        if (lat != 0.0 && long != 0.0) {
                            request["latitude"] = lat
                            request["longitude"] = long
                        }

                        viewModel.signup(request, Constants.REGISTER)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        showErrorToast("Something went wrong: ${e.message}")
                    }
                }


            }
        })

    }

    private fun signUpShowHidePassword() {
        if (binding.edtOldPassword.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            binding.ivPassword.setImageResource(R.drawable.iv_show_eye)
            binding.edtOldPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            binding.ivPassword.setImageResource(R.drawable.pass_invisible)
            binding.edtOldPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        binding.edtOldPassword.setSelection(binding.edtOldPassword.length())
    }


    private fun signUpShowHideConfirmPassword() {
        if (binding.edtConfirmPassword.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            binding.ivConfirmPassword.setImageResource(R.drawable.iv_show_eye)
            binding.edtConfirmPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            binding.ivConfirmPassword.setImageResource(R.drawable.pass_invisible)
            binding.edtConfirmPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        binding.edtConfirmPassword.setSelection(binding.edtConfirmPassword.length())
    }
    private fun isPasswordInputValid(): Boolean {
        val oldPassword = binding.edtOldPassword.text.toString().trim()
        val confirmPassword = binding.edtConfirmPassword.text.toString().trim()

        val passwordPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#\$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#\$%^&*(),.?\":{}|<>]{12,}\$")
        binding.passwordValidation.visibility = View.GONE

        return when {
            oldPassword.isBlank() -> {
                showErrorToast("Enter password")
                false
            }

            confirmPassword.isBlank() -> {
                showErrorToast("Enter confirm password")
                false
            }

            !passwordPattern.matches(oldPassword) -> {
                binding.passwordValidation.visibility = View.VISIBLE
//                showErrorToast("Password requirement should be a minimum of 12 characters, 1 uppercase, 1 lowercase, and 1 special character.")
                false
            }

            oldPassword != confirmPassword -> {
                showErrorToast("Passwords not matched")
                false
            }

            else -> true
        }
    }


}