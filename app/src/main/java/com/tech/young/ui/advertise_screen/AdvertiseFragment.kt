package com.tech.young.ui.advertise_screen

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
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
import com.tech.young.data.api.Constants
import com.tech.young.data.api.SimpleApiResponse
import com.tech.young.data.model.GetAdsAPiResponse
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.BottomSheetCameraGalleryBinding
import com.tech.young.databinding.FragmentAdvertiseBinding
import com.tech.young.databinding.ItemLayoutAdvertisePopupBinding
import com.tech.young.ui.home.HomeActivity
import com.tech.young.ui.signup_process.RegistrationDataHolder
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@AndroidEntryPoint
class AdvertiseFragment : BaseFragment<FragmentAdvertiseBinding>() {

    private val viewModel: AdvertiseFragmentVm by viewModels()
    private lateinit var cameraGalleryBottomSheet: BaseCustomBottomSheet<BottomSheetCameraGalleryBinding>
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private var select = 0
    private var selectedImagePart: MultipartBody.Part? = null

    //// camera
    private var photoFile: File? = null
    private var photoURI: Uri? = null
    private var imageUri : Uri? = null

    // adapter
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<GetAdsAPiResponse.Data.Ad, AdsItemViewBinding>

    override fun onCreateView(view: View) {
        initBottomSheet()
        galleryLauncher()
        viewModel.getAds(Constants.GET_ADS)
        initAdapter()
        // click
        initOnClick()
        setObserver()
    }

    private fun setObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            when(it?.status){
                Status.LOADING ->{
                    showLoading()
                }
                Status.SUCCESS ->{
                    hideLoading()
                    when(it.message){
                        "advertise" ->{
                            val myDataModel : SimpleApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                showToast(myDataModel.message.toString())
                                val intent = Intent(requireContext(), HomeActivity :: class.java)
                                startActivity(intent)
                                requireActivity().finishAffinity()
                                showDialog()
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
                    showToast(it.message.toString())
                }
                else ->{

                }
            }
        })
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_advertise
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    private fun galleryLauncher() {
        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    imageUri = data?.data
                    if (imageUri != null) {
                        selectedImagePart = BindingUtils.createImageMultipartFromUri(
                            requireContext(),
                            imageUri!!,
                            "file" // <-- or any string key like "profile", "photo"
                        )
                        Log.i("ImageUpload", "galleryLauncher: $imageUri")
                    }
                }
            }
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

    /** handle click **/
    private fun initOnClick(){
        viewModel.onClick.observe(requireActivity()){
            when(it?.id){
                R.id.ivCheck -> {
                    if (select == 0) {
                        select = 1
                        binding.check = true
                    } else {
                        select = 0
                        binding.check = false
                    }
                }
                R.id.etUploadFile ->{
                cameraGalleryBottomSheet.show()
                }

                R.id.tvSubmit->{
                    if (isEmptyField()){
                        val data = HashMap<String,RequestBody>()
                        data["name"] = binding.etName.text.toString().trim().toRequestBody()
                        data["company"] = binding.etCompany.text.toString().trim().toRequestBody()
                        data["email"] = binding.etEmail.text.toString().trim().toRequestBody()
                        data["website"] = binding.etWebsite.text.toString().trim().toRequestBody()

                        if (selectedImagePart != null){
                            viewModel.advertise(data,Constants.GET_ADS,selectedImagePart)
                        }
                        else {
                            showToast("Please select image")
                        }
                    }


//                    showDialog()
                }

            }
        }
    }

    private fun initAdapter() {
        adsAdapter = SimpleRecyclerViewAdapter(R.layout.ads_item_view, BR.bean) { v, m, pos ->
            when (v.id) {

            }
        }
        binding.rvAds.adapter = adsAdapter
    }

    private var getList = listOf(
        "", "", "", "", ""
    )

    /** dialog **/
    private fun showDialog() {
        val bindingDialog = ItemLayoutAdvertisePopupBinding.inflate(layoutInflater)

        val dialog = Dialog(requireContext())
        dialog.setContentView(bindingDialog.root)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setDimAmount(0f)
        }
        BindingUtils.statusBarStyleBlack(requireActivity())
        BindingUtils.styleSystemBars(requireActivity(),
            ContextCompat.getColor(requireContext(), R.color.colorSecondary2)
        )

        bindingDialog.tvDone.setOnClickListener {
            BindingUtils.statusBarStyleBlack(requireActivity())
            BindingUtils.styleSystemBars(requireActivity(),
                ContextCompat.getColor(requireContext(), R.color.white)
            )

            dialog.dismiss()
        }
        dialog.setCancelable(false)
        dialog.show()
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
                    photoURI = photoFile!!.absoluteFile.toUri()
                    if (photoURI != null) {
                        selectedImagePart = BindingUtils.createImageMultipartFromUri(
                            requireContext(),
                            photoURI!!,
                            "file" // or appropriate key
                        )
                        Log.i("ImageUpload", "cameraLauncher: $photoURI")
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }



    private fun isEmptyField() : Boolean {
        if (TextUtils.isEmpty(binding.etName.text.toString().trim())){
            showToast("Please enter name")
            return false
        }
        if (TextUtils.isEmpty(binding.etCompany.text.toString().trim())){
            showToast("Please enter company")
            return false
        }
        if (TextUtils.isEmpty(binding.etEmail.text.toString().trim())){
            showToast("Please enter email")
            return false
        }
        if (TextUtils.isEmpty(binding.etWebsite.text.toString().trim())){
            showToast("Please enter website")
            return false

        }



        return true
    }


}