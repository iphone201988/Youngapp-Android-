package com.tech.young.ui.vault_screen.people_screen

import android.util.Log
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
import com.tech.young.data.DropDownData
import com.tech.young.data.api.Constants
import com.tech.young.data.model.GetAdsAPiResponse
import com.tech.young.data.model.GetUserApiResponse
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.FragmentPeopleBinding
import com.tech.young.databinding.ItemLayoutPeoplesBinding
import com.tech.young.ui.vault_screen.CommonVaultFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PeopleFragment : BaseFragment<FragmentPeopleBinding>() {

    private val viewModel : PeopleFragmentVm by viewModels()
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<GetAdsAPiResponse.Data.Ad, AdsItemViewBinding>
    var selectedUserIds = ""
    private var selectedCategory: ArrayList<DropDownData>? = null
    private lateinit var userAdapter : SimpleRecyclerViewAdapter<GetUserApiResponse.Data.User, ItemLayoutPeoplesBinding>
    override fun onCreateView(view: View) {
        initOnClick()
        viewModel.getAds(Constants.GET_ADS)
        getUsers()
        initAdapter()
        setObserver()
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

        val selectedActualValues = selectedCategory?.joinToString(",") { it.actualValue }
        Log.i("dsdsa", "getUsers: $selectedActualValues")


        if (selectedActualValues != null){
            val data = HashMap<String, Any>().apply {
                put("category", selectedActualValues)  // <- set as comma-separated string
                put("page", 1)
                put("limit", 20)
            }
            viewModel.getUsers(data, Constants.GET_USERS)
        }

    }


    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner , Observer {
            when(it?.id){
                R.id.ivBack ->{
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                    CommonVaultFragment.selectedUserId = selectedUserIds
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