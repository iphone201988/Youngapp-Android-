package com.tech.young.ui.exchange.screens

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.tech.young.data.model.CategoryModel
import com.tech.young.data.model.SavedPostApiResponse
import com.tech.young.data.model.VaultExchangeApiResponse
import com.tech.young.databinding.CategoryItemViewBinding
import com.tech.young.databinding.FragmentVaultExchangeBinding
import com.tech.young.databinding.ItemLayoutVaultExchangeBinding
import com.tech.young.ui.common.CommonActivity
import com.tech.young.ui.exchange.ExchangeVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VaultExchangeFragment : BaseFragment<FragmentVaultExchangeBinding>() {
    private val viewModel: ExchangeVM by viewModels()

    private lateinit var categoryAdapter: SimpleRecyclerViewAdapter<CategoryModel, CategoryItemViewBinding>
    private lateinit var vaultAdapter: SimpleRecyclerViewAdapter<VaultExchangeApiResponse.Data.Vault, ItemLayoutVaultExchangeBinding>
    private var apiTitle : String ?= null
    override fun getLayoutResource(): Int {
       return R.layout.fragment_vault_exchange
    }

    override fun getViewModel(): BaseViewModel {
       return viewModel
    }

    override fun onCreateView(view: View) {
        // view
        initView()
        // click


        getVault()
        initOnClick()
        // observer

        initAdapter()
        initObserver()
    }

    private fun initAdapter() {
        categoryAdapter=SimpleRecyclerViewAdapter(R.layout.category_item_view, BR.bean){ v, m, pos->
            when(v.id){
                R.id.clMain->{
                     apiTitle = mapTitleToApiValue(m.title)
                    val data = HashMap<String,Any>()
                    data["userType"] = apiTitle!!
                    data["page"] = 1
                    viewModel.getVault(data, Constants.GET_VAULT)

                    categoryAdapter.list.forEach {
                        it.isSelected = false
                    }
                    categoryAdapter.list[pos].isSelected = true
                    categoryAdapter.notifyDataSetChanged()
                }
            }
        }
        categoryAdapter.list=categoryList()
        binding.rvCategories.adapter=categoryAdapter

        vaultAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_vault_exchange,BR.bean){v,m,pos ->
            val consReport = v.rootView.findViewById<ConstraintLayout>(R.id.consReport)

            when(v.id){
                R.id.ivSaves ->{
                    viewModel.vaultSaveUnSave( Constants.SAVE_UNSAVE_VAULT+m._id)
                }
                R.id.ivSendChat ->{

//                    val parentView = v.parent as? ViewGroup
//                    val etChat = parentView?.findViewById<AppCompatEditText>(R.id.etChat)
//                    val commentText = etChat?.text?.toString()?.trim()
//                    if (commentText?.isNotEmpty() == true){
//                        val data = HashMap<String,Any>()
//                        data["vaultId"] = m._id.toString()
//                        data["comment"] = commentText
//                        viewModel.addComment(data,Constants.VAULT_ADD_COMMENT)
//                    }
//                    else{
//                        showToast("Please add comment")
//                    }

                }
                R.id.ivHeart ->{
                  //  viewModel.likeDislike(Constants.LIKE_DISLIKE_POST+m._id)
                }
                R.id.consMain ->{
                    val intent= Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("from","vault_room")
                    intent.putExtra("vaultId",m._id)
                    startActivity(intent)
                }

                R.id.reportBtn -> {
                    consReport.visibility = if (consReport.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                }
                R.id.consReport ->{
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("from", "report_user")
                    intent.putExtra("userId", m._id)
                    intent.putExtra("reportType","vault")
                    startActivity(intent)
                    consReport.visibility = View.GONE
                }
            }

        }
        binding.rvVault.adapter = vaultAdapter
    }

    private fun getVault() {
        val data = HashMap<String,Any>()
        data["userType"] = "general_member"
        data["page"] = 1
        viewModel.getVault(data, Constants.GET_VAULT)
    }

    /** handle view **/
    private fun initView(){

    }

    /** handle click **/
    private fun initOnClick(){

        viewModel.onClick.observe(viewLifecycleOwner,  Observer {
            when(it?.id){
                R.id.addVault ->{
                    val intent = Intent(requireContext(), CommonActivity::class.java).putExtra("from","common_vault")
                    startActivity(intent)
                }
            }
        })
    }

    /** handle observer **/
    private fun initObserver(){
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            when(it?.status){
                Status.LOADING ->{
                    showLoading()
                }
                Status.SUCCESS ->{
                    hideLoading()
                    when(it.message){
                        "getVault" ->{
                            var myDataModel : VaultExchangeApiResponse? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data!= null){
                                    vaultAdapter .list = myDataModel.data?.vaults
                                }
                            }
                        }
                        "vaultSaveUnSave" ->{
                            val myDataModel : SavedPostApiResponse? = BindingUtils.parseJson(it.data.toString())
                            if(myDataModel != null){
                                if (myDataModel.data != null){
                                    showToast(myDataModel.message.toString())
                                    if (apiTitle != null){
                                        val data = HashMap<String,Any>()
                                        data["userType"] = apiTitle!!
                                        data["page"] = 1
                                        viewModel.getVault(data, Constants.GET_VAULT)
                                    }
                                    else{
                                        val data = HashMap<String,Any>()
                                        data["userType"] = "general_member"
                                        data["page"] = 1
                                        viewModel.getVault(data, Constants.GET_VAULT)
                                    }

                                }
                            }
                        }
                        "likeDislike" ->{
                            val myDataModel : SavedPostApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if(myDataModel != null){
                                if (myDataModel.data != null){
                                    showToast(myDataModel.message.toString())
                                    getVault()
                                }
                            }
                        }
                    }
                }
                Status.ERROR ->{
                    hideLoading()
                    showToast(it.message.toString())
                }
                else ->{

                }
            }
        })
    }


    private fun mapTitleToApiValue(title: String): String {
        return when (title) {
            "Advisors" -> "financial_advisor"
            "Startups" -> "startup"
            "Small Businesses" -> "small_business"
            "Investor" -> "investor"
            "Firm" -> "financial_firm"
            "Members" -> "general_member"
            else -> title.lowercase().replace(" ", "_")
        }
    }

    private fun categoryList():ArrayList<CategoryModel>{
        val list=ArrayList<CategoryModel>()
        list.add(CategoryModel("Members",true))
        list.add(CategoryModel("Advisors"))
        list.add(CategoryModel("Startups"))
        list.add(CategoryModel("Small Businesses"))
        list.add(CategoryModel("Investor"))
        list.add(CategoryModel("Firm"))
        return list
    }

    override fun onResume() {
        super.onResume()
        getVault()
    }


}