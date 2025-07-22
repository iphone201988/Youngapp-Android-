package com.tech.young.ui.policies_about

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
import com.tech.young.data.model.GetAdsAPiResponse
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.FragmentPoliciesBinding
import com.tech.young.databinding.PolicyItemViewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PoliciesFragment : BaseFragment<FragmentPoliciesBinding>() {
    private val viewModel: AboutPolicesVM by viewModels()
    // adapter
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<GetAdsAPiResponse.Data.Ad, AdsItemViewBinding>
    private lateinit var policiesAdapter: SimpleRecyclerViewAdapter<String, PolicyItemViewBinding>
    override fun onCreateView(view: View) {
       // view
        initView()


        viewModel.getAds(Constants.GET_ADS)
        // click
        initOnClick()
        // observer
        initObserver()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_policies
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /** handle view **/
    private fun initView(){
        initAdapter()

    }

    /** handle click **/
    private fun initOnClick(){
        viewModel.onClick.observe(requireActivity()){
            when(it?.id){
            }
        }

    }

    /** handle api response **/
    private fun initObserver(){
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            when(it?.status){
                Status.LOADING ->{
                    showLoading()
                }
                Status.SUCCESS ->{
                    hideLoading()
                    when(it.message){
                        "getAds" ->{
                            val myDataModel : GetAdsAPiResponse? = BindingUtils.parseJson(it.data.toString())
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
                else ->{}
            }
        })
    }

    /** handle adapter **/
    private fun initAdapter() {
        adsAdapter = SimpleRecyclerViewAdapter(R.layout.ads_item_view, BR.bean) { v, m, pos ->
            when (v.id) {

            }
        }
        binding.rvAds.adapter=adsAdapter

        policiesAdapter = SimpleRecyclerViewAdapter(R.layout.policy_item_view, BR.bean) { v, m, pos ->
            when (v.id) {

            }
        }
        policiesAdapter.list = getList
        binding.rvPolicies.adapter=policiesAdapter
    }

    private var getList = listOf(
        "", "", "", "", ""
    )

}