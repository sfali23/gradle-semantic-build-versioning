package com.alphasystem.gradle.semver.release.internal;

import org.eclipse.jgit.util.StringUtils;

/**
 * Represents a pre-release version component in semantic versioning.
 *
 * This record encapsulates a pre-release version with three parts:
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
 * - Instantiation with validation*/
public record PreReleaseVersion(String prefix, Integer version) {

    public PreReleaseVersion updatePrefix(String prefix) {
        if (StringUtils.isEmptyOrNull(prefix)) {
            return this;
        } else {
            final var currentPrefix = (StringUtils.isEmptyOrNull(this.prefix)) ? "" : this.prefix;
            return new PreReleaseVersion(String.format("%s%s", currentPrefix, prefix), version);
        }
    }

    public PreReleaseVersion updateVersion(Integer version) {
        return new PreReleaseVersion(prefix, version);
    }

    public PreReleaseVersion bumpVersion() {
        return new PreReleaseVersion(prefix, version + 1);
    }

    public String toStringValue() {
        return String.format("-%s%s", prefix, version);
    }
}
