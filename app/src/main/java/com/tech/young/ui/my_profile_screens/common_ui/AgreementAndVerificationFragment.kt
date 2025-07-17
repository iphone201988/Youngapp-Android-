package com.tech.young.ui.my_profile_screens.common_ui

import android.view.View
import androidx.fragment.app.viewModels
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.databinding.FragmentAgreementAndVerificationBinding
import com.tech.young.ui.my_profile_screens.YourProfileVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AgreementAndVerificationFragment : BaseFragment<FragmentAgreementAndVerificationBinding>() {
    private val viewModel: YourProfileVM by viewModels()
    private var fromEdit: Int? = 0
    private var select = 0
    override fun onCreateView(view: View) {
        // view
        initView()
        // click
        initOnClick()
        // observer
        initObserver()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_agreement_and_verification
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /** handle view **/
    private fun initView() {
        //fromEdit=intent.getIntExtra("from",0)
        binding.from = fromEdit
    }

    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity()) {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }

                R.id.actionToggleBtn -> {
                    // handle click
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
                    // handle api call
                }
            }
        }

    }

    /** handle observer **/
    private fun initObserver() {

    }


}