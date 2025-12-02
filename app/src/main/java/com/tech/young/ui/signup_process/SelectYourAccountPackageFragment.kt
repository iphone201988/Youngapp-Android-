package com.tech.young.ui.signup_process

import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.google.gson.Gson
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.showToast
import com.tech.young.data.SubscriptionFeatureList
import com.tech.young.databinding.FragmentSelectYourAccountPackageBinding
import com.tech.young.databinding.ItemLayoutSubscriptionFeaturesBinding
import com.tech.young.ui.auth.AuthCommonVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SelectYourAccountPackageFragment : BaseFragment<FragmentSelectYourAccountPackageBinding>() , PurchasesUpdatedListener , BillingClientStateListener{

    private lateinit var subscriptionAdapter : SimpleRecyclerViewAdapter<SubscriptionFeatureList,ItemLayoutSubscriptionFeaturesBinding>
    private val viewModel: AuthCommonVM by viewModels()
    private var subscriptionList = ArrayList<SubscriptionFeatureList>()
    private var isPremiumSelected = false


    private lateinit var billingClient: BillingClient

    private lateinit var plansList: MutableList<ProductDetails>
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

                R.id.tvSkip ->{
                    findNavController().navigate(R.id.navigateToTermAndConditionsFragment)

                }

                R.id.tvNext -> {
                     //   purchaseSubscription("all_region_subscription_monthly")
                    findNavController().navigate(R.id.navigateToTermAndConditionsFragment)
                }

                R.id.premiumPlan -> {
                    isPremiumSelected = !isPremiumSelected // Toggle state

                    if (isPremiumSelected) {
                        // Selected state
//                        binding.premiumRadio.setImageResource(R.drawable.select_radio)
                        binding.premiumPlan.background = ContextCompat.getDrawable(
                            requireActivity(),
                            R.drawable.bg_premium
                        )
                        binding.tvPremiumTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                        binding.tvPremiumPrice.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                        binding.tvPremiumMonth.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

                    } else {
                        // Unselected state
//                        binding.premiumRadio.setImageResource(R.drawable.unselect_radio) // your unselected icon
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



    private fun purchaseSubscription(planId: String) {
        if (!billingClient.isReady) {
            Log.e("BillingClient", "Service not ready, reconnecting...")
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        purchaseSubscription(planId) // ✅ Retry after reconnect
                    } else {
                        showToast("Billing setup failed, please try again.")
                    }
                }
                override fun onBillingServiceDisconnected() {
                    Log.w("BillingClient", "Disconnected. Retrying...")
                    billingClient.startConnection(this)
                }
            })
            return
        }

        // ✅ Ensure plans are available
        if (!::plansList.isInitialized || plansList.isEmpty()) {
            showToast("Loading plans, please wait...")
          //  queryAvailableSubscriptions()
            return
        }

        // ✅ Find only monthly/yearly plan
        val productDetails = plansList.find { it.productId == planId }
        if (productDetails == null) {
            showToast("Subscription plan not found.")
            return
        }

        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
        if (offerToken.isNullOrEmpty()) {
            showToast("No active offer for this plan.")
            return
        }

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                )
            )
            .build()

        val result = billingClient.launchBillingFlow(requireActivity(), billingFlowParams)
        Log.d("BillingClient", "Launch result: ${result.responseCode}")
    }

//    private fun queryAvailableSubscriptions() {
//
//        CoroutineScope(Dispatchers.Main).launch {
//
//            val params = QueryProductDetailsParams.newBuilder()
//                .setProductList(
//                    listOf(
//                        QueryProductDetailsParams.Product.newBuilder()
//                            .setProductId("your_product_id_here")   // your subscription ID
//                            .setProductType(BillingClient.ProductType.SUBS)
//                            .build()
//                    )
//                )
//                .build()
//
//            billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsResult ->
//
//                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//
//                    val productDetailsList = productDetailsResult.productDetailsList
//
//                    if (!productDetailsList.isNullOrEmpty()) {
//                        plansList = productDetailsList
//
//                        Log.d("plansList", Gson().toJson(plansList))
//
//                    } else {
//                        Log.d("plansList", "No products found!")
//                    }
//
//                } else {
//                    Log.d("billingResult123", billingResult.debugMessage)
//                }
//            }
//
//        }
//    }


    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {

            for (purchase in purchases) {

                handlePurchase(purchase)

                Log.d("BillingPurchase", "Full Purchase Data: $purchase")

                // Single subscription product ID
                val productId = purchase.products.firstOrNull() ?: ""
                Log.d("BillingPurchase", "Product ID: $productId")

                // ✔ Only ONE plan → Always 30 days
                val days = 30

                // ✔ Static price for your one plan
                val planPrice = 19.99    // ← change price if needed

//                // Send purchase data to server
//                val data = HashMap<String, Any>()
//                data["productId"] = productId
//                data["orderId"] = purchase.orderId ?: ""
//                data["purchaseToken"] = purchase.purchaseToken
//                data["planPrice"] = planPrice
//                data["purchaseTime"] = purchase.purchaseTime
//                data["days"] = days
//                data["currencyType"] = "USD"
//                data["group_id"] = model._id.toString()
//
//                viewModel.addPurchase(Constants.NEW_SUBSCRIPTION_PURCHASE, data)

                Log.e("testPurchaseSuccess---", "Plan: $productId, Days: $days")
            }

        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {

            Log.e("testPurchaseCanceled", purchases.toString())

        } else {

            Log.e(
                "testPurchaseError",
                "Code: ${billingResult.responseCode}, Purchases: $purchases"
            )
        }
    }


    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            val acknowledgePurchaseParams =
                AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken)
                    .build()
            billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.e("qwerty---", billingResult.toString())
                }
            }
        }
    }

    override fun onBillingServiceDisconnected() {

    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            //queryAvailableSubscriptions()
        } else {
            Log.d("startConnection", "onConnected: ")
        }
    }



}
