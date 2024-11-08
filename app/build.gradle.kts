plugins {

    //id("com.android.application") //this line of code shows errors
    id("com.google.gms.google-services")
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    //id("dagger.hilt.android.plugin") this line of code is crashing
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.spaTi"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.spaTi"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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
}

dependencies {
    // Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation(libs.firebase.firestore)

    // AndroidX libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    //gson
    implementation ("com.google.code.gson:gson:2.8.9")

    // Material Design
    implementation(libs.material)
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("com.google.android.material:material:1.11.0")

    // Navigation components
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database.ktx)

    // Unit Testing dependencies
    testImplementation(libs.junit)

    // Android Instrumented Testing dependencies
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //Live Data
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")

    //Fragment
    implementation("androidx.fragment:fragment-ktx:1.8.3")

    //Dagger Hilt
    implementation("com.google.dagger:hilt-android:2.48")  // Use the latest version
    kapt("com.google.dagger:hilt-android-compiler:2.48")

    //constraint layout
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
}

