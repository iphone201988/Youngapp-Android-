package com.tech.young.ui.stream_screen

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.tech.young.BR

import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showToast
import com.tech.young.data.api.Constants
import com.tech.young.data.api.SimpleApiResponse
import com.tech.young.data.model.AddCommentApiResponse
import com.tech.young.data.model.AddRatingApiResponse
import com.tech.young.data.model.CommentLikeDislikeApiResponse
import com.tech.young.data.model.GetAdsAPiResponse
import com.tech.young.data.model.GetCommentApiResponse
import com.tech.young.data.model.GetCommentApiResponsePost
import com.tech.young.data.model.GetRecordedCommentApiResponse
import com.tech.young.data.model.StreamDetailApiResponse
import com.tech.young.databinding.FragmentRecordedStreamBinding
import com.tech.young.databinding.ItemLayoutCommentViewBinding
import com.tech.young.databinding.ItemLayputRecordedCommentsBinding
import com.tech.young.ui.home.HomeActivity
import com.tech.young.ui.user_profile.UserProfileFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

@AndroidEntryPoint
class RecordedStreamFragment : BaseFragment<FragmentRecordedStreamBinding>() {

    private val viewModel : StreamVM by viewModels()

    private var streamUrl : String ? = null
    private var streamId : String ? = null

    private var player: ExoPlayer? = null

    private var page  = 1
    private var isLoading = false
    private var isLastPage = false
    private var totalPages : Int ? = null

    private lateinit var commentAdapter: SimpleRecyclerViewAdapter<GetRecordedCommentApiResponse.Data.Message, ItemLayputRecordedCommentsBinding>

    override fun onCreateView(view: View) {
        iniView()
        initOnClick()
        initAdapter()
        initObserver()


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

    private fun getComments() {
        page = 1
        val data = HashMap<String,Any>()
            data["liveStreamId"] = streamId.toString()
            data["page"] = 1
            data["limit"] = 20
            viewModel.getRecordedComment(data,Constants.GET_RECORDED_STREAM_COMMENT)

    }

    private fun loadNextPage() {
        isLoading = true
        page++

        val data = HashMap<String,Any>()
        if (streamId != null){
            data["liveStreamId"] = streamId.toString()
            data["page"] = page
            data["limit"] = 20
            viewModel.getRecordedComment(data,Constants.GET_RECORDED_STREAM_COMMENT)
        }
    }

    private fun initAdapter() {
        commentAdapter = SimpleRecyclerViewAdapter(R.layout.item_layput_recorded_comments,
            BR.bean){ v, m, pos ->
            when(v.id){
            }

        }
        binding.rvComments.adapter = commentAdapter
        commentAdapter.notifyDataSetChanged()
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
                        data["liveStreamId"] =  streamId.toString()
                        data["message"] = binding.etChat.text.toString().trim()
                        viewModel.addComment(data,Constants.GET_RECORDED_STREAM_COMMENT)
                        binding.etChat.setText("")
                    }
                }

            }
        })
    }

    private fun iniView() {
        streamUrl = arguments?.getString("streamUrl")
        streamId = arguments?.getString("streamId")
        Log.i("dsaasdas", "iniView: $streamUrl , $streamId" )

        if (streamUrl != null){
            initializePlayer()
        }
        if (streamId != null){
            getComments()
        }


    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_recorded_stream
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }





    private fun initializePlayer() {

            val fullUrl = Constants.BASE_URL_IMAGE + (streamUrl ?: "")

        if (fullUrl.isNotEmpty()){
            val playerView = binding.playerView
            playerView.visibility = View.VISIBLE

            player = ExoPlayer.Builder(requireContext()).build()
            playerView.player = player

            val videoUrl =fullUrl
            val mediaItem = MediaItem.fromUri(videoUrl)
            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.playWhenReady = true
        }



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
                        "addComment" -> {
                            val myDataModel: GetRecordedCommentApiResponse? =
                                BindingUtils.parseJson(it.data.toString())

                            val newComment: GetRecordedCommentApiResponse.Data.Message? =
                                myDataModel
                                    ?.data
                                    ?.messages
                                    ?.firstOrNull { it?._id != null }

                            if (newComment != null) {
                                binding.rvComments.visibility = View.VISIBLE

                                commentAdapter.addToTop(newComment)

                                binding.etChat.setText("")
                                binding.rvComments.scrollToPosition(0)
                            }
                        }


                        "getRecordedComment" ->{
                            val myDataModel : GetRecordedCommentApiResponse? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
//                                if (myDataModel.data  != null){
//                                    binding.rvComments.visibility  = View.VISIBLE
//                                    if (myDataModel.data?.messages != null){
//                                        commentAdapter.list = myDataModel.data?.messages
//                                    }
//                                }

                                if (myDataModel.data  != null){
                                    binding.rvComments.visibility  = View.VISIBLE
                                    if (myDataModel.data?.messages != null){
                                        totalPages = myDataModel.pagination?.total ?: 1
                                        if (totalPages != null && page <= totalPages!!) {
                                            isLoading = false
                                        }
                                        if (page == 1){
                                            commentAdapter.list = myDataModel.data?.messages
                                            commentAdapter.notifyDataSetChanged()
                                        } else{
                                            commentAdapter.addToList(myDataModel.data?.messages)
                                            commentAdapter.notifyDataSetChanged()
//                                        commentAdapter.list = myDataModel.data?.comments
                                        }
                                    }
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

    override fun onDestroyView() {
        super.onDestroyView()
        player?.release()
        player = null
    }

}