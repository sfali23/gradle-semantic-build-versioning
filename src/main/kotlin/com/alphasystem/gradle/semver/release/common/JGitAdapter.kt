package com.alphasystem.gradle.semver.release.common

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File
import java.io.IOException
import java.util.*
import java.util.stream.Collectors
import java.util.stream.StreamSupport

/**
 * Provides an adapter for interacting with a Git repository using the JGit library. This class
 * supports various Git-related operations such as retrieving commits, branches, tags, and analyzing
 * repository states.
 */
class JGitAdapter(private val workingDir: File) {
    private val repository: Repository = JGitAdapter.initRepository(workingDir)
    private var git: Git? = null

    fun getRepository(): Repository = repository

    fun getGit(): Git {
        if (git == null) {
            git = Git(repository)
        }
        return git!!
    }

    @Throws(IOException::class)
    fun getHeadCommit(): ObjectId = repository.resolve(Constants.HEAD)!!

    @Throws(IOException::class)
    private fun getShortHash(id: ObjectId): String = repository.newObjectReader().abbreviate(id).name()

    @Throws(IOException::class)
    fun getShortHash(): String = getShortHash(getHeadCommit())

    @Throws(IOException::class)
    fun getCurrentBranch(): String = repository.branch

    fun getRevWalk(): RevWalk = RevWalk(repository)

    @Throws(GitAPIException::class)
    fun getTagsForCurrentBranch(): List<String> {
        val tags = getGit().tagList().call().stream()
            .collect(Collectors.groupingBy { tagRef: Ref -> getRevWalk().parseCommit(getNonNullObjectId(tagRef)).id })

        val ref = repository.resolve(repository.branch)
        if (ref != null) {
            return StreamSupport.stream(getGit().log().add(ref).call().spliterator(), false)
                .flatMap { rev: RevCommit -> tags.getOrDefault(rev.id, emptyList()).stream() }
                .map { tagRef: Ref -> tagRef.name.replace(Constants.R_TAGS, "") }
                .collect(Collectors.toList())
        }
        return emptyList()
    }

    fun getCommits(): List<String> {
        return try {
            val branchRef = repository.resolve(repository.branch)
            StreamSupport.stream(getGit().log().add(branchRef).call().spliterator(), false)
                .map { obj: RevCommit -> obj.fullMessage }
                .collect(Collectors.toList())
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getUnReleasedCommits(start: String): List<String> {
        return getUnReleasedCommits(start, Constants.HEAD)
    }

    fun getUnReleasedCommits(start: String, end: String): List<String> {
        return try {
            val startId = repository.resolve(start)!!
            val endId = repository.resolve(end)!!
            val walk = getRevWalk()

            val startCommit = walk.parseCommit(startId)
            val endCommit = walk.parseCommit(endId)

            StreamSupport.stream(
                getGit().log().addRange(startCommit, endCommit).call().spliterator(), false
            )
                .map { commit: RevCommit ->
                    val shortHash = try {
                        getShortHash(commit.id)
                    } catch (e: IOException) {
                        throw RuntimeException(e)
                    }
                    val message = commit.shortMessage
                    "Commit($shortHash, $message)"
                }
                .collect(Collectors.toList())
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getCommitBetween(start: String): List<String> {
        return getCommitBetween(start, Constants.HEAD)
    }

    fun getCommitBetween(start: String, end: String): List<String> {
        return try {
            val startId = repository.resolve(start)!!
            val endId = repository.resolve(end)!!
            val walk = getRevWalk()

            val startCommit = walk.parseCommit(startId)
            val endCommit = walk.parseCommit(endId)

            StreamSupport.stream(
                getGit().log().addRange(startCommit, endCommit).call().spliterator(), false
            )
                .map { obj: RevCommit -> obj.fullMessage }
                .collect(Collectors.toList())
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun hasUncommittedChanges(): Boolean {
        return try {
            getGit().status().call().hasUncommittedChanges()
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        fun apply(workingDir: File): JGitAdapter = JGitAdapter(workingDir)

        fun initRepository(workingDir: File): Repository = initRepository(workingDir, false)

        fun initRepository(workingDir: File, initialize: Boolean): Repository {
            return try {
                if (initialize) {
                    Git.init().setDirectory(workingDir).call()
                }

                val builder = FileRepositoryBuilder()
                    .setWorkTree(workingDir)
                    .findGitDir(workingDir)

                val gitDir = builder.gitDir
                if (gitDir == null || !gitDir.exists()) {
                    throw RuntimeException(
                        "Unable to find Git repository in: " + workingDir.absolutePath
                    )
                }

                if (gitDir.parentFile.absolutePath != workingDir.absolutePath) {
                    builder.workTree = gitDir.parentFile
                }

                builder.build()
            } catch (e: Exception) {
                throw RuntimeException("Failed to initialize repository", e)
            }
        }

        private fun getNonNullObjectId(ref: Ref): ObjectId {
            return Optional.ofNullable(ref.peeledObjectId).orElseGet { ref.objectId }
        }
    }
}
