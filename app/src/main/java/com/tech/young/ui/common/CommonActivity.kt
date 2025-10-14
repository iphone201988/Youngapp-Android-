package com.tech.young.ui.common

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.tech.young.R
import com.tech.young.base.BaseActivity
import com.tech.young.base.BaseViewModel
import com.tech.young.base.utils.BindingUtils
import com.tech.young.data.DropDownData
import com.tech.young.data.UserData
import com.tech.young.data.model.GetProfileApiResponse
import com.tech.young.data.model.ShareData
import com.tech.young.data.model.StreamData
import com.tech.young.databinding.ActivityCommonBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CommonActivity : BaseActivity<ActivityCommonBinding>()  {
    private val viewModel:CommonVM by viewModels()
    private val navController: NavController by lazy {
        (supportFragmentManager.findFragmentById(R.id.mainNavigationHost) as NavHostFragment).navController
    }
    override fun getLayoutResource(): Int {
        return R.layout.activity_common
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
        BindingUtils.statusBarStyleBlack(this)
        BindingUtils.styleSystemBars(this, getColor(R.color.white))
        // view
        initView()
        // click
        initOnClick()
        // observer
        initObserver()
    }

    /** handle view **/
    private fun initView(){
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                navController.graph =
                    navController.navInflater.inflate(R.navigation.common_navigation).apply {
                        val message = intent.getStringExtra("from")
                        if (message != null) {
                            val bundle=Bundle()
                            when (message) {
                                "view_more"->{
                                    setStartDestination(R.id.fragmentViewMore)
                                }
                                "view_message" -> {
                                    val threadId = intent.getStringExtra("threadId")
                                    val userData = intent.getParcelableExtra<UserData>("userData")

                                    bundle.putString("threadId", threadId)
                                    bundle.putParcelable("userData", userData)

                                    setStartDestination(R.id.fragmentViewMessage)
                                }
                                "new_message" -> {
                                    setStartDestination(R.id.fragmentNewMessage)
                                }
                                "edit_profile" -> {
                                    val profileData=intent.getParcelableExtra<GetProfileApiResponse.GetProfileApiResponseData>("profileData")
                                    bundle.putParcelable("profileData", profileData)
                                    setStartDestination(R.id.fragmentEditProfile)
                                }
                                "professional_info" -> {
                                    val profileData=intent.getParcelableExtra<GetProfileApiResponse.GetProfileApiResponseData>("profileData")
                                    bundle.putParcelable("profileData", profileData)
                                    setStartDestination(R.id.fragmentProfessionalInfo)
                                }
                                "personal_preference" -> {
                                    val profileData=intent.getParcelableExtra<GetProfileApiResponse.GetProfileApiResponseData>("profileData")
                                    bundle.putParcelable("profileData", profileData)
                                    setStartDestination(R.id.fragmentPersonalPreference)
                                }
                                "account_verify" -> {
                                    val url = intent.getStringExtra("url")
                                    bundle.putString("url",url)
                                    setStartDestination(R.id.fragmentDigitVerification)
                                }
                                "normal_family"-> {
                                    val profileData =
                                        intent.getParcelableExtra<GetProfileApiResponse.GetProfileApiResponseData>(
                                            "profileData"
                                        )
                                    bundle.putParcelable("profileData", profileData)
                                    setStartDestination(R.id.fragmentFamilyDetails)
                                }
                                "normal_finance_detail"->{
                                    val profileData=intent.getParcelableExtra<GetProfileApiResponse.GetProfileApiResponseData>("profileData")
                                    bundle.putParcelable("profileData", profileData)
                                    setStartDestination(R.id.fragmentFinanceInfo)
                                }
                                "normal_investment"->{
                                    val profileData=intent.getParcelableExtra<GetProfileApiResponse.GetProfileApiResponseData>("profileData")
                                    bundle.putParcelable("profileData", profileData)
                                    setStartDestination(R.id.fragmentInvestmentInfo)
                                }
                                "payment_details" -> {
                                    val userId = intent.getStringExtra("userId")
                                    bundle.putString("userId",userId)
                                    setStartDestination(R.id.fragmentPaymentDetails)
                                }
                                "payment_history" -> {
                                    setStartDestination(R.id.fragemntPaymentHistory)
                                }
                                "payment_setup" -> {
                                    setStartDestination(R.id.fragmentPaymentSetUp)
                                }
                                "report_user" -> {
                                    val userId=intent.getStringExtra("userId").toString()
                                    val reportType = intent.getStringExtra("reportType")
                                    bundle.putString("userId",userId)
                                    bundle.putString("reportType",reportType)
                                    setStartDestination(R.id.fragmentReport)
                                }
                                "user_profile"->{
                                    val userId=intent.getStringExtra("userId").toString()
                                    bundle.putString("userId",userId)
                                    setStartDestination(R.id.fragmentUserProfile)
                                }
                                "common_share"->{
                                    setStartDestination(R.id.fragmentCommonShare)
                                }
                                "share_confirmation"->{
                                    val shareData = intent.getParcelableExtra<ShareData>("share_data")
                                    Log.i("dfdsfsdfs", "initView: $shareData")
                                    bundle.putParcelable("share_data", shareData)
                                    setStartDestination(R.id.fragmentShareConfirmation)
                                }
                                "common_stream"->{

                                    setStartDestination(R.id.fragmentCommonStream)
                                }
                                "stream_confirmation"->{
                                    val streamData = intent.getParcelableExtra<StreamData>("stream_data")
                                    Log.i("dfdsfsdfs", "initView: $streamData")
                                    bundle.putParcelable("stream_data", streamData)
                                    setStartDestination(R.id.fragmentStreamConfirmation)
                                }
                                "vault_confirmation"->{
                                    setStartDestination(R.id.fragmentVaultConfirmation)
                                }
                                "vault_room"->{
                                    val vaultID = intent.getStringExtra("vaultId")
                                    bundle.putString("vaultId", vaultID)
                                    setStartDestination(R.id.fragmentVaultRoom)
                                }
                                "common_vault"->{
                                    setStartDestination(R.id.fragmentCommonVault)
                                }
                                "business_info"->{
                                    val profileData=intent.getParcelableExtra<GetProfileApiResponse.GetProfileApiResponseData>("profileData")
                                    bundle.putParcelable("profileData", profileData)
                                    setStartDestination(R.id.fragmentBusinessInfo)
                                }
                                "form_upload"->{
                                    val profileData=intent.getParcelableExtra<GetProfileApiResponse.GetProfileApiResponseData>("profileData")
                                    bundle.putParcelable("profileData", profileData)
                                    setStartDestination(R.id.fragmentFormUpload)
                                }
                                "people"->{
                                    val selectedCategory = intent.getParcelableArrayListExtra<DropDownData>("selectedCategory")
                                    bundle.putParcelableArrayList("selectedCategory", selectedCategory)
                                    setStartDestination(R.id.fragmentPeople)
                                }
                                "exchange_share_details" ->{
                                    val userId=intent.getStringExtra("userId").toString()
                                    bundle.putString("userId",userId)
                                    setStartDestination(R.id.fragmentExchangeShareDetails)
                                }
                                "news_details" ->{
                                    val url=intent.getStringExtra("url").toString()
                                    bundle.putString("url",url)
                                    setStartDestination(R.id.fragmentNewsDetails)
                                }
                                "live_stream" ->{
                                    val roomId=intent.getStringExtra("room_id").toString()
                                    bundle.putString("room_id",roomId)
                                    setStartDestination(R.id.fragmentLiveStream)

                                }
                                "consumer_live_stream" ->{
                                    val roomId=intent.getStringExtra("streamId").toString()
                                    bundle.putString("streamId",roomId)
                                    setStartDestination(R.id.fragmentConsumerLiveStream)
                                }

                                "stream_detail" ->{
                                    val streamId=intent.getStringExtra("streamId").toString()
                                    bundle.putString("streamId",streamId)
                                    setStartDestination(R.id.fragmentStreamDetail)
                                }
                                "recorded_stream" ->{
                                    val streamId=intent.getStringExtra("streamUrl").toString()
                                    bundle.putString("streamUrl",streamId)
                                    setStartDestination(R.id.fragmentRecordedStream)
                                }
                                "single_news"->{
                                    val linkUrl=intent.getStringExtra("linkUrl").toString()
                                    bundle.putString("linkUrl",linkUrl)
                                    setStartDestination(R.id.fragmentNewsWeb)
                                }
                                "exchange" ->{
                                    setStartDestination(R.id.fragmentExchange)
                                }
                                "ecosystem" ->{
                                    setStartDestination(R.id.fragmentEcosystem)

                                }
                                "myShare" ->{
                                    val role  = intent.getStringExtra("role").toString()
                                    var id =  intent.getStringExtra("userId")

                                    bundle.putString("role",role)
                                    bundle.putString("userId",id)
                                    setStartDestination(R.id.fragmentMyShare)
                                }
                                "image" ->{
                                    val url  = intent.getStringExtra("url").toString()
                                    bundle.putString("url", url)
                                    setStartDestination(R.id.fragmentImageZoom)
                                }
                            }
                            navController.setGraph(this, bundle)
                        }
                    }
            }
        }

    }

    /**handle click **/
    private fun initOnClick(){

    }

    /**handle observer **/
    private fun initObserver(){

    }

}