package com.tech.young.ui.exchange.screens

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.tech.young.data.FilterItem
import com.tech.young.data.SortingItem
import com.tech.young.data.api.Constants
import com.tech.young.data.api.SimpleApiResponse
import com.tech.young.data.model.CategoryModel
import com.tech.young.data.model.GetStreamApiResponse
import com.tech.young.data.model.SavedPostApiResponse
import com.tech.young.data.model.VaultExchangeApiResponse
import com.tech.young.databinding.CategoryItemViewBinding
import com.tech.young.databinding.FragmentStreamExchangeBinding
import com.tech.young.databinding.ItemLayoutSortDataBinding
import com.tech.young.databinding.ItemLayoutStreamExchangeBinding
import com.tech.young.ui.common.CommonActivity
import com.tech.young.ui.exchange.ExchangeVM
import com.tech.young.ui.exchange.Filterable
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.internal.ignoreIoExceptions

@AndroidEntryPoint
class StreamExchangeFragment : BaseFragment<FragmentStreamExchangeBinding>() , Filterable {
    private val viewModel: ExchangeVM by viewModels()
    private lateinit var categoryAdapter: SimpleRecyclerViewAdapter<CategoryModel, CategoryItemViewBinding>
    private lateinit var streamAdapter : SimpleRecyclerViewAdapter<GetStreamApiResponse.Data.Post,ItemLayoutStreamExchangeBinding>
    private  lateinit  var categoryData : ArrayList<CategoryModel>
    private lateinit var sortAdapter : SimpleRecyclerViewAdapter<SortingItem, ItemLayoutSortDataBinding>
    private var sortList = ArrayList<SortingItem>()

    private var selectedCategoryTitle: String? = null
    private var selectedKey : String ?= null
    private var userSelectedKey : String ? = null
    private var selectedFilterData : FilterItem ? = null
    private var searchData : String ? = null

    companion object {
        var selectedCategoryForExchange: String? = null
    }
    override fun getLayoutResource(): Int {
        return R.layout.fragment_stream_exchange
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // view
        initView()
        // click
        initOnClick()
        // observer
        initObserver()
    }

    /** handle view **/
    private fun initView() {
        getSortList()
        initAdapter()


        // Set category list
        categoryData = categoryList(selectedCategoryForExchange)
        selectedCategoryTitle = categoryData.find { it.isSelected }?.title

        categoryAdapter.list = categoryData
        binding.rvCategories.adapter = categoryAdapter

        // Initial fetch
        selectedCategoryTitle?.let { getStreamExchange(it) }
        Log.i("fsdfsdfsd", "initView: $categoryData , $selectedCategoryTitle  ,$selectedCategoryForExchange")
    }

    private fun getSortList() {
        sortList.add(SortingItem("Followed", "byFollowers"))
        sortList.add(SortingItem("Saved", "bySave"))
        sortList.add(SortingItem("Booms", "byBoom"))
    }
    private fun initAdapter() {
        categoryAdapter=SimpleRecyclerViewAdapter(R.layout.category_item_view, BR.bean){ v, m, pos->
            when(v.id){
                R.id.clMain->{
                    if (v.id == R.id.clMain && !m.isSelected) {
                        categoryData.forEach { it.isSelected = false }
                        categoryData[pos].isSelected = true
                        selectedCategoryTitle = m.title
                        categoryAdapter.notifyDataSetChanged()

                        getStreamExchange(m.title)
                    }

                }

            }
        }
        categoryAdapter.list=categoryList()
        binding.rvCategories.adapter=categoryAdapter


        streamAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_stream_exchange, BR.bean){v,m,pos ->
            val consReport = v.rootView.findViewById<ConstraintLayout>(R.id.consReport)
            val title = v.rootView.findViewById<TextView>(R.id.tvReport)


            // Initial visibility
            consReport.visibility = if (m.isReportVisible) View.VISIBLE else View.GONE


            if (sharedPrefManager.getUserId() == m.userId?._id){
                title.text = "Delete stream"
            }
            else{
                title.text = "Report"

            }

            when(v.id){
                R.id.consMain ->{

//                    val intent= Intent(requireContext(), CommonActivity::class.java)
//                    intent.putExtra("from","consumer_live_stream")
//                    intent.putExtra("room_id",m._id)
//                    startActivity(intent)

                    val intent= Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("from","stream_detail")
                    intent.putExtra("streamId", m._id)
                    startActivity(intent)

                }
                R.id.ivSaves -> {
                    val data = HashMap<String,String>()
                    data["type"] = "stream"
                    viewModel.saveUnSave(data , Constants.SAVE_UNSAVE_POST + m._id)
                }
                R.id.ivHeart ->{
                    val data = HashMap<String,String>()
                    data["type"] = "stream"
                    viewModel.likeDislike(data, Constants.LIKE_DISLIKE_POST + m._id)
                }
                R.id.reportBtn -> {
                    m.isReportVisible = !m.isReportVisible
                    streamAdapter.notifyDataSetChanged()
                }

                R.id.consReport ->{
                    if (title.text == "Report"){
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("from", "report_user")
                        intent.putExtra("userId", m._id)
                        intent.putExtra("reportType","stream")
                        startActivity(intent)
//                        consReport.visibility = View.GONE
                    }
                    else{
                        viewModel.deletePost( Constants.DELETE_POST+m._id)
//                        consReport.visibility = View.GONE

                    }
                    m.isReportVisible = false
                    streamAdapter.notifyItemChanged(pos)

                }
            }

        }
        binding.rvStream.adapter = streamAdapter
        streamAdapter.notifyDataSetChanged()


        sortAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_sort_data, BR.bean) { v, m, pos ->
            when (v.id) {
                R.id.consMain -> {
                    val clickedItem = sortList[pos]

                    val wasSelected = clickedItem.isSelected
                    sortList.forEach { it.isSelected = false }

                    // Toggle the clicked item
                    clickedItem.isSelected = !wasSelected
                    sortAdapter.notifyDataSetChanged()

                    selectedKey = if (clickedItem.isSelected) clickedItem.key else ""

                   // Log.i("SelectedKey", if (selectedKey.isEmpty()) "Deselected, no sort active" else selectedKey)

                    // Always call API with proper key (or blank)
                    getStreamExchange(selectedCategoryTitle.toString())

                    binding.rvSort.visibility = View.GONE
                }


            }
        }

        binding.rvSort.adapter = sortAdapter
        sortAdapter.list = sortList
    }

    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer {
            when(it?.id){
                R.id.ivSort, R.id.tvSort ->{
                binding.rvSort.visibility = View.VISIBLE
                }

                R.id.addStream ->{
                    val intent = Intent(requireContext(), CommonActivity::class.java).putExtra("from","common_stream")
                    startActivity(intent)
                }
            }
        })

    }

    /** handle observer **/
    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            when(it?.status){
                Status.LOADING ->{
                    showLoading()
                }
                Status.SUCCESS ->{
                    hideLoading()
                    when(it.message) {
                        "getStream" ->{
                            val myDataModel :GetStreamApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    streamAdapter.list  = myDataModel.data?.posts
                                }
                            }
                        }
                        "saveUnSave" ->{
                        val myDataModel : SavedPostApiResponse ? = BindingUtils.parseJson(it.data.toString())
                        if(myDataModel != null){
                            if (myDataModel.data != null){
                                showToast(myDataModel.message.toString())
                                selectedCategoryTitle?.let { getStreamExchange(it) }

                            }
                        }
                    }
                        "likeDislike" ->{
                            val myDataModel : SavedPostApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if(myDataModel != null){
                                if (myDataModel.data != null){
                                    showToast(myDataModel.message.toString())
                                    selectedCategoryTitle?.let { getStreamExchange(it) }
                                }
                            }
                        }
                        "deletePost" ->{
                            val myDataModel : SimpleApiResponse? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                showToast(myDataModel.message.toString())
                                selectedCategoryTitle?.let { getStreamExchange(it) }

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

    private fun categoryList(selectedCategory: String? = null): ArrayList<CategoryModel> {
        val items = listOf("Members", "Advisors", "Startups", "Small Businesses", "Investor", "Firm")
        return ArrayList(items.mapIndexed { index, item ->
            val isSelected = if (selectedCategory != null) {
                item.equals(selectedCategory, ignoreCase = true)
            } else index == 0
            CategoryModel(item, isSelected)
        })
    }


    private fun getStreamExchange(title: String) {
        val apiTitle = mapTitleToApiValue(title)
        val data = hashMapOf<String, Any>(
            "userType" to apiTitle,
            "type" to "stream",
            "page" to 1
        )

        if (selectedKey?.isNotEmpty() == true) {
            data[selectedKey!!] = true
        }
        if (userSelectedKey?.isNotEmpty() == true) {
            data[userSelectedKey!!] = true
        }


        // New support for key-value pair (e.g., rating = 1)
        if (selectedFilterData != null){
            selectedFilterData?.let {
                if (it.key.isNotEmpty() && it.value != null) {
                    data[it.key] = it.value
                }
            }
        }
        if (searchData?.isNotEmpty() == true){
            data["search"] = searchData!!
        }

        viewModel.getStream(data, Constants.GET_ALL_POST)
    }

    override fun onFilterApplied(selectedFilter: FilterItem?) {
        selectedFilterData = selectedFilter
         userSelectedKey = if (selectedFilter?.isSelected == true) selectedFilter.key else ""
        Log.i("dfdsfsdfsd", "onFilterApplied: $selectedKey , $selectedFilter")
        getStreamExchange(selectedCategoryTitle.toString()) // ← Pass selectedFilter

    }

    override fun onSearchQueryChanged(query: String) {
        searchData = query
        getStreamExchange(selectedCategoryTitle.toString())
    }

    override fun onResume() {
        super.onResume()
        Log.i("dsadsad", "onResume: $selectedCategoryTitle")
        getStreamExchange(selectedCategoryTitle.toString())
    }
}