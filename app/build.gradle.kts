plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

import java.util.Properties
import java.io.FileInputStream

// Load app configuration
val appConfigFile = file("app-config.properties")
val appConfig = Properties()
if (appConfigFile.exists()) {
    appConfig.load(FileInputStream(appConfigFile))
}

val appName = appConfig.getProperty("APP_NAME", "My Client App")
val appVersionName = appConfig.getProperty("APP_VERSION_NAME", "1.0.0")
val appVersionCode = appConfig.getProperty("APP_VERSION_CODE", "1").toInt()
val apkName = appConfig.getProperty("APK_NAME", "client-app")

android {
    namespace = "com.pocpe.todo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.pocpe.todo"
        minSdk = 24
        targetSdk = 34
        versionCode = appVersionCode
        versionName = appVersionName
        resValue("string", "app_name", appName)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "$appName (Debug)")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    
    // Meta SDK (Facebook)
    implementation("com.facebook.android:facebook-android-sdk:16.2.0")
    
    // Mixpanel
    implementation("com.mixpanel.android:mixpanel-android:7.3.1")
    
    // Google Play Services for Advertising ID
    implementation("com.google.android.gms:play-services-ads-identifier:18.0.1")
    
    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
