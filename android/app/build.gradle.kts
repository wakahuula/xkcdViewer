plugins {
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
    androidTestImplementation(libs.android.test.runner)
    androidTestImplementation(libs.android.test.junit)
}
