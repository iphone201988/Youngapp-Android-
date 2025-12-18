package com.tech.young.ui.my_profile_screens.profile_fragments

import android.content.Intent
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
import com.tech.young.base.utils.showCustomToast
import com.tech.young.ui.common.CommonActivity
import com.tech.young.ui.ecosystem.EcosystemFragment
import com.tech.young.ui.exchange.ExchangeFragment
import com.tech.young.ui.share_screen.CommonShareFragment
import com.tech.young.ui.stream_screen.CommonStreamFragment
import com.tech.young.ui.vault_screen.CommonVaultFragment
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
        binding.shareLayout.tabShare.setOnClickListener {
//            val intent = Intent(requireContext(), CommonActivity::class.java).putExtra("from","common_share")
//            startActivity(intent)

                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, CommonShareFragment())
                    .addToBackStack(null)
                    .commit()

        }
        binding.shareLayout.tabStream.setOnClickListener {
//            val intent = Intent(requireContext(), CommonActivity::class.java).putExtra("from","common_stream")
//            startActivity(intent)

            if (sharedPrefManager.isSubscribed()){
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, CommonStreamFragment())
                    .addToBackStack(null)
                    .commit()
            } else{
                showCustomToast("Please subscribe to access this feature. Go to Profile Details > Account Details > Upgrade Plan.  ")

            }


        }
        binding.shareLayout.tabVault.setOnClickListener {
//            val intent = Intent(requireContext(), CommonActivity::class.java).putExtra("from","common_vault")
//            startActivity(intent)
            if (sharedPrefManager.isSubscribed()){
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, CommonVaultFragment())
                    .addToBackStack(null)
                    .commit()
            }else{
                showCustomToast("Please subscribe to access this feature. Go to Profile Details > Account Details > Upgrade Plan.  ")

            }

        }

        binding.tabLayoutBottom.tabExchange.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, ExchangeFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.tabLayoutBottom.tabEcosystem.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, EcosystemFragment())
                .addToBackStack(null)
                .commit()
        }
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