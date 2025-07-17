plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin1kept)
    alias(libs.plugins.kotlinparcelize)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.tech.young"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tech.young"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.firebase.messaging.ktx)
//    implementation(libs.firebase.auth.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.databinding.runtime)
    implementation(libs.dagger)
    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.play.services.location)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.glide)
    implementation(libs.sdp.android)
    implementation(libs.ssp.android)
    implementation(libs.gson)
    kapt(libs.dagger.compiler)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.lottie)
    implementation(libs.converter.gson)


    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    //loader
    implementation(libs.android.spinkit)
    implementation(libs.ccp)
    implementation(libs.imagepicker)

    // rating bar
    implementation("com.github.wdsqjq:AndRatingBar:1.0.6")
    // calendar
    implementation("com.kizitonwose.calendar:view:2.0.3")

    // calender
    implementation ("com.applandeo:material-calendar-view:1.9.2")
    //dhaval image piceker
    implementation ("com.github.dhaval2404:imagepicker:2.1")
    //firebase
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    // media soup
    implementation ("io.github.haiyangwu:mediasoup-client:3.4.0")
    // socket
    implementation("io.socket:socket.io-client:2.0.1")



    // Video Player
    implementation ("com.google.android.exoplayer:exoplayer-core:2.18.1")
    implementation ("com.google.android.exoplayer:exoplayer-ui:2.18.1")
    implementation ("com.google.android.exoplayer:exoplayer-smoothstreaming:2.18.1")

}