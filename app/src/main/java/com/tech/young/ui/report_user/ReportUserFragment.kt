package com.tech.young.ui.report_user

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.util.FileUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
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
import com.tech.young.data.api.Constants
import com.tech.young.data.api.SimpleApiResponse
import com.tech.young.data.model.GetAdsAPiResponse
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.BottomSheetCameraGalleryBinding
import com.tech.young.databinding.BotttomSheetTopicsBinding
import com.tech.young.databinding.FragmentReportUserBinding
import com.tech.young.databinding.ItemLayoutDropDownBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@AndroidEntryPoint
class ReportUserFragment : BaseFragment<FragmentReportUserBinding>() , BaseCustomBottomSheet.Listener {

    private val viewModel: ReportUserVM by viewModels()
    private var imageUri : Uri ?= null
    private var reportType : String ? = null
    // adapter
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<GetAdsAPiResponse.Data.Ad, AdsItemViewBinding>

    private lateinit var reasonBottomSheet : BaseCustomBottomSheet<BotttomSheetTopicsBinding>
    private lateinit var reasonAdapter : SimpleRecyclerViewAdapter<DropDownData, ItemLayoutDropDownBinding>
    private lateinit var cameraGalleryBottomSheet: BaseCustomBottomSheet<BottomSheetCameraGalleryBinding>
    private var reportList = ArrayList<DropDownData>()
    private var reason : String ? = null
    private var userId : String ? = null
    private var getList = listOf(
        "", "", "", "", ""
    )


    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private var selectedImagePart: MultipartBody.Part? = null

    //// camera
    private var photoFile: File? = null
    private var photoURI: Uri? = null


    override fun onCreateView(view: View) {
        getReportList()
        viewModel.getAds(Constants.GET_ADS)
        initBottomSheet()
        galleryLauncher()
        // view
        initView()





        // click
        initOnClick()
        // observer
        initObserver()
    }

    private fun initBottomSheet() {
        reasonBottomSheet = BaseCustomBottomSheet(requireContext(),R.layout.botttom_sheet_topics,this)

        reasonBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        reasonBottomSheet.behavior.isDraggable = true

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

    override fun getLayoutResource(): Int {
        return R.layout.fragment_report_user
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }


    /** handle view **/
    private fun initView() {
        userId= arguments?.getString("userId").toString()
        reportType = arguments?.getString("reportType").toString()


        // adapter
        initAdapter()

    }

    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity()) {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }

                R.id.tvSubmit -> {
                    // handle click
                    if (isEmptyField()){
                        if (userId != null){
                            val multipartImage = imageUri?.let { convertImageToMultipart(it) }

                            val data = HashMap<String,RequestBody>()
                            data["id"] = userId.toString().toRequestBody()
                            data["reason"] = binding.etReasonForReport.text.toString().toRequestBody()
                            data["additionalDetails"] = binding.etDescription.text.toString().toRequestBody()
                            data["type"] = reportType.toString().toRequestBody()

                            viewModel.reportUser(data, Constants.SEND_REPORT,multipartImage)
                        }

                    }
                }

                R.id.tvCancel -> {
                    // handle click
                }
                R.id.etReasonForReport ->{
                    reasonBottomSheet.show()
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
                        "reportUser" ->{
                            val myDataModel : SimpleApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                showToast(myDataModel.message.toString())
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }
                        }
                        "getAds" ->{
                            hideLoading()
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


    /** handle adapter **/
    private fun initAdapter() {
        adsAdapter = SimpleRecyclerViewAdapter(R.layout.ads_item_view, BR.bean) { v, m, pos ->
            when (v.id) {

            }
        }
        binding.rvAds.adapter = adsAdapter

        reasonAdapter  = SimpleRecyclerViewAdapter(R.layout.item_layout_drop_down,BR.bean){v,m,pos ->
            when(v.id){
                R.id.consMain ->{
                    binding.etReasonForReport.setText(m.title)
                    reason = m.title
                    reasonBottomSheet.dismiss()
                }
            }
        }
        reasonBottomSheet.binding.rvTopics.adapter = reasonAdapter
        reasonAdapter.list = reportList
        reasonAdapter.notifyDataSetChanged()
    }

    private fun isEmptyField() :Boolean {
        if (TextUtils.isEmpty(binding.etReasonForReport.text.toString().trim())){
            showToast("Please choose reason of report")
            return false
        }
        return true
    }

    override fun onViewClick(view: View?) {

    }

    private fun convertImageToMultipart(imageUri: Uri): MultipartBody.Part {
        val file = FileUtil.getTempFile(requireContext(), imageUri)
        return MultipartBody.Part.createFormData(
            "screenshots",
            file!!.name,
            file.asRequestBody("image/png".toMediaTypeOrNull())
        )
    }

    private fun getReportList() {
        reportList.add(DropDownData("Spam"))
        reportList.add(DropDownData("Harassment"))
        reportList.add(DropDownData("Hate Speech"))
        reportList.add(DropDownData("Violence or Threats"))
        reportList.add(DropDownData("Sexually Explicit Content"))
        reportList.add(DropDownData("Child Exploitation"))
        reportList.add(DropDownData("Self-Harm or Suicide"))
        reportList.add(DropDownData("False Information"))
        reportList.add(DropDownData("Scam or Fraud"))
        reportList.add(DropDownData("Impersonation"))
        reportList.add(DropDownData("Inappropriate Username or Profile"))
        reportList.add(DropDownData("Intellectual Property Violation"))
        reportList.add(DropDownData("Privacy Violation"))
        reportList.add(DropDownData("Terrorism or Extremism"))
        reportList.add(DropDownData("Illegal Activity"))
        reportList.add(DropDownData("Disruptive Behavior"))
        reportList.add(DropDownData("Wrong Category"))

    }
}