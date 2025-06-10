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
    implementation(libs.androidx.emoji2.bundled)
    // 🔥 메모리 누수 감지 (개발용)
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")

    // 🗺️ 카카오 지도 SDK (추가)
    implementation("com.kakao.maps.open:android:2.12.8")

    // 위치 권한 (필요시)
    implementation ("com.google.android.gms:play-services-location:21.0.1")


    //Jetpack DataStore + AES256 암호화 액세스 및 리프레시 저장 +키스토어
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // ✅ Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.play.services.maps)
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-fragment:1.1.0")
    kapt("androidx.hilt:hilt-compiler:1.1.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    // ✅ Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")

    // ✅ Google 로그인
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // ✅ 안드로이드 기본 구성 요소
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.34.0")

    // ✅ Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.3")
    implementation("androidx.navigation:navigation-compose:2.8.3")  // ✨ 추가

    // ✅ 네트워크 (업데이트 필요)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")  // ✨ 업데이트
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")  // ✨ 추가



    // ✅ 이미지
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")
    implementation("io.coil-kt:coil-compose:2.5.0")  // ✨ 업데이트

    // ✅ 기타
    implementation("com.leinardi.android:speed-dial:3.3.0")

    // ✅ 테스트
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // ✅ Compose + Hilt (업데이트)
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))  // ✨ 업데이트
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // ✨ 중요: 누락된 Lifecycle Compose 의존성들 추가
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")

    // ✨ Foundation 추가 (Pager용)
    implementation("androidx.compose.foundation:foundation")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}