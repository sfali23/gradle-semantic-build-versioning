package semverrelease.tasks

import com.alphasystem.gradle.semver.release.common.JGitAdapter
import org.eclipse.jgit.lib.Ref
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import semverrelease.ANSI_RED
import semverrelease.ANSI_RESET
import semverrelease.RELEASE_GROUP

abstract class PushChanges: DefaultTask() {

    @get:Internal
    abstract val workingDirectory: RegularFileProperty

    @get:Internal
    abstract val tagRef: Property<Ref>

    init {
        group = RELEASE_GROUP
        description = "Push changes to remote"
    }

    @TaskAction
    fun pushChanges() {
        if (tagRef.isPresent) {
            println("Pushing tag ref ${tagRef.get().name}")
            JGitAdapter(workingDirectory.get().asFile).pushTag(tagRef.get())
        } else {
            println("$ANSI_RED${"Tag ref is not present"}$ANSI_RESET")
        }
    }
}
