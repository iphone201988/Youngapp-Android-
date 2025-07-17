package com.tech.young.ui.news

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.data.RssItem
import com.tech.young.databinding.FragmentNewsDetailBinding
import com.tech.young.databinding.ItemLayoutNewsDataBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewsDetailFragment : BaseFragment<FragmentNewsDetailBinding>() {

    private val viewModel : NewsViewModel by viewModels()
    private lateinit var newsAdapter : SimpleRecyclerViewAdapter<RssItem, ItemLayoutNewsDataBinding>

    override fun onCreateView(view: View) {

    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_news_detail
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

}