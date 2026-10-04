package de.stoupas.xkcd.build

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import java.util.Properties

abstract class ReleaseGuardExtension {
    abstract val versionCode: Property<Int>
    abstract val previousVersionCode: Property<Int>
    abstract val versionName: Property<String>
}

class ReleaseGuardPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("xkcdRelease", ReleaseGuardExtension::class.java)
        val verify = project.tasks.register("verifyReleaseConfiguration") { task ->
            task.group = "verification"
            task.description = "Blocks releases without original signing and a confirmed published version."
            task.doLast {
                val file = project.rootProject.file("android/key.properties")
                val properties = Properties()
                if (file.isFile) file.inputStream().use(properties::load)
                try {
                    validateReleaseConfiguration(properties, project.rootProject.file("android").toPath(), extension.versionCode.get(), extension.previousVersionCode.orNull)
                    val name = extension.versionName.orNull
                    require(!name.isNullOrBlank() && !name.contains("dev", ignoreCase = true)) { "Release blocked: confirmed release versionName is required" }
                } catch (failure: IllegalArgumentException) {
                    throw GradleException(failure.message ?: "Release configuration is invalid")
                }
            }
        }
        project.tasks.matching { it.name == "preReleaseBuild" }.configureEach { it.dependsOn(verify) }
    }
}
