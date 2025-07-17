package com.tech.young.ui.stream_screen.stream_confirmation

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.dhaval2404.imagepicker.util.FileUtil
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.data.api.Constants
import com.tech.young.data.model.StreamApiResponse
import com.tech.young.data.model.StreamData
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.FragmentStreamConfirmationBinding
import com.tech.young.databinding.ItemLayoutStreamPopupBinding
import com.tech.young.ui.common.CommonActivity
import com.tech.young.ui.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@AndroidEntryPoint
class StreamConfirmationFragment : BaseFragment<FragmentStreamConfirmationBinding>() {

    private val viewModel: StreamConfirmationVM by viewModels()
    private var select = 0
    private var streamData: StreamData? = null


    // adapter
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<String, AdsItemViewBinding>

    private var getList = listOf(
        "", "", "", "", ""
    )

    override fun onCreateView(view: View) {
        // view
        initView()
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
                        "streamPost" ->{
                            val myDataModel : StreamApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    if (myDataModel.data?.post?._id != null){
                                        if (myDataModel.data?.post?.scheduleDate != null){
                                            val intent = Intent(requireContext() , HomeActivity::class.java)
                                            startActivity(intent)
                                            requireActivity().finishAffinity()
                                        }
                                        else{
                                            val intent= Intent(requireContext(), CommonActivity::class.java)
                                            intent.putExtra("from","live_stream")
                                            intent.putExtra("room_id", myDataModel.data?.post?._id)
                                            startActivity(intent)
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
                Status.ERROR ->{

                }
                else ->{

                }
            }
        })
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_stream_confirmation
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
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

                R.id.tvConfirm -> {
                    showStreamDialog()
                }
                R.id.tvReadyToLaunch -> {
                    streamData?.let { data ->
                        val title = data.title
                        val description = data.description
                        val topic = data.topic
                        val scheduleDate = data.scheduleDate
                        val image = data.image

                        // Required fields check (scheduleDate is optional)
                        if (title.isNullOrBlank() || description.isNullOrBlank() || topic.isNullOrBlank()) {
                            Log.w("LaunchError", "Missing required fields: title=$title, description=$description, topic=$topic")
                            return@let
                        }

                        val multipartImage = image?.let { convertImageToMultipart(it) }

                        val requestMap = HashMap<String, RequestBody>().apply {
                            put("title", title.toRequestBody())
                            put("description", description.toRequestBody())
                            put("topic", topic.toRequestBody())
                            put("type", "stream".toRequestBody())

                            // Include scheduleDate only if it's not null or blank
                            if (!scheduleDate.isNullOrBlank()) {
                                put("scheduleDate", scheduleDate.toRequestBody())
                            }
                        }

                        viewModel.streamPost(requestMap, Constants.CREATE_SHARE, multipartImage)
                    } ?: run {
                        Log.e("LaunchError", "StreamData is null")
                    }
                }


            }
        }

    }


    /** handle view **/
    private fun initView() {
        streamData = arguments?.getParcelable("stream_data")
        Log.i("StreamData", "initView: $streamData")

        streamData?.let { data ->
            binding.tvTitle.text = data.title
            binding.tvTopic.text = data.topic
            binding.tvDescription.text = data.description
            binding.ivAdsImage.setImageURI(data.image)

            // Show/hide "Ready to Launch" based on scheduleDate
            binding.tvReadyToLaunch.visibility =
                if (data.scheduleDate != null) View.GONE else View.VISIBLE
        } ?: run {
            Log.w("StreamData", "No stream data found in arguments")
        }

        initAdapter()
    }


    /** dialog **/
    private fun showStreamDialog() {
        val bindingDialog = ItemLayoutStreamPopupBinding.inflate(layoutInflater)

        val dialog = Dialog(requireContext())
        dialog.setContentView(bindingDialog.root)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setDimAmount(0f)
        }
        BindingUtils.statusBarStyleBlack(requireActivity())
        BindingUtils.styleSystemBars(
            requireActivity(), ContextCompat.getColor(requireContext(), R.color.colorSecondary2)
        )

        bindingDialog.tvConfirm.setOnClickListener {
            BindingUtils.statusBarStyleBlack(requireActivity())
            BindingUtils.styleSystemBars(
                requireActivity(), ContextCompat.getColor(requireContext(), R.color.white)
            )
            streamData?.let { data ->
                val title = data.title
                val description = data.description
                val topic = data.topic
                val scheduleDate = data.scheduleDate
                val image = data.image

                // Required fields check (scheduleDate is optional)
                if (title.isNullOrBlank() || description.isNullOrBlank() || topic.isNullOrBlank()) {
                    Log.w("LaunchError", "Missing required fields: title=$title, description=$description, topic=$topic")
                    return@let
                }

                val multipartImage = image?.let { convertImageToMultipart(it) }

                val requestMap = HashMap<String, RequestBody>().apply {
                    put("title", title.toRequestBody())
                    put("description", description.toRequestBody())
                    put("topic", topic.toRequestBody())
                    put("type", "stream".toRequestBody())

                    // Include scheduleDate only if it's not null or blank
                    if (!scheduleDate.isNullOrBlank()) {
                        put("scheduleDate", scheduleDate.toRequestBody())
                    }
                }

                viewModel.streamPost(requestMap, Constants.CREATE_SHARE, multipartImage)
            } ?: run {
                Log.e("LaunchError", "StreamData is null")
            }
            dialog.dismiss()
        }
        bindingDialog.ivCheck.setOnClickListener {
            BindingUtils.statusBarStyleBlack(requireActivity())
            BindingUtils.styleSystemBars(
                requireActivity(), ContextCompat.getColor(requireContext(), R.color.white)
            )
            if (select == 0) {
                select = 1
                bindingDialog.check = true
            } else {
                select = 0
                bindingDialog.check = false
            }

        }
        dialog.setCancelable(false)
        dialog.show()
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

    private fun convertImageToMultipart(imageUri: Uri): MultipartBody.Part {
        val file = FileUtil.getTempFile(requireContext(), imageUri)
        return MultipartBody.Part.createFormData(
            "image",
            file!!.name,
            file.asRequestBody("image/png".toMediaTypeOrNull())
        )
    }

}