package com.tech.young.ui.payment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.util.Log
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
import com.tech.young.data.model.GetProfileApiResponse.GetProfileApiResponseData
import com.tech.young.data.model.UpdateUserProfileResponse
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.FragmentPaymentDetailsBinding
import com.tech.young.databinding.ItemLayoutDeactivateAccountBinding
import com.tech.young.databinding.ItemLayoutDeleteAccountPopupBinding
import com.tech.young.databinding.ItemLayoutLogoutPopupBinding
import com.tech.young.ui.MySplashActivity
import com.tech.young.ui.change_password.ChangePasswordFragment
import com.tech.young.ui.common.CommonActivity
import com.tech.young.ui.my_profile_screens.common_ui.EditProfileDetailFragment
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class PaymentDetailsFragment : BaseFragment<FragmentPaymentDetailsBinding>() , BaseCustomDialog.Listener {
    private val viewModel: PaymentVM by viewModels()

    private var userId : String ? = null

    private var profileData: GetProfileApiResponseData?=null


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
                    val fragment = EditProfileDetailFragment().apply {
                        arguments = Bundle().apply {
                            putParcelable("profileData", profileData)
                        }
                    }

                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, fragment)
                        .addToBackStack(null)
                        .commit()
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
                R.id.tvChangePassword ->{
                    val fragment = ChangePasswordFragment().apply {

                    }

                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, fragment)
                        .addToBackStack(null)
                        .commit()
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
                                    profileData = myDataModel.data
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
                                    generatePdfFromRawJson(requireContext(), jsonString, profileData)
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

    @SuppressLint("SimpleDateFormat")
    fun generatePdfFromRawJson(context: Context, jsonString: String, userData: GetProfileApiResponseData?) {
        try {
            // 1️⃣ Pretty-print JSON
            val prettyJson = try {
                if (jsonString.trim().startsWith("[")) {
                    JSONArray(jsonString).toString(4)
                } else {
                    JSONObject(jsonString).toString(4)
                }
            } catch (e: Exception) {
                jsonString
            }

            // 2️⃣ Create PDF setup
            val pdfDocument = PdfDocument()
            val paint = Paint().apply { textSize = 12f }

            val pageWidth = 595
            val pageHeight = 842
            var pageNumber = 1
            var yPosition = 40f
            val lineSpacing = 20f

            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            // 3️⃣ Wrap text to fit width
            val wrappedLines = mutableListOf<String>()
            var start = 0
            while (start < prettyJson.length) {
                val count = paint.breakText(prettyJson, start, prettyJson.length, true, (pageWidth - 40).toFloat(), null)
                wrappedLines.add(prettyJson.substring(start, start + count))
                start += count
            }

            // 4️⃣ Write lines across multiple pages
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

            // 5️⃣ Generate dynamic file name
            val firstName = userData?.user?.firstName?.takeIf { it.isNotBlank() } ?: ""
            val lastName = userData?.user?.lastName?.takeIf { it.isNotBlank() } ?: ""
            val displayName = listOf(firstName, lastName)
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .ifEmpty { "User" }

            val formattedDate = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()).format(Date())
            val safeFileName = "$displayName - $formattedDate.pdf".replace(",", "")

            // 6️⃣ Save PDF to Downloads
            val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsFolder.exists()) downloadsFolder.mkdirs()

            val file = File(downloadsFolder, safeFileName)
            file.outputStream().use { pdfDocument.writeTo(it) }
            pdfDocument.close()

            Log.i("PDF_FILE", "Saved PDF: $file")
            Log.d("PDF_SIZE", "PDF size = ${file.length()} bytes")

            // 7️⃣ Share PDF
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share JSON PDF"))

        } catch (e: Exception) {
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