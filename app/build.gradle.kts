

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.eco_plate"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.eco_plate"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // Local development URL (commented out)
            // buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:3000/v1/\"")
            // Using Railway hosted backend
            buildConfigField("String", "BASE_URL", "\"https://ecoplate-backend-production.up.railway.app/v1/\"")
            // Stripe test publishable key
            buildConfigField("String", "STRIPE_PUBLISHABLE_KEY", "\"pk_test_51QShCmBNrRwRN8PQv6eHNNbnAKGOWK5aeB5KBBMmNh3c1xFPTGXHQX7i4F5mCFYO3nE7u8YpTVMVRNy7TsC8gJpD00sJQXDyjS\"")
            isMinifyEnabled = false
        }
        release {
            // Using Railway hosted backend (production)
            buildConfigField("String", "BASE_URL", "\"https://ecoplate-backend-production.up.railway.app/v1/\"")
            // buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:3000/v1/\"")
            // Stripe test publishable key
            buildConfigField("String", "STRIPE_PUBLISHABLE_KEY", "\"pk_test_51QShCmBNrRwRN8PQv6eHNNbnAKGOWK5aeB5KBBMmNh3c1xFPTGXHQX7i4F5mCFYO3nE7u8YpTVMVRNy7TsC8gJpD00sJQXDyjS\"")

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
    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }
    
    lint {
        disable += "PermissionLaunchedDuringComposition"
        abortOnError = false
    }
}

// KSP configuration
ksp {
    arg("dagger.hilt.shareTestComponents", "true")
    arg("dagger.fastInit", "ENABLED")
    arg("dagger.experimentalDaggerErrorMessages", "ENABLED")
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.glance.preview)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // Jetpack Compose BOM for consistent versions
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    
    // Accompanist libraries for additional Compose features
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")
    implementation("com.google.accompanist:accompanist-pager:0.32.0")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.32.0")
    implementation("com.google.accompanist:accompanist-placeholder:0.32.0")
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // Google Maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.maps.android:maps-compose:4.3.0")
    implementation("com.google.maps.android:maps-compose-utils:4.3.0")
    
    // Google Places (for address autocomplete)
    implementation("com.google.android.libraries.places:places:3.3.0")
    
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // JSON Parsing
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // Dependency Injection - Hilt
    implementation("com.google.dagger:hilt-android:2.56.2")
    ksp("com.google.dagger:hilt-compiler:2.56.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Room Database for local caching
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // Compose LiveData
    implementation("androidx.compose.runtime:runtime-livedata")
    
    // SplashScreen API
    implementation("androidx.core:core-splashscreen:1.0.1")
    
    // Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Widgets
    implementation("androidx.glance:glance:1.1.1")
    implementation("androidx.glance:glance-appwidget:1.1.1")
    implementation("androidx.glance:glance-material:1.1.1")
    implementation("androidx.glance:glance-material3:1.1.1")
    // Widget Debug Previews
    debugImplementation("androidx.glance:glance-preview:1.1.1")
    debugImplementation("androidx.glance:glance-appwidget-preview:1.1.1")

    // Notifications
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // CameraX
    implementation("androidx.camera:camera-core:1.3.3")
    implementation("androidx.camera:camera-camera2:1.3.3")
    implementation("androidx.camera:camera-lifecycle:1.3.3")
    implementation("androidx.camera:camera-view:1.3.3")
    // ML Kit barcode scanning
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    //Text Scan
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")

    //Maps Addresses
    implementation("com.google.android.libraries.places:places:3.5.0")

    // Stripe SDK for payments
    implementation("com.stripe:stripe-android:20.35.0")
}