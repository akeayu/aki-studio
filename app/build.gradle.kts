plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.akidigitalstudio.aki"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.akidigitalstudio.aki"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "android.test.InstrumentationTestRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    // CPU Interpreter only; no GPU, NNAPI, Play Services, or network runtime.
    implementation("com.google.ai.edge.litert:litert:2.4.0")

    // Platform-window posture and folding-feature geometry; all processing remains on-device.
    implementation("androidx.window:window-java:1.4.0")

    testImplementation("junit:junit:4.13.2")
}
