package com.tech.young.ui.change_password

import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showErrorToast
import com.tech.young.base.utils.showToast
import com.tech.young.data.api.Constants
import com.tech.young.data.api.SimpleApiResponse
import com.tech.young.databinding.FragmentChangePasswordBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangePasswordFragment : BaseFragment<FragmentChangePasswordBinding>() {

    private val viewModel : ChangePasswordFragmentVm by viewModels()


    override fun getLayoutResource(): Int {
        return R.layout.fragment_change_password
    }

    override fun getViewModel(): BaseViewModel {
           return viewModel
    }

    override fun onCreateView(view: View) {
        initOnClick()
        initObserver()
    }

    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer{
            when(it?.status){
                Status.LOADING -> {
                    showLoading()
                }
                Status.SUCCESS ->  {
                    hideLoading()
                    val myDataModel : SimpleApiResponse ? = BindingUtils.parseJson(it.data.toString())
                    if (myDataModel != null){
                        showToast(myDataModel.message.toString())
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
                Status.ERROR ->  {
                    hideLoading()
                    showToast(it.message.toString())
                }
                else ->  {

                }
            }
        })
    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer{
            when(it?.id){
                R.id.tvUpdatePassword ->{
                    if (isEmptyField()){
                        val data = HashMap<String, Any>()
                        data["currentPassword"] = binding.etOldPassword.text.toString().trim()
                        data["newPassword"] = binding.etNewPassword.text.toString().trim()
                        viewModel.updatePassword(data, Constants.RESET_PASSWORD)
                    }
                }
                R.id.ivPassword ->{
                    showOldPassword()
                }
                R.id.ivNewPassword ->{
                    showNewPassword()
                }
                R.id.ivConfirmPassword ->{
                    showConfirmPassword()
                }
            }
        })
    }

    private fun showOldPassword() {
        if (binding.etOldPassword.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            binding.ivPassword.setImageResource(R.drawable.iv_show_eye)
            binding.etOldPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            binding.ivPassword.setImageResource(R.drawable.pass_invisible)
            binding.etOldPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        binding.etOldPassword.setSelection(binding.etOldPassword.length())
    }


    private fun showNewPassword() {
        if (binding.etNewPassword.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            binding.ivNewPassword.setImageResource(R.drawable.iv_show_eye)
            binding.etNewPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            binding.ivNewPassword.setImageResource(R.drawable.pass_invisible)
            binding.etNewPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        binding.etNewPassword.setSelection(binding.etNewPassword.length())
    }


    private fun showConfirmPassword() {
        if (binding.etConfirmPassword.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            binding.ivConfirmPassword.setImageResource(R.drawable.iv_show_eye)
            binding.etConfirmPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            binding.ivConfirmPassword.setImageResource(R.drawable.pass_invisible)
            binding.etConfirmPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        binding.etConfirmPassword.setSelection(binding.etConfirmPassword.length())
    }
    private fun isEmptyField() : Boolean {

        val oldPassword = binding.etOldPassword.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        val passwordPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#\$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#\$%^&*(),.?\":{}|<>]{12,}\$")

        return when {

            oldPassword.isBlank() ->{
                showErrorToast("Enter old password")
                false
            }
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
}