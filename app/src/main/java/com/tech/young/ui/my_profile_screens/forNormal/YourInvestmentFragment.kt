package com.tech.young.ui.my_profile_screens.forNormal

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.databinding.FragmentYourInvestmentBinding
import com.tech.young.databinding.ItemInvestmentsBinding
import com.tech.young.ui.my_profile_screens.YourProfileVM
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class YourInvestmentFragment : BaseFragment<FragmentYourInvestmentBinding>() {

    private val viewModel: YourProfileVM by viewModels()
    private lateinit var investmentAdapter : SimpleRecyclerViewAdapter<String , ItemInvestmentsBinding>


    override fun getLayoutResource(): Int {
        return R.layout.fragment_your_investment
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {

        initAdapter()
    }

    private fun initAdapter() {
        investmentAdapter = SimpleRecyclerViewAdapter(
            R.layout.item_investments,
            BR.bean
        ) { v, m, pos ->
            when(v.id){
                R.id.consMain ->{
                }
            }
        }

          binding.rvInvestment.adapter = investmentAdapter
        investmentAdapter.list = listOf("", "", "")
    }


}