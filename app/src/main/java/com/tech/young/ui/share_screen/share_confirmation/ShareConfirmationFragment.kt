package com.tech.young.ui.share_screen.share_confirmation

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
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
import com.tech.young.base.utils.showCustomToast
import com.tech.young.base.utils.showToast
import com.tech.young.data.api.Constants
import com.tech.young.data.model.CreatePostApiResponse
import com.tech.young.data.model.GetAdsAPiResponse
import com.tech.young.data.model.ShareData
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.FragmentShareConfirmationBinding
import com.tech.young.ui.ecosystem.EcosystemFragment
import com.tech.young.ui.exchange.ExchangeFragment
import com.tech.young.ui.home.HomeActivity
import com.tech.young.ui.share_screen.CommonShareFragment
import com.tech.young.ui.stream_screen.CommonStreamFragment
import com.tech.young.ui.vault_screen.CommonVaultFragment
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@AndroidEntryPoint
class ShareConfirmationFragment : BaseFragment<FragmentShareConfirmationBinding>() {

    private val viewModel: ShareConfirmationVM by viewModels()

    private var shareData : ShareData ?= null

    // adapter
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<GetAdsAPiResponse.Data.Ad, AdsItemViewBinding>

    private var getList = listOf(
        "", "", "", "", ""
    )

    override fun onCreateView(view: View) {
        // view
        initView()

        viewModel.getAds(Constants.GET_ADS)
        // click
        initOnClick()



        binding.shareLayout.tabShare.setOnClickListener {


//            val intent = Intent(requireContext(), CommonActivity::class.java).putExtra("from","common_share")
//            startActivity(intent)

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, CommonShareFragment())
                .addToBackStack(null)
                .commit()

        }
        binding.shareLayout.tabStream.setOnClickListener {
//            val intent = Intent(requireContext(), CommonActivity::class.java).putExtra("from","common_stream")
//            startActivity(intent)
            if (sharedPrefManager.isSubscribed()){
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, CommonStreamFragment())
                    .addToBackStack(null)
                    .commit()
            }else{
                showCustomToast("Please subscribe to access this feature. Go to Profile Details > Account Details > Upgrade Plan.  ")

            }

        }
        binding.shareLayout.tabVault.setOnClickListener {
//            val intent = Intent(requireContext(), CommonActivity::class.java).putExtra("from","common_vault")
//            startActivity(intent)
            if (sharedPrefManager.isSubscribed()){
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, CommonVaultFragment())
                    .addToBackStack(null)
                    .commit()
            }else{
                showCustomToast("Please subscribe to access this feature. Go to Profile Details > Account Details > Upgrade Plan.  ")

            }

        }

        binding.tabLayoutBottom.tabExchange.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, ExchangeFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.tabLayoutBottom.tabEcosystem.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, EcosystemFragment())
                .addToBackStack(null)
                .commit()
        }


    }


    override fun getLayoutResource(): Int {
        return R.layout.fragment_share_confirmation
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /** handle view **/
    private fun initView() {

        shareData = arguments?.getParcelable("share_data")
        Log.i("StreamData", "initView: $shareData")

        shareData?.let { data ->
            binding.tvTitle.text = data.title
            binding.tvTopic.text = data.topic
            binding.tvDescription.text = data.description
            if (data.image != null){
                binding.ivAdsImage.visibility = View.VISIBLE
                binding.ivAdsImage.setImageURI(data.image)
            }
            else{
                binding.ivAdsImage.visibility = View.GONE
            }

            binding.etSymbol.text = data.symbolValue
            binding.tvSymbol.text = data.symbol

        } ?: run {
            Log.w("StreamData", "No stream data found in arguments")
        }

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
                        "getAds" ->{
                            val myDataModel : GetAdsAPiResponse? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    adsAdapter.list = myDataModel.data?.ads
                                }
                            }
                        }
                        "sharePost" ->{
                            val myDataModel : CreatePostApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    showToast(myDataModel.message.toString())
                                    val intent = Intent(requireContext(), HomeActivity::class.java)
                                    startActivity(intent)
                                   // requireActivity().onBackPressedDispatcher.onBackPressed()
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

    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity()) {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                R.id.tvConfirm ->{

                    val multipartImage = shareData?.image?.let { convertImageToMultipart(it) }
                    val data = HashMap<String, RequestBody>()
                    data["title"] = shareData?.title.toString().toRequestBody()
                    data["description"] = shareData?.description.toString().toRequestBody()
                    data["topic"] = shareData?.topic.toString().toRequestBody()
                    shareData?.symbol?.takeIf { it.isNotEmpty() }?.let {
                        data["symbol"] = it.lowercase().toRequestBody()
                    }
                    shareData?.symbolValue?.takeIf { it.isNotEmpty() }?.let {
                        data["symbolValue"] = it.toRequestBody()
                    }
                    data["type"] = "share".toRequestBody()
                    viewModel.sharePost(data, Constants.CREATE_SHARE,multipartImage)
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


    private fun convertImageToMultipart(imageUri: Uri): MultipartBody.Part {
        val file = FileUtil.getTempFile(requireContext(), imageUri)
        return MultipartBody.Part.createFormData(
            "image",
            file!!.name,
            file.asRequestBody("image/png".toMediaTypeOrNull())
        )
    }
}