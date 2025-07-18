package com.tech.young.ui.signup_process

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
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.permission.PermissionHandler
import com.tech.young.base.permission.Permissions
import com.tech.young.base.utils.BaseCustomBottomSheet
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.BindingUtils.setNavigationBarStyle
import com.tech.young.data.api.Constants
import com.tech.young.databinding.BottomSheetCameraGalleryBinding
import com.tech.young.databinding.FragmentAddYourPicBinding
import com.tech.young.ui.auth.AuthCommonVM
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class AddYourPicFragment : BaseFragment<FragmentAddYourPicBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    private lateinit var cameraGalleryBottomSheet: BaseCustomBottomSheet<BottomSheetCameraGalleryBinding>
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

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
    }

    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity(), Observer {
            when (it?.id) {
                R.id.ivBack -> {
                    findNavController().popBackStack()
                }

                R.id.ivAddNew, R.id.tvTakePicture -> {
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
        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    val imageUri: Uri? = data?.data
                    if (imageUri != null) {
                        val imageFile =
                            BindingUtils.convertImageToMultipart(imageUri, requireActivity())
                        RegistrationDataHolder.profileImage = imageFile
                        binding.ivUserImage.setImageURI(imageUri)
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

    private var cameraLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode === Activity.RESULT_OK) {
                try {
                    photoURI = photoFile!!.absoluteFile.toUri()
                    if (photoURI != null) {
                        binding.ivUserImage.setImageURI(photoURI)

                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }

}