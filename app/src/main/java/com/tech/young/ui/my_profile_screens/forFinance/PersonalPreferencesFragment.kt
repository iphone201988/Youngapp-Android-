package com.tech.young.ui.my_profile_screens.forFinance

import android.view.View
import androidx.fragment.app.viewModels
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.databinding.FragmentPersonalPreferencesBinding


class PersonalPreferencesFragment : BaseFragment<FragmentPersonalPreferencesBinding>() {

    private val viewModel : FinanceAccountVm by viewModels()

    override fun onCreateView(view: View) {
        setupYesNoToggleForInvestment()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_personal_preferences
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
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