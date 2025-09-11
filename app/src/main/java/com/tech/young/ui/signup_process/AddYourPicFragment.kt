package com.tech.young.ui.signup_process

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
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
import com.tech.young.base.utils.BindingUtils.setNavigationBarStyle
import com.tech.young.data.AdditionalPhotos
import com.tech.young.data.api.Constants
import com.tech.young.data.model.ImageType
import com.tech.young.databinding.BottomSheetCameraGalleryBinding
import com.tech.young.databinding.FragmentAddYourPicBinding
import com.tech.young.databinding.ItemLayoutAdditionalPhotosBinding
import com.tech.young.ui.auth.AuthCommonVM
import com.tech.young.ui.signup_process.RegistrationDataHolder.imageMultiplatform
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@AndroidEntryPoint
class AddYourPicFragment : BaseFragment<FragmentAddYourPicBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    private lateinit var cameraGalleryBottomSheet: BaseCustomBottomSheet<BottomSheetCameraGalleryBinding>
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var photosAdapter : SimpleRecyclerViewAdapter<AdditionalPhotos, ItemLayoutAdditionalPhotosBinding>
    private var selectedImageIndex = -1
    private var imageList = MutableList(5) { AdditionalPhotos(null) }
    private var currentImagePosition = -1
    private var selectedImageType: String = ""
    //// camera
    private var photoFile: File? = null
    private var photoURI: Uri? = null
    override fun onCreateView(view: View) {
        initView()
        initOnClick()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_add_your_pic
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }


    private fun initView() {
        BindingUtils.statusBarStyleBlack(requireActivity())
        BindingUtils.styleSystemBars(requireActivity(), requireActivity().getColor(R.color.white))
        galleryLauncher()
        setNavigationBarStyle(
            activity = requireActivity(),
            navigationBarColorResId = R.color.white,
            isLightIcons = true
        )

        if (Constants.chooseAccountType == "General Member") {
            binding.consAddAdditionalPhoto.visibility = View.GONE
        } else {
            binding.consAddAdditionalPhoto.visibility = View.VISIBLE
        }
        initBottomSheet()
        prepareImageList(5)
        initAdapter()
    }

    private fun prepareImageList(slotCount: Int) {
        imageList.clear()
        repeat(slotCount) {
            imageList.add(AdditionalPhotos(image = null)) // Explicitly set null
        }
    }
    private fun initAdapter() {
        photosAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_additional_photos, BR.bean) { v, m, pos ->
            when (v.id) {
                R.id.consMain -> {
                    selectedImageType = "additional"
                    currentImagePosition = pos
                    Log.i("fdssdfsdfsd", "initAdapter: $pos  , $currentImagePosition" )

                    cameraGalleryBottomSheet.show()

                }
            }
        }
        binding.rvAdditionalPhotos.adapter = photosAdapter
        photosAdapter.list = imageList
    }

    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity(), Observer {
            when (it?.id) {
                R.id.ivBack -> {
                    findNavController().popBackStack()
                }

                R.id.ivAddNew, R.id.tvTakePicture -> {
                    selectedImageType = "profile"
                    cameraGalleryBottomSheet.show()
                }


                R.id.tvNext -> {
                    if (Constants.chooseAccountType == "Financial Advisor" || Constants.chooseAccountType == "Financial Firm") {
                        findNavController().navigate(R.id.navigateToCrdNumberVerificationFragment)
                    } else {
                        findNavController().navigate(R.id.navigateToAddYourPersonalInfoFragment)
                    }


                }

            }
        })

    }



    private fun galleryLauncher() {
//        pickImageLauncher =
//            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//                if (result.resultCode == Activity.RESULT_OK) {
//                    val data = result.data
//                    val imageUri: Uri? = data?.data
//                    if (imageUri != null) {
//                        val imageFile =
//                            BindingUtils.convertImageToMultipart(imageUri, requireActivity())
//                        RegistrationDataHolder.profileImage = imageFile
//                        binding.ivUserImage.setImageURI(imageUri)
//                    }
//                }
//            }

        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                Log.d("PickImageLauncher", "Activity result received. Result code: ${result.resultCode}")

                if (result.resultCode != Activity.RESULT_OK) {
                    Log.w("PickImageLauncher", "Activity result not OK")
                    return@registerForActivityResult
                }

                val imageUri = result.data?.data
                if (imageUri == null) {
                    Log.e("PickImageLauncher", "Image URI is null")
                    return@registerForActivityResult
                }

                Log.d("PickImageLauncher", "Picked image URI: $imageUri")

                val imageFile = BindingUtils.convertImageToMultipart(imageUri, requireActivity())
                if (imageFile == null) {
                    Log.e("PickImageLauncher", "Failed to convert image to multipart")
                    return@registerForActivityResult
                }

                Log.d("PickImageLauncher", "Multipart created: $imageFile")
                Log.d("PickImageLauncher", "Selected image type: $selectedImageType")

                when (selectedImageType) {
                    "profile" -> {
                        RegistrationDataHolder.profileImage = imageFile
                        binding.ivUserImage.setImageURI(imageUri)
                        Log.d("PickImageLauncher", "Profile image set successfully")
                    }

                    "additional" -> {
                        Log.d("PickImageLauncher", "Updating additional photo at position: $currentImagePosition")

                        // Update image in list for adapter
                        if (currentImagePosition in imageList.indices) {
                            imageList[currentImagePosition].image = imageUri
                            photosAdapter.notifyItemChanged(currentImagePosition)
                        } else {
                            Log.w("PickImageLauncher", "Invalid image position: $currentImagePosition")
                            return@registerForActivityResult
                        }

                        // Convert and update MultipartBody.Part
                        convertMultipartPartGal(imageUri)?.let { part ->
                            if (currentImagePosition in imageMultiplatform.indices) {
                                imageMultiplatform[currentImagePosition] = part
                            } else if (imageMultiplatform.size < 5) {
                                // Pad the list if needed
                                while (imageMultiplatform.size < currentImagePosition) {
                                    imageMultiplatform.add(null)
                                }
                                imageMultiplatform.add(part)
                            } else {
                                Log.w("PickImageLauncher", "Invalid imageMultiplatform index")
                            }
                        } ?: Log.e("PickImageLauncher", "Failed to create multipart from URI")
                    }

                    else -> {
                        Log.w("PickImageLauncher", "Unhandled image type: $selectedImageType")
                    }
                }
            }

    }

//    private fun convertMultipartPartGal(imageUri: Uri): MultipartBody.Part {
//        val file = FileUtil.getTempFile(requireContext(), imageUri)
//        val fileName =
//            "${file!!.nameWithoutExtension}_${System.currentTimeMillis()}.${file.extension}"
//        val newFile = File(file.parent, fileName)
//        file.renameTo(newFile)
//        return MultipartBody.Part.createFormData(
//            "additionalPhotos", newFile.name, newFile.asRequestBody("image/*".toMediaTypeOrNull())
//        )
//    }

    private fun convertMultipartPartGal(imageUri: Uri): MultipartBody.Part? {
        val file = FileUtil.getTempFile(requireContext(), imageUri)
        if (file == null) {
            Log.e("convertMultipartPartGal", "FileUtil returned null for URI: $imageUri")
            return null
        }

        val fileName =
            "${file.nameWithoutExtension}_${System.currentTimeMillis()}.${file.extension}"
        val newFile = File(file.parent, fileName)
        file.renameTo(newFile)

        return MultipartBody.Part.createFormData(
            "additionalPhotos",
            newFile.name,
            newFile.asRequestBody("image/*".toMediaTypeOrNull())
        )
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

    /** show camera & gallery bottom sheet **/
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

    // camera intent
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

//    private var cameraLauncher: ActivityResultLauncher<Intent> =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode === Activity.RESULT_OK) {
//                try {
//                    photoURI = photoFile!!.absoluteFile.toUri()
//                    if (photoURI != null) {
//                        binding.ivUserImage.setImageURI(photoURI)
//
//                    }
//                } catch (ex: Exception) {
//                    ex.printStackTrace()
//                }
//            }
//        }



    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d("CameraLauncher", "Activity result received. Result code: ${result.resultCode}")

            if (result.resultCode == Activity.RESULT_OK) {
                val uri = photoFile?.absoluteFile?.toUri()

                if (uri == null) {
                    Log.e("CameraLauncher", "URI is null")
                    return@registerForActivityResult
                }

                Log.d("CameraLauncher", "Captured photo URI: $uri")

                val imageFile = BindingUtils.convertImageToMultipart(uri, requireActivity())

                if (imageFile == null) {
                    Log.e("CameraLauncher", "Failed to convert image to multipart")
                    return@registerForActivityResult
                }

                Log.d("CameraLauncher", "Multipart created: $imageFile")
                Log.d("CameraLauncher", "Selected image type: $selectedImageType")

                when (selectedImageType) {
                    "profile" -> {
                        RegistrationDataHolder.profileImage = imageFile
                        binding.ivUserImage.setImageURI(uri)
                        Log.d("CameraLauncher", "Profile image set successfully")
                    }

                    "additional" -> {
                        Log.d("CameraLauncher", "Current image position: $currentImagePosition")

                        if (currentImagePosition in imageList.indices) {
                            imageList[currentImagePosition].image = uri
                            photosAdapter.notifyItemChanged(currentImagePosition)
                        } else {
                            Log.w("CameraLauncher", "Invalid image position: $currentImagePosition")
                        }

                        convertMultipartPart(uri)?.let { part ->
                            if (currentImagePosition in imageMultiplatform.indices) {
                                imageMultiplatform[currentImagePosition] = part
                            } else if (imageMultiplatform.size < 5) {
                                // Pad if list has < 5 items
                                while (imageMultiplatform.size < currentImagePosition) {
                                    imageMultiplatform.add(null)
                                }
                                imageMultiplatform.add(part)
                            } else {
                                Log.w("CameraLauncher", "Invalid index for imageMultiplatform update")
                            }
                        } ?: Log.e("CameraLauncher", "Failed to create Multipart from URI")

                        Log.d("CameraLauncher", "Additional photo updated at position $currentImagePosition")
                    }

                    else -> {
                        Log.w("CameraLauncher", "Invalid image type or position")
                    }
                }
            } else {
                Log.w("CameraLauncher", "Activity result not OK")
            }
        }



}