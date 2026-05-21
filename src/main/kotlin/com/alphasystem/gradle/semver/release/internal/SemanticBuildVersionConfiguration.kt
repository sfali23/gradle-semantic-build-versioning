package com.alphasystem.gradle.semver.release.internal

import com.alphasystem.gradle.semver.release.*
import semverrelease.AutoBump
import semverrelease.PreReleaseConfig
import semverrelease.SnapshotConfig

@JvmRecord
data class SemanticBuildVersionConfiguration(
    val startingVersion: String = DefaultStartingVersion,
    val tagPrefix: String = DefaultTagPrefix,
    val forceBump: Boolean = DefaultBooleanValue,
    val promoteToRelease: Boolean = DefaultBooleanValue,
    val snapshot: Boolean = DefaultBooleanValue,
    val newPreRelease: Boolean = DefaultBooleanValue,
    val autoBump: AutoBump = AutoBump(),
    val defaultBumpLevel: VersionComponent = DefaultBumpLevel,
    val componentToBump: VersionComponent = DefaultComponentToBump,
    val snapshotConfig: SnapshotConfig = SnapshotConfig(),
    val preReleaseConfig: PreReleaseConfig = PreReleaseConfig(),
    val hotfixBranchPattern: Regex = DefaultHotfixBranchPattern,
    val extraReleaseBranches: List<String> = listOf()
) {

    private fun releaseBranches(): Set<String> =  DefaultReleaseBranches.plus(extraReleaseBranches).toSet()

    fun isAutoBumpEnabled(): Boolean = autoBump.isEnabled()

    fun isReleaseBranch(branchName: String): Boolean = releaseBranches().contains(branchName)
}
