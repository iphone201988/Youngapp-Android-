package com.tech.young.ui.my_profile_screens.forNormal

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import com.tech.young.BR
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.data.model.SimilarProfile
import com.tech.young.databinding.FragmentSimilarProfileBinding
import com.tech.young.databinding.ItemLayoutSimilarProfileBinding
import com.tech.young.ui.my_profile_screens.YourProfileVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SimilarProfileFragment : BaseFragment<FragmentSimilarProfileBinding>() {

    private val viewModel: YourProfileVM by activityViewModels()
    private lateinit var similarAdapter : SimpleRecyclerViewAdapter<SimilarProfile, ItemLayoutSimilarProfileBinding>

    override fun getLayoutResource(): Int {
        return com.tech.young.R.layout.fragment_similar_profile
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        initAdapter()
        getData()
    }

    private fun getData() {
        viewModel.performanceData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.similarProfile != null) {
                Log.d("PerformanceData", "Received similarProfile: ${data.similarProfile}")
                similarAdapter.list = data.similarProfile
                similarAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun initAdapter() {
        similarAdapter = SimpleRecyclerViewAdapter(
            com.tech.young.R.layout.item_layout_similar_profile,
            BR.bean
        ) { v, m, pos ->
            when(v.id){
                com.tech.young.R.id.consMain -> {
                }
            }
        }
        binding.rvSimilarProfile.adapter = similarAdapter
    }
}
