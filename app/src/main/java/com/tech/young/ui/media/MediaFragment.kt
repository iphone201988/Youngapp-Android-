package com.tech.young.ui.media

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showToast
import com.tech.young.data.api.Constants
import com.tech.young.data.api.SimpleApiResponse
import com.tech.young.data.model.GetAdsAPiResponse
import com.tech.young.data.model.MediaApiResponse
import com.tech.young.databinding.FragmentMediaBinding
import com.tech.young.databinding.ItemLayoutMediaBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MediaFragment : BaseFragment<FragmentMediaBinding>() {

    private val viewModel : MediaFragmentVm by viewModels()
    private lateinit var mediaAdapter :  SimpleRecyclerViewAdapter<MediaApiResponse.Data, ItemLayoutMediaBinding>
    override fun onCreateView(view: View) {
        viewModel.getMedia(Constants.media)
        initAdapter()
        initObserver()
    }

    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            when(it?.status){
                Status.LOADING ->{
                    showLoading()
                }
                Status.SUCCESS ->{
                    hideLoading()
                    when(it.message){
                        "getMedia" ->{
                            val myDataModel : MediaApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            try {
                                if (myDataModel != null){
                                    if (myDataModel.data  != null){
                                        mediaAdapter.list = myDataModel.data
                                    }
                                }
                            }catch (e : Exception){
                                e.printStackTrace()
                            }
                        }
                    }
                }
                Status.ERROR ->{
                    hideLoading()
                    showToast(it.message.toString())
                }
                else ->{

                }
            }
        })
    }

    private fun initAdapter() {
        mediaAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_media,  BR.bean){v,m,pos ->

        }
        binding.rvMedia.adapter = mediaAdapter
        mediaAdapter.notifyDataSetChanged()
    }

    override fun getLayoutResource(): Int {
           return R.layout.fragment_media
    }

    override fun getViewModel(): BaseViewModel {
        return  viewModel
    }

}