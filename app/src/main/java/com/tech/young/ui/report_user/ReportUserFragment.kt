package com.tech.young.ui.report_user

import android.app.Activity
import android.net.Uri
import android.text.TextUtils
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.tech.young.base.utils.BaseCustomBottomSheet
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showToast
import com.tech.young.data.DropDownData
import com.tech.young.data.api.Constants
import com.tech.young.data.api.SimpleApiResponse
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.BotttomSheetTopicsBinding
import com.tech.young.databinding.FragmentReportUserBinding
import com.tech.young.databinding.ItemLayoutDropDownBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@AndroidEntryPoint
class ReportUserFragment : BaseFragment<FragmentReportUserBinding>() , BaseCustomBottomSheet.Listener {

    private val viewModel: ReportUserVM by viewModels()
    private var imageUri : Uri ?= null
    private var reportType : String ? = null
    // adapter
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<String, AdsItemViewBinding>

    private lateinit var reasonBottomSheet : BaseCustomBottomSheet<BotttomSheetTopicsBinding>
    private lateinit var reasonAdapter : SimpleRecyclerViewAdapter<DropDownData, ItemLayoutDropDownBinding>
    private var reportList = ArrayList<DropDownData>()
    private var reason : String ? = null
    private var userId : String ? = null
    private var getList = listOf(
        "", "", "", "", ""
    )

    override fun onCreateView(view: View) {
        getReportList()
        initBottomSheet()
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
                    ImagePicker.with(this)
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .createIntent { intent ->
                            startForImageResult.launch(intent)
                        }
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
                        "reportUser" ->{
                            val myDataModel : SimpleApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                showToast(myDataModel.message.toString())
                                requireActivity().onBackPressedDispatcher.onBackPressed()
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
        adsAdapter.list = getList
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