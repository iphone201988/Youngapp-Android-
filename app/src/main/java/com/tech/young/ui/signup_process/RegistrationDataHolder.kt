package com.tech.young.ui.signup_process

import okhttp3.MultipartBody

object RegistrationDataHolder {
    var profileImage: MultipartBody.Part? = null
    var imageMultiplatform: MutableList<MultipartBody.Part?> = MutableList(5) { null }

    var userId: String? = null
    var role: String? = null
    var crdNumber: String? = null

    // Register for general member
    var age: String? = null
    var gender: String? = null
    var maritalStatus: String? = null
    var children: String? = null
    var homeOwnership: String? = null

    // Financial Advisor & Insurance Broker Account Registration
    var productsServicesOffered: String? = null
    var areasOfExpertise: String? = null

    // Startup and Small Business Account Registration
    var industry: String? = null
    var interestedIn: String? = null

    // Select your interest
    var objective: String? = null
    var financialExperience: String? = null
    var investments: String? = null
    var servicesInterested: String? = null


}
