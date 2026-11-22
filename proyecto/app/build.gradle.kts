import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Lee claves locales (API key y firma) desde local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

fun localOrEnv(key: String): String? =
    localProperties.getProperty(key) ?: System.getenv(key)

val linceApiKey = localProperties.getProperty("LINCE_API_KEY") ?: ""
val releaseStoreFile = localOrEnv("PMTOURS_STORE_FILE")
    ?.let { file(it) }
    ?.takeIf { it.exists() }

android {
    namespace = "com.pminteractive.pmtours"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.pminteractive.pmtours"
        minSdk = 26
        targetSdk = 37
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Inyecta la clave como constante en BuildConfig
        buildConfigField("String", "LINCE_API_KEY", "\"$linceApiKey\"")
        // URL base centralizada para Retrofit (es obligatorio que termine con "/")
        buildConfigField("String", "LINCE_BASE_URL", "\"https://ws.southbound.travel/lince-api/\"")
    }

    signingConfigs {
        releaseStoreFile?.let { store ->
            create("release") {
                storeFile = store
                storePassword = localOrEnv("PMTOURS_STORE_PASSWORD")
                keyAlias = localOrEnv("PMTOURS_KEY_ALIAS")
                keyPassword = localOrEnv("PMTOURS_KEY_PASSWORD")
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    // Dependencias de clase
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("androidx.navigation:navigation-compose:2.10.1")
    implementation("com.kizitonwose.calendar:compose:2.6.0")
    // Red y consumo de API REST (Retrofit + Gson + Logs)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}
