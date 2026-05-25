package semverrelease.tasks

import com.alphasystem.gradle.semver.release.common.JGitAdapter
import com.alphasystem.gradle.semver.release.internal.SemanticBuildVersion
import com.alphasystem.gradle.semver.release.internal.SemanticBuildVersionConfiguration
import org.eclipse.jgit.lib.Ref
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import semverrelease.RELEASE_GROUP

abstract class TagTask : DefaultTask() {

    @get:Internal
    abstract val version: Property<String>

    @get:Internal
    abstract val releaseTagComment: Property<String>

    @get:Internal
    abstract val addUnReleasedCommitsToTagComment: Property<Boolean>

    @get:Internal
    abstract val config: Property<SemanticBuildVersionConfiguration>

    @get:Internal
    abstract val workingDirectory: RegularFileProperty

    @get:Internal
    abstract val tagRef: Property<Ref>

    init {
        group = RELEASE_GROUP
        description = "Create a tag"
    }

    @TaskAction
    fun createTag() {
        val workingDir = workingDirectory.get().asFile
        val baseConfig = config.get()
        val semanticBuildVersion = SemanticBuildVersion(workingDir, baseConfig)
        val tagPrefix = baseConfig.tagPrefix
        val version = version.get()
        val message = getTagComment(semanticBuildVersion)
        val tag = "$tagPrefix$version"

        val ref = JGitAdapter(workingDir).createTag(tag, message.isNotBlank(), message)
        tagRef.set(ref)
    }

    private fun getTagComment(semanticBuildVersion: SemanticBuildVersion): String {
        val releaseTagComment = releaseTagComment.get()

        val unreleasedCommits =
            if (addUnReleasedCommitsToTagComment.get()) semanticBuildVersion.getUnReleasedCommits()
            else listOf()
        val defaultComment = if (releaseTagComment.isNotBlank()) "$releaseTagComment: ${version.get()}" else ""

        return listOf(defaultComment, *unreleasedCommits.toTypedArray()).joinToString(System.lineSeparator())
    }
}
