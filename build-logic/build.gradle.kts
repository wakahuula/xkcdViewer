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
        create("legacyResources") {
            id = "xkcd.legacy-resources"
            implementationClass = "de.stoupas.xkcd.build.LegacyResourcesPlugin"
        }
    }
}
