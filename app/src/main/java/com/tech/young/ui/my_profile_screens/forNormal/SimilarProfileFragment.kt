package com.tech.young.ui.my_profile_screens.forNormal

import android.os.Bundle
import android.view.View
import androidx.core.R
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.tech.young.BR
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.databinding.FragmentSimilarProfileBinding
import com.tech.young.databinding.FragmentSimilarProfileBindingImpl
import com.tech.young.databinding.ItemInvestmentsBinding
import com.tech.young.ui.my_profile_screens.YourProfileVM
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue


@AndroidEntryPoint
class SimilarProfileFragment : BaseFragment<FragmentSimilarProfileBinding>() {

    private val viewModel: YourProfileVM by viewModels()
    private lateinit var similarAdapter : SimpleRecyclerViewAdapter<String , ItemInvestmentsBinding>



    override fun getLayoutResource(): Int {
        return com.tech.young.R.layout.fragment_similar_profile
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        initAdapter()
    }



    private fun initAdapter() {
        similarAdapter = SimpleRecyclerViewAdapter(
            com.tech.young.R.layout.item_investments,
            BR.bean
        ) { v, m, pos ->
            when(v.id){
                com.tech.young.R.id.consMain ->{
                }
            }
        }

        binding.rvSimilarProfile.adapter = similarAdapter
        similarAdapter.list = listOf("", "", "")
    }

}