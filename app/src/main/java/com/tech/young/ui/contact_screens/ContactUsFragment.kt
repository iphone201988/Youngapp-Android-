package com.tech.young.ui.contact_screens

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showToast
import com.tech.young.data.api.Constants
import com.tech.young.data.api.SimpleApiResponse
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.DialogPoliciesBinding
import com.tech.young.databinding.FragmentContactUsBinding
import com.tech.young.databinding.PolicyItemViewBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.util.FileUtil
import com.tech.young.base.permission.PermissionHandler
import com.tech.young.base.permission.Permissions
import com.tech.young.base.utils.BaseCustomBottomSheet
import com.tech.young.data.model.GetAdsAPiResponse
import com.tech.young.databinding.BottomSheetCameraGalleryBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@AndroidEntryPoint
class ContactUsFragment : BaseFragment<FragmentContactUsBinding>() {

    private val viewModel: ContactUsFragmentVm by viewModels()
    private var select = 0


    // adapter
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<GetAdsAPiResponse.Data.Ad, AdsItemViewBinding>

    private lateinit var policiesAdapter: SimpleRecyclerViewAdapter<String, PolicyItemViewBinding>

    private lateinit var cameraGalleryBottomSheet: BaseCustomBottomSheet<BottomSheetCameraGalleryBinding>
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    //// camera
    private var photoFile: File? = null
    private var photoURI: Uri? = null
    private var imageUri : Uri? = null


    private var isEdited = false

    override fun onCreateView(view: View) {
        initBottomSheet()
        galleryLauncher()

        // click
        initOnClick()
        viewModel.getAds(Constants.GET_ADS)
        // adapter
        initAdapter()

        initObserver()

        with(binding) {
            // Watch all editable fields
            etSubject.addTextChangedListener { isEdited = true }
            etName.addTextChangedListener { isEdited = true }
            etCompany.addTextChangedListener { isEdited = true }
            etEmail.addTextChangedListener { isEdited = true }
            etMessage.addTextChangedListener { isEdited = true }

            // Upload file click → also counts as editing
            etUploadFile.setOnClickListener {
                isEdited = true
                // your upload logic
            }
        }
    }


    fun hasUserEdited(): Boolean = isEdited
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

    private fun galleryLauncher() {
        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    imageUri = data?.data
                    if (imageUri != null) {
                        imageUri?.let { uri ->
                            val fileName = getFileNameFromUri(requireContext(), uri)
                            binding.etUploadFile.setText(fileName)
                        }
//                        selectedImagePart = BindingUtils.createImageMultipartFromUri(
//                            requireContext(),
//                            imageUri!!,
//                            "file" // <-- or any string key like "profile", "photo"
//                        )
                        Log.i("ImageUpload", "galleryLauncher: $imageUri")
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
                    imageUri?.let { uri ->
                        val fileName = getFileNameFromUri(requireContext(), uri)
                        binding.etUploadFile.setText(fileName)
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



    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }

        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }

        return result ?: "unknown_file"
    }

    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            when(it?.status){
                Status.LOADING ->{
                    showLoading()
                }
                Status.SUCCESS ->{
                    hideLoading()
                    when(it.message){
                        "contactUs" ->{
                            val myDataModel : SimpleApiResponse ? = BindingUtils.parseJson(it.data.toString())
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
                    showToast(it.message.toString())
                }
                else ->{

                }
            }
        })
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_contact_us
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity()) {
            when (it?.id) {
                R.id.tvAgreePolicy -> {
                    //policiesDialogBox.show()
                    showPolicyDialog()
                }

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
//                    ImagePicker.with(this)
//                        .compress(1024)
//                        .maxResultSize(1080, 1080)
//                        .createIntent { intent ->
//                            startForImageResult.launch(intent)
//                        }

                    cameraGalleryBottomSheet.show()
                }
                R.id.tvSubmit ->{
                    if (isEmptyField()){
                        val multipartImage = imageUri?.let { convertImageToMultipart(it) }
                        val data = HashMap<String, RequestBody>()
                        data["subject"] = binding.etSubject.text.toString().trim().toRequestBody()
                        data["name"] = binding.etName.text.toString().trim().toRequestBody()
                        data["company"] = binding.etCompany.text.toString().trim().toRequestBody()
                        data["email"] = binding.etEmail.text.toString().trim().toRequestBody()
                        data["message"] = binding.etMessage.text.toString().trim().toRequestBody()
                        viewModel.contactUs(data, Constants.CONTACT_US,multipartImage)

                    }
                }
            }
        }
    }

    /** handle adapter **/
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

    /** show dialog **/
    private fun showPolicyDialog() {
        val bindingDialog = DialogPoliciesBinding.inflate(layoutInflater)

        val dialog = Dialog(requireContext())
        dialog.setContentView(bindingDialog.root)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setDimAmount(0f)
        }
        BindingUtils.statusBarStyleBlack(requireActivity())
        BindingUtils.styleSystemBars(requireActivity(), getColor(requireContext(), R.color.colorSecondary2))

        policiesAdapter =
            SimpleRecyclerViewAdapter(R.layout.policy_item_view, BR.bean) { v, m, pos ->
                when (v.id) {

                }
            }
        policiesAdapter.list = listOf(
            "",""
        )
        bindingDialog.rvPolicy.adapter = policiesAdapter
        policiesAdapter.notifyDataSetChanged()

        bindingDialog.tvSubmit.setOnClickListener {
            BindingUtils.statusBarStyleBlack(requireActivity())
            BindingUtils.styleSystemBars(requireActivity(), getColor(requireContext(), R.color.white))
                    select = 1
                    binding.check = true
            dialog.dismiss()
        }
        bindingDialog.close.setOnClickListener{
            BindingUtils.statusBarStyleBlack(requireActivity())
            BindingUtils.styleSystemBars(requireActivity(), getColor(requireContext(), R.color.white))
            dialog.dismiss()

        }
        dialog.setCancelable(false)
        dialog.show()
    }

//    private val startForImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
//        try {
//            val resultCode = result.resultCode
//            val data = result.data
//            if (resultCode == Activity.RESULT_OK) {
//                val fileUri = data?.data
//                imageUri = fileUri
//
//                //  Log.i("dasd", ": $imageUri")
//            } else if (resultCode == ImagePicker.RESULT_ERROR) {
//
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
    private fun isEmptyField() : Boolean{
        if (TextUtils.isEmpty(binding.etSubject.text.toString().trim())){
            showToast("Please enter subject")
            return false
        }
        if (TextUtils.isEmpty(binding.etName.text.toString().trim())){
            showToast("Please enter name")
            return false
        }
        if (TextUtils.isEmpty(binding.etEmail.text.toString().trim())){
            showToast("Please enter email")
            return false
        }
        return true
    }

    private fun convertImageToMultipart(imageUri: Uri): MultipartBody.Part {
        val file = FileUtil.getTempFile(requireContext(), imageUri)
        return MultipartBody.Part.createFormData(
            "file",
            file!!.name,
            file.asRequestBody("image/png".toMediaTypeOrNull())
        )
    }

}