package com.tech.young.ui.notification_fragment

import com.tech.young.base.BaseViewModel
import com.tech.young.data.api.ApiHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NotificationFragmentVm @Inject constructor(val apiHelper: ApiHelper) : BaseViewModel() {
}