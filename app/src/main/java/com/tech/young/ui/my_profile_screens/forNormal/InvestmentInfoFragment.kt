package com.tech.young.ui.my_profile_screens.forNormal

import android.view.View
import androidx.fragment.app.viewModels
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.databinding.FragmentInvestmentInfoBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InvestmentInfoFragment : BaseFragment<FragmentInvestmentInfoBinding>() {
    private val viewModel:EditDetailsVM by viewModels()
    override fun onCreateView(view: View) {
        // view
        initView()
        // click
        initOnClick()
        // observer
        initObserver()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_investment_info
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /** handle view **/
    private fun initView(){

    }

    /** handle click **/
    private fun initOnClick(){
        viewModel.onClick.observe(requireActivity()){
            when(it?.id){
                R.id.ivBack -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                R.id.tvSave -> {
                    // handle click
                }
            }
        }
    }

    /** handle observer **/
    private fun initObserver(){

    }

}