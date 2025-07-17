package com.tech.young.ui.ecosystem

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
import com.tech.young.data.model.GetLatestUserApiResponse
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.CategoryItemViewBinding
import com.tech.young.databinding.FragmentEcosystemBinding
import com.tech.young.databinding.ItemViewUsersBinding
import com.tech.young.ui.common.CommonActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EcosystemFragment : BaseFragment<FragmentEcosystemBinding>() {

    private val viewModel: EcosystemVM by viewModels()

    // adapters
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<String, AdsItemViewBinding>
    private lateinit var categoryAdapter: SimpleRecyclerViewAdapter<CategoryModel, CategoryItemViewBinding>
    private lateinit var usersAdapter: SimpleRecyclerViewAdapter<GetLatestUserApiResponse.Data.User, ItemViewUsersBinding>

    // list data
    private lateinit var categoryData: ArrayList<CategoryModel>
    private var selectedCategoryTitle: String? = null
    private val getList = listOf("", "", "", "", "")

    companion object {
        var selectedCategoryForEcosystem: String? = null
    }

    override fun getLayoutResource(): Int = R.layout.fragment_ecosystem
    override fun getViewModel(): BaseViewModel = viewModel

    override fun onCreateView(view: View) {
        initView()
        initOnClick()
        initObserver()
    }

    /** Setup initial view state **/
    private fun initView() {


        getLatestUser()

        initAdapters()

        // Initialize and set category list once
        categoryData = categoryList(selectedCategoryForEcosystem)
        selectedCategoryTitle = categoryData.find { it.isSelected }?.title

        categoryAdapter.list = categoryData
        binding.rvCategories.adapter = categoryAdapter

        // If needed, you can call a method here like:
        // fetchUsersOrAds(selectedCategoryTitle)
    }

    private fun getLatestUser() {
        val data = HashMap<String,Any>()
        data["category"] = "general_member"
        viewModel.getLatestUser(data,Constants.ECO_SYSTEM)
    }

    /** Click observers **/
    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity()) {
            when (it?.id) {
                R.id.ivBack -> {
                    // handle back
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
                    if (!m.isSelected) {
                        categoryData.forEach { it.isSelected = false }
                        categoryData[pos].isSelected = true
                        selectedCategoryTitle = m.title
                        categoryAdapter.notifyDataSetChanged()

                        val apiTitle = mapTitleToApiValue(selectedCategoryTitle!!)
                        val data = hashMapOf<String, Any>(
                            "category" to apiTitle,
                        )
                        viewModel.getLatestUser(data,Constants.ECO_SYSTEM)
                    // Optional: Fetch/update data on selection
                        // fetchUsersOrAds(selectedCategoryTitle!!)
                    }
                }
            }
        }

        usersAdapter = SimpleRecyclerViewAdapter(R.layout.item_view_users, BR.bean) { v, m, pos ->
            when (v.id) {
                R.id.clInbox ->{
                    val intent= Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("from","user_profile")
                    intent.putExtra("userId","${m._id}")
                    startActivity(intent)
                }
            }
        }
        binding.rvUsers.adapter = usersAdapter

        adsAdapter = SimpleRecyclerViewAdapter(R.layout.ads_item_view, BR.bean) { v, m, pos ->
            when (v.id) {
                // Handle ad item clicks if needed
            }
        }
        adsAdapter.list = getList
        binding.rvAds.adapter = adsAdapter
    }

    /** Create initial category list **/
    private fun categoryList(selectedCategory: String? = null): ArrayList<CategoryModel> {
        val items = listOf(
            "Members",
            "Advisors",
            "Startups",
            "Small Businesses",
            "Insurance",
            "VCs"
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
            "Insurance" -> "insurance"
            "VCs" -> "investor"
            "Members" -> "general_member"
            else -> title.lowercase().replace(" ", "_")
        }
    }
}