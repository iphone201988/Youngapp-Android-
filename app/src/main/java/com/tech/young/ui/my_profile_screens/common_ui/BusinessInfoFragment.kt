package com.tech.young.ui.my_profile_screens.common_ui

import android.view.View
import androidx.fragment.app.viewModels
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.databinding.FragmentBusinessInfoBinding
import com.tech.young.ui.my_profile_screens.YourProfileVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BusinessInfoFragment : BaseFragment<FragmentBusinessInfoBinding>() {
    private val viewModel:YourProfileVM by viewModels()
    override fun onCreateView(view: View) {
        // view
        initView()
        // click
        initOnClick()
        // observer
        initObserver()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_business_info
    }

    override fun getViewModel(): BaseViewModel {
      return viewModel
    }

    /** handle view **/
    private fun initView(){
        setupYesNoToggleForInvestment()

    }

    /** handle click **/
    private fun initOnClick(){
        viewModel.onClick.observe(requireActivity()){
            when(it?.id){
                R.id.ivBack->{
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                R.id.actionToggleBtn->{
                    // handle click
                }
                R.id.tvSave->{
                    // handle click
                }
                R.id.tvSaveRevenue->{
                    // handle click
                }
            }
        }

    }
    /** handle observer **/
    private fun initObserver(){

    }

    private fun setupYesNoToggleForInvestment() {
        binding.yesOptionInvestment.label.text = "Yes"
        binding.noOptionInvestment.label.text = "No"
        binding.yesOptionInvestment.box.setOnClickListener {
            binding.yesOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_selected)
            binding.noOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_unselected)
        }
        binding.noOptionInvestment.box.setOnClickListener {
            binding.noOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_selected)
            binding.yesOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_unselected)
        }
        binding.noOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_selected)
        binding.yesOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_unselected)
    }

}