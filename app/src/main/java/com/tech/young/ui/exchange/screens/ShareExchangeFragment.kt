package com.tech.young.ui.exchange.screens

import android.content.Intent
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
import com.tech.young.data.SortingItem
import com.tech.young.data.api.Constants
import com.tech.young.data.api.SimpleApiResponse
import com.tech.young.data.model.CategoryModel
import com.tech.young.data.model.ExchangeShareApiResponse
import com.tech.young.data.model.SavedPostApiResponse
import com.tech.young.databinding.CategoryItemViewBinding
import com.tech.young.databinding.FragmentShareExchangeBinding
import com.tech.young.databinding.ItemLayoutShareExchangeBinding
import com.tech.young.databinding.ItemLayoutSortDataBinding
import com.tech.young.ui.common.CommonActivity
import com.tech.young.ui.exchange.ExchangeVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShareExchangeFragment : BaseFragment<FragmentShareExchangeBinding>() {

    private val viewModel: ExchangeVM by viewModels()

    private lateinit var shareAdapter: SimpleRecyclerViewAdapter<ExchangeShareApiResponse.Data.Post, ItemLayoutShareExchangeBinding>
    private lateinit var categoryAdapter: SimpleRecyclerViewAdapter<CategoryModel, CategoryItemViewBinding>
    private lateinit var sortAdapter : SimpleRecyclerViewAdapter<SortingItem, ItemLayoutSortDataBinding>

    private lateinit var categoryData: ArrayList<CategoryModel>
    private var selectedCategoryTitle: String? = null

    companion object {
        var selectedCategoryForExchange: String? = null
    }

    override fun getLayoutResource(): Int = R.layout.fragment_share_exchange
    override fun getViewModel(): BaseViewModel = viewModel

    override fun onCreateView(view: View) {
        initView()
        initOnClick()
        initObserver()
    }

    /** View setup **/
    private fun initView() {
        initAdapters()

        // Set category list
        categoryData = categoryList(selectedCategoryForExchange)
        selectedCategoryTitle = categoryData.find { it.isSelected }?.title

        categoryAdapter.list = categoryData
        binding.rvCategories.adapter = categoryAdapter

        // Initial fetch
        selectedCategoryTitle?.let { getShareExchange(it) }
    }

    /** Adapter setup **/
    private fun initAdapters() {

        shareAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_share_exchange, BR.bean) { v, m, _ ->
            val consReport = v.rootView.findViewById<ConstraintLayout>(R.id.consReport)
            val title = v.rootView.findViewById<TextView>(R.id.tvReport)
            if (sharedPrefManager.getUserId() == m.userId?._id){
                title.text = "Delete post"
            }
            else{
                title.text = "Report"

            }

            when (v.id) {
                R.id.ivSaves -> {
                    val data = HashMap<String,String>()
                    data["type"] = "share"
                    viewModel.saveUnSave(data , Constants.SAVE_UNSAVE_POST + m._id)
                }
                R.id.ivHeart ->{
                    val data = HashMap<String,String>()
                    data["type"] = "share"
                    viewModel.likeDislike(data, Constants.LIKE_DISLIKE_POST + m._id)
                }
                R.id.iv_reshare -> viewModel.reshare(Constants.RESHARE_POST + m._id)
                R.id.reportBtn -> {
                    consReport.visibility = if (consReport.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }
                R.id.consReport ->{
                    if (title.text == "Report"){
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("from", "report_user")
                        intent.putExtra("userId", m._id)
                        intent.putExtra("reportType","share")
                        startActivity(intent)
                        consReport.visibility = View.GONE
                    }
                    else{
                        viewModel.deletePost( Constants.DELETE_POST+m._id)
                        consReport.visibility = View.GONE

                    }

                }
                R.id.consMain ->{
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("from", "exchange_share_details")
                    intent.putExtra("userId", m._id)
                    startActivity(intent)
                }
            }
        }
        binding.rvShare.adapter = shareAdapter

        categoryAdapter = SimpleRecyclerViewAdapter(R.layout.category_item_view, BR.bean) { v, m, pos ->
            if (v.id == R.id.clMain && !m.isSelected) {
                categoryData.forEach { it.isSelected = false }
                categoryData[pos].isSelected = true
                selectedCategoryTitle = m.title
                categoryAdapter.notifyDataSetChanged()

                getShareExchange(m.title)
            }
        }


    }

    /** API call **/
    private fun getShareExchange(title: String) {
        val apiTitle = mapTitleToApiValue(title)
        val data = hashMapOf<String, Any>(
            "userType" to apiTitle,
            "type" to "share",
            "page" to 1
        )
        viewModel.getShare(data, Constants.GET_ALL_POST)
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
                        "getShare" ->{
                            var myDataModel : ExchangeShareApiResponse? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data!= null){
                                    shareAdapter.list = myDataModel.data?.posts

                                }
                            }
                        }
                        "saveUnSave" ->{
                            val myDataModel : SavedPostApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if(myDataModel != null){
                                if (myDataModel.data != null){
                                    showToast(myDataModel.message.toString())
                                    selectedCategoryTitle?.let { getShareExchange(it) }
                                }
                            }
                        }
                        "likeDislike" ->{
                            val myDataModel : SavedPostApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if(myDataModel != null){
                                if (myDataModel.data != null){
                                    showToast(myDataModel.message.toString())
                                    selectedCategoryTitle?.let { getShareExchange(it) }
                                }
                            }
                        }
                        "reshare" ->{
                            val myDataModel : SimpleApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                showToast(myDataModel.message.toString())
                            }
                        }
                        "deletePost" ->{
                            val myDataModel : SimpleApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                showToast(myDataModel.message.toString())
                                selectedCategoryTitle?.let { getShareExchange(it) }

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


    /** Optional click listeners **/
    private fun initOnClick() {
        // No clicks for now
        viewModel.onClick.observe(viewLifecycleOwner, Observer {
            when(it?.id){
                R.id.addShare ->{
                    val intent = Intent(requireContext(), CommonActivity::class.java).putExtra("from","common_share")
                    startActivity(intent)
                }
            }
        })
    }

    /** Category list with preselection **/
    private fun categoryList(selectedCategory: String? = null): ArrayList<CategoryModel> {
        val items = listOf("Members", "Advisors", "Startups", "Small Businesses", "Investor", "Firm")
        return ArrayList(items.mapIndexed { index, item ->
            val isSelected = if (selectedCategory != null) {
                item.equals(selectedCategory, ignoreCase = true)
            } else index == 0
            CategoryModel(item, isSelected)
        })
    }

    /** Mapping title to API field **/
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

    override fun onDestroy() {
        super.onDestroy()
        selectedCategoryForExchange = null
    }

    override fun onResume() {
        super.onResume()
        getShareExchange("general_member")
    }
}