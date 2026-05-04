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

        buildConfigField("String", "BASE_URL", "\"https://theboom.app:8000/api/v1/\"")
        buildConfigField("String", "BASE_URL_IMAGE", "\"https://youngappbucket.s3.us-east-2.amazonaws.com\"")
        buildConfigField("String", "GOOGLE_API_KEY", "\"AIzaSyD5Jt2e9ocVmXovnsOsdmtdhPRkP8m9IhQ\"")
        buildConfigField("String", "HEADER_API", "\"lkcMuYllSgc3jsFi1gg896mtbPxIBzYkEL\"")
        buildConfigField("String", "SOCKET_URL", "\"https://theboom.app:8000\"")
        buildConfigField("String", "FINNHUB_BASE_URL", "\"https://finnhub.io/api/v1/\"")
        buildConfigField("String", "FINNHUB_API_KEY", "\"d1s7jehr01qskg7s19agd1s7jehr01qskg7s19b0\"")
        buildConfigField("String", "DIDIT_CLIENT_ID", "\"QDPf650HjF-NyD4ARVlA4w\"")
        buildConfigField("String", "DIDIT_CLIENT_SECRET", "\"oVFp1ZwgNE_uNsF9b0Rz3hhbGg05zS-gd3boekX2aks\"")
        buildConfigField("String", "DIDIT_BASE_AUTH_URL", "\"https://apx.didit.me/\"")
        buildConfigField("String", "DIDIT_BASE_VERIFICATION_URL", "\"https://verification.didit.me/\"")
        buildConfigField("String", "RSS_FEED_URL", "\"https://feeds.content.dowjones.io/public/rss/mw_topstories\"")
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }

    //signing info - key - boom, pass- boom123
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.androidx.espresso.core)
    implementation(libs.billing.ktx)
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
    implementation("androidx.media3:media3-exoplayer:1.8.0")
    implementation("androidx.media3:media3-ui:1.8.0")

    implementation("org.jsoup:jsoup:1.16.1")


    //Image zoom
    implementation ("com.github.chrisbanes:PhotoView:2.3.0")


    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")



    //mp chart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.shimmer)

}