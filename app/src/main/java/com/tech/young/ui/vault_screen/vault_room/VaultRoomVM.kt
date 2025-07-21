package com.tech.young.ui.vault_screen.vault_room

import com.tech.young.base.BaseViewModel
import com.tech.young.base.utils.Resource
import com.tech.young.base.utils.event.SingleRequestEvent
import com.tech.young.data.api.ApiHelper
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VaultRoomVM @Inject constructor(val apiHelper: ApiHelper) :  BaseViewModel() {

    val observeCommon = SingleRequestEvent<JsonObject>()

    fun getVaultData(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiGetOnlyAuthToken(url)
                if (response.isSuccessful && response.body() != null) {
                    observeCommon.postValue(Resource.success("getVaultData", response.body()))
                } else {
                    observeCommon.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            } catch (e: Exception) {
                observeCommon.postValue(Resource.error(e.message.toString(), null))
            }
        }
    }

    fun joinLeaveRoom(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiPutWithoutRawBody(url)
                if (response.isSuccessful && response.body() != null) {
                    observeCommon.postValue(Resource.success("joinLeaveRoom", response.body()))
                } else {
                    observeCommon.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            } catch (e: Exception) {
                observeCommon.postValue(Resource.error(e.message.toString(), null))
            }
        }
    }

    fun getComment(data: HashMap<String, Any>, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiGetWithQueryAuth(url, data)
                if (response.isSuccessful && response.body() != null) {
                    observeCommon.postValue(Resource.success("getComment", response.body()))
                } else {
                    observeCommon.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            } catch (e: Exception) {
                observeCommon.postValue(Resource.error(e.message.toString(), null))
            }
        }
    }


    fun addComment(data: HashMap<String, Any>, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiPostForRawBody(url, data)
                if (response.isSuccessful && response.body() != null) {
                    observeCommon.postValue(Resource.success("addComment", response.body()))
                } else {
                    observeCommon.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            } catch (e: Exception) {
                observeCommon.postValue(Resource.error(e.message.toString(), null))
            }
        }
    }


    fun likeDislikeComment(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiPutWithoutRawBody(url)
                if (response.isSuccessful && response.body() != null) {
                    observeCommon.postValue(Resource.success("likeDislikeComment", response.body()))
                } else {
                    observeCommon.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            } catch (e: Exception) {
                observeCommon.postValue(Resource.error(e.message.toString(), null))
            }
        }
    }

    fun getAds(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiGetOnlyAuthToken(url)
                if (response.isSuccessful && response.body() != null) {
                    observeCommon.postValue(Resource.success("getAds", response.body()))
                } else {
                    observeCommon.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            } catch (e: Exception) {
                observeCommon.postValue(Resource.error(e.message.toString(), null))
            }
        }


    }

    fun rating(data: HashMap<String, Any>, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiPostForRawBody(url, data)
                if (response.isSuccessful && response.body() != null) {
                    observeCommon.postValue(Resource.success("rating", response.body()))
                } else {
                    observeCommon.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            } catch (e: Exception) {
                observeCommon.postValue(Resource.error(e.message.toString(), null))
            }
        }
    }
}