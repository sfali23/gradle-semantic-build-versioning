package com.alphasystem.gradle.semver.release;

import java.util.Map;

/**
 * Represents the various components of a software version in a versioning system.
 *
 * Each component is associated with an integer index, which can be used to uniquely
 * identify and retrieve the respective version component. This enum is typically used
 * in semantic versioning processes for determining which part of the version should be
 * adjusted during version updates.
 *
 * The available version components are:
 * - NONE: Represents an undefined or no version component.
 * - MAJOR: Represents the major version component.
 * - MINOR: Represents the minor version component.
 * - PATCH: Represents the patch version component.
 * - HOT_FIX: Represents a hotfix version component.
 * - NEW_PRE_RELEASE: Represents the creation of a new pre-release.
 * - PRE_RELEASE: Represents a pre-release version component.
 * - PROMOTE_TO_RELEASE: Represents promoting a pre-release to a release.
 * - SNAPSHOT: Represents a snapshot version.
 *
 * The index can be used to map to specific components via a predefined mapping.
 */
public enum VersionComponent {
    NONE(0),
    MAJOR(2),
    MINOR(4),
    PATCH(8),
    HOT_FIX(16),
    NEW_PRE_RELEASE(32),
    PRE_RELEASE(64),
    PROMOTE_TO_RELEASE(128),
    SNAPSHOT(256);

    private static final Map<Integer, VersionComponent> indexToComponent = Map.of(
            NONE.index, VersionComponent.NONE,
            MAJOR.index, VersionComponent.MAJOR,
            MINOR.index, VersionComponent.MINOR,
            PATCH.index, VersionComponent.PATCH,
            HOT_FIX.index, VersionComponent.HOT_FIX,
            NEW_PRE_RELEASE.index, VersionComponent.NEW_PRE_RELEASE,
            PRE_RELEASE.index, VersionComponent.PRE_RELEASE,
            PROMOTE_TO_RELEASE.index, VersionComponent.PROMOTE_TO_RELEASE,
            SNAPSHOT.index, VersionComponent.SNAPSHOT);

    private final int index;

    VersionComponent(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public static VersionComponent fromIndex(int index) {
        return indexToComponent.get(index);
    }
}
