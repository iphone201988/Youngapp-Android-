package com.tech.young.ui.my_profile_screens.forFinance

import android.view.View
import androidx.fragment.app.viewModels
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.databinding.FragmentProfessionalInformationBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfessionalInformationFragment : BaseFragment<FragmentProfessionalInformationBinding>() {

    private val viewModel : FinanceAccountVm by viewModels()


    override fun onCreateView(view: View) {
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_professional_information
    }

    override fun getViewModel(): BaseViewModel {
          return viewModel
    }

}