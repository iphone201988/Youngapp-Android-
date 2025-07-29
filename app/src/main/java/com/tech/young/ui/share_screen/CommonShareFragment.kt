package com.tech.young.ui.share_screen

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.showToast
import com.tech.young.data.api.Constants
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.FragmentCommonShareBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.util.FileUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tech.young.base.utils.BaseCustomBottomSheet
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.data.DropDownData
import com.tech.young.data.model.CreatePostApiResponse
import com.tech.young.data.model.GetAdsAPiResponse
import com.tech.young.data.model.ShareData
import com.tech.young.data.model.StreamData
import com.tech.young.databinding.BotttomSheetTopicsBinding
import com.tech.young.databinding.ItemLayoutDropDownBinding
import com.tech.young.ui.common.CommonActivity
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@AndroidEntryPoint
class CommonShareFragment : BaseFragment<FragmentCommonShareBinding>() ,BaseCustomBottomSheet.Listener{
    private val viewModel: ShareVM by viewModels()

    // adapter
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<GetAdsAPiResponse.Data.Ad, AdsItemViewBinding>
    private lateinit var topicBottomSheet : BaseCustomBottomSheet<BotttomSheetTopicsBinding>
    private lateinit var topicAdapter : SimpleRecyclerViewAdapter<DropDownData, ItemLayoutDropDownBinding>
    private var topicList = ArrayList<DropDownData>()

    private var selectedOption: String = "stock"  // default

    private var imageUri : Uri ?= null

    override fun onCreateView(view: View) {
        // view
        initView()

        viewModel.getAds(Constants.GET_ADS)
        // click
        initOnClick()
        // observer
        initObserver()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_common_share
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    private fun initBottomsheet() {
        topicBottomSheet = BaseCustomBottomSheet(requireContext(),R.layout.botttom_sheet_topics,this)

        topicBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        topicBottomSheet.behavior.isDraggable = true



    }

    /** handle view **/
    private fun initView() {
        initBottomsheet()
        getTopicsList()
        initAdapter()
        setupToggle()

        binding.etDescription.setOnTouchListener { v, event ->
            val parent = v.parent ?: return@setOnTouchListener false  // Safely accessing the parent
            parent.requestDisallowInterceptTouchEvent(true)
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_UP -> parent.requestDisallowInterceptTouchEvent(false)
            }
            false
        }
    }

    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity()) {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }

                R.id.tvShare -> {
                    // handle click
                    if (isEmptyField()){


                        val shareData = ShareData(
                          title = binding.etTitle.text.toString().trim(),
                            topic =  binding.etTopic.text.toString().trim(),
                            description = binding.etDescription.text.toString().trim(),
                            symbol = selectedOption,
                            symbolValue =  binding.etSymbol.text.toString().trim(),
                            image = imageUri

                        )

                        val intent = Intent(requireContext(), CommonActivity::class.java).apply {
                            putExtra("from", "share_confirmation")
                            putExtra("share_data", shareData)
                        }
                        startActivity(intent)


                    }
                }
                R.id.ivUploadImage ->{
                    ImagePicker.with(this)
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .createIntent { intent ->
                            startForImageResult.launch(intent)
                        }
                }
                R.id.etTopic ->{
                    topicBottomSheet.show()
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
                binding.ivUploadImage.setImageURI(imageUri)

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
                        "sharePost" ->{
                            val myDataModel : CreatePostApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    showToast(myDataModel.message.toString())
                                    requireActivity().onBackPressedDispatcher.onBackPressed()
                                }
                            }
                        }
                        "getAds" ->{
                            val myDataModel : GetAdsAPiResponse? = BindingUtils.parseJson(it.data.toString())
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

        topicAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_drop_down,BR.bean){v,m,pos ->
            when(v.id){
                R.id.consMain , R.id.title->{
                    binding.etTopic.setText(m.title)
                    topicBottomSheet.dismiss()
                }
            }
        }
        topicBottomSheet.binding.rvTopics.adapter = topicAdapter
        topicAdapter.list = topicList
        topicAdapter.notifyDataSetChanged()
    }

    private var getList = listOf(
        "", "", "", "", ""
    )

    private fun setupToggle() {
        binding.yesOption.label.text = "Stock"
        binding.noOption.label.text = "Crypto"

        // Default selection
        selectedOption = "stock"
        binding.yesOption.box.setBackgroundResource(R.drawable.ic_check_selected)
        binding.noOption.box.setBackgroundResource(R.drawable.ic_check_unselected)

        binding.yesOption.box.setOnClickListener {
            selectedOption = "stock"
            binding.yesOption.box.setBackgroundResource(R.drawable.ic_check_selected)
            binding.noOption.box.setBackgroundResource(R.drawable.ic_check_unselected)
        }

        binding.noOption.box.setOnClickListener {
            selectedOption = "crypto"
            binding.noOption.box.setBackgroundResource(R.drawable.ic_check_selected)
            binding.yesOption.box.setBackgroundResource(R.drawable.ic_check_unselected)
        }
    }


    private fun isEmptyField() : Boolean {
        if (TextUtils.isEmpty(binding.etTitle.text.toString().trim())){
            showToast("Please enter title")
            return false
        }
        if (TextUtils.isEmpty(binding.etTopic.text.toString().trim())){
            showToast("Please select topic")
            return false
        }
        if (TextUtils.isEmpty(binding.etDescription.text.toString().trim())){
            showToast("Please enter description")
            return false
        }
        if (TextUtils.isEmpty(binding.etSymbol.text.toString().trim())){
            showToast("Please enter symbol")
            return false
        }
        return true
    }
    private fun convertImageToMultipart(imageUri: Uri): MultipartBody.Part {
        val file = FileUtil.getTempFile(requireContext(), imageUri)
        return MultipartBody.Part.createFormData(
            "image",
            file!!.name,
            file.asRequestBody("image/png".toMediaTypeOrNull())
        )
    }

    override fun onViewClick(view: View?) {

    }


    private fun getTopicsList() {
        topicList.add(DropDownData("Stocks"))
        topicList.add(DropDownData("Insurance"))
        topicList.add(DropDownData("Retirement"))
        topicList.add(DropDownData("Savings"))
        topicList.add(DropDownData("Investment Management"))
        topicList.add(DropDownData("Child Education"))
        topicList.add(DropDownData("Student Loan Management"))
        topicList.add(DropDownData("Debt Management"))
        topicList.add(DropDownData("Tax Planning"))
        topicList.add(DropDownData("Financial Planning"))
        topicList.add(DropDownData("Wealth Education"))
        topicList.add(DropDownData("Estate Planning"))
        topicList.add(DropDownData("Investor"))
        topicList.add(DropDownData("Venture Capitalist"))
        topicList.add(DropDownData("Small Business"))
        topicList.add(DropDownData("Grants"))
        topicList.add(DropDownData("Loans"))
        topicList.add(DropDownData("Insurance"))
        topicList.add(DropDownData("Annuities"))
    }
}