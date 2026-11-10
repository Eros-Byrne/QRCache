import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    namespace = "app.qrcache"
    compileSdk = 36

    defaultConfig {
        applicationId = "app.qrcache"
        minSdk = 23
        targetSdk = 36
        versionCode = 6
        versionName = "1.1.0"
    }

    signingConfigs {
        // The upload key comes from environment variables (see docs/RELEASING.md).
        // It is never stored in the repository.
        val keystore = System.getenv("QRCACHE_KEYSTORE_FILE")
        if (!keystore.isNullOrBlank()) {
            create("upload") {
                storeFile = file(keystore)
                storePassword = System.getenv("QRCACHE_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("QRCACHE_KEY_ALIAS")
                keyPassword = System.getenv("QRCACHE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Upload key when it is set. Otherwise, with -Pqrcache.testSigning, the debug key,
            // so CI can hand out an installable release build for testing.
            signingConfig = signingConfigs.findByName("upload")
                ?: if (providers.gradleProperty("qrcache.testSigning").isPresent) {
                    signingConfigs.getByName("debug")
                } else {
                    null
                }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        // java.time and other newer Java APIs on Android 6–7
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        // Robolectric needs merged resources for the database tests.
        unitTests.isIncludeAndroidResources = true
    }
}

room {
    // Exported schemas are needed to write migrations later.
    schemaDirectory("$projectDir/schemas")
}

ksp {
    arg("room.generateKotlin", "true")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    coreLibraryDesugaring(libs.android.desugarJdkLibs)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.datastore.preferences)

    // Camera + on-device QR decoding (bundled model: works offline, no Play services needed)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.mlkitVision)
    implementation(libs.mlkit.barcodeScanning)

    // Local history database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit4)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.robolectric)
}
