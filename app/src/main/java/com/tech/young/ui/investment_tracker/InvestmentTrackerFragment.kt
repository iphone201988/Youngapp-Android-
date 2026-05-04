package com.tech.young.ui.investment_tracker

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.databinding.FragmentInvestmentTrackerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InvestmentTrackerFragment : BaseFragment<FragmentInvestmentTrackerBinding>() {

    private val viewModel: InvestmentTrackingVm by activityViewModels()
    private var isProgrammaticSelection = false

    override fun getLayoutResource(): Int {
        return R.layout.fragment_investment_tracker
    }


    override fun getViewModel(): BaseViewModel {
        return  viewModel
    }

    override fun onCreateView(view: View) {

        val fullText = "Quarterly profile updates improve your Match Score accuracy by up to 34%"

        val spannable = SpannableString(fullText)

        val matchScoreStart = fullText.indexOf("Match Score")
        val matchScoreEnd = matchScoreStart + "Match Score".length
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.colorPrimary)),
            matchScoreStart,
            matchScoreEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val percentStart = fullText.indexOf("34%")
        val percentEnd = percentStart + "34%".length
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.green)),
            percentStart,
            percentEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvInfo.text = spannable

        initViewPagerAdapter()

        // Handle starting tab from navigation arguments
        val tabIndex = arguments?.getInt("tabIndex", 0) ?: 0
        binding.viewPager.post {
            binding.viewPager.currentItem = tabIndex
        }
    }

    fun switchToValuesTab() {
        isProgrammaticSelection = true
        binding.viewPager.currentItem = 1
    }

    /** view pager & tab layout handling **/
    private fun initViewPagerAdapter() {
        val adapter = ViewPagerAdapter(this)
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Portfolio"
                1 -> "Values & Goals"
                else -> "Portfolio"
            }
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 1) {
                    if (!isProgrammaticSelection) {
                        viewModel.selectedInvestment.value = null
                    }
                }
                isProgrammaticSelection = false
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {
                if (tab?.position == 1) {
                    viewModel.selectedInvestment.value = null
                }
            }
        })
    }

    inner class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> PortfolioFragment()
                1 -> ValuesGoalFragment()
                else -> PortfolioFragment()
            }
        }
    }
}
