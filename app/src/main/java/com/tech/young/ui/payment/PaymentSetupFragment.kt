package com.tech.young.ui.payment

import android.view.View
import androidx.fragment.app.viewModels
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.FragmentPaymentSetupBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentSetupFragment : BaseFragment<FragmentPaymentSetupBinding>() {
    private val viewModel: PaymentVM by viewModels()
    private var select = 0

    // adapter
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<String, AdsItemViewBinding>
    override fun onCreateView(view: View) {
        // view
        initView()
        // click
        initOnClick()
        // observer
        initObserver()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_payment_setup
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /** handle view **/
    private fun initView() {
        // adapter
        initAdapter()
    }

    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity()) {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }

                R.id.ivCheck, R.id.tvDesc -> {
                    if (select == 0) {
                        select = 1
                        binding.check = true
                    } else {
                        select = 0
                        binding.check = false
                    }
                }

                R.id.tvButton -> {
                    // handle click
                }
            }
        }

    }

    /** handle observer **/
    private fun initObserver() {

    }

    /** handle adapter **/
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
}