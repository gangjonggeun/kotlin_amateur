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
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // 🛡️ Room 데이터베이스 최적화 설정
        javaCompileOptions {
            annotationProcessorOptions {
                argument("room.schemaLocation", "$projectDir/schemas")
                argument("room.incremental", "true")
                argument("room.expandProjection", "true")
            }
        }
        
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
    // 🛡️ KAPT 최적화 설정
    kapt {
        correctErrorTypes = true
        useBuildCache = true
        
        // 메모리 최적화
        javacOptions {
            option("-Xmx1024m")
        }
        
        // Room 스키마 검증 최적화
        arguments {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental", "true")
        }
    }
    
    kotlin {
        jvmToolchain(17) // ✅ Kotlin 2.1과 Compose 1.6 이상에 필수
    }
}
dependencies {
    // 🔥 핵심! Fragment 의존성 추가
    implementation(libs.androidx.fragment.ktx)
    
    // ✅ 기본 AndroidX 라이브러리들 (libs 방식으로 통일)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity)
    
    // ✅ Navigation (libs 방식)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.navigation.compose)
    
    // ✅ Hilt 의존성 (이미 libs 방식)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    
    // ✅ Compose BOM & Core (libs 방식)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
    
    // 🔥 Material Icons 추가 (핵심!)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.material.icons.extended)
    
    // ✅ Network & Image (libs 방식)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.coil.compose)
    
    // ✅ Firebase & Google Services (libs 방식)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.play.services.auth)
    implementation(libs.play.services.base)
    implementation(libs.play.services.tasks)
    implementation(libs.play.services.location)
    
    // ✅ 기타 라이브러리들 (libs 방식)
    implementation(libs.kakao.maps)
    implementation(libs.androidx.paging.compose)
    
    // 🛡️ Room 데이터베이스 최적화 (메모리 안전)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // SQLite 드라이버 명시적 추가 (호환성 문제 해결)
    implementation("androidx.sqlite:sqlite-ktx:2.4.0")
    
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)
    
    // 🔥 메모리 누수 감지 (Debug만)
    debugImplementation(libs.leakcanary.android)
    
    // ✅ 테스트 라이브러리들
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}