import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        FileInputStream(file).use { load(it) }
    }
}

fun localOrEnv(key: String): String? =
    localProperties.getProperty(key) ?: System.getenv(key)

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
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    // Conecta los ViewModels con Compose y retiene el estado ante giros de pantalla
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    // Gestiona la navegación y el flujo entre pantallas en una sola Activity
    implementation("androidx.navigation:navigation-compose:2.10.1")
    // Libreria del calendario
    implementation("com.kizitonwose.calendar:compose:2.6.0")
}