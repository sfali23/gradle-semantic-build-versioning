package com.alphasystem.gradle.semver.release.internal

import com.alphasystem.gradle.semver.release.common.TestRepository
import com.alphasystem.gradle.semver.release.test.toSemanticBuildVersionConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Files
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
        val updateConfig = toSemanticBuildVersionConfiguration("{startingVersion=0.2.0}")
        val sbv = SemanticBuildVersion(workingDirectory, updateConfig)

        mainBranchName = repository.getBranchName()
        repository.makeChanges().commit("Initial commit")
        createAnnotatedTag(sbv)

        repository.makeChanges().commit("Second commit").makeChanges().commit("Third commit [minor]").makeChanges()
        createAnnotatedTag(sbv)

        repository.commit().makeChanges().commit("Fourth commit").makeChanges()
        createAnnotatedTag(sbv)

        repository.commit().makeChanges().commit("Fifth commit").makeChanges()
        createAnnotatedTag(sbv)

        repository.commit().makeChanges().commit("Sixth commit").commit().commit().commit().commit()
        createAnnotatedTag(sbv)

        close()
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
