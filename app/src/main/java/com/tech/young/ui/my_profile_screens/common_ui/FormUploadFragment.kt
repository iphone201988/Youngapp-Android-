package com.tech.young.ui.my_profile_screens.common_ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import com.github.dhaval2404.imagepicker.util.FileUtil
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
import com.tech.young.data.ImageModel
import com.tech.young.data.api.Constants
import com.tech.young.data.model.GetProfileApiResponse.GetProfileApiResponseData
import com.tech.young.data.model.UpdateUserProfileResponse
import com.tech.young.databinding.AddPhotosItemViewBinding
import com.tech.young.databinding.BottomSheetCameraGalleryBinding
import com.tech.young.databinding.FragmentFormUploadBinding
import com.tech.young.ui.my_profile_screens.YourProfileVM
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@AndroidEntryPoint
class FormUploadFragment : BaseFragment<FragmentFormUploadBinding>(),
    BaseCustomBottomSheet.Listener {
    private val viewModel: YourProfileVM by viewModels()
    private var profileData: GetProfileApiResponseData? = null

    // adapter
    private lateinit var yourImageAdapter: SimpleRecyclerViewAdapter<ImageModel, AddPhotosItemViewBinding>
    private lateinit var cameraGalleryBottomSheet: BaseCustomBottomSheet<BottomSheetCameraGalleryBinding>
    private var imageMultiplatform: MutableList<MultipartBody.Part> = mutableListOf()
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

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
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_form_upload
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /** handle view **/
    private fun initView() {
        galleryLauncher()
        // bottom sheet
        initBottomSheet()
        // adapter
        initAdapter()
        profileData = arguments?.getParcelable("profileData")
        if (profileData != null) {
            if (!profileData?.user?.formUpload.isNullOrEmpty()) {
                if (imageUriList.none { it.type == "1" }) {
                    imageUriList.add(ImageModel(null, null, "1"))
                }
                profileData?.user?.formUpload?.forEach {
                    imageUriList.add(ImageModel(it, null, "2"))
                }
                yourImageAdapter.list = imageUriList
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

                R.id.actionToggleBtn -> {
                    // handle click
                }

                R.id.tvSaveAdditional -> {
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

    /** handle bottom sheet **/
    private fun initBottomSheet() {
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

    /** handle adapter **/
    private fun initAdapter() {
        yourImageAdapter = SimpleRecyclerViewAdapter(
            R.layout.add_photos_item_view, BR.bean
        ) { v, m, pos ->
            when (v.id) {
                R.id.clImage, R.id.ivImage -> {
                    cameraGalleryBottomSheet.show()
                }
            }
        }
        yourImageAdapter.list = getImageList()
        binding.rvYourPhotos.adapter = yourImageAdapter
    }

    private fun getImageList(): ArrayList<ImageModel> {
        val list = ArrayList<ImageModel>()
        list.add(ImageModel(null, null, "1"))
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
                                        imageUriList.add(ImageModel(null, uri, "2"))
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
            Permissions.check(requireContext(),
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
                            imageUriList.add(ImageModel(null, null, "1"))
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
        return MultipartBody.Part.createFormData("formUpload", file.name, requestFile)
    }

    private fun convertMultipartPartGal(imageUri: Uri): MultipartBody.Part {
        val file = FileUtil.getTempFile(requireContext(), imageUri)
        val fileName =
            "${file!!.nameWithoutExtension}_${System.currentTimeMillis()}.${file.extension}"
        val newFile = File(file.parent, fileName)
        file.renameTo(newFile)
        return MultipartBody.Part.createFormData(
            "formUpload", newFile.name, newFile.asRequestBody("image/*".toMediaTypeOrNull())
        )
    }

    override fun onViewClick(view: View?) {

    }

    private fun apiCall() {
        val data = HashMap<String, RequestBody>()
        viewModel.updateProfile(Constants.UPDATE_USER, data, null, imageMultiplatform)
    }


}