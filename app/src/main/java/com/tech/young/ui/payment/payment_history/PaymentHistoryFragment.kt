package com.tech.young.ui.payment.payment_history

import android.view.View
import androidx.fragment.app.viewModels
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.FragmentPaymentHistoryBinding
import com.tech.young.databinding.ItemLayoutPaymentHistoryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentHistoryFragment : BaseFragment<FragmentPaymentHistoryBinding>() {

    private val viewModel: PaymentHistoryVM by viewModels()


    // adapter
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<String, AdsItemViewBinding>

    private lateinit var historyAdapter: SimpleRecyclerViewAdapter<String, ItemLayoutPaymentHistoryBinding>


    private var getList = listOf(
        "", "", "", "", ""
    )

    private var historyList = listOf(
        "", "", ""
    )

    override fun onCreateView(view: View) {

        // view
        initView()
        // click
        initOnClick()
    }


    override fun getLayoutResource(): Int {
        return R.layout.fragment_payment_history
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

            }
        }

    }

    /** handle adapter **/
    private fun initAdapter() {
        adsAdapter = SimpleRecyclerViewAdapter(R.layout.ads_item_view, BR.bean) { v, m, pos ->
            when (v.id) {

            }
        }
        adsAdapter.list = getList
        binding.rvAds.adapter = adsAdapter

        historyAdapter =
            SimpleRecyclerViewAdapter(R.layout.item_layout_payment_history, BR.bean) { v, m, pos ->
                when (v.id) {

                }
            }
        historyAdapter.list = historyList
        binding.rvPaymentHistory.adapter = historyAdapter
    }

}