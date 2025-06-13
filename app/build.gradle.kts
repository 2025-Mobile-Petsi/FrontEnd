plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {

   viewBinding{
       enable = true
   }
    namespace = "com.example.petsi"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.petsi"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    //지도 관련
    implementation("com.naver.maps:map-sdk:3.21.0")
    //밑에 두 개는 날씨 관련
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //bottom_nav관련 implementation
    implementation("com.google.android.material:material:1.11.0")

    //지도 검색 관련
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")

    //위치 권한 추가
    implementation("com.google.android.gms:play-services-location:21.0.1")
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // JSON 변환 (Gson 사용 시)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
}