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
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.databinding.FragmentInvestmentTrackerBinding
import com.tech.young.ui.my_profile_screens.YourProfileVM
import com.tech.young.ui.my_profile_screens.forNormal.SimilarProfileFragment
import com.tech.young.ui.my_profile_screens.forNormal.YourInvestmentFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue


@AndroidEntryPoint
class InvestmentTrackerFragment : BaseFragment<FragmentInvestmentTrackerBinding>() {

    private val viewModel: InvestmentTrackingVm by viewModels()

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