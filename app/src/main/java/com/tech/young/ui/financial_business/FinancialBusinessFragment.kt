package com.tech.young.ui.financial_business

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
import com.tech.young.data.model.BusinessPlanApiResponse
import com.tech.young.data.model.BusinessPlanApiResponseData
import com.tech.young.databinding.FragmentFinancialBusinessBinding
import com.tech.young.databinding.ItemLayoutBusinessPlanBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FinancialBusinessFragment : BaseFragment<FragmentFinancialBusinessBinding>() {

    private val viewModel : FinancialBusinessVm  by viewModels()

    private lateinit var  businessAdapter : SimpleRecyclerViewAdapter<BusinessPlanApiResponseData, ItemLayoutBusinessPlanBinding>


    override fun getLayoutResource(): Int {
         return R.layout.fragment_financial_business
    }

    override fun getViewModel(): BaseViewModel {
          return viewModel
    }

    override fun onCreateView(view: View) {
        iniObserver()
        initAdapter()

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.getBusinessPlan(Constants.BUSINESS_PLAN)
        }
    }



    private fun iniObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer{
            when(it?.status){
                Status.LOADING -> {
                    if (!binding.swipeRefresh.isRefreshing) {
                        binding.shimmerLayout.visibility = View.VISIBLE
                        binding.shimmerLayout.startShimmer()
                        binding.rvInvestmentPlan.visibility = View.GONE
                    }
                }
                Status.SUCCESS -> {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    binding.rvInvestmentPlan.visibility = View.VISIBLE

                    binding.swipeRefresh.isRefreshing = false
                    val myDataModel :  BusinessPlanApiResponse ? = BindingUtils.parseJson(it.data.toString())
                    if (myDataModel != null){
                        if (myDataModel.data != null) {
                            businessAdapter.list = myDataModel.data
                            businessAdapter.notifyDataSetChanged()
                        }
                    }

                }
                Status.ERROR ->  {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    binding.rvInvestmentPlan.visibility = View.VISIBLE

                    binding.swipeRefresh.isRefreshing = false
                    showToast(it.message.toString())
                }
                else -> {

                }
            }
        })
    }

    private fun initAdapter() {
        businessAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_business_plan, BR.bean){v,m,pos ->

        }
        binding.rvInvestmentPlan.adapter = businessAdapter


    }


    override fun onResume() {
        super.onResume()
        viewModel.getBusinessPlan(Constants.BUSINESS_PLAN)

    }
}