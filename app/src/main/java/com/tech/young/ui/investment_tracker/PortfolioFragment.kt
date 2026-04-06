package com.tech.young.ui.investment_tracker

import InvestmentAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.data.InvestmentItem
import com.tech.young.databinding.FragmentPortfolioBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue


@AndroidEntryPoint
class PortfolioFragment : BaseFragment<FragmentPortfolioBinding>() {


    private val viewModel: InvestmentTrackingVm by viewModels()

    override fun getLayoutResource(): Int {
        return R.layout.fragment_portfolio
    }

    override fun getViewModel(): BaseViewModel {
    return viewModel
    }

    override fun onCreateView(view: View) {

        setupRecyclerView()
    }




    private fun setupRecyclerView() {

        val list = listOf(
            InvestmentItem("AAPL", "Apple Inc.", "Stock", "$4,200", false),
            InvestmentItem("VTI", "Vanguard Total Market ETF", "ETF", "$4,200", true),
            InvestmentItem("BTC", "Bitcoin", "Crypto", "$4,200", true),
            InvestmentItem("REIT1", "Realty Income Corp", "Real Estate", "$4,200", false)
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = InvestmentAdapter(list)
    }

}