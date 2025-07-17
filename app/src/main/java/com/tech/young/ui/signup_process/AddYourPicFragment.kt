package com.tech.young.ui.signup_process

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.BindingUtils.setNavigationBarStyle
import com.tech.young.data.api.Constants
import com.tech.young.databinding.FragmentAddYourPicBinding
import com.tech.young.ui.auth.AuthCommonVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddYourPicFragment : BaseFragment<FragmentAddYourPicBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
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
    }

    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity(), Observer {
            when (it?.id) {
                R.id.ivBack -> {
                    findNavController().popBackStack()
                }

                R.id.ivAddNew, R.id.tvTakePicture -> {
                    checkPermission()
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

    private fun checkPermission() {
        if (!BindingUtils.hasPermissions(requireContext(), BindingUtils.permissions)) {
            permissionResultLauncher.launch(BindingUtils.permissions)
        } else {
            selectImage()
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

}