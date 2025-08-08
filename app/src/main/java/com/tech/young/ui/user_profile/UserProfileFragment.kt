package com.tech.young.ui.user_profile

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Resource
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showToast
import com.tech.young.data.UserData
import com.tech.young.data.api.Constants
import com.tech.young.data.api.SimpleApiResponse
import com.tech.young.data.model.AddRatingApiResponse
import com.tech.young.data.model.GetAdsAPiResponse
import com.tech.young.data.model.GetOtherUserProfileData
import com.tech.young.data.model.GetOtherUserProfileModel
import com.tech.young.data.model.GetOtherUserProfileModelData
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.DialogRatingBinding
import com.tech.young.databinding.FragmentUserProfileBinding
import com.tech.young.databinding.ShareProfileItemViewBinding
import com.tech.young.databinding.YourPhotosItemViewBinding
import com.tech.young.ui.common.CommonActivity
import com.tech.young.ui.home.HomeActivity
import com.tech.young.ui.share_screen.CommonShareFragment
import com.tech.young.ui.stream_screen.CommonStreamFragment
import com.tech.young.ui.vault_screen.CommonVaultFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserProfileFragment : BaseFragment<FragmentUserProfileBinding>() {
    private val viewModel: UserProfileVM by viewModels()

    // adapter
    private lateinit var yourImageAdapter: SimpleRecyclerViewAdapter<String, YourPhotosItemViewBinding>
    private lateinit var shareAdapter: SimpleRecyclerViewAdapter<String, ShareProfileItemViewBinding>
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<GetAdsAPiResponse.Data.Ad, AdsItemViewBinding>
    private var userId:String?=null
    private var chatId : String ? = null
    private var userData : GetOtherUserProfileData? = null
    private var select = 0
    override fun onCreateView(view: View) {
        // view
        initView()
        // click
        initOnClick()
        // observer
        initObserver()

        binding.ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            if (fromUser) {
//                binding.tvAverageRating.text = "($rating)"
                Handler(Looper.getMainLooper()).postDelayed({
                    setRating(rating)
                }, 1000)
                Log.i("RatingBar", "User selected rating: $rating")
                // Handle the rating value
            }
        }
    }

    private fun setRating(rating: Float) {
        if (userId != null){
            val data = HashMap<String,Any>()
            data["ratings"]  = rating
            data["type"] = "user"
            data["id"] =  userId.toString()

            viewModel.rating(data,Constants.RATING)
        }
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_user_profile
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /** handle view **/
    private fun initView() {
        userId= arguments?.getString("userId").toString()
        apiCall()
        viewModel.getAds(Constants.GET_ADS)
        // adapter
        initAdapter()

        binding.includeShare.tabShare.setOnClickListener {
//            val intent = Intent(requireContext(),CommonActivity::class.java).putExtra("from","common_share")
//            startActivity(intent)

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, CommonShareFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.includeShare.tabStream.setOnClickListener {
//            val intent = Intent(requireContext(),CommonActivity::class.java).putExtra("from","common_stream")
//            startActivity(intent)

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, CommonStreamFragment())
                .addToBackStack(null)
                .commit()
        }


        binding.includeShare.tabVault.setOnClickListener {
//            val intent = Intent(requireContext(),CommonActivity::class.java).putExtra("from","common_vault")
//            startActivity(intent)

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, CommonVaultFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity()) {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }

                R.id.tvFollow -> {
                    if (userId != null){
                        viewModel.followUnfollowUser(Constants.FOLLOW_UNFOLLOW+userId)
                    }

                }

                R.id.tvFollowingUser -> {
                    if (userId != null){
                        viewModel.followUnfollowUser(Constants.FOLLOW_UNFOLLOW+userId)
                    }

                }

                R.id.ivCheck, R.id.tvDesc -> {
                    viewModel.updateCustomer(Constants.UPDATE_CUSTOMER + userId)
                }

                R.id.tvMessage -> {
                        val chatUser = UserData(
                            _id = userData?._id,
                            profileImage = userData?.profileImage ,
                            role = userData?.role,
                            username = userData?.username
                        )



                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("from", "view_message")
                    intent.putExtra("threadId",chatId)
                    intent.putExtra("userData", chatUser)
                    startActivity(intent)
                }

                R.id.ivReport , R.id.tvReportUser-> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("from", "report_user")
                    intent.putExtra("userId", userId)
                    intent.putExtra("reportType","user")
                    startActivity(intent)
                }

                R.id.icRatingArrow -> {
                    showRatingDialog()
                }
//                R.id.actionToggleBtn ->{
//                    HomeActivity.navClick.postValue(Resource.success("clicked",true))
//                }



            }
        }

    }

    /** handle observer **/
    private fun initObserver() {
        viewModel.userProfileObserver.observe(requireActivity()){
            when(it?.status){
                Status.LOADING -> showLoading()
                Status.SUCCESS -> {
                    hideLoading()
                    when(it.message){
                        "getUserProfile"->{
                            try {
                                val myDataModel : GetOtherUserProfileModel? = BindingUtils.parseJson(it.data.toString())
                                if (myDataModel != null){
                                    if (myDataModel.data != null){
                                        userData = myDataModel.data.user
                                        binding.bean = myDataModel.data.user
                                        chatId= myDataModel.data.user?.chatId
                                        if (myDataModel.data.user?.isFollowed == true){
                                            binding.tvFollow.visibility = View.INVISIBLE
                                            binding.tvFollowingUser.visibility = View.VISIBLE
                                            binding.tvMessage.visibility = View.VISIBLE
                                        }
                                        else{
                                            binding.tvFollow.visibility = View.VISIBLE
                                            binding.tvFollowingUser.visibility = View.GONE
                                            binding.tvMessage.visibility = View.GONE
                                        }
                                        yourImageAdapter.list = myDataModel.data?.user?.additionalPhotos
                                        yourImageAdapter.notifyDataSetChanged()

                                    }
                                }
                            }
                            catch (e:Exception){
                                e.printStackTrace()
                            }
                        }
                        "updateCustomer" -> {
                            val myDataModel: SimpleApiResponse? = BindingUtils.parseJson(it.data.toString())
                            myDataModel?.let { data ->
                                showToast(data.message.toString())
                                apiCall()
                            }
                        }
                        "followUnfollowUser" ->{
                            val myDataModel : SimpleApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel !=  null){
                                showToast(myDataModel.message.toString())
                                apiCall()

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
                        "rating" ->{
                            hideLoading()
                            val myDataModel : AddRatingApiResponse? =  BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){

                                }
                            }
                        }

                    }
                }
                Status.ERROR -> {
                    hideLoading()
                    try {
                        showToast(it.message.toString())
                    }
                    catch (e:Exception){
                        e.printStackTrace()
                    }
                }
                else -> {

                }
            }
        }
    }

    /** handle adapter **/
    private fun initAdapter() {
        yourImageAdapter = SimpleRecyclerViewAdapter(
            R.layout.your_photos_item_view, BR.bean
        ) { v, m, pos ->
            when (v.id) {

            }
        }
        yourImageAdapter.list = getList
        binding.rvYourPhotos.adapter = yourImageAdapter


        shareAdapter =
            SimpleRecyclerViewAdapter(R.layout.share_profile_item_view, BR.bean) { v, m, pos ->
                when (v.id) {

                }
            }

        shareAdapter.list = getList
        binding.rvShare.adapter = shareAdapter


        adsAdapter = SimpleRecyclerViewAdapter(R.layout.ads_item_view, BR.bean) { v, m, pos ->
            when (v.id) {

            }
        }
        binding.rvAds.adapter = adsAdapter
    }

    private var getList = listOf(
        "", "", "", "", ""
    )

    /** handle dialog **/
    private fun showRatingDialog() {
        val bindingDialog = DialogRatingBinding.inflate(layoutInflater)

        val dialog = Dialog(requireContext())
        dialog.setContentView(bindingDialog.root)

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setDimAmount(0f) // We use purple background in the layout instead
        }
        BindingUtils.statusBarStyleBlack(requireActivity())
        BindingUtils.styleSystemBars(
            requireActivity(), ContextCompat.getColor(requireContext(), R.color.colorSecondary2)
        )

        bindingDialog.close.setOnClickListener {
            BindingUtils.statusBarStyleBlack(requireActivity())
            BindingUtils.styleSystemBars(
                requireActivity(), ContextCompat.getColor(requireContext(), R.color.white)
            )
            dialog.dismiss()
        }

        // Example rating data: 5★ to 1★
        val ratingData = listOf(1985, 356, 130, 90, 33)
        val total = ratingData.sum().toFloat()

        val container = bindingDialog.ratingDistributionContainer
        container.removeAllViews()

        for (i in 0 until 5) {
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_rating_distribution, container, false)

            val starText = itemView.findViewById<TextView>(R.id.star_text)
            val starCount = itemView.findViewById<TextView>(R.id.star_count)
            val starProgress = itemView.findViewById<ProgressBar>(R.id.star_progress)

            val percent = (ratingData[i] / total * 100).toInt()

            starText.text = "${5 - i}★"
            starCount.text = ratingData[i].toString()
            starProgress.progress = percent

            container.addView(itemView)
        }

        dialog.show()
    }

    /** api call **/
    private fun apiCall(){
        if (userId!=null) {
            val data = HashMap<String, Any>()
            data["userId"]=userId.toString()
            viewModel.getUserProfile(Constants.GET_USER_PROFILE,data)
        }

    }
}