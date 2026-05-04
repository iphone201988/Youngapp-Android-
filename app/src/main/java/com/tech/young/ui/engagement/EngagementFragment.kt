package com.tech.young.ui.engagement

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.tech.young.data.model.Analytic
import com.tech.young.data.model.GetPerformanceApiResponse
import com.tech.young.data.model.GetPostAnalysisApiResponse
import com.tech.young.databinding.FragmentEngagementBinding
import com.tech.young.databinding.ItemLayoutAnalyticsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EngagementFragment : BaseFragment<FragmentEngagementBinding>() {

    private val viewModel : EngagementVm by viewModels()


    private lateinit var engagementAdapter : SimpleRecyclerViewAdapter<Analytic , ItemLayoutAnalyticsBinding>

    override fun getLayoutResource(): Int {
        return R.layout.fragment_engagement
    }

    override fun getViewModel(): BaseViewModel {
         return viewModel
    }

    override fun onCreateView(view: View) {

        initAdapter()
        initObserver()


        binding.swipeRefresh.setOnRefreshListener {
            viewModel.getEngagement(Constants.POST_ANALYTICS)
        }

    }

    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer{
            when(it?.status){
                Status.LOADING -> {
                    if (!binding.swipeRefresh.isRefreshing) {
                        showSkeleton()
                    }
                }
                Status.SUCCESS -> {
                    hideSkeleton()
                    val myDataModel :  GetPostAnalysisApiResponse ? = BindingUtils.parseJson(it.data.toString())
                    binding.swipeRefresh.isRefreshing = false

                    if (myDataModel != null){
                        if (myDataModel.data != null) {
                            if (myDataModel.data.analytics != null){
                                engagementAdapter.list = myDataModel.data.analytics as List<Analytic>
                            }
                        }
                    }

                }
                Status.ERROR ->  {
                    hideSkeleton()
                    binding.swipeRefresh.isRefreshing = false

                    showToast(it.message.toString())
                }
                else -> {

                }
            }
        })
    }

    private fun showSkeleton() {
        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.shimmerViewContainer.startShimmer()
        binding.rvAnalytics.visibility = View.GONE
    }

    private fun hideSkeleton() {
        binding.shimmerViewContainer.stopShimmer()
        binding.shimmerViewContainer.visibility = View.GONE
        binding.rvAnalytics.visibility = View.VISIBLE
    }

    private fun initAdapter() {
        engagementAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_analytics , BR.bean){v,m, pos ->

        }
        binding.rvAnalytics.adapter = engagementAdapter
    }


    override fun onResume() {
        super.onResume()
        viewModel.getEngagement(Constants.POST_ANALYTICS)

    }





}