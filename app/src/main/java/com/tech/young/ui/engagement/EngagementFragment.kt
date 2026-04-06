package com.tech.young.ui.engagement

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
import com.tech.young.databinding.FragmentEngagementBinding
import com.tech.young.databinding.ItemLayoutAnalyticsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EngagementFragment : BaseFragment<FragmentEngagementBinding>() {

    private val viewModel : EngagementVm by viewModels()


    private lateinit var engagementAdapter : SimpleRecyclerViewAdapter<String , ItemLayoutAnalyticsBinding>

    override fun getLayoutResource(): Int {
        return R.layout.fragment_engagement
    }

    override fun getViewModel(): BaseViewModel {
         return viewModel
    }

    override fun onCreateView(view: View) {

        initAdapter()

    }

    private fun initAdapter() {
        engagementAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_analytics , BR.bean){v,m, pos ->

        }
        binding.rvAnalytics.adapter = engagementAdapter
        engagementAdapter.list = listOf<String>("","","")
    }

}