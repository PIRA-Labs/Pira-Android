plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.taak.watch.signedtest"
    compileSdk = 34

    val taakStorePassword = System.getenv("TAAK_STORE_PASSWORD")
    val taakKeyPassword = System.getenv("TAAK_KEY_PASSWORD")
    val hasTaakSigning = !taakStorePassword.isNullOrBlank() && !taakKeyPassword.isNullOrBlank()
    if (hasTaakSigning) signingConfigs {
        create("taak") {
            storeFile = file("../signing/taak-watch.keystore")
            storePassword = taakStorePassword
            keyAlias = "taak-watch"
            keyPassword = taakKeyPassword
        }
    }

    defaultConfig {
        applicationId = "ai.pira.android"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        getByName("debug") {
            if (hasTaakSigning) signingConfig = signingConfigs.getByName("taak")
        }

        getByName("release") {
            isMinifyEnabled = false
            if (hasTaakSigning) signingConfig = signingConfigs.getByName("taak")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(files("libs/xms-wearable-lib_1.4_release.aar"))
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core-ktx:1.13.1")
    testImplementation(kotlin("test"))
}
