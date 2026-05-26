package semverrelease

import com.alphasystem.gradle.semver.release.internal.SemanticBuildVersionConfiguration
import org.gradle.api.Plugin
import org.gradle.api.Project
import semverrelease.tasks.DetermineVersionTask
import semverrelease.tasks.PushChanges
import semverrelease.tasks.TagTask

abstract class SemanticBuildVersioningPlugin : Plugin<Project> {

    private var config = SemanticBuildVersionConfiguration()

    override fun apply(project: Project) {
        val extension = project.extensions.create("semverrelease", SemanticBuildVersioningExtension::class.java, project)

        project.afterEvaluate {
            buildConfig(extension)
        }

        val determineVersionTask = project.tasks.register("determineVersion", DetermineVersionTask::class.java) {
            it.config.set(config)
            it.workingDirectory.set(project.projectDir)
        }

        project.tasks.register("printVersion") { it ->
            it.group = RELEASE_GROUP
            it.description = "Print the current version"
            it.dependsOn(determineVersionTask)
            it.doLast {
                println("Projected version is: $ANSI_GREEN${determineVersionTask.flatMap { it.version }.get()}$ANSI_RESET")
            }
        }

        project.tasks.register("generateTag", TagTask::class.java) { it ->
            it.config.set(config)
            it.version.set(determineVersionTask.flatMap { it.version })
            it.releaseTagComment.set(extension.releaseTagComment)
            it.addUnReleasedCommitsToTagComment.set(extension.addUnReleasedCommitsToTagComment)
            it.workingDirectory.set(project.projectDir)
            it.dependsOn("determineVersion")
        }

        project.tasks.register("pushChanges", PushChanges::class.java) {
            it.workingDirectory.set(project.projectDir)
            it.config.set(config)
        }
    }

    private fun buildConfig(extension: SemanticBuildVersioningExtension) {
        if (extension.startingVersion.isPresent) {
            config = config.copy(startingVersion = extension.startingVersion.get())
        }
        if (extension.tagPrefix.isPresent) {
            config = config.copy(tagPrefix = extension.tagPrefix.get())
        }
        if (extension.forceBump.isPresent) {
            config = config.copy(forceBump = extension.forceBump.get())
        }
        if (extension.newPreRelease.isPresent) {
            config = config.copy(newPreRelease = extension.newPreRelease.get())
        }
        if (extension.promoteToRelease.isPresent) {
            config = config.copy(promoteToRelease = extension.promoteToRelease.get())
        }
        if (extension.snapshot.isPresent) {
            config = config.copy(snapshot = extension.snapshot.get())
        }
        if (extension.defaultBumpLevel.isPresent) {
            config = config.copy(defaultBumpLevel = extension.defaultBumpLevel.get())
        }
        if (extension.componentToBump.isPresent) {
            config = config.copy(componentToBump = extension.componentToBump.get())
        }
        if (extension.snapshotConfig.isPresent) {
            config = config.copy(snapshotConfig = extension.snapshotConfig.get())
        }
        if (extension.preReleaseConfig.isPresent) {
            config = config.copy(preReleaseConfig = extension.preReleaseConfig.get())
        }
        if (extension.hotfixBranchPattern.isPresent) {
            config = config.copy(hotfixBranchPattern = extension.hotfixBranchPattern.get())
        }
        if (extension.extraReleaseBranches.isPresent) {
            config = config.copy(extraReleaseBranches = extension.extraReleaseBranches.get())
        }
    }
}
