package com.tech.young.ui.featured

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.databinding.FragmentFeaturedBinding
import com.tech.young.ui.my_share.MyShareFragmentVm
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue


@AndroidEntryPoint
class FeaturedFragment : BaseFragment<FragmentFeaturedBinding>() {


    private val viewModel : MyShareFragmentVm  by viewModels()

    override fun getLayoutResource(): Int {
        return R.layout.fragment_featured
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
    }


}