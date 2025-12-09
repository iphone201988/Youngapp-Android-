package com.tech.young.ui.payment.payment_history

import com.google.gson.JsonObject
import com.tech.young.base.BaseViewModel
import com.tech.young.base.utils.Resource
import com.tech.young.base.utils.event.SingleRequestEvent
import com.tech.young.data.api.ApiHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentHistoryVM @Inject constructor(val apiHelper: ApiHelper) : BaseViewModel() {

    val observeCommon = SingleRequestEvent<JsonObject>()

    fun getAds(url : String){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiGetOnlyAuthToken(url)
                if (response.isSuccessful && response.body() != null){
                    observeCommon.postValue(Resource.success("getAds", response.body()))
                }
                else{
                    observeCommon.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            }catch (e : Exception){
                observeCommon.postValue(Resource.error(e.message.toString(), null))
            }
        }
    }

    fun getPaymentHistory(url : String){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiGetOnlyAuthToken(url)
                if (response.isSuccessful && response.body() != null){
                    observeCommon.postValue(Resource.success("getPaymentHistory", response.body()))
                }
                else{
                    observeCommon.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            }catch (e : Exception){
                observeCommon.postValue(Resource.error(e.message.toString(), null))
            }
        }
    }
}