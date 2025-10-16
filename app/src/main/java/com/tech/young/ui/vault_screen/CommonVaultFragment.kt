package com.tech.young.ui.vault_screen

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
import com.tech.young.base.permission.PermissionHandler
import com.tech.young.base.permission.Permissions
import com.tech.young.data.model.GetAdsAPiResponse
import com.tech.young.databinding.BottomSheetCameraGalleryBinding
import com.tech.young.ui.ecosystem.EcosystemFragment
import com.tech.young.ui.exchange.ExchangeFragment
import com.tech.young.ui.my_profile_screens.common_ui.EditProfileDetailFragment
import com.tech.young.ui.vault_screen.people_screen.PeopleFragment
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@AndroidEntryPoint
class CommonVaultFragment : BaseFragment<FragmentCommonVaultBinding>()  , BaseCustomBottomSheet.Listener{
    private val viewModel: VaultVM by viewModels()


    // adapter
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<GetAdsAPiResponse.Data.Ad, AdsItemViewBinding>
    private lateinit var topicBottomSheet : BaseCustomBottomSheet<BotttomSheetTopicsBinding>
    private lateinit var categoryBottomSheet : BaseCustomBottomSheet<BottomSheetCategoryBinding>
    private lateinit var cameraGalleryBottomSheet: BaseCustomBottomSheet<BottomSheetCameraGalleryBinding>

    private lateinit var topicAdapter : SimpleRecyclerViewAdapter<DropDownData, ItemLayoutDropDownBinding>
    private lateinit var categoryAdapter : SimpleRecyclerViewAdapter<DropDownData, ItemLayoutDropDownBinding>
    private lateinit var selectedCategoryAdapter : SimpleRecyclerViewAdapter<DropDownData, ItemLayoutRvCategoryBinding>
    private var topicList = ArrayList<DropDownData>()
    private var categoryList = ArrayList<DropDownData>()
    private var selectedCategory = ArrayList<DropDownData>()
    private var selectedPlayer = ArrayList<String>()

    private var visibilityMode : String = "public"


    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private var selectedImagePart: MultipartBody.Part? = null

    //// camera
    private var photoFile: File? = null
    private var photoURI: Uri? = null
    private var imageUri : Uri? = null


    companion object{
        var selectedUserId: String = ""
        var reload   = false
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
        galleryLauncher()
        viewModel.getAds(Constants.GET_ADS)
        getTopicsList()

        Log.i("reload", "initView: $reload")
        if (!reload){
            selectedCateGoryList()
            reload = false
        }

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


        binding.tabLayoutBottom.tabExchange.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, ExchangeFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.tabLayoutBottom.tabEcosystem.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, EcosystemFragment())
                .addToBackStack(null)
                .commit()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.nestedScrollView) { view, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            // Only adjust padding if keyboard is visible
            view.setPadding(0, 0, 0, imeHeight)
            insets
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


        cameraGalleryBottomSheet =
            BaseCustomBottomSheet(requireContext(), R.layout.bottom_sheet_camera_gallery) {
                when (it.id) {
                    R.id.openCamara, R.id.openCamaraImage -> {
                        openCamera()
                        cameraGalleryBottomSheet.dismiss()
                    }

                    R.id.icon_emoji_new, R.id.tvChooseFromGallery -> {
                        if (!BindingUtils.hasPermissions(
                                requireContext(),
                                BindingUtils.permissions
                            )
                        ) {
                            permissionResultLauncher.launch(BindingUtils.permissions)
                        } else {
                            selectImage()
                        }
                        cameraGalleryBottomSheet.dismiss()
                    }
                }

            }
        cameraGalleryBottomSheet.behavior.isDraggable = true
        cameraGalleryBottomSheet.setCancelable(true)
    }

    private fun galleryLauncher() {
        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    imageUri = data?.data
                    if (imageUri != null){
                        binding.ivUploadImage.setImageURI(imageUri)
                    }

//                    if (imageUri != null) {
//                        selectedImagePart = BindingUtils.createImageMultipartFromUri(
//                            requireContext(),
//                            imageUri!!,
//                            "file" // <-- or any string key like "profile", "photo"
//                        )
//                        Log.i("ImageUpload", "galleryLauncher: $imageUri")
//                    }
                }
            }
    }

    private var allGranted = false
    private val permissionResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            for (it in permissions.entries) {
                it.key
                val isGranted = it.value
                allGranted = isGranted
            }
            when {
                allGranted -> {
                    selectImage()
                }

            }
        }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(Intent.createChooser(intent, "Select Picture"))
    }


    private fun openCamera() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Permissions.check(requireContext(), Manifest.permission.CAMERA, 0, object : PermissionHandler() {
                override fun onGranted() {
                    openCameraIntent()
                }
            })
        } else {
            openCameraIntent()
        }
    }


    private fun openCameraIntent() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = BindingUtils.createImageFile(requireContext())
        val authority = "${requireContext().packageName}.provider"
        val photoURI: Uri = FileProvider.getUriForFile(
            requireContext(), authority, photoFile!!
        )
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        cameraLauncher.launch(cameraIntent)

    }

    private var cameraLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    imageUri = photoFile!!.absoluteFile.toUri()
                    if (imageUri != null){
                        binding.ivUploadImage.setImageURI(imageUri)
                    }
//                    if (photoURI != null) {
//                        selectedImagePart = BindingUtils.createImageMultipartFromUri(
//                            requireContext(),
//                            photoURI!!,
//                            "file" // or appropriate key
//                        )
//                        Log.i("ImageUpload", "cameraLauncher: $photoURI")
//                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }

    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity()) {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()


                }
                R.id.ivUploadImage ->{
//                    ImagePicker.with(this)
//                        .compress(1024)
//                        .maxResultSize(1080, 1080)
//                        .createIntent { intent ->
//                            startForImageResult.launch(intent)
//                        }

                    cameraGalleryBottomSheet.show()
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
//                    val intent = Intent(requireContext(), CommonActivity::class.java).apply {
//                        putExtra("from", "people")
//                        putParcelableArrayListExtra("selectedCategory", ArrayList(selectedCategory))
//                    }
//                    startActivity(intent)

                    val fragment = PeopleFragment().apply {
                        arguments = Bundle().apply {
                            putParcelableArrayList("selectedCategory", ArrayList(selectedCategory))
                        }
                    }


                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, fragment)
                        .addToBackStack(null)
                        .commit()
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

//    private val startForImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
//        try {
//            val resultCode = result.resultCode
//            val data = result.data
//            if (resultCode == Activity.RESULT_OK) {
//                val fileUri = data?.data
//                imageUri = fileUri
//                binding.ivUploadImage.setImageURI(fileUri)
//
//                //  Log.i("dasd", ": $imageUri")
//            } else if (resultCode == ImagePicker.RESULT_ERROR) {
//
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }



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
        topicList.add(DropDownData("Crypto"))
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
        Log.i("dasdasdasdas", "isEmptyField: $selectedUserId")
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