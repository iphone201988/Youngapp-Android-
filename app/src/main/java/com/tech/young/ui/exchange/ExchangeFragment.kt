package com.tech.young.ui.exchange

import android.util.Log
import android.util.SparseArray
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.databinding.FragmentExchangeBinding
import com.tech.young.ui.exchange.screens.ShareExchangeFragment
import com.tech.young.ui.exchange.screens.VaultExchangeFragment
import com.tech.young.ui.my_profile_screens.saved_fragments.ShareFragment
import com.tech.young.ui.my_profile_screens.saved_fragments.StreamFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.Tab
import com.google.android.material.tabs.TabLayoutMediator
import com.tech.young.BR
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.data.FilterItem
import com.tech.young.data.SortingItem
import com.tech.young.databinding.ItemLayoutFiterBinding
import com.tech.young.databinding.ItemLayoutSortDataBinding
import com.tech.young.ui.exchange.screens.StreamExchangeFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExchangeFragment : BaseFragment<FragmentExchangeBinding>() {

    private val viewModel: ExchangeVM by viewModels()
    private lateinit var filterAdapter  : SimpleRecyclerViewAdapter<FilterItem, ItemLayoutFiterBinding>
    private var filterList = ArrayList<FilterItem>()
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private var searchJob: Job? = null

    override fun onCreateView(view: View) {
        // view
        initView()
        // click
        initOnClick()
        // observer
        initObserver()

        // Search TextListener
        binding.etSearch.doAfterTextChanged {
            sendSearchQueryToActiveFragment(it.toString())
        }
    }


    override fun getLayoutResource(): Int {
        return R.layout.fragment_exchange
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /** handle view **/
    private fun initView() {
        // viewpager adapter
        getFilterList()
        initViewPagerAdapter()
        initAdapter()

    }

    private fun getFilterList() {
        filterList.clear()

        filterList.add(FilterItem("Date Posted", isSelected = true)) // No API key
        filterList.add(FilterItem("Distance", key = "distance", value = true))
        filterList.add(FilterItem("Rating", isHeader = true)) // just a header, not selectable

        filterList.add(FilterItem("1 Star", key = "rating", value = 1))
        filterList.add(FilterItem("2 Star", key = "rating", value = 2))
        filterList.add(FilterItem("3 Star", key = "rating", value = 3))
        filterList.add(FilterItem("4 Star", key = "rating", value = 4))
        filterList.add(FilterItem("5 Star", key = "rating", value = 5))
    }


    private fun initAdapter() {
        filterAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_fiter, BR.bean) { v, m, pos ->
            when (v.id) {
                R.id.consMain -> {
                    val clickedItem = filterList[pos]
                    val wasSelected = clickedItem.isSelected
                    filterList.forEach { it.isSelected = false }
                    clickedItem.isSelected = !wasSelected
                    filterAdapter.notifyDataSetChanged()

                    val currentItem = binding.viewPager.currentItem
                    val fragment = viewPagerAdapter.getFragment(currentItem)
                    Log.i("dsfsdfs", "initAdapter: $clickedItem, $currentItem , $fragment")

                    if (fragment is Filterable) {
                        fragment.onFilterApplied(clickedItem)
                    }

                    binding.rvFilter.visibility = View.GONE
                }
            }
        }

        binding.rvFilter.adapter = filterAdapter
        filterAdapter.list = filterList

    }

    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer {
            when(it?.id){
                R.id.filterList ->{
                    binding.rvFilter.visibility = View.VISIBLE
                }
            }
        })
    }


    private fun sendSearchQueryToActiveFragment(query: String) {
        searchJob?.cancel()  // Cancel any previous search

        searchJob = lifecycleScope.launch {
            delay(1000) // 300ms debounce delay
            Log.i("search", "Debounced Query Sent: $query")

            val currentPosition = binding.viewPager.currentItem
            val fragment = viewPagerAdapter.getFragment(currentPosition)
            if (fragment is Filterable) {
                fragment.onSearchQueryChanged(query)
            }
        }
    }


    /** handle observer **/
    private fun initObserver() {

    }

    /** view pager & tab layout handling **/
    private fun initViewPagerAdapter() {
        viewPagerAdapter = ViewPagerAdapter(this)
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.adapter = viewPagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Share"
                1 -> "Stream"
                2 -> "Vault"
                else -> "Share"
            }
        }.attach()

        for (i in 0 until binding.tabLayout.tabCount) {
            binding.tabLayout.getTabAt(i)?.view?.setBackgroundResource(
                if (i == binding.tabLayout.selectedTabPosition) R.drawable.tab_bg else R.color.transparent
            )
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
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

        // Hold fragment references safely
        private val fragmentMap = SparseArray<Fragment>()

        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            val fragment = when (position) {
                0 -> ShareExchangeFragment()
                1 -> StreamExchangeFragment()
                2 -> VaultExchangeFragment()
                else -> ShareExchangeFragment()
            }
            fragmentMap.put(position, fragment)
            return fragment
        }

        // Safely return fragment if already created
        fun getFragment(position: Int): Fragment? = fragmentMap.get(position)
    }

}