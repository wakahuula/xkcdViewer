package de.stoupas.xkcd.build

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class GenerateLegacyResources : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() = generateLegacyResources(inputDirectory.get().asFile.toPath(), outputDirectory.get().asFile.toPath())
}

abstract class VerifyLegacyResources : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputDirectory: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val generatedDirectory: DirectoryProperty

    @TaskAction
    fun verify() = verifyLegacyResources(inputDirectory.get().asFile.toPath(), generatedDirectory.get().asFile.toPath())
}

class LegacyResourcesPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val generation = project.tasks.register("generateLegacyResources", GenerateLegacyResources::class.java) { task ->
            task.group = "resources"
            task.description = "Generate Compose resources without modifying the original contributor assets."
            task.inputDirectory.set(project.rootProject.layout.projectDirectory.dir("assets"))
            task.outputDirectory.set(project.layout.buildDirectory.dir("generated/legacyComposeResources"))
        }
        val verification = project.tasks.register("verifyLegacyResources", VerifyLegacyResources::class.java) { task ->
            task.group = "verification"
            task.description = "Check generated translations, fonts and contributor data against their sources."
            task.inputDirectory.set(generation.flatMap { it.inputDirectory })
            task.generatedDirectory.set(generation.flatMap { it.outputDirectory })
        }
        project.tasks.matching { it.name == "check" }.configureEach { it.dependsOn(verification) }
    }
}
