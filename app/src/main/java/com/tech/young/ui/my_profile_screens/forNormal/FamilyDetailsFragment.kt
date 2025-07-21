package com.tech.young.ui.my_profile_screens.forNormal

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
import com.tech.young.data.model.DummyLists.homeOwnershipStatusList
import com.tech.young.data.model.GetProfileApiResponse.GetProfileApiResponseData
import com.tech.young.data.model.UpdateUserProfileResponse
import com.tech.young.databinding.BotttomSheetTopicsBinding
import com.tech.young.databinding.FragmentFamilyDetailsBinding
import com.tech.young.databinding.ItemLayoutDropDownBinding
import com.tech.young.ui.my_profile_screens.YourProfileVM
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@AndroidEntryPoint
class FamilyDetailsFragment : BaseFragment<FragmentFamilyDetailsBinding>(),
    BaseCustomBottomSheet.Listener {
    private val viewModel: YourProfileVM by viewModels()

    // data
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
        return R.layout.fragment_family_details
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /** handle view **/
    private fun initView() {
        profileData = arguments?.getParcelable("profileData")
        if (profileData!=null){
            binding.bean=profileData
        }
        initBottomSheet()
        initAdapter()

    }

    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity()) {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }

                R.id.edtChildren -> {
                    type="1"
                    commonAdapter.list=childrenList()
                    commonBottomSheet.show()
                }

                R.id.etEducation -> {
                    type="2"
                    commonAdapter.list=getEduLevel()
                    commonBottomSheet.show()
                }

                R.id.etResidence -> {
                    type="3"
                    commonAdapter.list=homeOwnershipStatusList()
                    commonBottomSheet.show()
                }

                R.id.tvSaveFamily -> {
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
                                    binding.edtChildren.setText(m.title)
                                }
                                "2"->{
                                    binding.etEducation.setText(m.title)
                                }
                                "3"->{
                                    binding.etResidence.setText(m.title)
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
        data["children"] = binding.edtChildren.text.toString().toRequestBody()
        data["educationLevel"] = binding.etEducation.text.toString().toRequestBody()
        data["residenceStatus"] = binding.etResidence.text.toString().toRequestBody()
        viewModel.updateProfile(Constants.UPDATE_USER, data,null,null)
    }
}