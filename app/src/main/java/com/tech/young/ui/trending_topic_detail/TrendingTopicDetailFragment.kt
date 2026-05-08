package com.tech.young.ui.trending_topic_detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showToast
import com.tech.young.data.api.Constants
import com.tech.young.data.model.GetSavedPostApiResponse
import com.tech.young.data.model.SavedPostApiResponse
import com.tech.young.databinding.FragmentTrendingTopicDetailBinding
import com.tech.young.databinding.ItemLayoutMyShareBinding
import com.tech.young.ui.exchange.exchange_share_detail.ExchangeShareDetailFragment
import com.tech.young.ui.home.HomeActivity
import com.tech.young.ui.user_profile.UserProfileFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TrendingTopicDetailFragment : BaseFragment<FragmentTrendingTopicDetailBinding>() {

    private val viewModel : TrendingTopicVm  by viewModels()
    private lateinit var shareAdapter: SimpleRecyclerViewAdapter<GetSavedPostApiResponse.Data.Post, ItemLayoutMyShareBinding>
    private var page  = 1
    private var isLoading = false
    private var isLastPage = false
    private var totalPages : Int ? = null
    private var topic  : String ?= null

    override fun getLayoutResource(): Int {
       return R.layout.fragment_trending_topic_detail
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        getData()
        initAdapter()
        initObserver()


        binding.rvTrendingTopic.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                if (dy <= 0) return

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && page < totalPages!!) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3 &&
                        firstVisibleItemPosition >= 0
                    ) {
                        isLoading = true
                        loadNextPage()
                    }
                }
            }

            private fun loadNextPage() {
                isLoading = true
                page++

                if(topic != null ){
                    val data = HashMap<String,Any>()
                    data["topic"] = topic.toString()
                    data["page"] = page
                    viewModel.getTrendingTopicDetail(Constants.GET_TRENDING_TOPIC_DETAIL,data)
                }

            }
        })
    }

    private fun getData() {
        topic = arguments?.getString("topic").toString()
        val data = HashMap<String, Any>()
        data["topic"] = topic.toString()
        data["page"] = 1
        viewModel.getTrendingTopicDetail(Constants.GET_TRENDING_TOPIC_DETAIL,data)
    }



    private fun initAdapter() {


        shareAdapter=SimpleRecyclerViewAdapter(R.layout.item_layout_my_share, BR.bean){
                v,m,pos->
            when(v.id){
                R.id.consMain ->{
                    val fragment = ExchangeShareDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString("userId", m._id)
                        }
                    }

                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, fragment)
                        .addToBackStack(null)
                        .commit()
                }
                R.id.ivUserImage , R.id.tvUserName->{
                    val bundle = Bundle().apply {
                        putString("from", "user_profile")
                        putString("userId", m?.userId?._id) // assuming m._id is a String
                    }
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

                R.id.ivHeart , R.id.tvSaves->{
                    val data = HashMap<String,String>()
                    data["type"] = "share"
              //      viewModel.likeDislike(data, Constants.LIKE_DISLIKE_POST + m._id)

                }


            }
        }
        binding.rvTrendingTopic.adapter=shareAdapter
    }



    private fun initObserver(){
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            when(it?.status){
                Status.LOADING ->{
                    hideLoading()
                }
                Status.SUCCESS ->{
                    hideLoading()
                    when(it.message){
                        "getTrendingTopicDetail" ->{
                            var myDataModel : GetSavedPostApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data!= null){

                                    totalPages = myDataModel.pagination?.total ?: 1
                                    if (page <= totalPages!!) {
                                        isLoading = false
                                    }
                                    if (page == 1){
                                        shareAdapter.list = myDataModel.data?.posts
                                        shareAdapter.notifyDataSetChanged()
                                    } else{
                                        shareAdapter.addToList(myDataModel.data?.posts)
                                        shareAdapter.notifyDataSetChanged()

                                    }
                                    //   shareAdapter.list = myDataModel.data!!.posts
                                }
                            }
                        }
                        "likeDislike" ->{
                            val myDataModel : SavedPostApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if(myDataModel != null){
                                if (myDataModel.data != null){
                                    showToast(myDataModel.message.toString())
//                                    getSavedData()
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

}