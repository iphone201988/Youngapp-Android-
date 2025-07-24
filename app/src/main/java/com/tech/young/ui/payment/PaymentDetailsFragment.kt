package com.tech.young.ui.payment

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.view.View
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.BaseCustomDialog
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showToast
import com.tech.young.data.api.Constants
import com.tech.young.data.api.SimpleApiResponse
import com.tech.young.data.model.DownloadHistoryApiResponse
import com.tech.young.data.model.GetAdsAPiResponse
import com.tech.young.data.model.GetProfileApiResponse
import com.tech.young.data.model.UpdateUserProfileResponse
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.FragmentPaymentDetailsBinding
import com.tech.young.databinding.ItemLayoutDeactivateAccountBinding
import com.tech.young.databinding.ItemLayoutDeleteAccountPopupBinding
import com.tech.young.databinding.ItemLayoutLogoutPopupBinding
import com.tech.young.ui.MySplashActivity
import com.tech.young.ui.common.CommonActivity
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException

@AndroidEntryPoint
class PaymentDetailsFragment : BaseFragment<FragmentPaymentDetailsBinding>() , BaseCustomDialog.Listener {
    private val viewModel: PaymentVM by viewModels()

    private var userId : String ? = null

    private lateinit var deactivateAccount : BaseCustomDialog<ItemLayoutDeactivateAccountBinding>
    private lateinit var deleteAccount : BaseCustomDialog<ItemLayoutDeleteAccountPopupBinding>

    // adapter
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<GetAdsAPiResponse.Data.Ad, AdsItemViewBinding>
    override fun onCreateView(view: View) {
        // view
        initView()
        initBottomSheet()
        // click
        initOnClick()
        // observer
        initObserver()
    }

    private fun initBottomSheet() {
        deactivateAccount = BaseCustomDialog(requireContext(),R.layout.item_layout_deactivate_account,this)
        deleteAccount = BaseCustomDialog(requireContext(),R.layout.item_layout_delete_account_popup, this)
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_payment_details
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /** handle view **/
    private fun initView() {

        viewModel.getProfile(Constants.GET_USER_PROFILE)
        viewModel.getAds(Constants.GET_ADS)
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

                R.id.ivPayment -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("from", "payment_history")
                    startActivity(intent)
                }

                R.id.ivAccount, R.id.ivPay, R.id.ivPlan -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("from", "payment_setup")
                    startActivity(intent)
                }
                R.id.tvDownload ->{
                    viewModel.downloadHistory(Constants.DOWNLOAD_HISTORY)
                }
                R.id.tvDeactivate ->{
                    deactivateAccount.show()
                }
                R.id.tvDeleteAccount ->{
                    deleteAccount.show()
                }
            }
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
                        "getProfile" ->{
                            val myDataModel : GetProfileApiResponse? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                   binding.bean = myDataModel.data
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
                        "downloadHistory" ->{
                            val myDataModel : DownloadHistoryApiResponse  ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    val jsonString = it.data.toString()
                                    generatePdfFromRawJson(requireContext(), jsonString)
                                }
                            }
                        }
                        "updateProfile" -> {
                            try {
                                val myDataModel: UpdateUserProfileResponse? =
                                    BindingUtils.parseJson(it.data.toString())
                                if (myDataModel != null) {
                                    if (myDataModel.data != null) {
                                        showToast(myDataModel.message.toString())
                                        sharedPrefManager.clear()
                                        val intent = Intent(requireContext(), MySplashActivity::class.java)
                                        startActivity(intent)
                                        requireActivity().finishAffinity()
                                    }
                                    else{
                                        showToast(myDataModel.message.toString())
                                    }
                                }
                                else{
                                    showToast(it.message.toString())
                                }
                            }
                            catch (e:Exception){
                                e.printStackTrace()
                            }

                        }
                        "deleteAccount" ->{
                            val myDataModel : SimpleApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                sharedPrefManager.clear()
                                val intent = Intent(requireContext(), MySplashActivity::class.java)
                                startActivity(intent)
                                requireActivity().finishAffinity()
                            }

                        }
                    }
                }
                Status.ERROR ->{
                    hideLoading()
                    showToast(it.message.toString())
                }
                else ->{}
            }
        })
    }


    // convert json data to pdf

    fun generatePdfFromRawJson(context: Context, jsonString: String) {
        try {
            val pdfDocument = PdfDocument()
            val paint = Paint()
            paint.textSize = 12f

            val pageWidth = 595
            val pageHeight = 842
            var pageNumber = 1
            var yPosition = 40f
            val lineSpacing = 20f

            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            // Split JSON into lines that fit the page width
            val wrappedLines = jsonString.chunked(100) // Adjust chunk size if needed

            for (line in wrappedLines) {
                if (yPosition > pageHeight - 40) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = 40f
                }

                canvas.drawText(line, 20f, yPosition, paint)
                yPosition += lineSpacing
            }

            pdfDocument.finishPage(page)

            // Save the PDF file
            val file = File(context.cacheDir, "raw_json_data.pdf")
            try {
                file.outputStream().use { pdfDocument.writeTo(it) }
            } catch (e: IOException) {
                e.printStackTrace()
                return
            } finally {
                pdfDocument.close()
            }

            // Share the PDF
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share JSON PDF"))
        }catch (e : Exception){
            e.printStackTrace()
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

    override fun onViewClick(view: View?) {
        when(view?.id){
            R.id.tvDeactivate ->{
                val data = HashMap<String, RequestBody>()
                data["isDeactivatedByUser"] = "true".toRequestBody("text/plain".toMediaTypeOrNull())
                viewModel.updateProfile(Constants.UPDATE_USER, data,null,null)

            }
            R.id.tvNoDeactivate ->{
                deactivateAccount.dismiss()
            }
            R.id.tvDelete ->{
                viewModel.deleteAccount(Constants.DELETE_ACCOUNT)
                deleteAccount.dismiss()
            }
            R.id.tvNoDelete ->{
                deleteAccount.dismiss()
            }
        }

    }

}