package com.tech.young.ui.investment_tracker

import InvestmentAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showToast
import com.tech.young.data.api.Constants
import com.tech.young.data.model.GetPortfolioApiResponse
import com.tech.young.databinding.FragmentPortfolioBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PortfolioFragment : BaseFragment<FragmentPortfolioBinding>() {


    private val viewModel: InvestmentTrackingVm by activityViewModels()
    private lateinit var investmentAdapter: InvestmentAdapter

    override fun getLayoutResource(): Int {
        return R.layout.fragment_portfolio
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {

        setupRecyclerView()
        setupSwipeRefresh()
        initObserver()
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.getPortfolio(Constants.GET_INVESTMENT)
        }
    }

    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            when (it?.status) {
                Status.LOADING -> {
                    if (!binding.swipeRefresh.isRefreshing) {
                        binding.shimmerLayout.visibility = View.VISIBLE
                        binding.shimmerLayout.startShimmer()
                        binding.recyclerView.visibility = View.GONE
                    }
                }

                Status.SUCCESS -> {
                    binding.swipeRefresh.isRefreshing = false
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                    
                    when (it.message) {
                        "getPortfolio" -> {
                            val myDataModel: GetPortfolioApiResponse? =
                                BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null) {
                                if (myDataModel.data != null) {
                                    investmentAdapter.updateList(myDataModel.data)
                                }
                            }
                        }
                    }


                }

                Status.ERROR -> {
                    binding.swipeRefresh.isRefreshing = false
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                    showToast(it.message.toString())
                }

                else -> {}
            }
        })
    }


    private fun setupRecyclerView() {
        investmentAdapter = InvestmentAdapter(emptyList()){ item ->
            viewModel.selectedInvestment.value = item
            (parentFragment as? InvestmentTrackerFragment)?.switchToValuesTab()
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = investmentAdapter
    }

    override fun onResume() {
        super.onResume()
        viewModel.getPortfolio(Constants.GET_INVESTMENT)

    }
}
