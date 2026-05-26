package semverrelease.tasks

import com.alphasystem.gradle.semver.release.common.JGitAdapter
import com.alphasystem.gradle.semver.release.internal.SemanticBuildVersionConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import semverrelease.ANSI_GREEN
import semverrelease.ANSI_RESET
import semverrelease.RELEASE_GROUP

abstract class PushChanges: DefaultTask() {

    @get:Internal
    abstract val workingDirectory: RegularFileProperty

    @get:Internal
    abstract val config: Property<SemanticBuildVersionConfiguration>

    init {
        group = RELEASE_GROUP
        description = "Push changes to remote"
    }

    @TaskAction
    fun pushChanges() {
        val workingDir = workingDirectory.get().asFile
        val baseConfig = config.get()
        val tag = "${baseConfig.tagPrefix}${project.version}"
        println("${ANSI_GREEN}Pushing tag: $tag$ANSI_RESET")
        JGitAdapter(workingDir).pushTag(tag)
    }
}
