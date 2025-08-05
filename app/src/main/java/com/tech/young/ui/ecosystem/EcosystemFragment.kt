package com.tech.young.ui.ecosystem

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
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
import com.tech.young.data.FilterItem
import com.tech.young.data.api.Constants
import com.tech.young.data.model.CategoryModel
import com.tech.young.data.model.GetAdsAPiResponse
import com.tech.young.data.model.GetLatestUserApiResponse
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.CategoryItemViewBinding
import com.tech.young.databinding.FragmentEcosystemBinding
import com.tech.young.databinding.ItemLayoutFiterBinding
import com.tech.young.databinding.ItemViewUsersBinding
import com.tech.young.ui.common.CommonActivity
import com.tech.young.ui.exchange.ExchangeFragment
import com.tech.young.ui.home.HomeActivity
import com.tech.young.ui.share_screen.CommonShareFragment
import com.tech.young.ui.user_profile.UserProfileFragment
import com.tech.young.utils.VerticalPagination
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EcosystemFragment : BaseFragment<FragmentEcosystemBinding>() {

    private val viewModel: EcosystemVM by viewModels()

    // adapters
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<GetAdsAPiResponse.Data.Ad, AdsItemViewBinding>
    private lateinit var categoryAdapter: SimpleRecyclerViewAdapter<CategoryModel, CategoryItemViewBinding>
    private lateinit var usersAdapter: SimpleRecyclerViewAdapter<GetLatestUserApiResponse.Data.User, ItemViewUsersBinding>
    private var searchJob: Job? = null
    private var apiTitle : String ? = null
    private lateinit var filterAdapter  : SimpleRecyclerViewAdapter<FilterItem, ItemLayoutFiterBinding>
    private var filterList = ArrayList<FilterItem>()
    // list data
    private lateinit var categoryData: ArrayList<CategoryModel>
    private var selectedCategoryTitle: String? = null
    private val getList = listOf("", "", "", "", "")
    private var userSelectedKey : String ? = null
    private var selectedFilterData : FilterItem ? = null
    private var searchData : String ? = null


    private var page  = 1
    private var isLoading = false
    private var isLastPage = false
    private var totalPages : Int ? = null


    companion object {
        var selectedCategoryForEcosystem: String? = null
    }

    override fun getLayoutResource(): Int = R.layout.fragment_ecosystem
    override fun getViewModel(): BaseViewModel = viewModel

    override fun onCreateView(view: View) {
        initView()
        initOnClick()
        searchView()
        initObserver()

        binding.rvUsers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                        selectedCategoryTitle?.let { loadNextPage(it) }
                    }
                }
            }
        })


        binding.tabLayoutBottom.tabEcosystem.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, EcosystemFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.tabLayoutBottom.tabExchange.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, ExchangeFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun loadNextPage(title: String) {
        isLoading = true
        page++


        val apiTitle = mapTitleToApiValue(title)  // ← safe mapping for userType
        Log.i("Dasdasd", "getShareExchange:$apiTitle ")

        val data = hashMapOf<String, Any>(
            "category" to apiTitle
        )
        if (userSelectedKey?.isNotEmpty() == true) {
            data[userSelectedKey!!] = true
        }

        if (selectedFilterData != null){
            selectedFilterData?.let {
                if (it.key.isNotEmpty() && it.value != null) {
                    data[it.key] = it.value
                }
            }
        }

        if (searchData != null){
            data["search"] = searchData!!
        }

        viewModel.getLatestUser(data,Constants.GET_USERS)
    }


    private fun getFilterList() {
        filterList.clear()

        filterList.add(FilterItem("Date Posted", isSelected = true)) // No API key
        filterList.add(FilterItem("Number of Customers", key = "byCustomers", value = true))
        filterList.add(FilterItem("Number of Followers", key = "byFollowers", value = true))
        filterList.add(FilterItem("Rating", isHeader = true)) // just a header, not selectable

        filterList.add(FilterItem("1 Star", key = "rating", value = 1))
        filterList.add(FilterItem("2 Star", key = "rating", value = 2))
        filterList.add(FilterItem("3 Star", key = "rating", value = 3))
        filterList.add(FilterItem("4 Star", key = "rating", value = 4))
        filterList.add(FilterItem("5 Star", key = "rating", value = 5))
    }


    private fun searchView() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Cancel any previous job to avoid multiple triggers
                searchJob?.cancel()

                // Launch coroutine after delay
                searchJob = lifecycleScope.launch {
                    delay(500) // 3 seconds delay

                    searchData = s.toString().trim()
                    getLatestUser(selectedCategoryTitle.toString())

                }
            }
        })
    }

//    private fun callSearchApi(query: String) {
//        if (apiTitle != null){
//            val data = HashMap<String,Any>()
//            data["search"] = query
//            data["category"] = apiTitle.toString()
//            viewModel.getLatestUser(data,Constants.GET_USERS)
//        }
//        else{
//            val data = HashMap<String,Any>()
//            data["search"] = query
//            data["category"] = "general_member"
//            viewModel.getLatestUser(data,Constants.GET_USERS)
//        }
//
//    }

    /** Setup initial view state **/
    private fun initView() {
        getFilterList()
        viewModel.getAds(Constants.GET_ADS)

        binding.includeShare.tabShare.setOnClickListener {
//            val intent = Intent(requireContext(),CommonActivity::class.java).putExtra("from","common_share")
//            startActivity(intent)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, CommonShareFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.includeShare.tabStream.setOnClickListener {
//            val intent = Intent(requireContext(),CommonActivity::class.java).putExtra("from","common_stream")
//            startActivity(intent)

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, CommonShareFragment())
                .addToBackStack(null)
                .commit()
        }


        binding.includeShare.tabVault.setOnClickListener {
//            val intent = Intent(requireContext(),CommonActivity::class.java).putExtra("from","common_vault")
//            startActivity(intent)

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, CommonShareFragment())
                .addToBackStack(null)
                .commit()
        }

        initAdapters()

        // Initialize and set category list once
        categoryData = categoryList(selectedCategoryForEcosystem)
        selectedCategoryTitle = categoryData.find { it.isSelected }?.title

        categoryAdapter.list = categoryData
        binding.rvCategories.adapter = categoryAdapter

   //     selectedCategoryTitle?.let { getLatestUser(it) }


        // If needed, you can call a method here like:
        // fetchUsersOrAds(selectedCategoryTitle)
    }

//    private fun getLatestUser() {
//        val data = HashMap<String,Any>()
//        data["category"] = "general_member"
//        viewModel.getLatestUser(data,Constants.GET_USERS)
//    }

    private fun getLatestUser(title: String) {
        val apiTitle = mapTitleToApiValue(title)  // ← safe mapping for userType
        Log.i("Dasdasd", "getShareExchange:$apiTitle ")

        val data = hashMapOf<String, Any>(
            "category" to apiTitle
        )
        if (userSelectedKey?.isNotEmpty() == true) {
            data[userSelectedKey!!] = true
        }

        if (selectedFilterData != null){
            selectedFilterData?.let {
                if (it.key.isNotEmpty() && it.value != null) {
                    data[it.key] = it.value
                }
            }
        }

        if (searchData != null){
            data["search"] = searchData!!
        }

        viewModel.getLatestUser(data,Constants.GET_USERS)

    }

    /** Click observers **/
    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity()) {
            when (it?.id) {
                R.id.ivBack -> {
                    // handle back
                }
                R.id.filterList ->{
                    binding.rvFilter.visibility =
                        if (binding.rvFilter.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                }
            }
        }
    }

    /** Data observers **/
    private fun initObserver() {
        // observe API data if needed

         viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
             when(it?.status){
                 Status.LOADING ->{
                     showLoading()
                 }
                 Status.SUCCESS ->{
                     hideLoading()
                     when(it.message){
                         "getLatestUser" ->{
                             val myDataModel : GetLatestUserApiResponse ? = BindingUtils.parseJson(it.data.toString())
                             if (myDataModel != null){
                                 if (myDataModel.data != null){
                                     if (myDataModel.data?.users != null){
                                         usersAdapter.list = myDataModel.data?.users
                                     }
                                 }
                             }
                         }
                         "getAds" ->{
                             hideLoading()
                             val myDataModel : GetAdsAPiResponse ? = BindingUtils.parseJson(it.data.toString())
                             if (myDataModel != null){
                                 if (myDataModel.data != null){
                                     adsAdapter.list = myDataModel.data?.ads
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

    /** Adapters **/
    private fun initAdapters() {
        categoryAdapter = SimpleRecyclerViewAdapter(R.layout.category_item_view, BR.bean) { v, m, pos ->
            when (v.id) {
                R.id.clMain -> {
//                    if (!m.isSelected) {
//                        categoryData.forEach { it.isSelected = false }
//                        categoryData[pos].isSelected = true
//                        selectedCategoryTitle = m.title
//                        categoryAdapter.notifyDataSetChanged()
//
//                         apiTitle = mapTitleToApiValue(selectedCategoryTitle!!)
//                        val data = hashMapOf<String, Any>(
//                            "category" to apiTitle.toString(),
//                        )
//                        viewModel.getLatestUser(data,Constants.ECO_SYSTEM)
//                    // Optional: Fetch/update data on selection
//                        // fetchUsersOrAds(selectedCategoryTitle!!)
//                    }

                    if (!m.isSelected) {
                        categoryData.forEach { it.isSelected = false }
                        categoryData[pos].isSelected = true
                        selectedCategoryTitle = m.title
                        categoryAdapter.notifyDataSetChanged()

                        getLatestUser(m.title)
                    }
                }
            }
        }

        usersAdapter = SimpleRecyclerViewAdapter(R.layout.item_view_users, BR.bean) { v, m, pos ->
            when (v.id) {
                R.id.clInbox ->{
//                    val intent= Intent(requireContext(), CommonActivity::class.java)
//                    intent.putExtra("from","user_profile")
//                    intent.putExtra("userId","${m._id}")
//                    startActivity(intent)

                    val bundle = Bundle().apply {
                        putString("from", "user_profile")
                        putString("userId", m._id) // assuming m._id is a String
                    }
                    val name = m.firstName + " " + m.lastName  // ← add space here
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
        binding.rvUsers.adapter = usersAdapter

        adsAdapter = SimpleRecyclerViewAdapter(R.layout.ads_item_view, BR.bean) { v, m, pos ->
            when (v.id) {
                // Handle ad item clicks if needed
            }
        }
        binding.rvAds.adapter = adsAdapter


        filterAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_fiter, BR.bean) { v, m, pos ->
            when (v.id) {
                R.id.consMain -> {
                    try {
                        val clickedItem = filterList.getOrNull(pos) // safe access
                        if (clickedItem != null) {
                            val wasSelected = clickedItem.isSelected

                            // Deselect all
                            filterList.forEach { it.isSelected = false }

                            // Toggle selection
                            if (!wasSelected) {
                                clickedItem.isSelected = true
                            }

                            filterAdapter.notifyDataSetChanged()

                            // Call onFilterApplied directly (same fragment)
                            if (wasSelected) {
                                onFilterApplied(null) // deselect case
                            } else {
                                onFilterApplied(clickedItem) // selected case
                            }

                            binding.rvFilter.visibility = View.GONE
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        binding.rvFilter.adapter = filterAdapter
        filterAdapter.list = filterList
        filterAdapter.notifyDataSetChanged()

    }


    fun onFilterApplied(selectedFilter: FilterItem?) {
        selectedFilterData = selectedFilter

        userSelectedKey = if (selectedFilter?.isSelected == true) {
            selectedFilter.key
        } else {
            ""
        }

        getLatestUser(selectedCategoryTitle.toString())
    }


    /** Create initial category list **/
    private fun categoryList(selectedCategory: String? = null): ArrayList<CategoryModel> {
        val items = listOf(
            "Members",
            "Advisors",
            "Startups",
            "Small Businesses",
            "Investor",
            "Firm"
        )

        return ArrayList(items.mapIndexed { index, item ->
            val isSelected = if (selectedCategory != null) {
                item.equals(selectedCategory, ignoreCase = true)
            } else {
                index == 0
            }
            CategoryModel(item, isSelected)
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

    override fun onResume() {
        super.onResume()
        getLatestUser(selectedCategoryTitle.toString())

    }
}