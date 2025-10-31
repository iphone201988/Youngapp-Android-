package com.tech.young.ui.my_profile_screens.saved_fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import com.tech.young.data.model.CategoryModel
import com.tech.young.data.model.GetSavedPostApiResponse
import com.tech.young.databinding.CategoryItemViewBinding
import com.tech.young.databinding.FragmentStreamBinding
import com.tech.young.databinding.StreamItemViewBinding
import com.tech.young.ui.common.CommonActivity
import com.tech.young.ui.exchange.stream_detail_fragment.StreamDetailFragment
import com.tech.young.ui.home.HomeActivity
import com.tech.young.ui.user_profile.UserProfileFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StreamFragment : BaseFragment<FragmentStreamBinding>() {
    private val viewModel:SavedFragmentVM by viewModels()
    // adapter
    private lateinit var categoryAdapter: SimpleRecyclerViewAdapter<CategoryModel, CategoryItemViewBinding>
    private lateinit var streamAdapter: SimpleRecyclerViewAdapter<GetSavedPostApiResponse.Data.Post, StreamItemViewBinding>
    private var apiTitle : String ? = null

    private var page  = 1
    private var isLoading = false
    private var isLastPage = false
    private var totalPages : Int ? = null


    private var selectedCategoryTitle: String = "Members"


    override fun onCreateView(view: View) {
       // view
        initView()

        // click
        initOnClick()
        // observer
        initObserver()


        binding.rvShare.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // 👇 Only continue if scrolling down
                if (dy <= 0) return

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && page < totalPages!!) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3 &&
                        firstVisibleItemPosition >= 0
                    ) {
                        isLoading = true // ✅ Lock before load

                        loadMoreData()

                    }
                }
            }


        })


    }


    private fun loadMoreData() {
        page ++
        isLoading = true


        val data = HashMap<String,Any>()
        if (apiTitle != null){
            data["userType"] =  apiTitle.toString()
        }
        else{
            data["userType"] = "general_member"
        }
        data["type"] = "stream"
        data["page"] = page
        data["limit"] = 20
        viewModel.savedShare(data, Constants.GET_SAVED_POST)
    }



    override fun getLayoutResource(): Int {
        return R.layout.fragment_stream
    }

    override fun getViewModel(): BaseViewModel {
      return viewModel
    }


    private fun getSavedData() {
           page = 1
            val data = HashMap<String,Any>()
            if (apiTitle != null){
                data["userType"] =  apiTitle.toString()
            }
            else{
                data["userType"] = "general_member"
            }
            data["type"] = "stream"
            data["page"] = 1
            data["limit"] = 20
            viewModel.savedShare(data, Constants.GET_SAVED_POST)

    }

    /** handle view **/
    private fun initView(){
        // adpater
        initAdapter()

    }

    /** handle click **/
    private fun initOnClick(){

    }

    /** handle observer **/
    private fun initObserver(){
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            when(it?.status){
                Status.LOADING ->{
                    showLoading()
                }
                Status.SUCCESS ->{
                    hideLoading()
                    when(it.message){
                        "savedShare" ->{
                            var myDataModel : GetSavedPostApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data!= null){

                                    totalPages = myDataModel.pagination?.total ?: 1
                                    if (page <= totalPages!!) {
                                        isLoading = false
                                    }
                                    if (page == 1){
                                        streamAdapter.list = myDataModel.data?.posts
                                        streamAdapter.notifyDataSetChanged()
                                    } else{
                                        streamAdapter.addToList(myDataModel.data?.posts)
                                        streamAdapter.notifyDataSetChanged()

                                    }

                                 //   streamAdapter.list = myDataModel.data!!.posts
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

    /** handle adapter **/
    private fun initAdapter(){
        categoryAdapter=SimpleRecyclerViewAdapter(R.layout.category_item_view, BR.bean){ v, m, pos->
            when(v.id){
                R.id.clMain->{
                    page = 1
                    apiTitle = mapTitleToApiValue(m.title)
                    selectedCategoryTitle = m.title
                    val data = HashMap<String,Any>()
                    data["userType"] = apiTitle!!
                    data["type"] = "stream"
                    data["page"] = 1
                    viewModel.savedShare(data,Constants.GET_SAVED_POST)
                    categoryAdapter.list.forEach {
                        it.isSelected = false
                    }
                    categoryAdapter.list[pos].isSelected = true
                    categoryAdapter.notifyDataSetChanged()
                }
            }
        }
        categoryAdapter.list=categoryList()
        binding.rvCategories.adapter=categoryAdapter

        streamAdapter=SimpleRecyclerViewAdapter(R.layout.stream_item_view, BR.bean){
                v,m,pos->
            when(v.id){
                R.id.consMain ->{

//                    val intent= Intent(requireContext(), CommonActivity::class.java)
//                    intent.putExtra("from","consumer_live_stream")
//                    intent.putExtra("room_id",m._id)
//                    startActivity(intent)

//                    val intent= Intent(requireContext(), CommonActivity::class.java)
//                    intent.putExtra("from","stream_detail")
//                    intent.putExtra("streamId", m._id)
//                    startActivity(intent)


                    val fragment = StreamDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString("streamId", m._id)
                        }
                    }

                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, fragment)
                        .addToBackStack(null)
                        .commit()

                }
                R.id.ivUserImage , R.id.tvUserName ->{
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
        binding.rvShare.adapter=streamAdapter
    }

    private fun mapTitleToApiValue(title: String): String {
        return when (title) {
            "Advisors" -> "financial_advisor"
            "Startups" -> "startup"
            "Small Businesses" -> "small_business"
            "Investor" -> "investor"
            "Firm" -> "financial_firm"
            "Members" -> "general_member"
            else -> title.lowercase().replace(" ", "_")
        }
    }

    private fun categoryList():ArrayList<CategoryModel>{
        val list=ArrayList<CategoryModel>()
        list.add(CategoryModel("Members",true))
        list.add(CategoryModel("Advisors"))
        list.add(CategoryModel("Startups"))
        list.add(CategoryModel("Small Businesses"))
        list.add(CategoryModel("Investor"))
        list.add(CategoryModel("Firm"))
        return list
    }
    private var getList = listOf(
        "", "", "", "", ""
    )
    override fun onResume() {
        super.onResume()
        getSavedData()
        restoreSelectedCategory()
    }

    private fun restoreSelectedCategory() {
        categoryAdapter.list.forEach {
            it.isSelected = it.title == selectedCategoryTitle
        }
        categoryAdapter.notifyDataSetChanged()
    }
}