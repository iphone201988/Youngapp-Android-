package com.tech.young.data.api

import com.tech.young.BuildConfig
import java.util.TimeZone


object Constants {
    const val BASE_URL = BuildConfig.BASE_URL
    const val BASE_URL_IMAGE = BuildConfig.BASE_URL_IMAGE

    val timeZone: String by lazy {
        val tz = TimeZone.getDefault().id

        if (tz == "Asia/Calcutta") "Asia/Kolkata" else tz
    }


    /**************** API LIST *****************/
    const val LOGIN = "user/login"
    const val REGISTER = "user/register"
    const val VERIFY_OTP = "user/verifyOtp"
    const val SENT_OTP  = "user/sendOtp"
    const val COMPLETE_REGISTRATION = "user/completeRegistration"
    const val VERIFY_2FA = "user/verify2FA"
    const val GET_TRENDING_TOPICS = "post/getTrendingTopics"
    const val GET_USER_PROFILE = "user/getUserProfile"
    const val GET_EVENTS = "event"
    const val CREATE_EVENT = "event"
    const val CONTACT_US = "user/contactUs"
    const val GET_SAVED_POST ="post/getSavedPosts"
    const val GET_USERS= "user/getUsers"
    const val GET_CHAT = "chat"
    const val SEND_REPORT  = "report"
    const val CHANGE_PASSWORD  = "user/changePassword"
    const val CREATE_VAULT = "vault"
    const val GET_CHAT_MESSAGE = "chat/"
    const val GET_VAULT = "vault"
    const val GET_SAVED_VAULT = "vault/getSavedVaults"
    const val GET_VAULT_DETAIL = "vault/"
    const val JOIN_LEAVE_VAULT = "vault/joinLeaveVault/"
    const val GET_ALL_POST = "post"
    const val SAVE_UNSAVE_POST = "post/saveUnsavePost/"
    const val SAVE_UNSAVE_VAULT = "vault/saveUnsaveVault/"
    const val VAULT_ADD_COMMENT = "vault/addComment"
    const val POST_ADD_COMMENT = "post/addComments"
    const val LIKE_DISLIKE_POST = "post/likeDislikePost/"
    const val RESHARE_POST  = "post/reshare/"
    const val UPDATE_USER = "user/updateUser"
    const val LOG_OUT = "user/logout"
    const val ECO_SYSTEM = "user/getLatestUsers"
    const val FOLLOW_UNFOLLOW = "user/followUnfollowUser/"
    const val  UPDATE_CUSTOMER = "user/updateCustomers/"
    const val GET_POST_BY_ID = "post/"
    const val ADD_COMMENT = "comment"
    const val GET_COMMENT  = "comment"
    const val LIKE_DISLIKE_COMMENT = "comment/"
    const val CREATE_SHARE = "post"
    const val DELETE_POST = "post/"
    const val UN_AUTH = "user/getUnauthUser"
    const val GET_ADS = "ads"
    const val RATING = "rating"
    const val GET_RATING = "rating"
    const val DELETE_VAULT = "vault/"
    const val DOWNLOAD_HISTORY = "post/downloadHistory"
    const val DELETE_ACCOUNT = "deleteAccount"

    const val SCHEDULE_STREAM = "event/scheduleStreamEvent/"

    const val media = "user/getUploadedMedia"


    const val DELETE_EVENT = "event/"
    const val EDIT_EVENTS = "event/editEvent/"

    const val RESET_PASSWORD = "user/resetPassword"

    const val LAST_24_HOURS = "post/subscription/last24hours"

    const val  UPGRADE_PLAN = "user/upgradePlan"
    const val NOTIFICATION = "user/getAllNotifications"

    const val PAYMENT_HISTORY = "payment/userPayments"

    const val GET_RECORDED_STREAM_COMMENT = "chat/getLiveStreamingMessages"

    const val POST_ANALYTICS = "post/analytics"

    const val AI_CHAT = "research/ask"

    const val GET_PERFORMANCE = "invest/performance"

    const val MONTHLY_ANALYSIS = "invest/montly-analysis"

    const val ADD_INVESTMENT = "invest/add-investment"

    const val UPDATE_INVESTMENT = "invest/update-investment"

    const val INVESTMENT_PLAN = "research/plans"

    const val BUSINESS_PLAN = "research/business"
    const val GET_INVESTMENT = "invest/get-investment"


    const val VARIABLES = "user/variables"


    const val ACCEPT_REJECT_EVENT = "event/respond"


    /*************** Temp value store****************/
    var chooseAccountType = ""

    var userLastLogin = ""

}