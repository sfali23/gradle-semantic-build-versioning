package com.alphasystem.gradle.semver.release.internal;

import com.alphasystem.gradle.semver.release.VersionComponent;
import gradlesemverrelease.PreReleaseConfig;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Represents a version with components for major, minor, patch, optional hotfix, optional pre-release,
 * optional snapshot, and a required pre-release configuration.
 * <p>
 * Instances of this class are immutable and follow semantic versioning principles.
 * <p>
 * The class enforces validation of the version components during instantiation to ensure correctness.
 */
public record Version(Integer major, Integer minor, Integer patch, Optional<Integer> hotfix, Optional<PreReleaseVersion> preRelease,
                      Optional<Snapshot> snapshot, PreReleaseConfig preReleaseConfig) {

    public Version {
        if (major == null) {
            throw new IllegalArgumentException("Major version cannot be null");
        }
        if (minor == null) {
            throw new IllegalArgumentException("Minor version cannot be null");
        }
        if (patch == null) {
            throw new IllegalArgumentException("Patch version cannot be null");
        }
        if (preReleaseConfig == null) {
            throw new IllegalArgumentException("PreReleaseConfig cannot be null");
        }
    }

    public boolean isHotfix() {
        return hotfix.isPresent();
    }

    public boolean isPreRelease() {
        return preRelease.isPresent();
    }

    public Version bumpVersion(final Snapshot snapshot, List<VersionComponent> componentsToBump) {
        return componentsToBump.stream().reduce(this, bumpVersion(snapshot), (version, finalVersion) -> finalVersion);
    }

    public String toStringValue() {
        return String.format("%d.%d.%d%s%s", major, minor, patch, hotfix.map(h -> "." + h).orElse(""),
                preRelease.map(PreReleaseVersion::toStringValue).orElse(""));
    }

    private Version bumpMajor() {
        return new Version(major + 1, 0, 0, Optional.empty(), Optional.empty(), snapshot, preReleaseConfig);
    }

    private Version bumpMinor() {
        return new Version(major, minor + 1, 0, hotfix, preRelease, snapshot, preReleaseConfig);
    }

    private Version bumpPatch() {
        return new Version(major, minor, patch + 1, hotfix, preRelease, snapshot, preReleaseConfig);
    }

    private Version bumpHotfix() {
        if (preRelease.isPresent()) {
            throw new IllegalArgumentException("Current version is a a pre-release.");
        }
        return new Version(major, minor, patch, hotfix.map(h -> h + 1).or(() -> Optional.of(1)), preRelease, snapshot, preReleaseConfig);
    }

    private Version bumpPreRelease() {
        if (preRelease.isEmpty()) {
            throw new IllegalArgumentException("Cannot bump pre-release because the latest version is not a pre-release version."
                    + " To create a new pre-release version, use newPreRelease instead");
        }
        return new Version(major, minor, patch, hotfix, preRelease.map(PreReleaseVersion::bumpVersion), snapshot, preReleaseConfig);
    }

    private Version newPreRelease() {
        if (preRelease.isPresent()) {
            throw new IllegalArgumentException("Current version is already pre-release");
        }
        return new Version(major, minor, patch, hotfix, Optional.of(Utils.toInitialPreReleaseVersion(preReleaseConfig)), snapshot,
                preReleaseConfig);
    }

    private Version promoteToRelease() {
        return new Version(major, minor, patch, hotfix, Optional.empty(), snapshot, preReleaseConfig);
    }

    private Version bumpSnapshot(Snapshot snapshot) {
        return new Version(major, minor, patch, hotfix, preRelease, Optional.ofNullable(snapshot), preReleaseConfig);
    }

    private static @NonNull BiFunction<Version, VersionComponent, Version> bumpVersion(final Snapshot snapshot) {
        return (version, c) -> switch (c) {
            case NONE -> version;
            case MAJOR -> version.bumpMajor();
            case MINOR -> version.bumpMinor();
            case PATCH -> version.bumpPatch();
            case HOT_FIX -> version.bumpHotfix();
            case NEW_PRE_RELEASE -> version.newPreRelease();
            case PRE_RELEASE -> version.bumpPreRelease();
            case PROMOTE_TO_RELEASE -> version.promoteToRelease();
            case SNAPSHOT -> version.bumpSnapshot(snapshot);
        };
    }
}
