package com.tech.young.ui.contact_screens

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.getColor
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
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@AndroidEntryPoint
class ContactUsFragment : BaseFragment<FragmentContactUsBinding>() {

    private val viewModel: ContactUsFragmentVm by viewModels()
    private var select = 0
    private var imageUri : Uri ?= null


    // adapter
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<String, AdsItemViewBinding>

    private lateinit var policiesAdapter: SimpleRecyclerViewAdapter<String, PolicyItemViewBinding>

    override fun onCreateView(view: View) {
        // click
        initOnClick()
        // adapter
        initAdapter()

        initObserver()
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
                    ImagePicker.with(this)
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .createIntent { intent ->
                            startForImageResult.launch(intent)
                        }
                }
                R.id.tvSubmit ->{
                    if (isEmptyField()){
                        val multipartImage = imageUri?.let { convertImageToMultipart(it) }
                        val data = HashMap<String, RequestBody>()
                        data["subject"] = binding.etSubject.text.toString().trim().toRequestBody()
                        data["name"] = binding.etName.text.toString().trim().toRequestBody()
                        data["company"] = binding.etCompany.text.toString().trim().toRequestBody()
                        data["email"] = binding.etEmail.text.toString().trim().toRequestBody()
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
        adsAdapter.list = getList
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

    private val startForImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        try {
            val resultCode = result.resultCode
            val data = result.data
            if (resultCode == Activity.RESULT_OK) {
                val fileUri = data?.data
                imageUri = fileUri

                //  Log.i("dasd", ": $imageUri")
            } else if (resultCode == ImagePicker.RESULT_ERROR) {

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun isEmptyField() : Boolean{
        if (TextUtils.isEmpty(binding.etSubject.text.toString().trim())){
            showToast("Please enter subject")
            return false
        }
        if (TextUtils.isEmpty(binding.etName.text.toString().trim())){
            showToast("Please enter name")
            return false
        }
        if (TextUtils.isEmpty(binding.etCompany.text.toString().trim())){
            showToast("Please enter company name")
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