package com.tech.young.ui.stream_screen.stream_confirmation

import com.google.gson.JsonObject
import com.tech.young.base.BaseViewModel
import com.tech.young.base.utils.Resource
import com.tech.young.base.utils.event.SingleRequestEvent
import com.tech.young.data.api.ApiHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class StreamConfirmationVM @Inject constructor(private val apiHelper: ApiHelper) :BaseViewModel() {

    val observeCommon = SingleRequestEvent<JsonObject>()

    fun streamPost(request: HashMap<String, RequestBody>, url: String, profileImage: MultipartBody.Part?){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiForPostMultipart(url, request, profileImage)
                if (response.isSuccessful && response.body() != null){
                    observeCommon.postValue(Resource.success("streamPost", response.body()))
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