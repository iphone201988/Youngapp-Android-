package com.tech.young.ui.investment_tracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.databinding.FragmentValuesGoalBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class ValuesGoalFragment : BaseFragment<FragmentValuesGoalBinding>() {

    private val viewModel: InvestmentTrackingVm by viewModels()


    override fun getLayoutResource(): Int {
      return  R.layout.fragment_values_goal
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
    }

}