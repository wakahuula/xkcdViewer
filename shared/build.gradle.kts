import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import de.stoupas.xkcd.build.GenerateLegacyResources

plugins {
    id("xkcd.legacy-resources")
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    jvmToolchain(21)
    android {
        namespace = "de.stoupas.xkcd.shared"
        compileSdk = 37
        minSdk = 23
        androidResources.enable = true
        withHostTestBuilder {}
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }
    jvm {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.resources)
            implementation(libs.coroutines.core)
            implementation(libs.serialization.json)
            implementation(libs.ktor.core)
            implementation(libs.ktor.content.negotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.coil.compose)
            implementation(libs.coil.ktor)
            implementation(libs.zoomimage.coil)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.runtime.compose)
            implementation(libs.navigation.compose)
            implementation(libs.settings)
            implementation(libs.aboutlibraries.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.coroutines.test)
            implementation(libs.ktor.mock)
            implementation(libs.settings.test)
        }
        androidMain.dependencies {
            implementation(libs.ktor.okhttp)
            implementation(libs.browser)
        }
        jvmTest.dependencies {
            implementation(kotlin("test-junit"))
            // Resource locale lookup reads the desktop system theme through Skiko.
            implementation(compose.desktop.currentOs)
        }
        jvmTest {
            resources.srcDir(rootProject.file("assets/locale"))
        }
    }
}

val prepareAppResources by tasks.registering(Sync::class) {
    from(tasks.named<GenerateLegacyResources>("generateLegacyResources").flatMap { it.outputDirectory })
    from(layout.projectDirectory.dir("src/commonMain/composeResources"))
    from(rootProject.file("LICENSE")) {
        into("files")
        rename { "app_license.txt" }
    }
    into(layout.buildDirectory.dir("generated/appComposeResources"))
    duplicatesStrategy = DuplicatesStrategy.FAIL
}

compose.resources {
    publicResClass = true
    packageOfResClass = "de.stoupas.xkcd.resources"
    generateResClass = always
    customDirectory(
        sourceSetName = "commonMain",
        directoryProvider = prepareAppResources.map { layout.dir(provider { it.destinationDir }).get() },
    )
}
