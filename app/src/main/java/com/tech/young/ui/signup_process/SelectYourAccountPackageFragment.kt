package com.tech.young.ui.signup_process

import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.data.SubscriptionFeatureList
import com.tech.young.databinding.FragmentSelectYourAccountPackageBinding
import com.tech.young.databinding.ItemLayoutSubscriptionFeaturesBinding
import com.tech.young.ui.auth.AuthCommonVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectYourAccountPackageFragment : BaseFragment<FragmentSelectYourAccountPackageBinding>() {

    private lateinit var subscriptionAdapter : SimpleRecyclerViewAdapter<SubscriptionFeatureList,ItemLayoutSubscriptionFeaturesBinding>
    private val viewModel: AuthCommonVM by viewModels()
    private var subscriptionList = ArrayList<SubscriptionFeatureList>()
    private var isPremiumSelected = false
    override fun onCreateView(view: View) {
        initView()
        initOnClick()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_select_your_account_package
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity(), Observer {
            when (it?.id) {
                R.id.ivBack -> {
                    findNavController().popBackStack()
                }

                R.id.tvNext -> {
                    findNavController().navigate(R.id.navigateToTermAndConditionsFragment)
                }

                R.id.premiumPlan -> {
                    isPremiumSelected = !isPremiumSelected // Toggle state

                    if (isPremiumSelected) {
                        // Selected state
                        binding.premiumRadio.setImageResource(R.drawable.select_radio)
                        binding.premiumPlan.background = ContextCompat.getDrawable(
                            requireActivity(),
                            R.drawable.bg_premium
                        )
                        binding.tvPremiumTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                        binding.tvPremiumPrice.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                        binding.tvPremiumMonth.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

                    } else {
                        // Unselected state
                        binding.premiumRadio.setImageResource(R.drawable.unselect_radio) // your unselected icon
                        binding.premiumPlan.background = ContextCompat.getDrawable(
                            requireActivity(),
                            R.drawable.bg_standard // your default background
                        )
                        binding.tvPremiumTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                        binding.tvPremiumPrice.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                        binding.tvPremiumMonth.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                    }
                }


//                R.id.premiumPlan -> {
//                    binding.standardRadio.setImageResource(R.drawable.unselect_radio)
//                    binding.premiumRadio.setImageResource(R.drawable.select_radio)
//
//                    binding.standardPlan.background =
//                        ContextCompat.getDrawable(requireActivity(), R.drawable.bg_standard)
//                    binding.premiumPlan.background =
//                        ContextCompat.getDrawable(requireActivity(), R.drawable.bg_premium)
//                }

//                R.id.standardPlan -> {
//                    binding.standardRadio.setImageResource(R.drawable.select_radio)
//                    binding.premiumRadio.setImageResource(R.drawable.unselect_radio)
//
//                    binding.standardPlan.background =
//                        ContextCompat.getDrawable(requireActivity(), R.drawable.bg_premium)
//                    binding.premiumPlan.background =
//                        ContextCompat.getDrawable(requireActivity(), R.drawable.bg_standard)
//                }
            }
        })

    }

    private fun initView() {
        getFeaturesList()
        initAdapter()
    }

    private fun initAdapter() {
        subscriptionAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_subscription_features, BR.bean){v,m,pos ->

        }
        binding.rvSubscription.adapter = subscriptionAdapter
        subscriptionAdapter.list = subscriptionList
        subscriptionAdapter.notifyDataSetChanged()

    }

    private fun getFeaturesList() {
        subscriptionList.add(SubscriptionFeatureList("Create Own Vault"))
        subscriptionList.add(SubscriptionFeatureList("Create and Join Live Streams"))
    }
}
