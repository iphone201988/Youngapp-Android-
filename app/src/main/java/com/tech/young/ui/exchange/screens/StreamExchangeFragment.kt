package com.tech.young.ui.exchange.screens

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
import com.tech.young.data.model.GetStreamApiResponse
import com.tech.young.data.model.SavedPostApiResponse
import com.tech.young.data.model.VaultExchangeApiResponse
import com.tech.young.databinding.CategoryItemViewBinding
import com.tech.young.databinding.FragmentStreamExchangeBinding
import com.tech.young.databinding.ItemLayoutStreamExchangeBinding
import com.tech.young.ui.common.CommonActivity
import com.tech.young.ui.exchange.ExchangeVM
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.internal.ignoreIoExceptions

@AndroidEntryPoint
class StreamExchangeFragment : BaseFragment<FragmentStreamExchangeBinding>() {
    private val viewModel: ExchangeVM by viewModels()
    private lateinit var categoryAdapter: SimpleRecyclerViewAdapter<CategoryModel, CategoryItemViewBinding>
    private lateinit var streamAdapter : SimpleRecyclerViewAdapter<GetStreamApiResponse.Data.Post,ItemLayoutStreamExchangeBinding>
    private  lateinit  var categoryData : ArrayList<CategoryModel>
    private var selectedCategoryTitle: String? = null


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

        initAdapter()


        // Set category list
        categoryData = categoryList(selectedCategoryForExchange)
        selectedCategoryTitle = categoryData.find { it.isSelected }?.title

        categoryAdapter.list = categoryData
        binding.rvCategories.adapter = categoryAdapter

        // Initial fetch
        selectedCategoryTitle?.let { getStreamExchange(it) }
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

            }

        }
        binding.rvStream.adapter = streamAdapter
        streamAdapter.notifyDataSetChanged()
    }

    /** handle click **/
    private fun initOnClick() {

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
        viewModel.getStream(data, Constants.GET_ALL_POST)
    }

}