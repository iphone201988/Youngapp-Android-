package com.tech.young.ui.vault_screen

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
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
import com.tech.young.data.model.GetUserApiResponse
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.BottomSheetCategoryBinding
import com.tech.young.databinding.BotttomSheetTopicsBinding
import com.tech.young.databinding.FragmentCommonVaultBinding
import com.tech.young.databinding.ItemLayoutDropDownBinding
import com.tech.young.databinding.ItemLayoutRvCategoryBinding
import com.tech.young.ui.common.CommonActivity
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.util.FileUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tech.young.data.model.GetAdsAPiResponse
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@AndroidEntryPoint
class CommonVaultFragment : BaseFragment<FragmentCommonVaultBinding>()  , BaseCustomBottomSheet.Listener{
    private val viewModel: VaultVM by viewModels()

    private var imageUri : Uri ?= null

    // adapter
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<GetAdsAPiResponse.Data.Ad, AdsItemViewBinding>
    private lateinit var topicBottomSheet : BaseCustomBottomSheet<BotttomSheetTopicsBinding>
    private lateinit var categoryBottomSheet : BaseCustomBottomSheet<BottomSheetCategoryBinding>

    private lateinit var topicAdapter : SimpleRecyclerViewAdapter<DropDownData, ItemLayoutDropDownBinding>
    private lateinit var categoryAdapter : SimpleRecyclerViewAdapter<DropDownData, ItemLayoutDropDownBinding>
    private lateinit var selectedCategoryAdapter : SimpleRecyclerViewAdapter<DropDownData, ItemLayoutRvCategoryBinding>
    private var topicList = ArrayList<DropDownData>()
    private var categoryList = ArrayList<DropDownData>()
    private var selectedCategory = ArrayList<DropDownData>()
    private var selectedPlayer = ArrayList<String>()

    private var visibilityMode : String = "public"


    companion object{
        var selectedUserId: String = ""
    }

    override fun onCreateView(view: View) {
        // view
        initView()
        // click
        initOnClick()
        // observer
        initObserver()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_common_vault
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /** handle view **/
    private fun initView() {
        initBottomsheet()
        viewModel.getAds(Constants.GET_ADS)
        getTopicsList()
        selectedCateGoryList()
        getCategoryList()
        initAdapter()

        binding.setDefault.setOnCheckedChangeListener { _, isChecked ->
             visibilityMode = if (isChecked) "public" else "private"
            Log.d("SwitchValue", "Visibility mode: $visibilityMode")

            // Optionally store it somewhere
            // myViewModel.visibilityMode.value = visibilityMode
        }

        binding.etDescription.setOnTouchListener { v, event ->
            val parent = v.parent ?: return@setOnTouchListener false  // Safely accessing the parent
            parent.requestDisallowInterceptTouchEvent(true)
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_UP -> parent.requestDisallowInterceptTouchEvent(false)
            }
            false
        }
    }

    private fun selectedCateGoryList() {
        selectedCategory.add(DropDownData("Members","general_member"))
    }


    private fun getCategoryList() {
        categoryList.add(DropDownData("Members", "general_member"))
        categoryList.add(DropDownData("Advisors", "financial_advisor"))
        categoryList.add(DropDownData("Startups", "startup"))
        categoryList.add(DropDownData("Small Businesses", "small_business"))
        categoryList.add(DropDownData("Insurance", "investor"))
        categoryList.add(DropDownData("Firm", "financial_firm"))

    }

    private fun initBottomsheet() {
        topicBottomSheet = BaseCustomBottomSheet(requireContext(),R.layout.botttom_sheet_topics,this)

        topicBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        topicBottomSheet.behavior.isDraggable = true


        categoryBottomSheet = BaseCustomBottomSheet(requireContext(),R.layout.bottom_sheet_category,this)

        categoryBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        categoryBottomSheet.behavior.isDraggable = true
    }

    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity()) {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()


                }
                R.id.ivUploadImage ->{
                    ImagePicker.with(this)
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .createIntent { intent ->
                            startForImageResult.launch(intent)
                        }
                }

                R.id.tvCreate -> {

                    if (isEmptyField()){
                        val selectedActualValues = selectedCategory.joinToString(",") { it.actualValue }
                        Log.i("dsadas", "initOnClick: $selectedActualValues")
                        val multipartImage = imageUri?.let { convertImageToMultipart(it) }

                        val data = HashMap<String, RequestBody>()
                        data["title"] = binding.etTitle.text.toString().trim().toRequestBody()
                        data["topic"] = binding.etTopic.text.toString().trim().toRequestBody()
                        data["description"] = binding.etDescription.text.toString().trim().toRequestBody()
                        data["access"] = visibilityMode.toRequestBody()
                        data["members"] = selectedUserId.toRequestBody()
                        data["category"] = selectedActualValues.toRequestBody()

                        viewModel.createVault(data, Constants.CREATE_VAULT,multipartImage)
                    }


                }
                R.id.etUsers -> {
                    selectedUserId = ""
                    val intent = Intent(requireContext(), CommonActivity::class.java).apply {
                        putExtra("from", "people")
                        putParcelableArrayListExtra("selectedCategory", ArrayList(selectedCategory))
                    }
                    startActivity(intent)
                }

                R.id.etTopic -> {
                    topicBottomSheet.show()
                }
                R.id.etCategory ->{
                    categoryBottomSheet.show()
                }
            }
        }

    }

    private val startForImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        try {
            val resultCode = result.resultCode
            val data = result.data
            if (resultCode == Activity.RESULT_OK) {
                val fileUri = data?.data
                imageUri = fileUri
                binding.ivUploadImage.setImageURI(fileUri)

                //  Log.i("dasd", ": $imageUri")
            } else if (resultCode == ImagePicker.RESULT_ERROR) {

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    /** handle observer **/
    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            when(it?.status){
                Status.LOADING ->{
                    showLoading()
                }
                Status.SUCCESS ->{
                    hideLoading()
                    when(it.message){
                        "createVault"  ->{
                            val myDataModel : GetUserApiResponse? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                showToast(myDataModel.message.toString())
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }
                        }
                        "getAds" ->{
                            val myDataModel : GetAdsAPiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    adsAdapter.list = myDataModel.data?.ads
                                }
                            }
                        }
                    }

                }
                Status.ERROR ->{
                    hideLoading()
                }
                else ->{

                }
            }
        })

    }

    /** handle adapter **/
    private fun initAdapter() {
        adsAdapter = SimpleRecyclerViewAdapter(R.layout.ads_item_view, BR.bean) { v, m, pos ->
            when (v.id) {

            }
        }
        binding.rvAds.adapter = adsAdapter


        topicAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_drop_down,BR.bean){v,m,pos ->
            when(v.id){
                R.id.consMain , R.id.title->{
                    binding.etTopic.setText(m.title)
                    topicBottomSheet.dismiss()
                }
            }
        }
        topicBottomSheet.binding.rvTopics.adapter = topicAdapter
        topicAdapter.list = topicList
        topicAdapter.notifyDataSetChanged()

        categoryAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_drop_down,BR.bean){v,m,pos ->
            when(v.id){
                R.id.consMain , R.id.title->{
                    if (!selectedCategory.contains(m)) {
                        selectedCategory.add(m)
                        selectedCategoryAdapter.list = selectedCategory
                        selectedCategoryAdapter.notifyDataSetChanged()

                        Log.i("dasdasdasd", "initAdapter: $selectedCategory")
                    }
                    categoryBottomSheet.dismiss()
                }
            }
        }
        categoryBottomSheet.binding.rvCategories.adapter = categoryAdapter
        categoryAdapter.list = categoryList
        categoryAdapter.notifyDataSetChanged()


        selectedCategoryAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_rv_category,BR.bean){v,m,pos ->
            when(v.id){
                R.id.ivCross ->{
                    selectedCategory.removeAt(pos)
                    selectedCategoryAdapter.list = selectedCategory
                    selectedCategoryAdapter.notifyDataSetChanged()
                }
            }
        }

        binding.rvSelectedCategory.adapter = selectedCategoryAdapter
        selectedCategoryAdapter.list = selectedCategory
        categoryAdapter.notifyDataSetChanged()
    }

    private var getList = listOf(
        "", "", "", "", ""
    )

    override fun onViewClick(view: View?) {

    }



    private fun getTopicsList() {
        topicList.add(DropDownData("Stocks"))
        topicList.add(DropDownData("Insurance"))
        topicList.add(DropDownData("Retirement"))
        topicList.add(DropDownData("Savings"))
        topicList.add(DropDownData("Investment Management"))
        topicList.add(DropDownData("Child Education"))
        topicList.add(DropDownData("Student Loan Management"))
        topicList.add(DropDownData("Debt Management"))
        topicList.add(DropDownData("Tax Planning"))
        topicList.add(DropDownData("Financial Planning"))
        topicList.add(DropDownData("Wealth Education"))
        topicList.add(DropDownData("Estate Planning"))
        topicList.add(DropDownData("Investor"))
        topicList.add(DropDownData("Venture Capitalist"))
        topicList.add(DropDownData("Small Business"))
        topicList.add(DropDownData("Grants"))
        topicList.add(DropDownData("Loans"))
        topicList.add(DropDownData("Insurance"))
        topicList.add(DropDownData("Annuities"))
    }


    private fun convertImageToMultipart(imageUri: Uri): MultipartBody.Part {
        val file = FileUtil.getTempFile(requireContext(), imageUri)
        return MultipartBody.Part.createFormData(
            "image",
            file!!.name,
            file.asRequestBody("image/png".toMediaTypeOrNull())
        )
    }

    private fun isEmptyField() : Boolean{
        if (TextUtils.isEmpty(binding.etTitle.text.toString().trim())){
            showToast("Please enter title")
            return false
        }
        if (TextUtils.isEmpty(binding.etTopic.text.toString().trim())){
            showToast("Please select topic")
            return false
        }
        if (TextUtils.isEmpty(binding.etDescription.text.toString().trim())){
            showToast("Please enter description")
            return false
        }
        if (selectedUserId.isEmpty()){
            showToast("Please choose users")
            return false
        }

        return true
    }
}