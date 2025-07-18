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
import com.tech.young.databinding.FragmentNewsWebBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewsWebFragment : BaseFragment<FragmentNewsWebBinding>() {
    private val viewModel:NewsViewModel by viewModels()
    private var url :String?=null
    override fun onCreateView(view: View) {
        // view
        initView()
        // click
        initOnClick()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_news_web
    }

    override fun getViewModel(): BaseViewModel {
      return viewModel
    }

    /** handle view **/
    private fun initView() {
        url = arguments?.getString("linkUrl")
        if (url != null) {
            binding.webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadsImagesAutomatically = true
                allowFileAccess = true
                useWideViewPort = true
                loadWithOverviewMode = true
            }
        }
        binding.webView.loadUrl("$url")
    }

    /** handle click **/
    private fun initOnClick(){
        viewModel.onClick.observe(requireActivity()){
            when(it?.id){
                R.id.ivBack->{
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }

}