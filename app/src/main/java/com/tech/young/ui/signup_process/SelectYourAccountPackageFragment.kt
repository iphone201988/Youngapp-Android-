package com.tech.young.ui.signup_process

import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.databinding.FragmentSelectYourAccountPackageBinding
import com.tech.young.ui.auth.AuthCommonVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectYourAccountPackageFragment : BaseFragment<FragmentSelectYourAccountPackageBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
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
                    binding.standardRadio.setImageResource(R.drawable.unselect_radio)
                    binding.premiumRadio.setImageResource(R.drawable.select_radio)

                    binding.standardPlan.background =
                        ContextCompat.getDrawable(requireActivity(), R.drawable.bg_standard)
                    binding.premiumPlan.background =
                        ContextCompat.getDrawable(requireActivity(), R.drawable.bg_premium)
                }

                R.id.standardPlan -> {
                    binding.standardRadio.setImageResource(R.drawable.select_radio)
                    binding.premiumRadio.setImageResource(R.drawable.unselect_radio)

                    binding.standardPlan.background =
                        ContextCompat.getDrawable(requireActivity(), R.drawable.bg_premium)
                    binding.premiumPlan.background =
                        ContextCompat.getDrawable(requireActivity(), R.drawable.bg_standard)
                }
            }
        })

    }

    private fun initView() {

    }
}
