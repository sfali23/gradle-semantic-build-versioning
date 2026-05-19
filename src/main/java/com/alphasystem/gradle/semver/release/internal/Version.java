package com.alphasystem.gradle.semver.release.internal;

import com.alphasystem.gradle.semver.release.VersionComponent;
import gradlesemverrelease.PreReleaseConfig;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final Pattern VERSION_REGEX = Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?(?:\\.(0|[1-9]\\d*))?$");

    public static final Comparator<Version> VERSION_COMPARATOR = new VersionComparator();

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

    /**
     * Applies transformations to a version string, parsing it into components of a {@link Version} object.
     *
     * The method processes a version string, identifying and extracting major, minor, patch, hotfix,
     * pre-release, and snapshot components based on the provided snapshot suffix and pre-release configuration.
     * If the input version string does not adhere to the expected format, an {@link IllegalArgumentException} is thrown.
     *
     * @param version the input version string to be processed, which must adhere to the expected version format.
     * @param snapshotSuffix the suffix used to identify snapshot versions (e.g., "SNAPSHOT").
     * @param preReleaseConfig the configuration for handling pre-release versions,
     *         including prefix, separator, and versioning rules.
     *
     * @return a constructed {@link Version} object containing the parsed version components.
     *
     * @throws IllegalArgumentException if the provided version format is invalid or does not match the expected pattern.
     */
    public static Version create(final String version, String snapshotSuffix, PreReleaseConfig preReleaseConfig) {
        var matcher = VERSION_REGEX.matcher(version);

        // Check if the version string matches the expected pattern
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid version format: " + version);
        }

        // Group 0 will be the entire version string, Groups 1 - 3 will be major, minor, and patch versions, so there must be
        // at least four groups. There will be a total of 6 groups.

        if (matcher.groupCount() < 4) {
            throw new IllegalArgumentException("Invalid version format: " + version);
        }

        // pre-release and snapshot
        final var maybePreReleaseOrSnapshot = getMatchedGroup(matcher, 4);
        final var metaInfo = getMatchedGroup(matcher, 5);

        final var maybePreReleaseVersion = maybePreReleaseOrSnapshot.map(value -> value.replaceAll("-" + snapshotSuffix, ""))
                .filter(value -> !value.isEmpty())
                .map(value -> Utils.toPreReleaseVersion(value, preReleaseConfig));

        final var maybeSnapshot = maybePreReleaseOrSnapshot.map(
                        value -> value.replaceAll(preReleaseConfig.preReleasePartPattern().pattern(), ""))
                .filter(value -> value.contains(snapshotSuffix))
                .map(value -> new Snapshot(snapshotSuffix, metaInfo.orElse(null)));

        final var hotfix = getMatchedGroup(matcher, 6).map(Integer::parseInt);

        return new Version(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)),
                hotfix,
                maybePreReleaseVersion, maybeSnapshot, preReleaseConfig);
    }

    private static Optional<String> getMatchedGroup(Matcher matcher, int group) {
        try {
            return Optional.ofNullable(matcher.group(group));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static class VersionComparator implements Comparator<Version> {

        @Override
        public int compare(final Version v1, final Version v2) {
            final var list = new ArrayList<Tuple>();
            list.add(new Tuple(v2.major(), v1.major()));
            list.add(new Tuple(v2.minor(), v1.minor()));
            list.add(new Tuple(v2.patch(), v1.patch()));
            list.add(new Tuple(v2.hotfix().orElse(0), v1.hotfix().orElse(0)));
            list.add(new Tuple(v2.preRelease.map(PreReleaseVersion::version).orElse(Integer.MAX_VALUE),
                    v1.preRelease.map(PreReleaseVersion::version).orElse(Integer.MAX_VALUE)));
            return compareTo(list);
        }

        private int compareTo(List<Tuple> tuples) {
            if (tuples.isEmpty()) {
                return 0;
            }
            final var head = tuples.getFirst();
            final var comparison = head.first.compareTo(head.second);
            if (comparison == 0) {
                return compareTo(tuples.subList(1, tuples.size()));
            } else {
                return comparison;
            }
        }
    }

    private record Tuple(Integer first, Integer second) {
    }
}
