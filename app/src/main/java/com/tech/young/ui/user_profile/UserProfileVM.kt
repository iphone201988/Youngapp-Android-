package com.tech.young.ui.user_profile

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
class UserProfileVM @Inject constructor(val apiHelper: ApiHelper) :BaseViewModel() {
    val userProfileObserver= SingleRequestEvent<JsonObject>()
    fun getUserProfile(url: String,data:HashMap<String,Any>){
        CoroutineScope(Dispatchers.IO).launch {
            userProfileObserver.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiGetWithQueryAuth(url,data)
                if (response.isSuccessful && response.body() != null){
                    userProfileObserver.postValue(Resource.success("getUserProfile", response.body()))
                }
                else{
                    userProfileObserver.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            }catch (e : Exception){
                userProfileObserver.postValue(Resource.error(e.message.toString(), null))
            }
        }
    }

    fun followUnfollowUser(url : String){
        CoroutineScope(Dispatchers.IO).launch {
            userProfileObserver.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiPutWithoutRawBody(url)
                if (response.isSuccessful && response.body() != null){
                    userProfileObserver.postValue(Resource.success("followUnfollowUser", response.body()))
                }
                else{
                    userProfileObserver.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            }catch (e : Exception){
                userProfileObserver.postValue(Resource.error(e.message.toString(), null))
            }
        }
    }

     fun updateCustomer(url : String){
         CoroutineScope(Dispatchers.IO).launch {
             userProfileObserver.postValue(Resource.loading(null))
             try {
                 val response = apiHelper.apiPutWithoutRawBody(url)
                 if (response.isSuccessful && response.body() != null){
                     userProfileObserver.postValue(Resource.success("updateCustomer", response.body()))
                 }
                 else{
                     userProfileObserver.postValue(
                         Resource.error(
                             handleErrorResponse(
                                 response.errorBody(),
                                 response.code()
                             ), null
                         )
                     )
                 }
             }catch (e : Exception){
                 userProfileObserver.postValue(Resource.error(e.message.toString(), null))
             }
         }
     }

    fun getAds(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            userProfileObserver.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiGetOnlyAuthToken(url)
                if (response.isSuccessful && response.body() != null) {
                    userProfileObserver.postValue(Resource.success("getAds", response.body()))
                } else {
                    userProfileObserver.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            } catch (e: Exception) {
                userProfileObserver.postValue(Resource.error(e.message.toString(), null))
            }
        }


    }
    fun rating(data: HashMap<String, Any>, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            userProfileObserver.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiPostForRawBody(url, data)
                if (response.isSuccessful && response.body() != null) {
                    userProfileObserver.postValue(Resource.success("rating", response.body()))
                } else {
                    userProfileObserver.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            } catch (e: Exception) {
                userProfileObserver.postValue(Resource.error(e.message.toString(), null))
            }
        }
    }

}