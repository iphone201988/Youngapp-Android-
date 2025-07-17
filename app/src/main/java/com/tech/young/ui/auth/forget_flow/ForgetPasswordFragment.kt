package com.tech.young.ui.auth.forget_flow

import android.os.Bundle
import android.provider.SyncStateContract.Constants
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showToast
import com.tech.young.data.model.SavedPostApiResponse
import com.tech.young.data.model.SendOtpApiResponse
import com.tech.young.databinding.FragmentForgetPasswordBinding
import com.tech.young.ui.auth.AuthCommonVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgetPasswordFragment : BaseFragment<FragmentForgetPasswordBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    private var email : String ? = null
    override fun onCreateView(view: View) {
        initView()
        initOnClick()
        setObserver()
    }

    private fun setObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner , Observer {
            when(it?.status){
                Status.LOADING ->{
                    showLoading()
                }
                Status.SUCCESS ->{
                    hideLoading()
                    when(it.message){
                        "sendOtp" ->{
                            val myDataModel : SendOtpApiResponse?= BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    val bundle = Bundle().apply {
                                        putString("userId", myDataModel.data?._id)
                                        putString("screen","forgot")

                                    }
                                    Log.i("sdsadasdad", "setObserver:  $bundle , ${myDataModel.data?._id}")
                                    findNavController().navigate(R.id.navigateToOtpFragment,bundle)
                                }
                            }

                        }
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
        return R.layout.fragment_forget_password
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

                R.id.tvContinue -> {
                    if (isEmptyField()){
                        email = binding.edtUsername.text.toString().trim()
                        val data = HashMap<String,Any>()
                        data["email"] = email.toString()
                        data["type"] =  1
                        viewModel.sendOtp(data,com.tech.young.data.api.Constants.SENT_OTP)
                    }

                }
            }
        })

    }

    private fun initView() {

    }

    private fun isEmptyField() : Boolean {
        if (TextUtils.isEmpty(binding.edtUsername.text.toString().trim())){
            showToast("Please enter email")
            return false
        }
        return true
    }


    }

