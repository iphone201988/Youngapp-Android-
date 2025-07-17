package com.tech.young.ui.advertise_screen

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.BindingUtils
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.FragmentAdvertiseBinding
import com.tech.young.databinding.ItemLayoutAdvertisePopupBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdvertiseFragment : BaseFragment<FragmentAdvertiseBinding>() {

    private val viewModel: AdvertiseFragmentVm by viewModels()
    private var select = 0

    // adapter
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<String, AdsItemViewBinding>

    override fun onCreateView(view: View) {
        initAdapter()
        // click
        initOnClick()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_advertise
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /** handle click **/
    private fun initOnClick(){
        viewModel.onClick.observe(requireActivity()){
            when(it?.id){
                R.id.ivCheck -> {
                    if (select == 0) {
                        select = 1
                        binding.check = true
                    } else {
                        select = 0
                        binding.check = false
                    }
                }

                R.id.tvSubmit->{
                    showDialog()
                }

            }
        }
    }

    private fun initAdapter() {
        adsAdapter = SimpleRecyclerViewAdapter(R.layout.ads_item_view, BR.bean) { v, m, pos ->
            when (v.id) {

            }
        }
        adsAdapter.list = getList
        binding.rvAds.adapter = adsAdapter
    }

    private var getList = listOf(
        "", "", "", "", ""
    )

    /** dialog **/
    private fun showDialog() {
        val bindingDialog = ItemLayoutAdvertisePopupBinding.inflate(layoutInflater)

        val dialog = Dialog(requireContext())
        dialog.setContentView(bindingDialog.root)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setDimAmount(0f)
        }
        BindingUtils.statusBarStyleBlack(requireActivity())
        BindingUtils.styleSystemBars(requireActivity(),
            ContextCompat.getColor(requireContext(), R.color.colorSecondary2)
        )

        bindingDialog.tvDone.setOnClickListener {
            BindingUtils.statusBarStyleBlack(requireActivity())
            BindingUtils.styleSystemBars(requireActivity(),
                ContextCompat.getColor(requireContext(), R.color.white)
            )

            dialog.dismiss()
        }
        dialog.setCancelable(false)
        dialog.show()
    }


}