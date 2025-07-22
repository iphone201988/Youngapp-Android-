package com.tech.young.ui.my_profile_screens.saved_fragments

import android.content.Intent
import android.view.View
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
import com.tech.young.data.model.CategoryModel
import com.tech.young.data.model.GetSavedPostApiResponse
import com.tech.young.databinding.CategoryItemViewBinding
import com.tech.young.databinding.FragmentStreamBinding
import com.tech.young.databinding.StreamItemViewBinding
import com.tech.young.ui.common.CommonActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StreamFragment : BaseFragment<FragmentStreamBinding>() {
    private val viewModel:SavedFragmentVM by viewModels()
    // adapter
    private lateinit var categoryAdapter: SimpleRecyclerViewAdapter<CategoryModel, CategoryItemViewBinding>
    private lateinit var streamAdapter: SimpleRecyclerViewAdapter<GetSavedPostApiResponse.Data.Post, StreamItemViewBinding>
    private var apiTitle : String ? = null

    override fun onCreateView(view: View) {
       // view
        initView()

        // click
        initOnClick()
        // observer
        initObserver()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_stream
    }

    override fun getViewModel(): BaseViewModel {
      return viewModel
    }


    private fun getSavedData() {
        if (apiTitle != null){
            val data = HashMap<String,Any>()
            data["userType"] =  apiTitle.toString()
            data["type"] = "stream"
            data["page"] = 1
            viewModel.savedShare(data, Constants.GET_SAVED_POST)
        }
        else{
            val data = HashMap<String,Any>()
            data["userType"] = "general_member"
            data["type"] = "stream"
            data["page"] = 1
            viewModel.savedShare(data, Constants.GET_SAVED_POST)
        }

    }

    /** handle view **/
    private fun initView(){
        // adpater
        initAdapter()
        binding.shareLayout.tabShare.setOnClickListener {
            val intent = Intent(requireContext(), CommonActivity::class.java).putExtra("from","common_share")
            startActivity(intent)
        }

        binding.shareLayout.tabStream.setOnClickListener {
            val intent = Intent(requireContext(), CommonActivity::class.java).putExtra("from","common_stream")
            startActivity(intent)
        }


        binding.shareLayout.tabVault.setOnClickListener {
            val intent = Intent(requireContext(), CommonActivity::class.java).putExtra("from","common_vault")
            startActivity(intent)
        }
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
                                    streamAdapter.list = myDataModel.data!!.posts
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
                    apiTitle = mapTitleToApiValue(m.title)
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

            }
        }
        binding.rvShare.adapter=streamAdapter
    }

    private fun mapTitleToApiValue(title: String): String {
        return when (title) {
            "Advisors" -> "financial_advisor"
            "Startups" -> "startup"
            "Small Businesses" -> "small_business"
            "Insurance" -> "insurance"
            "VCs" -> "investor"
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
        list.add(CategoryModel("Insurance"))
        list.add(CategoryModel("VCs"))
        return list
    }
    private var getList = listOf(
        "", "", "", "", ""
    )
    override fun onResume() {
        super.onResume()
        getSavedData()
    }
}