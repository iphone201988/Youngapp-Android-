package com.tech.young.ui.my_profile_screens.forNormal

import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showToast
import com.tech.young.data.api.Constants
import com.tech.young.data.model.GetProfileApiResponse.GetProfileApiResponseData
import com.tech.young.data.model.UpdateUserProfileResponse
import com.tech.young.databinding.FragmentInvestmentInfoBinding
import com.tech.young.ui.my_profile_screens.YourProfileVM
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@AndroidEntryPoint
class InvestmentInfoFragment : BaseFragment<FragmentInvestmentInfoBinding>() {
    private val viewModel:YourProfileVM by viewModels()
    // data
    private var profileData: GetProfileApiResponseData? = null
    override fun onCreateView(view: View) {

        ViewCompat.setOnApplyWindowInsetsListener(binding.clInvestment) { view, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            // Only adjust padding if keyboard is visible
            view.setPadding(0, 0, 0, imeHeight)
            insets
        }
        // view
        initView()
        // click
        initOnClick()
        // observer
        initObserver()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_investment_info
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /** handle view **/
    private fun initView(){
        profileData = arguments?.getParcelable("profileData")
        if (profileData!=null){
            binding.bean=profileData
        }

    }

    /** handle click **/
    private fun initOnClick(){
        viewModel.onClick.observe(requireActivity()){
            when(it?.id){
                R.id.ivBack -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                R.id.tvSave -> {
                    // handle click
                    apiCall()
                }
            }
        }
    }

    /** handle observer **/
    private fun initObserver() {
        viewModel.observeCommon.observe(requireActivity()) {
            when (it?.status) {
                Status.LOADING -> showLoading()
                Status.SUCCESS -> {
                    hideLoading()
                    when (it.message) {
                        "updateProfile" -> {
                            try {
                                val myDataModel: UpdateUserProfileResponse? =
                                    BindingUtils.parseJson(it.data.toString())
                                if (myDataModel != null) {
                                    if (myDataModel.data != null) {
                                        showToast(myDataModel.message.toString())
                                        requireActivity().onBackPressedDispatcher.onBackPressed()
                                    } else {
                                        showToast(myDataModel.message.toString())
                                    }
                                } else {
                                    showToast(it.message.toString())
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }
                    }
                }

                Status.ERROR -> {
                    hideLoading()
                    try {
                        showToast(it.message.toString())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                else -> {

                }
            }
        }

    }

    private fun apiCall() {
        val data = HashMap<String, RequestBody>()
        data["stockInvestments"] = binding.etStock.text.toString().toRequestBody()
        data["specificStockSymbols"] = binding.etSpecifyStock.text.toString().toRequestBody()
        data["cryptoInvestments"] = binding.etCrypto.text.toString().toRequestBody()
        data["specificCryptoSymbols"] = binding.etSpecifyCrypto.text.toString().toRequestBody()
        data["otherSecurityInvestments"] = binding.etOther.text.toString().toRequestBody()
        data["realEstate"] = binding.etRealEstate.text.toString().toRequestBody()
        data["retirementAccount"] = binding.etRetirement.text.toString().toRequestBody()
        data["savings"] = binding.etSavings.text.toString().toRequestBody()
        data["startups"] = binding.etStartUp.text.toString().toRequestBody()
        viewModel.updateProfile(Constants.UPDATE_USER, data, null,null)
    }

}