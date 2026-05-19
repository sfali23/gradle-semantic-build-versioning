package com.alphasystem.gradle.semver.release.internal;

import org.eclipse.jgit.util.StringUtils;

/**
 * Represents a snapshot version suffix that can optionally include metadata.
 *
 * This record is immutable and ensures that the snapshot suffix is non-empty.
 * The snapshot version may also include optional metadata, which is formatted
 * as part of the suffix string during string representation.
 *
 * Instances of this class are commonly used to denote a snapshot version in
 * versioning systems that follow semantic versioning.
 *
 * Constraints:
 * - The suffix must not be null or empty.
 *
 * Usage Scenarios:
 * - Constructing a snapshot version (e.g., "-SNAPSHOT" or "-SNAPSHOT+meta").
 * - Building snapshot components as part of a larger versioning scheme.
 *
 * Key Behaviors:
 * - Validation of the snapshot suffix during instantiation.
 * - A default constructor that initializes the suffix to "SNAPSHOT" with null metadata.
 * - A formatted string representation combining the suffix and optional metadata.
 */
public record Snapshot(String suffix, String meta) {

    public Snapshot {
        if (StringUtils.isEmptyOrNull(suffix)) {
            throw new IllegalArgumentException("Snapshot suffix cannot be empty");
        }
    }

    public Snapshot() {
        this("SNAPSHOT", null);
    }

    public String toStringValue() {
        var metaValue = "";
        if (!StringUtils.isEmptyOrNull(meta)) {
            metaValue = "+" + meta;
        }
        return String.format("-%s%s", suffix, metaValue);
    }
}
