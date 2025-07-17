package com.tech.young.ui.my_profile_screens

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.databinding.FragmentYourProfileBinding
import com.tech.young.ui.my_profile_screens.profile_fragments.CalendarFragment
import com.tech.young.ui.my_profile_screens.profile_fragments.EditProfileFragment
import com.tech.young.ui.my_profile_screens.profile_fragments.ProfileFragment
import com.tech.young.ui.my_profile_screens.profile_fragments.SavedFragment
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class YourProfileFragment : BaseFragment<FragmentYourProfileBinding>() {
    private val viewModel: YourProfileVM by viewModels()
    override fun onCreateView(view: View) {
        // view
        initView()
        // click
        initOnClick()
        // observer
        initObserver()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_your_profile
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /** handle view **/
    private fun initView() {
        initViewPagerAdapter()
    }

    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity()) {
            when (it?.id) {

            }
        }

    }

    /** handle api response **/
    private fun initObserver() {

    }

    /** view pager & tab layout handling **/
    private fun initViewPagerAdapter() {
        val adapter = ViewPagerAdapter(this)
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Profile"
                1 -> "Edit"
                2 -> "Calendar"
                3 -> "Saved"
                else -> "Profile"
            }
        }.attach()
    }

    inner class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int {
            return 4
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ProfileFragment()
                1 -> EditProfileFragment()
                2 -> CalendarFragment()
                3 -> SavedFragment()
                else -> ProfileFragment()
            }
        }
    }

}