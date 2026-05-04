package com.tech.young.ui.investment_plan

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
import com.tech.young.data.model.InvestmentPlanApiResponse
import com.tech.young.data.model.InvestmentPlanApiResponseData
import com.tech.young.databinding.FragmentInvestmentPlanBinding
import com.tech.young.databinding.ItemLayoutInvestmentPlanBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InvestmentPlanFragment : BaseFragment<FragmentInvestmentPlanBinding>() {

    private val viewModel : InvestmentVm by viewModels()
    private lateinit var investmentAdapter : SimpleRecyclerViewAdapter<InvestmentPlanApiResponseData, ItemLayoutInvestmentPlanBinding>

    override fun getLayoutResource(): Int {
        return R.layout.fragment_investment_plan
    }

    override fun getViewModel(): BaseViewModel {

        return viewModel
    }

    override fun onCreateView(view: View) {
        initAdapter()
        iniObserver()

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.getInvestmentPlan(Constants.INVESTMENT_PLAN)
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
                    val myDataModel :  InvestmentPlanApiResponse ? = BindingUtils.parseJson(it.data.toString())
                    if (myDataModel != null){
                        if (myDataModel.data != null) {
                                investmentAdapter.list = myDataModel.data
                                investmentAdapter.notifyDataSetChanged()
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
        investmentAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_investment_plan, BR.bean){v,m,pos ->

        }
        binding.rvInvestmentPlan.adapter = investmentAdapter
        investmentAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getInvestmentPlan(Constants.INVESTMENT_PLAN)
    }

}