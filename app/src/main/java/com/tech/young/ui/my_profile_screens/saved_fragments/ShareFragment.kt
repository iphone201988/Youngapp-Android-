package com.tech.young.ui.my_profile_screens.saved_fragments

import android.content.Intent
import android.os.Bundle
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
import com.tech.young.databinding.FragmentShareBinding
import com.tech.young.databinding.ShareItemViewBinding
import com.tech.young.ui.common.CommonActivity
import com.tech.young.ui.exchange.exchange_share_detail.ExchangeShareDetailFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShareFragment : BaseFragment<FragmentShareBinding>() {
    private val viewModel:SavedFragmentVM by viewModels()
    // adapter
    private lateinit var categoryAdapter:SimpleRecyclerViewAdapter<CategoryModel,CategoryItemViewBinding>
    private lateinit var shareAdapter:SimpleRecyclerViewAdapter<GetSavedPostApiResponse.Data.Post, ShareItemViewBinding>




    override fun onCreateView(view: View) {
        // view
        initView()

        getSavedData()
        // click
        initOnClick()
        // observer
        initObserver()
    }

    private fun getSavedData() {

        val data = HashMap<String,Any>()
        data["userType"] = "general_member"
        data["type"] = "share"
        data["page"] = 1
        viewModel.savedShare(data,Constants.GET_SAVED_POST)


    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_share
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /** handle view **/
    private fun initView(){
        // adapter
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
                                    shareAdapter.list = myDataModel.data!!.posts
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
        categoryAdapter=SimpleRecyclerViewAdapter(R.layout.category_item_view,BR.bean){v,m,pos->
            when(v.id){
                R.id.clMain->{
                    val apiTitle = mapTitleToApiValue(m.title)
                    val data = HashMap<String,Any>()
                    data["userType"] = apiTitle
                    data["type"] = "share"
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

        shareAdapter=SimpleRecyclerViewAdapter(R.layout.share_item_view,BR.bean){
                v,m,pos->
            when(v.id){
                R.id.consMain ->{
//                    val intent = Intent(requireContext(), CommonActivity::class.java)
//                    intent.putExtra("from", "exchange_share_details")
//                    intent.putExtra("userId", m._id)
//                    startActivity(intent)

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
            }
        }
        binding.rvShare.adapter=shareAdapter
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
    }


}