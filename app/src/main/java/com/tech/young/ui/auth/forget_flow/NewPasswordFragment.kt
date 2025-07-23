package com.tech.young.ui.auth.forget_flow

import android.text.InputType
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
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showErrorToast
import com.tech.young.base.utils.showToast
import com.tech.young.data.api.Constants
import com.tech.young.data.api.SimpleApiResponse
import com.tech.young.databinding.FragmentNewPasswordBinding
import com.tech.young.ui.auth.AuthCommonVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewPasswordFragment : BaseFragment<FragmentNewPasswordBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    override fun onCreateView(view: View) {
        initView()
        initOnClick()
        setObsever()
    }

    private fun setObsever() {
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            when(it?.status){
                Status.LOADING ->{
                    showLoading()
                }
                Status.SUCCESS ->{
                    hideLoading()
                    val myDataModel : SimpleApiResponse ? = BindingUtils.parseJson(it.data.toString())
                    if (myDataModel != null){
                        findNavController().navigate(R.id.navigateToPasswordChangedFragment)
                    }
                }
                Status.ERROR ->{
                    hideLoading()
                    showToast(it.message.toString())
                }
                else ->{

                }

            }
        })
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_new_password
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }


    private fun initView() {
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

                R.id.ivPassword ->{
                    signUpShowHidePassword()
                }
                R.id.ivConfirmPassword ->{
                    signUpShowHideConfirmPassword()
                }
                R.id.tvLoginButton -> {
                    val userId = arguments?.getString("userId")
                    if (isEmptyField()){
                        val data =  HashMap<String,Any>()
                        data["userId"] = userId.toString()
                        data["password"] = binding.edtNewPassword.text.toString().trim()
                        viewModel.changePassword(data,Constants.CHANGE_PASSWORD)
                    }
                }

            }
        })

    }

    private fun isEmptyField() : Boolean {

        val newPassword = binding.edtNewPassword.text.toString().trim()
        val confirmPassword = binding.edtConfirmPassword.text.toString().trim()

        val passwordPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#\$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#\$%^&*(),.?\":{}|<>]{12,}\$")

        return when {
            newPassword.isBlank() -> {
                showErrorToast("Enter new password")
                false
            }

            confirmPassword.isBlank() -> {
                showErrorToast("Enter confirm password")
                false
            }

            !passwordPattern.matches(newPassword) -> {
                showErrorToast("Password requirement should be a minimum of 12 characters, 1 uppercase, 1 lowercase, and 1 special character.")
                false
            }

            newPassword != confirmPassword -> {
                showErrorToast("Passwords do not match")
                false
            }

            else -> true
        }
    }


    private fun signUpShowHidePassword() {
        if (binding.edtNewPassword.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            binding.ivPassword.setImageResource(R.drawable.iv_show_eye)
            binding.edtNewPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            binding.ivPassword.setImageResource(R.drawable.pass_invisible)
            binding.edtNewPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        binding.edtNewPassword.setSelection(binding.edtNewPassword.length())
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
}