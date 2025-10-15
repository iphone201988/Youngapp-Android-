package com.tech.young.ui.change_password

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
class ChangePasswordFragmentVm @Inject constructor(private val apiHelper: ApiHelper) : BaseViewModel(){

    val observeCommon = SingleRequestEvent<JsonObject>()

    fun updatePassword(data : HashMap<String,Any>, url: String){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                apiHelper.apiPutForRawBodyAuth(url, data).let {
                    if (it.isSuccessful) {
                        observeCommon.postValue(Resource.success("updatePassword", it.body()))
                    } else observeCommon.postValue(
                        Resource.error(
                            handleErrorResponse(it.errorBody()), null
                        )
                    )
                }
            } catch (e: Exception) {
                observeCommon.postValue(
                    Resource.error(
                        e.message, null
                    )
                )
            }

        }
    }

}