package com.alphasystem.gradle.semver.release.internal

import com.alphasystem.gradle.semver.release.common.JGitAdapter
import com.alphasystem.gradle.semver.release.common.TestRepository
import com.alphasystem.gradle.semver.release.test.toSemanticBuildVersionConfiguration
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

@DisplayName("Semantic Build Version Test")
class SemanticBuildVersionTest {

    private val workingDirectory = Files.createTempDirectory(UUID.randomUUID().toString()).toFile()
    private val repository = TestRepository(workingDirectory)
    private val adapter = repository.getAdapter()
    private var config = SemanticBuildVersionConfiguration()
    private var mainBranchName = ""

    // @Test
    fun testSingle() {
        val workingDirectory = Paths.get("/Users/sfali/development/personal/gradle-semantic-build-versioning").toFile()
        val updateConfig = toSemanticBuildVersionConfiguration("{startingVersion=0.2.0}")
        val sbv = SemanticBuildVersion(workingDirectory, updateConfig)

        val adapter = JGitAdapter(workingDirectory)
        adapter.getGit().tagList().call().forEach { println(it.name) }

        println(adapter.getTagsForCurrentBranch())
        println(sbv.determineVersion())

    }

    private fun createAnnotatedTag(semanticBuildVersion: SemanticBuildVersion, annotated: Boolean = true): String {
        val tag = semanticBuildVersion.determineVersion()
        println("tag: $tag")
        val fullTag = config.tagPrefix + tag
        repository.tag(fullTag, annotated)
        return tag
    }

    private fun close() {
        runCatching { repository.close() }
        runCatching { workingDirectory.deleteRecursively() }
    }

}
