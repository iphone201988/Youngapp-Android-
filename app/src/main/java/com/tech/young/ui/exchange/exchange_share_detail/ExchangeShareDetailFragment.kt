package com.tech.young.ui.exchange.exchange_share_detail

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.createBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
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
import com.tech.young.data.model.GetPostDetailsApiResponse
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.FragmentExchangeShareDetailBinding
import com.tech.young.databinding.ItemLayoutPostCommentBinding
import com.tech.young.ui.ecosystem.EcosystemFragment
import com.tech.young.ui.exchange.ExchangeFragment
import com.tech.young.ui.exchange.ExchangeVM
import com.tech.young.ui.home.HomeActivity
import com.tech.young.ui.user_profile.UserProfileFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExchangeShareDetailFragment : BaseFragment<FragmentExchangeShareDetailBinding>() {
    private val viewModel: ExchangeVM by viewModels()
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<GetAdsAPiResponse.Data.Ad, AdsItemViewBinding>
    private lateinit var commentAdapter: SimpleRecyclerViewAdapter<GetCommentApiResponsePost.Data.Comment,ItemLayoutPostCommentBinding>

    private var getList = listOf(
        "", "", "", "", ""
    )


    private var page  = 1
    private var isLoading = false
    private var isLastPage = false
    private var totalPages : Int ? = null

    private var id : String ? = null
    private var name : String ? = null
    private var userId : String ? = null
    override fun onCreateView(view: View) {
        getData()
        initOnClick()
        initAdapter()
        initObserver()

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

        ViewCompat.setOnApplyWindowInsetsListener(binding.nestedScrollView) { view, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            // Only adjust padding if keyboard is visible
            view.setPadding(0, 0, 0, imeHeight)
            insets
        }


        binding.nestedScrollView.setOnScrollChangeListener { v: NestedScrollView, _, scrollY, _, oldScrollY ->
            val view = v.getChildAt(v.childCount - 1)
            if (view != null) {
                val diff = view.bottom - (v.height + v.scrollY)
                if (diff <= 0 && scrollY > oldScrollY) {

                    Log.d("Pagination", "Reached bottom, loading next page…")
                    // ✅ User reached bottom
                    if (!isLoading && totalPages != null && page < totalPages!!) {
                        isLoading = true
                        loadNextPage()
                    }

                }
            }
        }


    }

    private fun loadNextPage() {
        isLoading = true
        page++

        val data = HashMap<String,Any>()
        if (userId != null){
            data["id"] = userId.toString()
            data["type"] = "share"
            data["page"] = page
            viewModel.getComments(Constants.GET_COMMENT,data)
        }

    }

    private fun setRating(rating: Float) {
        if (userId != null){
            val data = HashMap<String,Any>()
            data["ratings"]  = rating
            data["type"] = "share"
            data["id"] =  userId.toString()

            viewModel.rating(data,Constants.RATING)
        }

    }

    private fun getComments() {
        page  = 1
        val data = HashMap<String,Any>()
        data["id"] = userId.toString()
        data["type"] = "share"
        data["page"] = 1
        viewModel.getComments(Constants.GET_COMMENT,data)
    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer {
            when(it?.id){
                R.id.ivBack ->{
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }

                R.id.ivSendChat ->{
                    if (TextUtils.isEmpty(binding.etChat.text.toString().trim())){
                        showToast("Please enter message")
                    }
                    else{
                        val data = HashMap<String,Any>()
                        data["id"] =  userId.toString()
                        data["comment"] = binding.etChat.text.toString().trim()
                        data["type"] = "share"
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

    private fun initAdapter() {

        adsAdapter = SimpleRecyclerViewAdapter(R.layout.ads_item_view, BR.bean) { v, m, pos ->
            when (v.id) {

            }
        }
        binding.rvAds.adapter = adsAdapter


        commentAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_post_comment,BR.bean){v,m,pos ->
            when(v.id){
                R.id.likeBtn ->{
                    viewModel.likeDislikeComment(Constants.LIKE_DISLIKE_COMMENT+m._id)
                }
                R.id.profileImage , R.id.name ->{
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
    }

    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            when(it?.status){
                Status.LOADING ->{
                    showLoading()
                }
                Status.SUCCESS ->{

                    when(it.message){
                        "getShareDetail"  ->{
                            getComments()
                            viewModel.getAds(Constants.GET_ADS)
                            val myDataModel : GetPostDetailsApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    if (myDataModel.data?.post != null){
                                        binding.bean  = myDataModel.data?.post
                                        id = myDataModel.data!!.post?.userId?._id
                                        name =  myDataModel.data!!.post?.userId?.firstName + " " + myDataModel.data!!.post?.userId?.lastName
                                    }

                                }
                            }
                        }
                        "addComment" ->{
                            val myDataModel  : AddCommentApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    showToast(myDataModel.message.toString())
                                    getComments()
                                }
                            }
                        }
                        "getComments" ->{
                            hideLoading()
                            val myDataModel : GetCommentApiResponsePost ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data  != null){
                                    binding.rvComments.visibility  = View.VISIBLE
                                    if (myDataModel.data?.comments != null){
                                        totalPages = myDataModel.data?.pagination?.total ?: 1
                                        if (totalPages != null && page <= totalPages!!) {
                                            isLoading = false
                                        }
                                        if (page == 1){
                                            commentAdapter.list = myDataModel.data?.comments
                                            commentAdapter.notifyDataSetChanged()
                                        } else{
                                            commentAdapter.addToList(myDataModel.data?.comments)
                                            commentAdapter.notifyDataSetChanged()
//                                        commentAdapter.list = myDataModel.data?.comments
                                        }
                                    }
                                }
                            }
                        }
                        "likeDislikeComment" ->{
                            val myDataModel : CommentLikeDislikeApiResponse ? = BindingUtils.parseJson(it.data.toString())
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
                            val myDataModel : AddRatingApiResponse ? =  BindingUtils.parseJson(it.data.toString())
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

    private fun getData() {
        userId = arguments?.getString("userId")
        if (userId != null){
            viewModel.getShareDetail(Constants.GET_POST_BY_ID+userId)

        }


    }

    override fun getLayoutResource(): Int {
         return R.layout.fragment_exchange_share_detail
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }


}