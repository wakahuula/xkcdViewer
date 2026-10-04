plugins {
    alias(libs.plugins.aboutlibraries.android)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
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
        versionCode = 13
        versionName = "1.4.6-kmp-dev"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures.compose = true
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(21)
}

// No unsigned or debug-signed release during the foundation phase.
val verifyReleaseConfiguration by tasks.registering {
    doLast {
        throw GradleException("Release is not enabled yet. Verify the published version and original signing configuration first.")
    }
}
tasks.matching { it.name == "preReleaseBuild" }.configureEach {
    dependsOn(verifyReleaseConfiguration)
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
    androidTestImplementation(libs.coroutines.test)
    androidTestImplementation(libs.compose.ui.test)
    androidTestImplementation(libs.coil.compose)
    androidTestImplementation(libs.coil.ktor)
    androidTestImplementation(libs.ktor.mock)
    androidTestImplementation(libs.aboutlibraries.core)
    debugImplementation(libs.compose.ui.test.manifest)
}
