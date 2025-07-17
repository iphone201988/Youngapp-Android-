package com.tech.young.ui.my_profile_screens.forNormal

import android.view.View
import androidx.fragment.app.viewModels
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.databinding.FragmentFinanceInfoBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FinanceInfoFragment : BaseFragment<FragmentFinanceInfoBinding>() {
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
        return R.layout.fragment_finance_info
    }

    override fun getViewModel(): BaseViewModel {
       return viewModel
    }

    /** handle view **/
    private fun initView(){
        setupYesNoToggleForInvestment()
        setupYesNoToggleForRetirment()
        setupYesNoToggleForEstate()
    }

    /** handle click **/
    private fun initOnClick(){
        viewModel.onClick.observe(requireActivity()){
            when(it?.id){
                R.id.ivBack -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                R.id.tvSaveFinance -> {
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

    private fun setupYesNoToggleForRetirment() {
        binding.yesOptionRetirement.label.text = "Yes"
        binding.noOptionRetirement.label.text = "No"
        binding.yesOptionRetirement.box.setOnClickListener {
            binding.yesOptionRetirement.box.setBackgroundResource(R.drawable.ic_check_selected)
            binding.noOptionRetirement.box.setBackgroundResource(R.drawable.ic_check_unselected)
        }
        binding.noOptionRetirement.box.setOnClickListener {
            binding.noOptionRetirement.box.setBackgroundResource(R.drawable.ic_check_selected)
            binding.yesOptionRetirement.box.setBackgroundResource(R.drawable.ic_check_unselected)
        }
        binding.noOptionRetirement.box.setBackgroundResource(R.drawable.ic_check_selected)
        binding.yesOptionRetirement.box.setBackgroundResource(R.drawable.ic_check_unselected)
    }

    private fun setupYesNoToggleForEstate() {
        binding.yesOptionRealEstate.label.text = "Yes"
        binding.noOptionRealEstate.label.text = "No"
        binding.yesOptionRealEstate.box.setOnClickListener {
            binding.yesOptionRealEstate.box.setBackgroundResource(R.drawable.ic_check_selected)
            binding.noOptionRealEstate.box.setBackgroundResource(R.drawable.ic_check_unselected)
        }
        binding.noOptionRealEstate.box.setOnClickListener {
            binding.noOptionRealEstate.box.setBackgroundResource(R.drawable.ic_check_selected)
            binding.yesOptionRealEstate.box.setBackgroundResource(R.drawable.ic_check_unselected)
        }
        binding.noOptionRealEstate.box.setBackgroundResource(R.drawable.ic_check_selected)
        binding.yesOptionRealEstate.box.setBackgroundResource(R.drawable.ic_check_unselected)
    }

}