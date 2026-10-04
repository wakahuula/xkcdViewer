plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.serialization.json)
    testImplementation(libs.junit)
    testImplementation(gradleTestKit())
}

tasks.test {
    systemProperty("xkcd.assets", rootDir.resolve("../assets").canonicalPath)
}

gradlePlugin {
    plugins {
        create("releaseGuard") {
            id = "xkcd.release-guard"
            implementationClass = "de.stoupas.xkcd.build.ReleaseGuardPlugin"
        }
        create("legacyResources") {
            id = "xkcd.legacy-resources"
            implementationClass = "de.stoupas.xkcd.build.LegacyResourcesPlugin"
        }
    }
}
