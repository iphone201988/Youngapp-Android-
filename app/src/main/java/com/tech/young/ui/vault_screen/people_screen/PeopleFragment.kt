package com.tech.young.ui.vault_screen.people_screen

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
import com.tech.young.base.utils.event.SingleRequestEvent
import com.tech.young.data.DropDownData
import com.tech.young.data.SubViewClickBean
import com.tech.young.data.api.Constants
import com.tech.young.data.model.GetAdsAPiResponse
import com.tech.young.data.model.GetUserApiResponse
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.FragmentPeopleBinding
import com.tech.young.databinding.ItemLayoutPeoplesBinding
import com.tech.young.ui.ecosystem.EcosystemFragment
import com.tech.young.ui.exchange.ExchangeFragment
import com.tech.young.ui.vault_screen.CommonVaultFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PeopleFragment : BaseFragment<FragmentPeopleBinding>() {

    private val viewModel : PeopleFragmentVm by viewModels()
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<GetAdsAPiResponse.Data.Ad, AdsItemViewBinding>
    var selectedUserIds = ""
    private var searchJob: Job? = null
    private var searchData : String ? = null

    private var selectedActualValues : String ?= null

    private var selectedCategory: ArrayList<DropDownData>? = null
    private lateinit var userAdapter : SimpleRecyclerViewAdapter<GetUserApiResponse.Data.User, ItemLayoutPeoplesBinding>


    private var page  = 1
    private var isLoading = false
    private var isLastPage = false
    private var totalPages : Int ? = null

     companion object{
         var sendData = SingleRequestEvent<Boolean>()
     }
    override fun onCreateView(view: View) {
        initOnClick()
        viewModel.getAds(Constants.GET_ADS)
        getUsers()
        searchView()
        initAdapter()
        setObserver()


        binding.tabLayoutBottom.tabExchange.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, ExchangeFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.tabLayoutBottom.tabEcosystem.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, EcosystemFragment())
                .addToBackStack(null)
                .commit()
        }


        binding.rvPeople.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

                        loadMoreUsers()
                   //     selectedCategoryTitle?.let { loadNextPage(it) }
                    }
                }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sendData.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.LOADING -> {
                    Log.i("sdfsdfsdfsd", "Loading")
                }
                Status.SUCCESS -> {
                    CommonVaultFragment.selectedUserId = selectedUserIds
                    Log.i("sdfsdfsdfsd", "Selected IDs: $selectedUserIds")
                }
                Status.ERROR -> {
                    Log.i("sdfsdfsdfsd", "Error")
                }
                else -> {}
            }
        }
    }


    private fun loadMoreUsers() {

        isLoading = true
        page ++
        if (selectedActualValues != null){
            val data = HashMap<String, Any>().apply {
                put("category", selectedActualValues!!)  // <- set as comma-separated string
                put("page", page)
                put("limit", 20)

                if (searchData != null){
                    put("search",searchData.toString())
                }
            }
            viewModel.getUsers(data, Constants.GET_USERS)
        }
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
                    getUsers()

                }
            }
        })
    }

    private fun initAdapter() {
        userAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_peoples,BR.bean){v,m,pos ->
            when(v.id){
                R.id.consMain -> {
                    // Toggle selection
                    m.isSelected = !m.isSelected

                    // Update only the clicked item
                    userAdapter.notifyItemChanged(pos)

                    // Collect selected user IDs
                    selectedUserIds = userAdapter.getList()
                        .filter { it.isSelected }
                        .joinToString(",") { it._id.orEmpty() }

                    Log.i("SelectedUserIds", selectedUserIds)

                }
            }
        }
        binding.rvPeople.adapter = userAdapter
        userAdapter.notifyDataSetChanged()



        adsAdapter = SimpleRecyclerViewAdapter(R.layout.ads_item_view, BR.bean) { v, m, pos ->
            when (v.id) {

            }
        }
        binding.rvAds.adapter = adsAdapter
    }

    private var getList = listOf(
        "", "", "", "", ""
    )

    private fun setObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            when(it?.status){
                Status.LOADING ->{
                    showLoading()
                }
                Status.SUCCESS ->{
                    hideLoading()
                    when(it.message){
                        "getUsers" ->{
                            val myDataModel : GetUserApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    if (myDataModel.data?.users != null){
                                        userAdapter.list = myDataModel.data?.users
                                    }
                                }
                            }
                        }
                        "getAds" ->{
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
                }
                else ->{

                }
            }
        })

    }

    private fun getUsers() {
        selectedCategory = arguments?.getParcelableArrayList("selectedCategory")
        Log.i("dsadasda", "getUsers: $selectedCategory")

        selectedActualValues = selectedCategory?.joinToString(",") { it.actualValue }
        Log.i("dsdsa", "getUsers: $selectedActualValues")


        page = 1
        if (selectedActualValues != null){
            val data = HashMap<String, Any>().apply {
                put("category", selectedActualValues!!)  // <- set as comma-separated string
                put("page", 1)
                put("limit", 20)

                if (searchData != null){
                    put("search",searchData.toString())
                }
            }
            viewModel.getUsers(data, Constants.GET_USERS)
        }

    }


    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner , Observer {
            when(it?.id){
                R.id.ivBack ->{
                 //   requireActivity().onBackPressedDispatcher.onBackPressed()
                    CommonVaultFragment.selectedUserId = selectedUserIds
                    Log.i("sdfsdfsdfsd", "initOnClick: $selectedUserIds")
                }
            }
        })
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_people
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

}