package com.tech.young.ui.exchange.screens

import android.content.Intent
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
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
import com.tech.young.data.model.SavedPostApiResponse
import com.tech.young.data.model.VaultExchangeApiResponse
import com.tech.young.databinding.CategoryItemViewBinding
import com.tech.young.databinding.FragmentVaultExchangeBinding
import com.tech.young.databinding.ItemLayoutSortDataBinding
import com.tech.young.databinding.ItemLayoutVaultExchangeBinding
import com.tech.young.ui.common.CommonActivity
import com.tech.young.ui.exchange.ExchangeVM
import com.tech.young.ui.exchange.Filterable
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VaultExchangeFragment : BaseFragment<FragmentVaultExchangeBinding>() , Filterable{
    private val viewModel: ExchangeVM by viewModels()

    private lateinit var categoryAdapter: SimpleRecyclerViewAdapter<CategoryModel, CategoryItemViewBinding>
    private lateinit var vaultAdapter: SimpleRecyclerViewAdapter<VaultExchangeApiResponse.Data.Vault, ItemLayoutVaultExchangeBinding>
    private var apiTitle : String ?= null
    private lateinit var sortAdapter : SimpleRecyclerViewAdapter<SortingItem, ItemLayoutSortDataBinding>
    private var sortList = ArrayList<SortingItem>()
    private var selectedCategoryTitle: String? = null
    private  lateinit  var categoryData : ArrayList<CategoryModel>

    companion object {
        var selectedCategoryForExchange: String? = null
    }
    override fun getLayoutResource(): Int {
       return R.layout.fragment_vault_exchange
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

    private fun initAdapter() {
        categoryAdapter=SimpleRecyclerViewAdapter(R.layout.category_item_view, BR.bean){ v, m, pos->
            when(v.id){
                R.id.clMain->{
//                     apiTitle = mapTitleToApiValue(m.title)
//                    val data = HashMap<String,Any>()
//                    data["userType"] = apiTitle!!
//                    data["page"] = 1
//                    viewModel.getVault(data, Constants.GET_VAULT)
//
//                    categoryAdapter.list.forEach {
//                        it.isSelected = false
//                    }
//                    categoryAdapter.list[pos].isSelected = true
//                    categoryAdapter.notifyDataSetChanged()

                    if (v.id == R.id.clMain && !m.isSelected) {
                        categoryData.forEach { it.isSelected = false }
                        categoryData[pos].isSelected = true
                        selectedCategoryTitle = m.title
                        categoryAdapter.notifyDataSetChanged()

                        getVault(m.title,"",null)
                    }
                }
            }
        }
        categoryAdapter.list=categoryList()
        binding.rvCategories.adapter=categoryAdapter

        vaultAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_vault_exchange,BR.bean){v,m,pos ->
            val consReport = v.rootView.findViewById<ConstraintLayout>(R.id.consReport)
            val title = v.rootView.findViewById<TextView>(R.id.tvReport)



// Initial visibility
            consReport.visibility = if (m.isReportVisible) View.VISIBLE else View.GONE

            if (sharedPrefManager.getUserId() == m.admin?._id){
                title.text = "Delete vault"
            }
            else{
                title.text = "Report"

            }
            when(v.id){
                R.id.ivSaves ->{
                    viewModel.vaultSaveUnSave( Constants.SAVE_UNSAVE_VAULT+m._id)
                }
                R.id.ivSendChat ->{

//                    val parentView = v.parent as? ViewGroup
//                    val etChat = parentView?.findViewById<AppCompatEditText>(R.id.etChat)
//                    val commentText = etChat?.text?.toString()?.trim()
//                    if (commentText?.isNotEmpty() == true){
//                        val data = HashMap<String,Any>()
//                        data["vaultId"] = m._id.toString()
//                        data["comment"] = commentText
//                        viewModel.addComment(data,Constants.VAULT_ADD_COMMENT)
//                    }
//                    else{
//                        showToast("Please add comment")
//                    }

                }
                R.id.ivHeart ->{
                  //  viewModel.likeDislike(Constants.LIKE_DISLIKE_POST+m._id)
                }
                R.id.consMain ->{
                    val intent= Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("from","vault_room")
                    intent.putExtra("vaultId",m._id)
                    startActivity(intent)
                }

                R.id.reportBtn -> {
                    m.isReportVisible = !m.isReportVisible
                    vaultAdapter.notifyDataSetChanged()
                }
                R.id.consReport ->{
                    if (title.text == "Report"){
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("from", "report_user")
                        intent.putExtra("userId", m._id)
                        intent.putExtra("reportType","vault")
                        startActivity(intent)
                }
                    else {
                        viewModel.deletePost( Constants.DELETE_VAULT+m._id)
                    }

                    m.isReportVisible = false
                    vaultAdapter.notifyItemChanged(pos)
                }
            }

        }
        binding.rvVault.adapter = vaultAdapter



        sortAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_sort_data, BR.bean) { v, m, pos ->
            when (v.id) {
                R.id.consMain -> {
                    val clickedItem = sortList[pos]

                    val wasSelected = clickedItem.isSelected
                    sortList.forEach { it.isSelected = false }

                    // Toggle the clicked item
                    clickedItem.isSelected = !wasSelected
                    sortAdapter.notifyDataSetChanged()

                    val selectedKey = if (clickedItem.isSelected) clickedItem.key else ""

                    Log.i("SelectedKey", if (selectedKey.isEmpty()) "Deselected, no sort active" else selectedKey)

                    // Always call API with proper key (or blank)
                   getVault(selectedCategoryTitle.toString(), selectedKey,null)

                    binding.rvSort.visibility = View.GONE
                }


            }
        }

        binding.rvSort.adapter = sortAdapter
        sortAdapter.list = sortList
    }

    private fun getVault(title: String,selectedKey: String, selectedFilter: FilterItem?) {
        val apiTitle = mapTitleToApiValue(title)
        val data = hashMapOf<String, Any>(
            "userType" to apiTitle,
            "page" to 1
        )

        if (selectedKey.isNotEmpty()) {
            data[selectedKey] = true
        }

        // New support for key-value pair (e.g., rating = 1)
        selectedFilter?.let {
            if (it.key.isNotEmpty() && it.value != null) {
                data[it.key] = it.value
            }
        }

        viewModel.getVault(data, Constants.GET_VAULT)
    }

    /** handle view **/
    private fun initView(){
        getSortList()
        initAdapter()


        // Set category list
        categoryData = categoryList(selectedCategoryForExchange)
        selectedCategoryTitle = categoryData.find { it.isSelected }?.title

        categoryAdapter.list = categoryData
        binding.rvCategories.adapter = categoryAdapter

        // Initial fetch
        selectedCategoryTitle?.let { getVault(it,"",null) }
    }
    private fun getSortList() {
        sortList.add(SortingItem("Followed", "byFollowers"))
        sortList.add(SortingItem("Saved", "bySave"))
        sortList.add(SortingItem("Booms", "byBoom"))
    }
    /** handle click **/
    private fun initOnClick(){

        viewModel.onClick.observe(viewLifecycleOwner,  Observer {
            when(it?.id){
                R.id.addVault ->{
                    val intent = Intent(requireContext(), CommonActivity::class.java).putExtra("from","common_vault")
                    startActivity(intent)
                }
                R.id.ivSort , R.id.tvSort ->{
                    binding.rvSort.visibility = View.VISIBLE
                }
            }
        })
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
                        "getVault" ->{
                            var myDataModel : VaultExchangeApiResponse? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data!= null){
                                    vaultAdapter .list = myDataModel.data?.vaults
                                }
                            }
                        }
                        "vaultSaveUnSave" ->{
                            val myDataModel : SavedPostApiResponse? = BindingUtils.parseJson(it.data.toString())
                            if(myDataModel != null){
                                if (myDataModel.data != null){
                                    showToast(myDataModel.message.toString())
                                    selectedCategoryTitle?.let { getVault(it,"",null) }


                                }
                            }
                        }
                        "likeDislike" ->{
                            val myDataModel : SavedPostApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if(myDataModel != null){
                                if (myDataModel.data != null){
                                    showToast(myDataModel.message.toString())
                                    selectedCategoryTitle?.let { getVault(it,"",null) }
                                }
                            }
                        }
                        "deletePost" ->{
                            val myDataModel : SimpleApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                showToast(myDataModel.message.toString())
                                selectedCategoryTitle?.let { getVault(it,"",null) }
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

    override fun onResume() {
        super.onResume()
        getVault(selectedCategoryTitle.toString(),"",null)
    }

    override fun onFilterApplied(selectedFilter: FilterItem) {
        val selectedKey = if (selectedFilter.isSelected) selectedFilter.key else ""
        Log.i("dfdsfsdfsd", "onFilterApplied: $selectedKey , $selectedFilter")
        getVault(selectedCategoryTitle.toString(), selectedKey, selectedFilter) // ← Pass selectedFilter
    }

    override fun onSearchQueryChanged(query: String) {

    }


}