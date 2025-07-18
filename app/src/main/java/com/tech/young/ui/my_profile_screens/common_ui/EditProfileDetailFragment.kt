package com.tech.young.ui.my_profile_screens.common_ui

import android.app.Activity
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.util.FileUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.BaseCustomBottomSheet
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.BindingUtils.compressImage
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showToast
import com.tech.young.data.DropDownData
import com.tech.young.data.api.Constants
import com.tech.young.data.model.DummyLists.getAgeList
import com.tech.young.data.model.DummyLists.getEduLevel
import com.tech.young.data.model.DummyLists.getGenderList
import com.tech.young.data.model.DummyLists.getIndustries
import com.tech.young.data.model.DummyLists.getMartialStatus
import com.tech.young.data.model.DummyLists.getRaceList
import com.tech.young.data.model.GetProfileApiResponse
import com.tech.young.data.model.GetProfileApiResponse.GetProfileApiResponseData
import com.tech.young.data.model.UpdateUserProfileResponse
import com.tech.young.databinding.BotttomSheetTopicsBinding
import com.tech.young.databinding.FragmentEditProfileDetailBinding
import com.tech.young.databinding.ItemLayoutDropDownBinding
import com.tech.young.ui.my_profile_screens.YourProfileVM
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@AndroidEntryPoint
class EditProfileDetailFragment : BaseFragment<FragmentEditProfileDetailBinding>(),
    BaseCustomBottomSheet.Listener {
    private val viewModel: YourProfileVM by viewModels()
    private var imageUri: Uri? = null
    // data
    private var profileData: GetProfileApiResponseData?=null

    private var profileImageMultipart: MultipartBody.Part? = null
    // race
    private lateinit var raceBottomSheet : BaseCustomBottomSheet<BotttomSheetTopicsBinding>
    private lateinit var raceAdapter : SimpleRecyclerViewAdapter<DropDownData, ItemLayoutDropDownBinding>
    // gender
    private lateinit var genderBottomSheet : BaseCustomBottomSheet<BotttomSheetTopicsBinding>
    private lateinit var genderAdapter : SimpleRecyclerViewAdapter<DropDownData, ItemLayoutDropDownBinding>
    // age
    private lateinit var ageBottomSheet : BaseCustomBottomSheet<BotttomSheetTopicsBinding>
    private lateinit var ageAdapter : SimpleRecyclerViewAdapter<DropDownData, ItemLayoutDropDownBinding>
    // martial/ education/ industry
    private lateinit var commonBottomSheet : BaseCustomBottomSheet<BotttomSheetTopicsBinding>
    private lateinit var commonAdapter : SimpleRecyclerViewAdapter<DropDownData, ItemLayoutDropDownBinding>
    private var type:Int?=null
    override fun onCreateView(view: View) {
        // view
        initView()
        // click
        initOnClick()
        // observer
        initObserver()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_edit_profile_detail
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /** handle view **/
    private fun initView() {
        val role = sharedPrefManager.getLoginData()?.role
        profileData = arguments?.getParcelable("profileData")
        if (profileData!=null){
            binding.bean=profileData
        }
        Log.i("dsadasdas", "initView: $role")
        if (role != null) {
            when (role) {
                "admin" -> {
                    // Handle admin-specific logic
                    Log.d("RoleCheck", "Admin user")
                }

                "general_member" -> {
                    // Handle general member logic
                    Log.d("RoleCheck", "General member")
                    type=1
                    binding.clProfile.visibility = View.VISIBLE
                }

                "financial_advisor" -> {
                    // Advisor-specific logic
                    Log.d("RoleCheck", "Financial advisor")
                    type=2
                    binding.clFinancialProfile.visibility = View.VISIBLE

                }

                "financial_firm" -> {
                    Log.d("RoleCheck", "Financial firm")
                    type=2
                    binding.clFinancialProfile.visibility = View.VISIBLE

                }

                "small_business" -> {
                    type=3
                    Log.d("RoleCheck", "Small business")
                    binding.clStartupProfile.visibility = View.VISIBLE
                }

                "startup" -> {
                    Log.d("RoleCheck", "Startup")
                    type=3
                    binding.clStartupProfile.visibility = View.VISIBLE
                }

                "investor" -> {
                    Log.d("RoleCheck", "Investor")
                    type=4
                    binding.clInvestorProfile.visibility = View.VISIBLE

                }

                else -> {
                    Log.w("RoleCheck", "Unknown role: $role")
                }
            }
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

                R.id.actionToggleBtn -> {
                    // handle click
                }

                R.id.ivImage, R.id.ivAdd -> {
                    startImagePicker()
                }
                /** for finance **/
                R.id.ivImageFinance, R.id.ivAddFinance -> {
                    startImagePicker()
                }

                /** for startup / small business **/
                R.id.ivImageStartup, R.id.ivAddStartup -> {
                    startImagePicker()
                }

                /** for investment / insurance  **/
                R.id.ivImageInvestor, R.id.ivAddInvestor -> {
                    startImagePicker()
                }

                R.id.etRace,R.id.etRaceFinance,R.id.etRaceStartup,R.id.etRaceInvestor->{
                    raceBottomSheet.show()
                }

                R.id.etGender,R.id.etGenderFinance,R.id.etGenderStartup,R.id.etGenderInvestor->{
                    genderBottomSheet.show()
                }
                R.id.etAge,R.id.etAgeFinance,R.id.etAgeStartup,R.id.etAgeInvestor->{
                    ageBottomSheet.show()
                }
                R.id.etMarital,R.id.etEducationFinance,R.id.etIndustryStartup->{
                    commonBottomSheet.show()
                }

                /** api call ***/
                R.id.tvUpdate,R.id.tvUpdateFinance,R.id.tvUpdateStartup,R.id.tvUpdateInvestor -> {
                    apiCall()
                }
            }
        }
    }

    /** handle observer **/
    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            when (it?.status) {
                Status.LOADING -> {
                    showLoading()
                }

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
                    showToast(it.message.toString())
                }

                else -> {}
            }
        })

    }

    /** image picker **/
    private fun startImagePicker() {
        ImagePicker.with(this).crop(1f, 1f).compress(1024).maxResultSize(1080, 1080)
            .createIntent { intent ->
                startForImageResult.launch(intent)
            }
    }

    /**** image launcher  ***/
    private val startForImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            try {
                val resultCode = result.resultCode
                val data = result.data
                if (resultCode == Activity.RESULT_OK) {
                    imageUri = data?.data
                    imageUri?.let {
                        val compressedUri = compressImage(imageUri!!, requireContext())
                        if (type!=null){
                            when(type){
                                1->{
                                    binding.ivImage.setImageURI(compressedUri)
                                }
                                2->{
                                    binding.ivImageFinance.setImageURI(compressedUri)
                                }
                                3->{
                                    binding.ivImageStartup.setImageURI(compressedUri)
                                }
                                4->{
                                    binding.ivImageInvestor.setImageURI(compressedUri)
                                }
                            }
                        }
                        profileImageMultipart = convertImageToMultipart(compressedUri!!)
                        Log.e("multipart", Gson().toJson(profileImageMultipart))
                    }
                } else if (resultCode == ImagePicker.RESULT_ERROR) {
                    Toast.makeText(
                        requireContext(),
                        ImagePicker.getError(data),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    private fun convertImageToMultipart(imageUri: Uri): MultipartBody.Part {
        val file = FileUtil.getTempFile(requireContext(), imageUri)
        return MultipartBody.Part.createFormData(
            "profileImage", file!!.name, file.asRequestBody("image/png".toMediaTypeOrNull())
        )
    }

    /** handle bottom sheets **/
    private fun initBottomSheet() {
        // race bottom sheet
        raceBottomSheet = BaseCustomBottomSheet(requireContext(),R.layout.botttom_sheet_topics,this)
        raceBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        raceBottomSheet.behavior.isDraggable = true

        // gender bottom sheet
        genderBottomSheet = BaseCustomBottomSheet(requireContext(),R.layout.botttom_sheet_topics,this)
        genderBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        genderBottomSheet.behavior.isDraggable = true

        // age bottom sheet
        ageBottomSheet = BaseCustomBottomSheet(requireContext(),R.layout.botttom_sheet_topics,this)
        ageBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        ageBottomSheet.behavior.isDraggable = true

        // common bottom sheet
        commonBottomSheet = BaseCustomBottomSheet(requireContext(),R.layout.botttom_sheet_topics,this)
        commonBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        commonBottomSheet.behavior.isDraggable = true
    }

    override fun onViewClick(view: View?) {

    }

    /** handle adapter **/
    private fun initAdapter() {
        // race adapter
        raceAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_drop_down, BR.bean) { v, m, pos ->
            when (v.id) {
                R.id.consMain, R.id.title -> {
                    if (type != null) {
                        when (type) {
                            1 -> binding.etRace.setText(m.title)
                            2 -> binding.etRaceFinance.setText(m.title)
                            3 -> binding.etRaceStartup.setText(m.title)
                            4 -> binding.etRaceInvestor.setText(m.title)
                        }
                    }
                    raceBottomSheet.dismiss()
                }
            }
        }
        raceBottomSheet.binding.rvTopics.adapter = raceAdapter
        raceAdapter.list = getRaceList()

        // gender adapter
        genderAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_drop_down, BR.bean) { v, m, pos ->
            when (v.id) {
                R.id.consMain, R.id.title -> {
                    if (type != null) {
                        when (type) {
                            1 -> binding.etGender.setText(m.title)
                            2 -> binding.etGenderFinance.setText(m.title)
                            3 -> binding.etGenderStartup.setText(m.title)
                            4 -> binding.etGenderInvestor.setText(m.title)
                        }
                    }
                    genderBottomSheet.dismiss()
                }
            }
        }
        genderBottomSheet.binding.rvTopics.adapter = genderAdapter
        genderAdapter.list = getGenderList()

        // age adapter
        ageAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_drop_down, BR.bean) { v, m, pos ->
            when (v.id) {
                R.id.consMain, R.id.title -> {
                    if (type != null) {
                        when (type) {
                            1 -> binding.etAge.setText(m.title)
                            2 -> binding.etAgeFinance.setText(m.title)
                            3 -> binding.etAgeStartup.setText(m.title)
                            4 -> binding.etAgeInvestor.setText(m.title)
                        }
                    }
                    ageBottomSheet.dismiss()
                }
            }
        }
        ageBottomSheet.binding.rvTopics.adapter = ageAdapter
        ageAdapter.list = getAgeList()

        // common adapter
        commonAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_drop_down, BR.bean) { v, m, pos ->
            when (v.id) {
                R.id.consMain, R.id.title -> {
                    if (type != null) {
                        when (type) {
                            1 -> binding.etMarital.setText(m.title)
                            2 -> binding.etEducationFinance.setText(m.title)
                            3 -> binding.etIndustryStartup.setText(m.title)
                        }
                    }
                    commonBottomSheet.dismiss()
                }
            }
        }
        commonBottomSheet.binding.rvTopics.adapter = commonAdapter
        when(type){
            1-> commonAdapter.list = getMartialStatus()
            2-> commonAdapter.list = getEduLevel()
            3-> commonAdapter.list= getIndustries()

        }
    }

    /*** api call **/
    private fun apiCall(){
        val data=HashMap<String, RequestBody>()
        when(type){
            1->{
                data["firstName"]=binding.etFirstName.text.toString().trim().toRequestBody()
                data["lastName"]=binding.edtLastName.text.toString().trim().toRequestBody()
                data["city"]=binding.edtCity.text.toString().trim().toRequestBody()
                data["state"]=binding.edtState.text.toString().trim().toRequestBody()
                data["race"]=binding.etRace.text.toString().trim().toRequestBody()
                data["gender"]=binding.etGender.text.toString().trim().toRequestBody()
                data["ageRange"]=binding.etAge.text.toString().trim().toRequestBody()
                data["maritalStatus"]=binding.etMarital.text.toString().trim().toRequestBody()
            }
            2->{
                data["firstName"]=binding.etFirstNameFinance.text.toString().trim().toRequestBody()
                data["lastName"]=binding.edtLastNameFinance.text.toString().trim().toRequestBody()
                data["company"]=binding.etCompanyFinance.text.toString().trim().toRequestBody()
                data["website"]=binding.etWebsiteFinance.text.toString().trim().toRequestBody()
                data["city"]=binding.edtCityFinance.text.toString().trim().toRequestBody()
                data["state"]=binding.edtStateFinance.text.toString().trim().toRequestBody()
                data["race"]=binding.etRaceFinance.text.toString().trim().toRequestBody()
                data["gender"]=binding.etGenderFinance.text.toString().trim().toRequestBody()
                data["ageRange"]=binding.etAgeFinance.text.toString().trim().toRequestBody()
                data["educationLevel"]=binding.etEducationFinance.text.toString().trim().toRequestBody()
            }
            3->{
                data["firstName"]=binding.etFirstNameStartup.text.toString().trim().toRequestBody()
                data["lastName"]=binding.edtLastNameStartup.text.toString().trim().toRequestBody()
                data["company"]=binding.etCompanyStartup.text.toString().trim().toRequestBody()
                data["website"]=binding.etWebsiteStartup.text.toString().trim().toRequestBody()
                data["city"]=binding.edtCityStartup.text.toString().trim().toRequestBody()
                data["state"]=binding.edtStateStartup.text.toString().trim().toRequestBody()
                data["race"]=binding.etRaceStartup.text.toString().trim().toRequestBody()
                data["gender"]=binding.etGenderStartup.text.toString().trim().toRequestBody()
                data["ageRange"]=binding.etAgeStartup.text.toString().trim().toRequestBody()
                data["industriesSeeking"]=binding.etIndustryStartup.text.toString().trim().toRequestBody()
                data["launchDate"]=binding.etLaunchDateStartup.text.toString().trim().toRequestBody()

            }
            4->{
                data["firstName"]=binding.etFirstNameInvestor.text.toString().trim().toRequestBody()
                data["lastName"]=binding.edtLastNameInvestor.text.toString().trim().toRequestBody()
                data["company"]=binding.etCompanyInvestor.text.toString().trim().toRequestBody()
                data["website"]=binding.etWebsiteInvestor.text.toString().trim().toRequestBody()
                data["city"]=binding.edtCityInvestor.text.toString().trim().toRequestBody()
                data["state"]=binding.edtStateInvestor.text.toString().trim().toRequestBody()
                data["race"]=binding.etRaceInvestor.text.toString().trim().toRequestBody()
                data["gender"]=binding.etGenderInvestor.text.toString().trim().toRequestBody()
                data["ageRange"]=binding.etAgeInvestor.text.toString().trim().toRequestBody()
                data["yearFounded"]=binding.etYearFoundedInvestor.text.toString().trim().toRequestBody()

            }
        }
        viewModel.updateProfile(Constants.UPDATE_USER, data,profileImageMultipart)
    }
}