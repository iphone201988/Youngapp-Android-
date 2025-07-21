package com.tech.young.ui.my_profile_screens.common_ui

import android.app.VoiceInteractor.Request
import android.util.Log
import android.view.View
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
import com.tech.young.data.model.DummyLists.getBusinessRevenueList
import com.tech.young.data.model.DummyLists.getFundRaisedList
import com.tech.young.data.model.DummyLists.getFundRaisingList
import com.tech.young.data.model.DummyLists.getStageOfBusiness
import com.tech.young.data.model.GetProfileApiResponse.GetProfileApiResponseData
import com.tech.young.data.model.UpdateUserProfileResponse
import com.tech.young.databinding.BotttomSheetTopicsBinding
import com.tech.young.databinding.FragmentBusinessInfoBinding
import com.tech.young.databinding.ItemLayoutDropDownBinding
import com.tech.young.ui.my_profile_screens.YourProfileVM
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@AndroidEntryPoint
class BusinessInfoFragment : BaseFragment<FragmentBusinessInfoBinding>(),BaseCustomBottomSheet.Listener {
    private val viewModel:YourProfileVM by viewModels()
    // data
    private var profileData: GetProfileApiResponseData? = null

    private var isSelected:Boolean?=false
    // bottom sheet
    private var type: String? = null
    private lateinit var commonBottomSheet: BaseCustomBottomSheet<BotttomSheetTopicsBinding>
    private lateinit var commonAdapter: SimpleRecyclerViewAdapter<DropDownData, ItemLayoutDropDownBinding>
    override fun onCreateView(view: View) {
        // view
        initView()
        // click
        initOnClick()
        // observer
        initObserver()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_business_info
    }

    override fun getViewModel(): BaseViewModel {
      return viewModel
    }

    /** handle view **/
    private fun initView(){
        val role = sharedPrefManager.getLoginData()?.role
        profileData = arguments?.getParcelable("profileData")
        if (profileData!=null){
            binding.bean=profileData
        }
        handleView(role)
        setupYesNoToggleForInvestment()
        initBottomSheet()
        initAdapter()

    }

    /** handle click **/
    private fun initOnClick(){
        viewModel.onClick.observe(requireActivity()){
            when(it?.id){
                R.id.ivBack->{
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                R.id.actionToggleBtn->{
                    // handle click
                }
                R.id.etRevenue->{
                    type="1"
                    commonAdapter.list=getBusinessRevenueList()
                    commonBottomSheet.show()
                }
                R.id.etStock->{
                    type="2"
                    commonAdapter.list= getStageOfBusiness()
                    commonBottomSheet.show()
                }
                R.id.etSpecifyStock->{
                    type="3"
                    commonAdapter.list= getFundRaisedList()
                    commonBottomSheet.show()
                }
                R.id.etCrypto->{
                    type="4"
                    commonAdapter.list= getFundRaisingList()
                    commonBottomSheet.show()
                }
                R.id.etSpecifyCrypto->{
                    type="5"
                    commonAdapter.list=getBusinessRevenueList()
                    commonBottomSheet.show()
                }
                R.id.tvSave,R.id.tvSaveRevenue->{
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

    private fun handleView(role:String?){
        if (role!=null){
            when(role){
                "startup"->{
                    binding.clInvestment.visibility=View.VISIBLE
                }
                else->{
                    binding.clBusiness.visibility=View.VISIBLE
                }
            }
        }
    }

    private fun setupYesNoToggleForInvestment() {
        binding.yesOptionInvestment.label.text = "Yes"
        binding.noOptionInvestment.label.text = "No"
        binding.yesOptionInvestment.box.setOnClickListener {
            isSelected=true
            binding.yesOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_selected)
            binding.noOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_unselected)
        }
        binding.noOptionInvestment.box.setOnClickListener {
            isSelected=false
            binding.noOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_selected)
            binding.yesOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_unselected)
        }
        if (profileData!=null){
            if (profileData?.user?.investors==true){
                isSelected=true
                binding.yesOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_selected)
                binding.noOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_unselected)
            }
            else{
                isSelected=false
                binding.noOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_selected)
                binding.yesOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_unselected)
            }
        }
        else {
            isSelected=false
            binding.noOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_selected)
            binding.yesOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_unselected)
        }
    }

    /** handle bottom sheet **/
    private fun initBottomSheet() {
        // years bottom sheet
        commonBottomSheet =
            BaseCustomBottomSheet(requireContext(), R.layout.botttom_sheet_topics, this)
        commonBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        commonBottomSheet.behavior.isDraggable = true
    }

    override fun onViewClick(view: View?) {

    }

    /** handle adapter **/
    private fun initAdapter() {
        // years adapter
        commonAdapter =
            SimpleRecyclerViewAdapter(R.layout.item_layout_drop_down, BR.bean) { v, m, pos ->
                when (v.id) {
                    R.id.consMain, R.id.title -> {
                        if (type != null) {
                            when (type) {
                                "1" -> binding.etRevenue.text = m.title
                                "2" -> binding.etStock.text = m.title
                                "3" -> binding.etSpecifyStock.text = m.title
                                "4" -> binding.etCrypto.text = m.title
                                "5" -> binding.etSpecifyCrypto.text = m.title
                            }
                        }
                        commonBottomSheet.dismiss()
                    }
                }
            }
        commonBottomSheet.binding.rvTopics.adapter = commonAdapter
    }
    /** api call **/
    private fun apiCall(){
        val data=HashMap<String,RequestBody>()
        val role = sharedPrefManager.getLoginData()?.role
        if (role!=null){
            when(role){
                "startup"->{
                    data["stageOfBusiness"]=binding.etStock.text.toString().toRequestBody()
                    data["fundsRaised"]=binding.etSpecifyStock.text.toString().toRequestBody()
                    data["fundsRaising"]=binding.etCrypto.text.toString().toRequestBody()
                    data["businessRevenue"]=binding.etSpecifyCrypto.text.toString().toRequestBody()
                }
                else->{
                    data["businessRevenue"]=binding.etRevenue.text.toString().toRequestBody()
                    data["investors"]=isSelected.toString().toRequestBody()
                }
            }
        }

        viewModel.updateProfile(Constants.UPDATE_USER,data,null,null)
    }
}