package com.tech.young.ui.auth

import com.tech.young.base.BaseViewModel
import com.tech.young.base.utils.Resource
import com.tech.young.base.utils.event.SingleRequestEvent
import com.tech.young.data.api.ApiHelper
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class AuthCommonVM @Inject constructor(
    private val apiHelper: ApiHelper,
) : BaseViewModel() {
    val observeCommon = SingleRequestEvent<JsonObject>()
    fun login(request: HashMap<String, Any>, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                apiHelper.apiForRawBody(request, url).let {
                    if (it.isSuccessful) {
                        observeCommon.postValue(Resource.success("LOGIN", it.body()))
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


    fun signup(request: HashMap<String, Any>, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                apiHelper.apiForRawBody(request, url).let {
                    if (it.isSuccessful) {
                        observeCommon.postValue(Resource.success("SIGNUP", it.body()))
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

    fun verifyOtp(request: HashMap<String, Any>, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                apiHelper.apiPutForRawBody(url, request).let {
                    if (it.isSuccessful) {
                        observeCommon.postValue(Resource.success("VerifyOtp", it.body()))
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

    fun verify2FA(request: HashMap<String, Any>, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                apiHelper.apiPutForRawBody(url, request).let {
                    if (it.isSuccessful) {
                        observeCommon.postValue(Resource.success("Verify2FA", it.body()))
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

    fun completeRegistration(
        request: HashMap<String, RequestBody>,
        url: String,
        profileImage: MultipartBody.Part?,
        partList:MutableList<MultipartBody.Part?>?

    ) {
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                apiHelper.apiForPutMultipart1(url, request, profileImage,partList).let {
                    if (it.isSuccessful) {
                        observeCommon.postValue(Resource.success("CompleteRegistration", it.body()))
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


    fun sendOtp(request: HashMap<String, Any>, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                apiHelper.apiPutForRawBodyWithoutAuth(url, request).let {
                    if (it.isSuccessful) {
                        observeCommon.postValue(Resource.success("sendOtp", it.body()))
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

    fun changePassword(data : HashMap<String,Any>, url: String){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                apiHelper.apiPutForRawBodyWithoutAuth(url, data).let {
                    if (it.isSuccessful) {
                        observeCommon.postValue(Resource.success("changePassword", it.body()))
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


    fun getDigit(url : String , data : HashMap<String,String>){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiGetWithQuery(data,url)
                if (response.isSuccessful && response.body() != null){
                    observeCommon.postValue(Resource.success("getDigit", response.body()))
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

    /*    var profileImage: MultipartBody.Part? = null
        val userId = MutableLiveData<String>()
        val role = MutableLiveData<String>()
        val crdNumber = MutableLiveData<String>()

        //<!--Register for general member-->
        val age = MutableLiveData<String>()
        val gender = MutableLiveData<String>()
        val maritalStatus = MutableLiveData<String>()
        val children = MutableLiveData<String>()
        val homeOwnership = MutableLiveData<String>()

        //Financial Advisor & Insurance Broker Account Registration-->
        val productsServicesOffered = MutableLiveData<String>()
        val areasOfExpertise = MutableLiveData<String>()


        //<!--Startup and Small Business Account Registration-->
        val industry = MutableLiveData<String>()
        val interestedIn = MutableLiveData<String>()


        // Select your interest
        val objective = MutableLiveData<String>()
        val financialExperience = MutableLiveData<String>()
        val investments = MutableLiveData<String>()
        val servicesInterested = MutableLiveData<String>()*/

}

