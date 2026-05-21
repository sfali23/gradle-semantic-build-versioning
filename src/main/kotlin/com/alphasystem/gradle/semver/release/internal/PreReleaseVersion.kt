package com.alphasystem.gradle.semver.release.internal

import gradlesemverrelease.PreReleaseConfig
import org.eclipse.jgit.util.StringUtils

/**
 * Represents a pre-release version component in semantic versioning.
 *
 * This data class encapsulates a pre-release version with two parts:
 * - A mandatory prefix representing the type or identifier of the pre-release version.
 * - A mandatory version number, which must be an integer.
 *
 * Key Characteristics:
 * - The prefix must not be null or empty.
 * - The version number must not be null.
 * - The class is immutable and ensures validation of the prefix and version during instantiation.
 *
 * Usage Scenarios:
 * - Representing a pre-release component as part of a semantic version.
 * - Bumping the version number for iterative pre-release versions.
 * - Generating a string representation of the pre-release version.
 *
 * Key Behaviors:
 * - Instantiation with validation
 */
data class PreReleaseVersion(
    val prefix: String?,
    val version: Int,
    val separator: String?
) {
    fun updatePrefix(prefix: String?): PreReleaseVersion {
        return if (StringUtils.isEmptyOrNull(prefix)) {
            this
        } else {
            val currentPrefix = if (StringUtils.isEmptyOrNull(this.prefix)) "" else this.prefix
            PreReleaseVersion(String.format("%s%s", currentPrefix, prefix), version, separator)
        }
    }

    fun updateVersion(version: Int): PreReleaseVersion {
        return PreReleaseVersion(prefix, version, separator)
    }

    fun bumpVersion(): PreReleaseVersion {
        return PreReleaseVersion(prefix, version + 1, separator)
    }

    fun toStringValue(): String {
        return String.format("-%s%s%s", prefix, separator ?: "", version)
    }

    companion object {
        fun create(preReleaseConfig: PreReleaseConfig): PreReleaseVersion =
            PreReleaseVersion(preReleaseConfig.prefix, preReleaseConfig.startingVersion, preReleaseConfig.separator)
    }
}
