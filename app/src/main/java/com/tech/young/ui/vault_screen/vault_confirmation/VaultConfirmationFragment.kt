package com.tech.young.ui.vault_screen.vault_confirmation

import android.view.View
import androidx.fragment.app.viewModels
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.FragmentVaultConfirmationBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class VaultConfirmationFragment : BaseFragment<FragmentVaultConfirmationBinding>() {

    private val viewModel: VaultConfirmationVM by viewModels()

    // adapter
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<String, AdsItemViewBinding>

    private var getList = listOf(
        "", "", "", "", ""
    )

    override fun onCreateView(view: View) {
        // view
        initView()
        // click
        initOnClick()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_vault_confirmation
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
    }
}