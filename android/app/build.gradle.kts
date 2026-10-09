import java.util.Properties
import de.stoupas.xkcd.build.ReleaseGuardExtension

plugins {
    id("xkcd.release-guard")
    alias(libs.plugins.aboutlibraries.android)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val signingProperties = Properties().apply {
    val source = rootProject.file("android/key.properties")
    if (source.isFile) source.inputStream().use(::load)
}
val confirmedVersionCode = providers.gradleProperty("xkcd.releaseVersionCode").map(String::toInt).orElse(13)
val confirmedVersionName = providers.gradleProperty("xkcd.releaseVersionName").orElse("1.4.6-kmp-dev")
configure<ReleaseGuardExtension> {
    versionCode.set(confirmedVersionCode)
    versionName.set(confirmedVersionName)
    previousVersionCode.set(providers.gradleProperty("xkcd.previousVersionCode").map(String::toInt))
}

android {
    namespace = "de.stoupas.xkcd"
    compileSdk = 37
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "de.stoupas.xkcd"
        minSdk = 23
        targetSdk = 37
        // Development baseline only. Published version and signing need verification.
        versionCode = confirmedVersionCode.get()
        versionName = confirmedVersionName.get()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures.compose = true
    signingConfigs {
        create("originalRelease") {
            signingProperties.getProperty("storeFile")?.let { storeFile = rootProject.file("android").resolve(it) }
            storePassword = signingProperties.getProperty("storePassword")
            keyAlias = signingProperties.getProperty("keyAlias")
            keyPassword = signingProperties.getProperty("keyPassword")
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("originalRelease")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.activity.compose)
    implementation(libs.browser)
    implementation(libs.coroutines.core)
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.resources)
    implementation(libs.coil.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.ktor.core)
    implementation(libs.ktor.okhttp)
    androidTestImplementation(libs.android.test.runner)
    androidTestImplementation(libs.android.test.junit)
    // Compose's older transitive Espresso uses InputManager.getInstance, removed on API 37.
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.coroutines.test)
    androidTestImplementation(libs.compose.ui.test)
    androidTestImplementation(libs.coil.compose)
    androidTestImplementation(libs.coil.ktor)
    androidTestImplementation(libs.ktor.mock)
    androidTestImplementation(libs.aboutlibraries.core)
    debugImplementation(libs.compose.ui.test.manifest)
}
