package com.tech.young.ui.my_profile_screens.forFinance

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import com.github.dhaval2404.imagepicker.util.FileUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.permission.PermissionHandler
import com.tech.young.base.permission.Permissions
import com.tech.young.base.utils.BaseCustomBottomSheet
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showToast
import com.tech.young.data.DropDownData
import com.tech.young.data.ImageModel
import com.tech.young.data.api.Constants
import com.tech.young.data.model.DummyLists.getIndustries
import com.tech.young.data.model.DummyLists.getProductOrAreaOfExpertise
import com.tech.young.data.model.DummyLists.getSeekingData
import com.tech.young.data.model.DummyLists.getYearsEmployed
import com.tech.young.data.model.GetProfileApiResponse.GetProfileApiResponseData
import com.tech.young.data.model.UpdateUserProfileResponse
import com.tech.young.databinding.AddPhotosItemViewBinding
import com.tech.young.databinding.BottomSheetCameraGalleryBinding
import com.tech.young.databinding.BotttomSheetTopicsBinding
import com.tech.young.databinding.FragmentPersonalPreferencesBinding
import com.tech.young.databinding.ItemLayoutDropDownBinding
import com.tech.young.ui.my_profile_screens.YourProfileVM
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@AndroidEntryPoint
class PersonalPreferencesFragment : BaseFragment<FragmentPersonalPreferencesBinding>(),
    BaseCustomBottomSheet.Listener {

    private val viewModel: YourProfileVM by viewModels()
    private var profileData: GetProfileApiResponseData? = null

    // common sheet & adapter
    private var type: String? = null
    private lateinit var commonBottomSheet: BaseCustomBottomSheet<BotttomSheetTopicsBinding>
    private lateinit var commonAdapter: SimpleRecyclerViewAdapter<DropDownData, ItemLayoutDropDownBinding>
    private lateinit var cameraGalleryBottomSheet: BaseCustomBottomSheet<BottomSheetCameraGalleryBinding>
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private var isFairness: Boolean = false
    // adapter
    private lateinit var yourImageAdapter: SimpleRecyclerViewAdapter<ImageModel, AddPhotosItemViewBinding>
    private var imageMultiplatform: MutableList<MultipartBody.Part> = mutableListOf()


    //// camera
    private var photoFile: File? = null
    private var photoURI: Uri? = null
    private var imageUriList = ArrayList<ImageModel>()

    override fun onCreateView(view: View) {
        // view
        initView()
        // click
        initOnClick()
        // observer
        initObserver()


        binding.etAbout.setOnTouchListener { v, event ->
            val parent = v.parent ?: return@setOnTouchListener false  // Safely accessing the parent
            parent.requestDisallowInterceptTouchEvent(true)
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_UP -> parent.requestDisallowInterceptTouchEvent(false)
            }
            false
        }



    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_personal_preferences
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /** handle view **/
    private fun initView() {
        val role = sharedPrefManager.getLoginData()?.role
        galleryLauncher()
        handleViewsOverRole(role)
        initBottomSheet()
        initAdapter()
        profileData = arguments?.getParcelable("profileData")
        if (profileData != null) {
            binding.bean = profileData
            Log.e("xxx","${profileData?.user?.fairnessForward}")
            if (!profileData?.user?.additionalPhotos.isNullOrEmpty()) {
                if (imageUriList.none { it.type == "1" }) {
                    imageUriList.add(ImageModel(null, null, "1"))
                }
                profileData?.user?.additionalPhotos?.forEach {
                    imageUriList.add(ImageModel(it,null,"2"))
                }
                yourImageAdapter.list=imageUriList
            }
        }
        setupYesNoToggleForInvestment()
    }

    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity()) {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                R.id.etProducts->{
                    type = "1"
                    commonAdapter.list = getProductOrAreaOfExpertise()
                    commonBottomSheet.show()
                }
                R.id.etExpertise->{
                    type = "2"
                    commonAdapter.list = getProductOrAreaOfExpertise()
                    commonBottomSheet.show()
                }
                R.id.etServiceProvided->{
                    type = "3"
                    commonAdapter.list = getSeekingData()
                    commonBottomSheet.show()
                }
                R.id.etIndustrySeeking->{
                    type = "4"
                    commonAdapter.list = getIndustries()
                    commonBottomSheet.show()
                }

                R.id.tvSaveAdditional->{
                    Log.e("multipart", Gson().toJson(imageMultiplatform))
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

    private fun setupYesNoToggleForInvestment() {
        binding.yesOptionInvestment.label.text = "Yes"
        binding.noOptionInvestment.label.text = "No"
        binding.yesOptionInvestment.box.setOnClickListener {
            isFairness=true
            binding.yesOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_selected)
            binding.noOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_unselected)
        }
        binding.noOptionInvestment.box.setOnClickListener {
            isFairness=false
            binding.noOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_selected)
            binding.yesOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_unselected)
        }
        if (profileData!=null){
            if (profileData?.user?.fairnessForward==true){
                isFairness=true
                binding.noOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_unselected)
                binding.yesOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_selected)
            } else {
                isFairness = false
                binding.noOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_selected)
                binding.yesOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_unselected)
            }
        }
        else{
            isFairness=false
            binding.noOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_selected)
            binding.yesOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_unselected)
        }

    }

    private fun handleViewsOverRole(role: String?) {
        if (role != null) {
            when (role) {
                "general_member"->{
                    binding.tvHeading.text="Additional Information"
                }
                "financial_advisor", "financial_firm" -> {
                    binding.tvHeading.text="Personal Preferences"
                    binding.clFairness.visibility = View.VISIBLE
                }

                "small_business" -> {
                    binding.tvHeading.text="Additional Information"
                    binding.clFairness.visibility = View.VISIBLE
                    binding.clSeeking.visibility = View.VISIBLE
                }

                "startup" -> {
                    binding.tvHeading.text="Additional Information"
                    binding.clSeeking.visibility = View.VISIBLE
                }

                "investor" -> {
                    binding.tvHeading.text="Additional Information"
                    binding.clFairness.visibility = View.VISIBLE
                    binding.clIndustrySeeking.visibility = View.VISIBLE
                }

                "insurance" -> {
                    binding.tvHeading.text="Additional Information"
                    binding.clFairness.visibility = View.VISIBLE
                    binding.clProducts.visibility = View.VISIBLE
                    binding.clExpertise.visibility = View.VISIBLE
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

        cameraGalleryBottomSheet =
            BaseCustomBottomSheet(requireContext(), R.layout.bottom_sheet_camera_gallery) {
                when (it.id) {
                    R.id.openCamara, R.id.openCamaraImage -> {
                        openCamera()
                        cameraGalleryBottomSheet.dismiss()
                    }

                    R.id.icon_emoji_new, R.id.tvChooseFromGallery -> {
                        if (!BindingUtils.hasPermissions(
                                requireContext(), BindingUtils.permissions
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

    override fun onViewClick(view: View?) {

    }

    /** handle adapter **/
    private fun initAdapter() {
        yourImageAdapter = SimpleRecyclerViewAdapter(
            R.layout.add_photos_item_view, BR.bean
        ) { v, m, pos ->
            when (v.id) {
                R.id.clImage,R.id.ivImage->{
                    cameraGalleryBottomSheet.show()
                }
                R.id.ivDelete -> {
                    if (!m.image_Url.isNullOrEmpty()) {
                        val imageUrl = m.image_Url!!

                        val part = MultipartBody.Part.createFormData(
                            "additionalPhotosToBeRemoved[]", // 👈 array key
                            null,
                            imageUrl.toRequestBody("text/plain".toMediaTypeOrNull())
                        )

                        viewModel.updateProfile(
                            Constants.UPDATE_USER,
                            hashMapOf(),              // no other data
                            null,                     // no single file
                            mutableListOf(part)       // ✅ send as a list
                        )
                    }
                }
            }
        }
        yourImageAdapter.list = getImageList()
        binding.rvYourPhotos.adapter = yourImageAdapter


        // children adapter
        commonAdapter =
            SimpleRecyclerViewAdapter(R.layout.item_layout_drop_down, BR.bean) { v, m, pos ->
                when (v.id) {
                    R.id.consMain, R.id.title -> {
                        if (type != null) {
                            when (type) {
                                "1"->{
                                    binding.etProducts.setText(m.title)
                                }
                                "2"->{
                                    binding.etExpertise.setText(m.title)
                                }
                                "3"->{
                                    binding.etServiceProvided.setText(m.title)
                                }
                                "4"->{
                                    binding.etIndustrySeeking.setText(m.title)
                                }
                            }
                        }

                        commonBottomSheet.dismiss()
                    }
                }
            }
        commonBottomSheet.binding.rvTopics.adapter = commonAdapter
    }

    private fun getImageList():ArrayList<ImageModel>{
        val list=ArrayList<ImageModel>()
        list.add(ImageModel(null,null,"1"))
        return list
    }


    private fun selectImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            action = Intent.ACTION_GET_CONTENT
        }
        pickImageLauncher.launch(Intent.createChooser(intent, "Select Picture"))
    }

    private fun galleryLauncher() {
        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.let { data ->
                        try {
                            val clipData = data.clipData
                            if (clipData != null) {
                                // Multiple images selected
                                for (i in 0 until clipData.itemCount) {
                                    clipData.getItemAt(i)?.uri?.let { uri ->
                                        if (imageUriList.none { it.type == "1" }) {
                                            imageUriList.add(ImageModel(null, null, "1"))
                                        }
                                        imageUriList.add(ImageModel(null, uri,"2"))
                                        convertMultipartPartGal(uri).let { part ->
                                            imageMultiplatform.add(part)
                                        }

                                        handleSelectedImages(imageUriList)
                                    }
                                }
                            } else {
                                // Single image selected
                                data.data?.let { uri ->
                                    if (imageUriList.none { it.type == "1" }) {
                                        imageUriList.add(ImageModel(null, null, "1"))
                                    }
                                    imageUriList.add(ImageModel(null, uri, "2"))
                                    convertMultipartPartGal(uri).let { part ->
                                        imageMultiplatform.add(part)
                                    }
                                    handleSelectedImages(imageUriList)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
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

    // camera intent
    private fun openCamera() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Permissions.check(
                requireContext(),
                Manifest.permission.CAMERA,
                0,
                object : PermissionHandler() {
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
            if (result.resultCode === Activity.RESULT_OK) {
                try {
                    photoURI = photoFile!!.absoluteFile.toUri()
                    if (photoURI != null) {
                        if (imageUriList.none { it.type == "1" }) {
                            imageUriList.add(ImageModel(null, null,  "1"))
                        }
                        imageUriList.add(ImageModel(null, photoURI!!, "2"))
                        convertMultipartPart(photoURI!!)?.let { part ->
                            imageMultiplatform.add(part)
                        }
                        handleSelectedImages(imageUriList)

                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }

    private fun handleSelectedImages(imageUris: ArrayList<ImageModel>) {
        yourImageAdapter.list = imageUris
    }

    private fun convertMultipartPart(imageUri: Uri): MultipartBody.Part? {
        val filePath = imageUri.path ?: return null
        val file = File(filePath)
        if (!file.exists()) {
            return null
        }
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("additionalPhotos", file.name, requestFile)
    }

    private fun convertMultipartPartGal(imageUri: Uri): MultipartBody.Part {
        val file = FileUtil.getTempFile(requireContext(), imageUri)
        val fileName =
            "${file!!.nameWithoutExtension}_${System.currentTimeMillis()}.${file.extension}"
        val newFile = File(file.parent, fileName)
        file.renameTo(newFile)
        return MultipartBody.Part.createFormData(
            "additionalPhotos", newFile.name, newFile.asRequestBody("image/*".toMediaTypeOrNull())
        )
    }

    /** api call **/
    private fun apiCall() {
        val data = HashMap<String, RequestBody>()
        val role = sharedPrefManager.getLoginData()?.role
            if (role != null) {
                when (role) {
                    "financial_advisor", "financial_firm" -> {
                        data["fairnessForward"] = isFairness.toString().toRequestBody()
                    }

                    "small_business" -> {
                        data["fairnessForward"] = isFairness.toString().toRequestBody()
                        data["seeking"] = binding.etServiceProvided.text.toString().toRequestBody()
                    }

                    "startup" -> {
                        data["seeking"] = binding.etServiceProvided.text.toString().toRequestBody()
                    }

                    "investor" -> {
                        data["fairnessForward"] = isFairness.toString().toRequestBody()
                        data["industriesSeeking"] = binding.etIndustrySeeking.text.toString().toRequestBody()
                    }

                    "insurance" -> {
                        data["fairnessForward"] = isFairness.toString().toRequestBody()
                        data["productsOffered"] = binding.etProducts.text.toString().toRequestBody()
                        data["areaOfExpertise"] = binding.etExpertise.text.toString().toRequestBody()
                    }
                }
            }
        data["about"] = binding.etAbout.text.toString().toRequestBody()

        viewModel.updateProfile(Constants.UPDATE_USER, data,null,imageMultiplatform)
    }
}