package com.tech.young.ui.exchange.exchange_share_detail

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.createBitmap
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
import com.tech.young.data.model.CommentLikeDislikeApiResponse
import com.tech.young.data.model.GetCommentApiResponsePost
import com.tech.young.data.model.GetPostDetailsApiResponse
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.FragmentExchangeShareDetailBinding
import com.tech.young.databinding.ItemLayoutPostCommentBinding
import com.tech.young.ui.exchange.ExchangeVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExchangeShareDetailFragment : BaseFragment<FragmentExchangeShareDetailBinding>() {
    private val viewModel: ExchangeVM by viewModels()
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<String, AdsItemViewBinding>
    private lateinit var commentAdapter: SimpleRecyclerViewAdapter<GetCommentApiResponsePost.Data.Comment,ItemLayoutPostCommentBinding>

    private var getList = listOf(
        "", "", "", "", ""
    )

    private var userId : String ? = null
    override fun onCreateView(view: View) {
        getData()
        getComments()
        initOnClick()
        initAdapter()
        initObserver()
    }

    private fun getComments() {
        val data = HashMap<String,Any>()
        data["id"] = userId.toString()
        data["type"] = "share"
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
            }
        })
    }

    private fun initAdapter() {

        adsAdapter = SimpleRecyclerViewAdapter(R.layout.ads_item_view, BR.bean) { v, m, pos ->
            when (v.id) {

            }
        }
        adsAdapter.list = getList
        binding.rvAds.adapter = adsAdapter


        commentAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_post_comment,BR.bean){v,m,pos ->
            when(v.id){
                R.id.likeBtn ->{
                    viewModel.likeDislikeComment(Constants.LIKE_DISLIKE_COMMENT+m._id)
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
                    hideLoading()
                    when(it.message){
                        "getShareDetail"  ->{
                            val myDataModel : GetPostDetailsApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    if (myDataModel.data?.post != null)
                                    binding.bean  = myDataModel.data?.post
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
                            val myDataModel : GetCommentApiResponsePost ? = BindingUtils.parseJson(it.data.toString())
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
                            val myDataModel : CommentLikeDislikeApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    getComments()
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