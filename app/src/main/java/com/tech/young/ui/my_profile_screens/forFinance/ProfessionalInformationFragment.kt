package com.tech.young.ui.my_profile_screens.forFinance

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
import com.tech.young.data.model.DummyLists.childrenList
import com.tech.young.data.model.DummyLists.getEduLevel
import com.tech.young.data.model.DummyLists.getLicenseList
import com.tech.young.data.model.DummyLists.getServicesOfferedList
import com.tech.young.data.model.DummyLists.getYearsInFinancialIndustryList
import com.tech.young.data.model.DummyLists.homeOwnershipStatusList
import com.tech.young.data.model.GetProfileApiResponse.GetProfileApiResponseData
import com.tech.young.data.model.UpdateUserProfileResponse
import com.tech.young.databinding.BotttomSheetTopicsBinding
import com.tech.young.databinding.FragmentProfessionalInformationBinding
import com.tech.young.databinding.ItemLayoutDropDownBinding
import com.tech.young.ui.my_profile_screens.YourProfileVM
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@AndroidEntryPoint
class ProfessionalInformationFragment : BaseFragment<FragmentProfessionalInformationBinding>(),BaseCustomBottomSheet.Listener {

    private val viewModel : YourProfileVM by viewModels()
    private var profileData: GetProfileApiResponseData?=null
    // common sheet & adapter
    private var type:String?=null
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
        return R.layout.fragment_professional_information
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
        initBottomSheet()
        initAdapter()
    }

    /**  handle click **/
    private fun initOnClick(){
        viewModel.onClick.observe(requireActivity()){
            when(it?.id){
                R.id.ivBack->{
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                R.id.etLicence -> {
                    type="1"
                    commonAdapter.list= getLicenseList()
                    commonBottomSheet.show()
                }

                R.id.etYears -> {
                    type="2"
                    commonAdapter.list= getYearsInFinancialIndustryList()
                    commonBottomSheet.show()
                }

                R.id.etServiceProvided -> {
                    type="3"
                    commonAdapter.list= getServicesOfferedList()
                    commonBottomSheet.show()
                }
                R.id.tvSubmit->{
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
                                    }
                                    else{
                                        showToast(myDataModel.message.toString())
                                    }
                                }
                                else{
                                    showToast(it.message.toString())
                                }
                            }
                            catch (e:Exception){
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

    /** handle bottom sheets **/
    private fun initBottomSheet() {
        commonBottomSheet =
            BaseCustomBottomSheet(requireContext(), R.layout.botttom_sheet_topics, this)
        commonBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        commonBottomSheet.behavior.isDraggable = true
    }

    override fun onViewClick(view: View?) {

    }

    /** handle adapter **/
    private fun initAdapter() {
        // children adapter
        commonAdapter =
            SimpleRecyclerViewAdapter(R.layout.item_layout_drop_down, BR.bean) { v, m, pos ->
                when (v.id) {
                    R.id.consMain, R.id.title -> {
                        if (type!=null){
                            when(type){
                                "1"->{
                                    binding.etLicence.setText(m.title)
                                }
                                "2"->{
                                    binding.etYears.setText(m.title)
                                }
                                "3"->{
                                    binding.etServiceProvided.setText(m.title)
                                }
                            }
                        }

                        commonBottomSheet.dismiss()
                    }
                }
            }
        commonBottomSheet.binding.rvTopics.adapter = commonAdapter
    }

    /** api call **/
    private fun apiCall() {
        val data = HashMap<String, RequestBody>()
        data["crdNumber"] = binding.etCrdNumber.text.toString().toRequestBody()
        data["certificates"] = binding.etLicence.text.toString().toRequestBody()
        data["servicesProvided"] = binding.etServiceProvided.text.toString().toRequestBody()
        data["yearsInFinancialIndustry"] = binding.etYears.text.toString().toRequestBody()
        data["occupation"] = binding.etOccupation.text.toString().toRequestBody()
        viewModel.updateProfile(Constants.UPDATE_USER, data,null,null)
    }
}