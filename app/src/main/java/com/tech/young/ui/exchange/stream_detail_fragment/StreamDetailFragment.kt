package com.tech.young.ui.exchange.stream_detail_fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
import com.tech.young.data.model.AddCommentApiResponse
import com.tech.young.data.model.AddRatingApiResponse
import com.tech.young.data.model.CommentLikeDislikeApiResponse
import com.tech.young.data.model.GetAdsAPiResponse
import com.tech.young.data.model.GetCommentApiResponsePost
import com.tech.young.data.model.StreamDetailApiResponse
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.FragmentStreamDetailBinding
import com.tech.young.databinding.ItemLayoutPostCommentBinding
import com.tech.young.ui.common.CommonActivity
import com.tech.young.ui.ecosystem.EcosystemFragment
import com.tech.young.ui.exchange.ExchangeFragment
import com.tech.young.ui.exchange.ExchangeVM
import com.tech.young.ui.home.HomeActivity
import com.tech.young.ui.user_profile.UserProfileFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.Date


@AndroidEntryPoint
class StreamDetailFragment : BaseFragment<FragmentStreamDetailBinding>() {

    private val viewModel: ExchangeVM by viewModels()
    private var streamId : String  ? = null
    private var streamUrl : String ? = null
    private var scheduleDate : String? = null
    private var currentUserId : String? = null
    private var ownerUserId : String? = null

    private var id : String ? = null
    private var name : String ? = null
    private lateinit var commentAdapter: SimpleRecyclerViewAdapter<GetCommentApiResponsePost.Data.Comment, ItemLayoutPostCommentBinding>

    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<GetAdsAPiResponse.Data.Ad, AdsItemViewBinding>


    private var getList = listOf(
        "", "", "", "", ""
    )
    override fun onCreateView(view: View) {
        initView()
        initOnClick()
        initAdapter()
        initObserver()


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

        binding.ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            if (fromUser) {
                binding.ratings.text = "($rating)"
                Handler(Looper.getMainLooper()).postDelayed({
                    setRating(rating)
                }, 1000)
                Log.i("RatingBar", "User selected rating: $rating")
                // Handle the rating value
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.nestedScrollView) { view, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            // Only adjust padding if keyboard is visible
            view.setPadding(0, 0, 0, imeHeight)
            insets
        }

    }

    private fun setRating(rating: Float) {
        if (streamId != null){
            val data = HashMap<String,Any>()
            data["ratings"]  = rating
            data["type"] = "stream"
            data["id"] =  streamId.toString()

            viewModel.rating(data,Constants.RATING)
        }
    }

    private fun initAdapter() {
        commentAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_post_comment,
            BR.bean){ v, m, pos ->
            when(v.id){
                R.id.likeBtn ->{
                    viewModel.likeDislikeComment(Constants.LIKE_DISLIKE_COMMENT+m._id)
                }
                R.id.profileImage, R.id.name ->{
                    val bundle = Bundle().apply {
                        putString("from", "user_profile")
                        putString("userId", m?.userId?._id) // assuming m._id is a String
                    }
//                    val name = m.firstName + " " + m.lastName  // ← add space here
                    val name  = m?.userId?.firstName + " " + m?.userId?.lastName
                    HomeActivity.userName  = name
                    val userProfileFragment = UserProfileFragment().apply {
                        arguments = bundle
                    }

                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, userProfileFragment)
                        .addToBackStack(null)
                        .commit()
                }
            }

        }
        binding.rvComments.adapter = commentAdapter
        commentAdapter.notifyDataSetChanged()


        adsAdapter = SimpleRecyclerViewAdapter(R.layout.ads_item_view, BR.bean) { v, m, pos ->
            when (v.id) {

            }
        }
        binding.rvAds.adapter = adsAdapter

    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer {
            when(it?.id){
                R.id.ivBack ->{
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                R.id.playBtn -> {
                    when {
                        scheduleDate != null -> {
                            // Stream is scheduled in future
                            Toast.makeText(requireContext(), "This stream is scheduled", Toast.LENGTH_SHORT).show()
                        }

                        !streamUrl.isNullOrEmpty() -> {
                            // Prefer recorded stream
                            val intent = Intent(requireContext(), CommonActivity::class.java).apply {
                                putExtra("from", "recorded_stream")
                                putExtra("streamUrl", streamUrl)
                            }
                            startActivity(intent)
                        }

                        !streamId.isNullOrEmpty() -> {
                            // Fallback to live stream via streamId
                            if (currentUserId.equals(ownerUserId)){
                                val intent= Intent(requireContext(), CommonActivity::class.java)
                                intent.putExtra("from","live_stream")
                                intent.putExtra("room_id",streamId)
                                startActivity(intent)
                            }else{
                                val intent = Intent(requireContext(), CommonActivity::class.java).apply {
                                    putExtra("from", "consumer_live_stream")
                                    putExtra("streamId", streamId)
                                }
                                startActivity(intent)
                            }

                        }

                        else -> {
                            Toast.makeText(requireContext(), "No valid stream available", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                R.id.ivSendChat ->{
                    if (TextUtils.isEmpty(binding.etChat.text.toString().trim())){
                        showToast("Please enter message")
                    }
                    else{
                        val data = HashMap<String,Any>()
                        data["id"] =  streamId.toString()
                        data["comment"] = binding.etChat.text.toString().trim()
                        data["type"] = "stream"
                        viewModel.addComment(data,Constants.ADD_COMMENT)
                        binding.etChat.setText("")
                    }
                }

                R.id.profileImage , R.id.tvUserName ->{
                    val bundle = Bundle().apply {
                        putString("from", "user_profile")
                        putString("userId", id) // assuming m._id is a String
                    }
//                    val name = m.firstName + " " + m.lastName  // ← add space here
//                    val name  = m?.userId?.firstName + " " + m?.userId?.lastName
                    HomeActivity.userName  = name
                    val userProfileFragment = UserProfileFragment().apply {
                        arguments = bundle
                    }

                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, userProfileFragment)
                        .addToBackStack(null)
                        .commit()
                }

            }
        })
    }

    private fun initObserver() {

        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            when(it?.status){
                Status.LOADING ->{
                    showLoading()
                }
                Status.SUCCESS ->{
                    when(it.message){
                        "getShareDetail" -> {
                            viewModel.getAds(Constants.GET_ADS)
                            val myDataModel: StreamDetailApiResponse? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null) {
                                myDataModel.data?.let { streamDetail ->
                                    binding.bean = streamDetail.post

                                     scheduleDate = streamDetail.post?.scheduleDate
                                     streamUrl = streamDetail.post?.streamUrl
                                     currentUserId = sharedPrefManager.getUserId()
                                     ownerUserId = streamDetail.post?.userId?._id
                                    id = myDataModel.data!!.post?.userId?._id
                                    name =  myDataModel.data!!.post?.userId?.firstName + " " + myDataModel.data!!.post?.userId?.lastName
                                    if (!scheduleDate.isNullOrEmpty()) {
                                        val dateTime = BindingUtils.convertUtcToLocalTime(scheduleDate) ?: ""
                                        val utcDate = BindingUtils.utcStringToDate(scheduleDate)


                                        if (ownerUserId != null) {
                                            if (ownerUserId.equals(currentUserId) ) {
                                                if (utcDate != null && utcDate <= Date()) {
                                                    binding.playBtn.text = "Start Live Streaming"
                                                } else {
                                                    binding.playBtn.text = "Schedule: $dateTime"
                                                }
                                            } else {
                                                binding.playBtn.text = "Schedule: $dateTime"
                                            }


                                        }
                                    } else {
                                        if (!streamUrl.isNullOrEmpty()) {
                                            binding.playBtn.text = "Play Recorded Stream"
                                            binding.playBtn.isEnabled = true
                                        } else {
                                            binding.playBtn.text = "Join Live Streaming"
                                            binding.playBtn.isEnabled = true
                                        }
                                    }



                                }
                            }
                        }
                        "addComment" ->{
                            hideLoading()
                            val myDataModel  : AddCommentApiResponse? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    showToast(myDataModel.message.toString())
                                    getComments()
                                }
                            }
                        }
                        "getComments" ->{
                            hideLoading()
                            val myDataModel : GetCommentApiResponsePost? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data  != null){
                                    binding.rvComments.visibility  = View.VISIBLE
                                    if (myDataModel.data?.comments != null){
                                        commentAdapter.list = myDataModel.data?.comments
                                    }
                                }
                            }
                        }
                        "likeDislikeComment" ->{
                            hideLoading()
                            val myDataModel : CommentLikeDislikeApiResponse? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    getComments()
                                }
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
                Status.ERROR ->{
                    hideLoading()
                    showToast(it.message.toString())
                }
                else ->{

                }
            }
        })
    }

    private fun initView() {
        streamId = arguments?.getString("streamId")
        if (streamId != null){
            viewModel.getShareDetail(Constants.GET_POST_BY_ID+streamId)
        }
        getComments()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_stream_detail
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    private fun getComments() {
        val data = HashMap<String,Any>()
        data["id"] = streamId.toString()
        data["type"] = "stream"
        viewModel.getComments(Constants.GET_COMMENT,data)
    }
}