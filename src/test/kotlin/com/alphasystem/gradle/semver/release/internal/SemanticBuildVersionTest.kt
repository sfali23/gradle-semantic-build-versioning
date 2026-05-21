package com.alphasystem.gradle.semver.release.internal

import com.alphasystem.gradle.semver.release.common.JGitAdapter
import com.alphasystem.gradle.semver.release.common.TestRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.util.UUID

@DisplayName("Semantic Build Version Test")
class SemanticBuildVersionTest {

    private val workingDirectory: File = Files.createTempDirectory(UUID.randomUUID().toString()).toFile()
    private val repository: TestRepository = TestRepository.create(workingDirectory)
    private val adapter: JGitAdapter = JGitAdapter(workingDirectory)
    private var config: SemanticBuildVersionConfiguration = SemanticBuildVersionConfiguration()
    private var mainBranchName = ""

    @Test
    fun testSingle() {
        mainBranchName = repository.getBranchName()

        val tags = "v0.1.0,v0.2.0,v1.0.0"
        tags.split(",").forEach { tag -> repository.commitAndTag(tag, true) }

        repository.makeChanges()

        createAnnotatedTag(true)

        close()
    }

    private fun createAnnotatedTag(annotated: Boolean): String {
        val tag = SemanticBuildVersion(workingDirectory, config).determineVersion()
        repository.tag(tag, annotated)
        return tag
    }

    private fun close() {
        runCatching { repository.close() }
        runCatching { workingDirectory.deleteRecursively() }
    }
}