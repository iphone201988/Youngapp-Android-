package com.tech.young.ui.my_profile_screens.profile_fragments

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.databinding.FragmentSavedBinding
import com.tech.young.ui.my_profile_screens.YourProfileVM
import com.tech.young.ui.my_profile_screens.saved_fragments.ShareFragment
import com.tech.young.ui.my_profile_screens.saved_fragments.StreamFragment
import com.tech.young.ui.my_profile_screens.saved_fragments.VaultFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.Tab
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SavedFragment : BaseFragment<FragmentSavedBinding>() {
    private val viewModel: YourProfileVM by viewModels()
    override fun onCreateView(view: View) {
        //  view
        initView()
        // click
        initOnClick()
        // observer
        initObserver()

    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_saved
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /** handle view **/
    private fun initView(){
        initViewPagerAdapter()
    }

    /** handle click **/
    private fun initOnClick(){

    }

    /** handle api response **/
    private fun initObserver(){

    }

    /** view pager & tab layout handling **/
    private fun initViewPagerAdapter(){
        val adapter = ViewPagerAdapter(this)
        binding.viewPager.isUserInputEnabled=false
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when(position){
                0-> "Share"
                1-> "Stream"
                2-> "Vault"
                else-> "Share"
            }
        }.attach()

        for (i in 0 until binding.tabLayout.tabCount) {
            binding.tabLayout.getTabAt(i)?.view?.setBackgroundResource(
                if (i == binding.tabLayout.selectedTabPosition) R.drawable.tab_bg else R.color.transparent
            )
        }

        binding.tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(p0: Tab?) {
                p0?.view?.setBackgroundResource(R.drawable.tab_bg)
            }

            override fun onTabUnselected(p0: Tab?) {
                p0?.view?.setBackgroundResource(R.color.transparent)
            }

            override fun onTabReselected(p0: Tab?) {
                p0?.view?.setBackgroundResource(R.drawable.tab_bg)
            }
        }

        )
    }

    inner class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int {
            return 3
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ShareFragment()
                1 -> StreamFragment()
                2 -> VaultFragment()
                else -> ShareFragment()
            }
        }
    }


}