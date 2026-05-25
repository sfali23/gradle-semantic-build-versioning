package semverrelease.tasks

import com.alphasystem.gradle.semver.release.internal.SemanticBuildVersion
import com.alphasystem.gradle.semver.release.internal.SemanticBuildVersionConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import semverrelease.RELEASE_GROUP

abstract class DetermineVersionTask : DefaultTask() {

    init {
        group = RELEASE_GROUP
        description = "Determine the version"
    }

    @get:Internal
    abstract val version: Property<String>

    @get:Internal
    abstract val config: Property<SemanticBuildVersionConfiguration>

    @get:Internal
    abstract val workingDirectory: RegularFileProperty

    @TaskAction
    fun determineVersion() {
        val determineVersion = SemanticBuildVersion(workingDirectory.get().asFile, config.get()).determineVersion()
        version.set(determineVersion)
    }
}
