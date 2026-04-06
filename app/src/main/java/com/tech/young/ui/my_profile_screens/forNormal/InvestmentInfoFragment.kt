package com.tech.young.ui.my_profile_screens.forNormal

import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.BaseCustomBottomSheet
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showToast
import com.tech.young.data.DropDownData
import com.tech.young.data.api.Constants
import com.tech.young.data.model.DummyLists.getYearsEmployed
import com.tech.young.data.model.GetProfileApiResponse.GetProfileApiResponseData
import com.tech.young.data.model.UpdateUserProfileResponse
import com.tech.young.databinding.BotttomSheetTopicsBinding
import com.tech.young.databinding.FragmentInvestmentInfoBinding
import com.tech.young.databinding.ItemLayoutDropDownBinding
import com.tech.young.ui.my_profile_screens.YourProfileVM
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@AndroidEntryPoint
class InvestmentInfoFragment : BaseFragment<FragmentInvestmentInfoBinding>()  ,  BaseCustomBottomSheet.Listener{
    private val viewModel:YourProfileVM by viewModels()
    // data
    private var profileData: GetProfileApiResponseData? = null
    private var type: String? = null

    private lateinit var commonBottomSheet: BaseCustomBottomSheet<BotttomSheetTopicsBinding>

    private lateinit var commonAdapter: SimpleRecyclerViewAdapter<DropDownData, ItemLayoutDropDownBinding>


    override fun onCreateView(view: View) {

        ViewCompat.setOnApplyWindowInsetsListener(binding.clInvestment) { view, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            // Only adjust padding if keyboard is visible
            view.setPadding(0, 0, 0, imeHeight)
            insets
        }
        // view
        initView()

        initBottomSheet()
        // click
        initOnClick()


        // observer
        initObserver()

        initAdapter()
    }

    /** handle bottom sheets **/
    private fun initBottomSheet() {
        // years bottom sheet
        commonBottomSheet =
            BaseCustomBottomSheet(requireContext(), R.layout.botttom_sheet_topics, this)
        commonBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        commonBottomSheet.behavior.isDraggable = true
    }
    private fun initAdapter() {
        commonAdapter =
            SimpleRecyclerViewAdapter(R.layout.item_layout_drop_down, BR.bean) { v, m, pos ->
                when (v.id) {
                    R.id.consMain, R.id.title -> {
                        if (type != null) {
                            when (type) {
                                "1" -> binding.etPrimaryInvestmentGoal.setText(m.title)
                                "2" -> binding.etInvestmentHorizon.setText(m.title)
                                "3" -> binding.etDeiImportance.setText(m.title)
                                "4" -> binding.etCommunityReinvestmentImportance.setText(m.title)
                                "5" ->binding.etEnvironmental.setText(m.title)
                                "6" ->binding.etFromInvestment.setText(m.title)
                                "7" -> binding.etEmergencyFund.setText(m.title)

                            }
                        }
                        commonBottomSheet.dismiss()
                    }
                }
            }
        commonBottomSheet.binding.rvTopics.adapter = commonAdapter
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
                R.id.etPrimaryInvestmentGoal ->{
                    type = "1"

                }
                R.id.etInvestmentHorizon ->{
                    type = "2"

                }
                R.id.etDeiImportance ->{
                    type = "3"

                }
                R.id.etCommunityReinvestmentImportance ->{
                    type = "4"

                }
                R.id.etEnvironmental ->{
                    type = "5"

                }
                R.id.etFromInvestment ->{
                    type = "6"

                }
                R.id.etEmergencyFund ->{
                    type = "7"

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

    override fun onViewClick(view: View?) {

    }

}