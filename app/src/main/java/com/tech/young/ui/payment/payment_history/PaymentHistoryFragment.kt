package com.tech.young.ui.payment.payment_history

import android.view.View
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
import com.tech.young.data.model.GetAdsAPiResponse
import com.tech.young.data.model.PaymentHistoryApiResponse
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.FragmentPaymentHistoryBinding
import com.tech.young.databinding.ItemLayoutPaymentHistoryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentHistoryFragment : BaseFragment<FragmentPaymentHistoryBinding>() {

    private val viewModel: PaymentHistoryVM by viewModels()


    // adapter
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<GetAdsAPiResponse.Data.Ad, AdsItemViewBinding>

    private lateinit var historyAdapter: SimpleRecyclerViewAdapter<PaymentHistoryApiResponse.Data.Payment, ItemLayoutPaymentHistoryBinding>


    private var getList = listOf(
        "", "", "", "", ""
    )

    private var historyList = listOf(
        "", "", ""
    )

    override fun onCreateView(view: View) {

        // view
        initView()

        viewModel.getAds(Constants.GET_ADS)
        viewModel.getPaymentHistory(Constants.PAYMENT_HISTORY)
        // click
        initOnClick()
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
                        "getAds" ->{
                            val myDataModel : GetAdsAPiResponse? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    adsAdapter.list = myDataModel.data?.ads
                                }
                            }
                        }
                        "getPaymentHistory" -> {
                            try {
                                val myDataModel: PaymentHistoryApiResponse? =
                                    BindingUtils.parseJson(it.data.toString())

                                val payments = myDataModel?.data?.payments

                                if (payments.isNullOrEmpty()) {

                                    // 🔥 No history → show text, hide list
                                    binding.tvNoHistoryAvailable.visibility = View.VISIBLE
                                    binding.rvPaymentHistory.visibility = View.GONE

                                } else {

                                    // 🔥 Data found → show list, hide text
                                    binding.tvNoHistoryAvailable.visibility = View.GONE
                                    binding.rvPaymentHistory.visibility = View.VISIBLE

                                    historyAdapter.list = payments
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
                else ->{}
            }
        })
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
        binding.rvAds.adapter = adsAdapter

        historyAdapter =
            SimpleRecyclerViewAdapter(R.layout.item_layout_payment_history, BR.bean) { v, m, pos ->
                when (v.id) {

                }
            }
        binding.rvPaymentHistory.adapter = historyAdapter
    }

}