package com.tech.young.ui.my_profile_screens.forNormal

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.tabs.TabLayoutMediator
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.BaseCustomDialog
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showToast
import com.tech.young.data.api.Constants
import com.tech.young.data.model.GetPerformanceApiResponse
import com.tech.young.databinding.FragmentPerformanceBinding
import com.tech.young.databinding.ItemInvestmentsBinding
import com.tech.young.databinding.ItemPopupPerformanceGraphBinding
import com.tech.young.ui.my_profile_screens.YourProfileVM
import com.tech.young.ui.my_profile_screens.profile_fragments.CalendarFragment
import com.tech.young.ui.my_profile_screens.profile_fragments.EditProfileFragment
import com.tech.young.ui.my_profile_screens.profile_fragments.ProfileFragment
import com.tech.young.ui.my_profile_screens.profile_fragments.SavedFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class PerformanceFragment : BaseFragment<FragmentPerformanceBinding>() {

    private val viewModel: YourProfileVM by activityViewModels()



    override fun getLayoutResource(): Int {
        return R.layout.fragment_performance
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
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


        ViewCompat.setOnApplyWindowInsetsListener(binding.clInvestment) { view, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            // Only adjust padding if keyboard is visible
            view.setPadding(0, 0, 0, imeHeight)
            insets
        }


        initViewPagerAdapter()
        initAdapter()

        initObserver()
    }

    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer{
            when(it?.status){
                Status.LOADING -> {
                    if (it.message == "getPerformance") {
                        binding.shimmerLayout.visibility = View.VISIBLE
                        binding.shimmerLayout.startShimmer()
                        binding.clInvestment.visibility = View.GONE
                    }
                }
                Status.SUCCESS -> {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    binding.clInvestment.visibility = View.VISIBLE
                    
                    when(it.message){
                        "getPerformance" ->{
                            val myDataModel :  GetPerformanceApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    myDataModel.data?.let {
                                        viewModel.performanceData.postValue(it)
                                    }
                                }
                                myDataModel.data?.performance?.let {
                                    binding.bean = myDataModel.data.performance
                                }

                            }
                        }
                        "getMonthlyAnalytics" ->{

                        }
                    }

                }
                Status.ERROR ->  {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    binding.clInvestment.visibility = View.VISIBLE
                    showToast(it.message.toString())
                }
                else -> {

                }
            }
        })
    }


    private fun initAdapter() {

    }







    /** view pager & tab layout handling **/
    private fun initViewPagerAdapter() {
        val adapter = ViewPagerAdapter(this)
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Your Investments"
                1 -> "Similar Profiles"
                else -> "Your Investments"
            }
        }.attach()
    }

    inner class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> YourInvestmentFragment()
                1 -> SimilarProfileFragment()
                else -> YourInvestmentFragment()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        viewModel.getPerformance(Constants.GET_PERFORMANCE)
    }

}
