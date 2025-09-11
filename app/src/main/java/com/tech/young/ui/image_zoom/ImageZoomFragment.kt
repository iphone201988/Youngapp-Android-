package com.tech.young.ui.image_zoom

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.data.api.Constants
import com.tech.young.databinding.FragmentImageZoomBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Objects


@AndroidEntryPoint
class ImageZoomFragment : BaseFragment<FragmentImageZoomBinding>() {

    private val viewModel : ImageZoomVm by viewModels()
    private var url : String ?= null
    override fun onCreateView(view: View) {
        initView()
        initOnClick()
    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer {
            when(it?.id){
                R.id.ivBack ->{
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun initView() {
        url= arguments?.getString("url").toString()
        Log.i("url", "initView: $url")
        if (url != null){
            Glide.with(Objects.requireNonNull(this))
                .load(Constants.BASE_URL_IMAGE + url) // image url
                .into(binding.iamge);
        }

    }

    override fun getLayoutResource(): Int {
        return  R.layout.fragment_image_zoom
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

}