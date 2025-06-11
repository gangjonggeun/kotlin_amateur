plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)


    id("com.google.dagger.hilt.android")

    id("androidx.navigation.safeargs.kotlin")
    id("org.jetbrains.kotlin.kapt") //kapt
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.kotlin_amateur"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.kotlin_amateur"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        // 🔥 메모리 관련 설정
        multiDexEnabled = true
        manifestPlaceholders["emoji_compat_config"] = "disabled"
        // 🔥 이미지 압축 설정
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // 🔥 Debug에서도 약간의 최적화
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    kotlin {
        jvmToolchain(17) // ✅ Kotlin 2.1과 Compose 1.6 이상에 필수
    }
}
dependencies {
    // 🔥 메모리 누수 감지만 debug에서
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")

    // 🗺️ 카카오 지도만 유지
    implementation("com.kakao.maps.open:android:2.12.8")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // 🔐 보안 관련 최소한만
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // ✅ Hilt (필수)
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // 🔥 Firebase 최소화 (Analytics 제거)
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-auth") // Auth만 유지

    // ✅ Google 로그인 - 수정된 부분! 🎯
    implementation("com.google.android.gms:play-services-auth:21.2.0") // 🔥 최신 버전
    implementation("com.google.android.gms:play-services-base:18.5.0") // 🔥 추가!
    implementation("com.google.android.gms:play-services-tasks:18.2.0") // 🔥 추가!

    // ✅ 안드로이드 기본 (최소화)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.activity:activity-ktx:1.8.2")

    // ✅ Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.3")
    implementation("androidx.navigation:navigation-compose:2.8.3")

    // 🔥 네트워크 최적화 (OkHttp 캐시 크기 제한)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // 🔥 이미지 라이브러리 통합 (Coil만 사용)
    implementation("io.coil-kt:coil-compose:2.5.0")

    // ✅ Compose (최소한만)
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.compose.foundation:foundation")

    debugImplementation("androidx.compose.ui:ui-tooling")
}