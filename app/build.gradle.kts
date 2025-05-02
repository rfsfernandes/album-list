plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "xyz.rfsfernandes.albumlist"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "xyz.rfsfernandes.albumlist"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // For obfuscation reasons. Probably overkill here but it's my standard practice
        buildConfigField("String", "DB_NAME", "\"album_list_db\"")
        buildConfigField("String", "API_ENDPOINT", "\"https://static.leboncoin.fr/\"")
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
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.navigation.compose)

    // Testing
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.kotlinx.coroutines.test)
    // Unit tests
    testImplementation(libs.turbine)
    testImplementation(libs.mockk.android)
    testImplementation(libs.mockk.agent)
    androidTestImplementation(libs.turbine)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.mockk.agent)

    // Room
    implementation(libs.room)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    ksp(libs.room.compiler)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converters.moshi)
    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.moshi)
    ksp(libs.moshi.codegen)

    // Paging
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.androidx.paging.common)

    // Coil
    implementation(libs.coil)

    implementation(libs.kotlinx.serialization)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.core)
    implementation(libs.koin.androidx.compose)
    testImplementation(libs.koin.test)
    testImplementation(libs.koin.test.junit)
    androidTestImplementation(libs.koin.test)
    androidTestImplementation(libs.koin.test.junit)
}