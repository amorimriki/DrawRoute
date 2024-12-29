plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services") // Plugin do Google Services
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1"

}

android {
    namespace = "com.example.drawroute"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.drawroute"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
// Firebase
    
    //Auth Platform
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.database)

    //Realtime Database
    implementation(libs.google.firebase.database)

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Material Design
    implementation(libs.material)
    implementation(libs.androidx.material3)

    // UI and Compose
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.play.services.maps)
    implementation(libs.firebase.crashlytics.buildtools)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.kotlin.stdlib.jdk7)
    implementation(libs.androidx.appcompat.v141)
    implementation(libs.androidx.core.ktx.v170)
    implementation(libs.gson)
    testImplementation(libs.junit)

    // Dependency to include Maps SDK for Android
    implementation (libs.kotlin.stdlib.jdk7)
    implementation (libs.androidx.appcompat.v141)
    implementation (libs.androidx.core.ktx.v170)
    implementation (libs.gson)
    testImplementation (libs.junit)

    // Dependency to include Maps SDK for Android
    implementation (libs.play.services.maps)
    implementation (libs.android.maps.utils)
    // [END_EXCLUDE]

    // Maps SDK for Android KTX Library
    implementation (libs.maps.ktx)

    // Maps SDK for Android Utility Library KTX Library
    implementation (libs.maps.utils.ktx)

    // Lifecycle Runtime KTX Library
    implementation (libs.androidx.lifecycle.runtime.ktx)

    implementation (libs.glide)
    annotationProcessor (libs.glide.compiler)
}

