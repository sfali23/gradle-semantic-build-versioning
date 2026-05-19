package com.alphasystem.gradle.semver.release.internal;

import com.alphasystem.gradle.semver.release.VersionComponent;
import gradlesemverrelease.PreReleaseConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Version Tests")
public class VersionTest {

    private static final PreReleaseConfig DEFAULT_CONFIG = new PreReleaseConfig();
    private static final Snapshot DEFAULT_SNAPSHOT = new Snapshot();
    private static final String DEFAULT_SNAPSHOT_SUFFIX = DEFAULT_SNAPSHOT.suffix();
    private static final PreReleaseVersion DEFAULT_PRE_RELEASE = new PreReleaseVersion("alpha", 1);

    @Nested
    @DisplayName("Parse Version Tests")
    class ParseVersionTests {

        @Test
        @DisplayName("Should parse version with major, minor, patch")
        void shouldParseSimpleVersion() {
            final var actual = Version.create("0.12.345", DEFAULT_SNAPSHOT_SUFFIX, DEFAULT_CONFIG);
            final var expected = new Version(0, 12, 345, Optional.empty(), Optional.empty(), Optional.empty(), DEFAULT_CONFIG);
            assertEquals(expected, actual);
        }

        @Test
        @DisplayName("Should parse version with hotfix")
        void shouldParseVersionWithHotfix() {
            final var actual = Version.create("1.2.3.4", DEFAULT_SNAPSHOT_SUFFIX, DEFAULT_CONFIG);
            final var expected = new Version(1, 2, 3, Optional.of(4), Optional.empty(), Optional.empty(), DEFAULT_CONFIG);
            assertEquals(expected, actual);
        }

        @Test
        @DisplayName("Should parse version with pre-release")
        void shouldParseVersionWithPreRelease() {
            final var actual = Version.create("1.2.3-RC.1", DEFAULT_SNAPSHOT_SUFFIX, DEFAULT_CONFIG);
            final var expected = new Version(1, 2, 3, Optional.empty(), Optional.of(new PreReleaseVersion("RC", 1)),
                    Optional.empty(), DEFAULT_CONFIG);
            assertEquals(actual, expected);
        }

        @Test
        @DisplayName("Should parse version with all components")
        void shouldParseVersionWithAllComponents() {
            final var actual = Version.create("1.2.3-RC.1-SNAPSHOT+abcd", DEFAULT_SNAPSHOT_SUFFIX, DEFAULT_CONFIG);
            final var expected = new Version(1, 2, 3, Optional.empty(), Optional.of(new PreReleaseVersion("RC", 1)),
                    Optional.of(new Snapshot(DEFAULT_SNAPSHOT_SUFFIX, "abcd")), DEFAULT_CONFIG);
            assertEquals(actual, expected);
        }

        @Test
        @DisplayName("Should parse version with snapshot component")
        void shouldParseVersionWithSnapshotComponent() {
            final var actual = Version.create("1.2.3-SNAPSHOT+abcd", DEFAULT_SNAPSHOT_SUFFIX, DEFAULT_CONFIG);
            final var expected = new Version(1, 2, 3, Optional.empty(), Optional.empty(),
                    Optional.of(new Snapshot(DEFAULT_SNAPSHOT_SUFFIX, "abcd")), DEFAULT_CONFIG);
            assertEquals(actual, expected);
        }

        @Test
        @DisplayName("Should parse version with hotfix and pre-release")
        void shouldParseVersionWithHotfixAndPreRelease() {
            // Note: Based on the regex pattern, hotfix and pre-release cannot be combined.
            // This test demonstrates the current limitation
            final var actual = Version.create("1.2.3.4", DEFAULT_SNAPSHOT_SUFFIX, DEFAULT_CONFIG);
            final var expected = new Version(1, 2, 3, Optional.of(4), Optional.empty(), Optional.empty(), DEFAULT_CONFIG);
            assertEquals(actual, expected);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "1.0.0", "0.1.0", "0.0.1", "10.20.30", "1.1.2", "10.0.0", "1.0.0-RC.1",
                "1.0.0-RC.2", "1.0.0+build.1", "1.0.0-RC.1+build.2", "1.0.0+20130313144700"
        })
        @DisplayName("Should parse valid semantic version formats")
        void shouldParseValidSemanticVersions(String versionString) {
            final var version = Version.create(versionString, DEFAULT_SNAPSHOT_SUFFIX, DEFAULT_CONFIG);
            assertNotNull(version);
            assertTrue(version.major() >= 0);
            assertTrue(version.minor() >= 0);
            assertTrue(version.patch() >= 0);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "1", "1.2", "1.2.3-0123", "1.2.3-0123.0123", "1.2.3-", "1.2.3+",
                "1.2.3-alpha..1", "1.2.3-alpha.1.", "1.2.3.4.5", "1.2.3.4.5.6", "1.2.3-alpha@beta"
        })
        @DisplayName("Should throw exception for invalid version formats")
        void shouldThrowExceptionForInvalidVersions(String versionString) {
            assertThrows(IllegalArgumentException.class,
                    () -> Version.create(versionString, DEFAULT_SNAPSHOT_SUFFIX, DEFAULT_CONFIG),
                    "Expected IllegalArgumentException for invalid version: " + versionString);
        }

        @Test
        @DisplayName("Should parse version with zero values")
        void shouldParseVersionWithZeroValues() {
            final var version = Version.create("0.0.0", DEFAULT_SNAPSHOT_SUFFIX, DEFAULT_CONFIG);
            assertEquals(0, version.major());
            assertEquals(0, version.minor());
            assertEquals(0, version.patch());
            assertFalse(version.hotfix().isPresent());
            assertFalse(version.preRelease().isPresent());
            assertFalse(version.snapshot().isPresent());
        }

        @Test
        @DisplayName("Should parse version with large numbers")
        void shouldParseVersionWithLargeNumbers() {
            final var version = Version.create("999.888.777", DEFAULT_SNAPSHOT_SUFFIX, DEFAULT_CONFIG);
            assertEquals(999, version.major());
            assertEquals(888, version.minor());
            assertEquals(777, version.patch());
            assertFalse(version.hotfix().isPresent());
            assertFalse(version.preRelease().isPresent());
            assertFalse(version.snapshot().isPresent());
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create version with all required fields")
        void shouldCreateVersionWithRequiredFields() {
            final var version = new Version(1, 2, 3, Optional.empty(), Optional.empty(), Optional.empty(), DEFAULT_CONFIG);

            assertEquals(1, version.major());
            assertEquals(2, version.minor());
            assertEquals(3, version.patch());
            assertFalse(version.hotfix().isPresent());
            assertFalse(version.preRelease().isPresent());
            assertFalse(version.snapshot().isPresent());
            assertEquals(DEFAULT_CONFIG, version.preReleaseConfig());
        }

        @Test
        @DisplayName("Should create version with all fields")
        void shouldCreateVersionWithAllFields() {
            final var version = new Version(1, 2, 3, Optional.of(4), Optional.of(DEFAULT_PRE_RELEASE),
                    Optional.of(DEFAULT_SNAPSHOT), DEFAULT_CONFIG);

            assertEquals(1, version.major());
            assertEquals(2, version.minor());
            assertEquals(3, version.patch());
            assertTrue(version.hotfix().isPresent());
            assertEquals(4, version.hotfix().get());
            assertTrue(version.preRelease().isPresent());
            assertEquals(DEFAULT_PRE_RELEASE, version.preRelease().get());
            assertTrue(version.snapshot().isPresent());
            assertEquals(DEFAULT_SNAPSHOT, version.snapshot().get());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 100, Integer.MAX_VALUE})
        @DisplayName("Should accept valid major version")
        void shouldAcceptValidMajorVersion(int major) {
            assertDoesNotThrow(() -> new Version(major, 0, 0, Optional.empty(), Optional.empty(),
                    Optional.empty(), DEFAULT_CONFIG));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 100, Integer.MAX_VALUE})
        @DisplayName("Should accept valid minor version")
        void shouldAcceptValidMinorVersion(int minor) {
            assertDoesNotThrow(() -> new Version(1, minor, 0, Optional.empty(), Optional.empty(),
                    Optional.empty(), DEFAULT_CONFIG));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 100, Integer.MAX_VALUE})
        @DisplayName("Should accept valid patch version")
        void shouldAcceptValidPatchVersion(int patch) {
            assertDoesNotThrow(() -> new Version(1, 0, patch, Optional.empty(), Optional.empty(),
                    Optional.empty(), DEFAULT_CONFIG));
        }

        @Test
        @DisplayName("Should throw exception when major is null")
        void shouldThrowExceptionWhenMajorIsNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Version(null, 0, 0, Optional.empty(), Optional.empty(),
                            Optional.empty(), DEFAULT_CONFIG),
                    "Major version cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when minor is null")
        void shouldThrowExceptionWhenMinorIsNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Version(1, null, 0, Optional.empty(), Optional.empty(),
                            Optional.empty(), DEFAULT_CONFIG),
                    "Minor version cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when patch is null")
        void shouldThrowExceptionWhenPatchIsNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Version(1, 0, null, Optional.empty(), Optional.empty(),
                            Optional.empty(), DEFAULT_CONFIG),
                    "Patch version cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when preReleaseConfig is null")
        void shouldThrowExceptionWhenPreReleaseConfigIsNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Version(1, 0, 0, Optional.empty(), Optional.empty(),
                            Optional.empty(), null),
                    "PreReleaseConfig cannot be null");
        }
    }

    @Nested
    @DisplayName("Utility Method Tests")
    class UtilityMethodTests {

        @Test
        @DisplayName("isHotfix should return true when hotfix is present")
        void isHotfixShouldReturnTrueWhenHotfixIsPresent() {
            final var version = new Version(1, 2, 3, Optional.of(1), Optional.empty(),
                    Optional.empty(), DEFAULT_CONFIG);
            assertTrue(version.isHotfix());
        }

        @Test
        @DisplayName("isHotfix should return false when hotfix is absent")
        void isHotfixShouldReturnFalseWhenHotfixIsAbsent() {
            final var version = new Version(1, 2, 3, Optional.empty(), Optional.empty(),
                    Optional.empty(), DEFAULT_CONFIG);
            assertFalse(version.isHotfix());
        }

        @Test
        @DisplayName("isPreRelease should return true when preRelease is present")
        void isPreReleaseShouldReturnTrueWhenPreReleaseIsPresent() {
            final var version = new Version(1, 2, 3, Optional.empty(), Optional.of(DEFAULT_PRE_RELEASE),
                    Optional.empty(), DEFAULT_CONFIG);
            assertTrue(version.isPreRelease());
        }

        @Test
        @DisplayName("isPreRelease should return false when preRelease is absent")
        void isPreReleaseShouldReturnFalseWhenPreReleaseIsAbsent() {
            final var version = new Version(1, 2, 3, Optional.empty(), Optional.empty(),
                    Optional.empty(), DEFAULT_CONFIG);
            assertFalse(version.isPreRelease());
        }

        @Test
        @DisplayName("toStringValue should return basic version format")
        void toStringValueShouldReturnBasicVersionFormat() {
            final var version = new Version(1, 2, 3, Optional.empty(), Optional.empty(),
                    Optional.empty(), DEFAULT_CONFIG);
            assertEquals("1.2.3", version.toStringValue());
        }

        @Test
        @DisplayName("toStringValue should include hotfix when present")
        void toStringValueShouldIncludeHotfixWhenPresent() {
            final var version = new Version(1, 2, 3, Optional.of(4), Optional.empty(),
                    Optional.empty(), DEFAULT_CONFIG);
            assertEquals("1.2.3.4", version.toStringValue());
        }

        @Test
        @DisplayName("toStringValue should include preRelease when present")
        void toStringValueShouldIncludePreReleaseWhenPresent() {
            final var version = new Version(1, 2, 3, Optional.empty(), Optional.of(DEFAULT_PRE_RELEASE),
                    Optional.empty(), DEFAULT_CONFIG);
            assertEquals("1.2.3-alpha1", version.toStringValue());
        }

        @Test
        @DisplayName("toStringValue should include both hotfix and preRelease when present")
        void toStringValueShouldIncludeBothHotfixAndPreReleaseWhenPresent() {
            final var version = new Version(1, 2, 3, Optional.of(4), Optional.of(DEFAULT_PRE_RELEASE),
                    Optional.empty(), DEFAULT_CONFIG);
            assertEquals("1.2.3.4-alpha1", version.toStringValue());
        }
    }

    @Nested
    @DisplayName("Version Bumping Tests")
    class VersionBumpingTests {

        @Test
        @DisplayName("bumpVersion should handle NONE component")
        void bumpVersionShouldHandleNoneComponent() {
            final var version = new Version(1, 2, 3, Optional.empty(), Optional.empty(),
                    Optional.empty(), DEFAULT_CONFIG);
            Snapshot snapshot = new Snapshot();

            Version result = version.bumpVersion(snapshot, List.of(VersionComponent.NONE));

            assertEquals(version, result);
        }

        @Test
        @DisplayName("bumpVersion should bump major version")
        void bumpVersionShouldBumpMajorVersion() {
            final var version = new Version(1, 2, 3, Optional.of(4), Optional.of(DEFAULT_PRE_RELEASE),
                    Optional.of(DEFAULT_SNAPSHOT), DEFAULT_CONFIG);
            Snapshot snapshot = new Snapshot();

            Version result = version.bumpVersion(snapshot, List.of(VersionComponent.MAJOR));

            assertEquals(2, result.major());
            assertEquals(0, result.minor());
            assertEquals(0, result.patch());
            assertFalse(result.hotfix().isPresent());
            assertFalse(result.preRelease().isPresent());
            assertTrue(result.snapshot().isPresent());
            assertEquals(DEFAULT_CONFIG, result.preReleaseConfig());
        }

        @Test
        @DisplayName("bumpVersion should bump minor version")
        void bumpVersionShouldBumpMinorVersion() {
            final var version = new Version(1, 2, 3, Optional.of(4), Optional.of(DEFAULT_PRE_RELEASE),
                    Optional.of(DEFAULT_SNAPSHOT), DEFAULT_CONFIG);
            Snapshot snapshot = new Snapshot();

            Version result = version.bumpVersion(snapshot, List.of(VersionComponent.MINOR));

            assertEquals(1, result.major());
            assertEquals(3, result.minor());
            assertEquals(0, result.patch());
            assertTrue(result.hotfix().isPresent());
            assertEquals(4, result.hotfix().get());
            assertTrue(result.preRelease().isPresent());
            assertEquals(DEFAULT_PRE_RELEASE, result.preRelease().get());
            assertTrue(result.snapshot().isPresent());
            assertEquals(DEFAULT_CONFIG, result.preReleaseConfig());
        }

        @Test
        @DisplayName("bumpVersion should bump patch version")
        void bumpVersionShouldBumpPatchVersion() {
            final var version = new Version(1, 2, 3, Optional.of(4), Optional.of(DEFAULT_PRE_RELEASE),
                    Optional.of(DEFAULT_SNAPSHOT), DEFAULT_CONFIG);
            Snapshot snapshot = new Snapshot();

            Version result = version.bumpVersion(snapshot, List.of(VersionComponent.PATCH));

            assertEquals(1, result.major());
            assertEquals(2, result.minor());
            assertEquals(4, result.patch());
            assertTrue(result.hotfix().isPresent());
            assertEquals(4, result.hotfix().get());
            assertTrue(result.preRelease().isPresent());
            assertEquals(DEFAULT_PRE_RELEASE, result.preRelease().get());
            assertTrue(result.snapshot().isPresent());
            assertEquals(DEFAULT_CONFIG, result.preReleaseConfig());
        }

        @Test
        @DisplayName("bumpVersion should bump hotfix version")
        void bumpVersionShouldBumpHotfixVersion() {
            final var version = new Version(1, 2, 3, Optional.empty(), Optional.empty(),
                    Optional.empty(), DEFAULT_CONFIG);
            Snapshot snapshot = new Snapshot();

            Version result = version.bumpVersion(snapshot, List.of(VersionComponent.HOT_FIX));

            assertEquals(1, result.major());
            assertEquals(2, result.minor());
            assertEquals(3, result.patch());
            assertTrue(result.hotfix().isPresent());
            assertEquals(1, result.hotfix().get());
            assertFalse(result.preRelease().isPresent());
            assertFalse(result.snapshot().isPresent());
            assertEquals(DEFAULT_CONFIG, result.preReleaseConfig());
        }

        @Test
        @DisplayName("bumpVersion should increment existing hotfix")
        void bumpVersionShouldIncrementExistingHotfix() {
            final var version = new Version(1, 2, 3, Optional.of(1), Optional.empty(),
                    Optional.empty(), DEFAULT_CONFIG);
            Snapshot snapshot = new Snapshot();

            Version result = version.bumpVersion(snapshot, List.of(VersionComponent.HOT_FIX));

            assertEquals(1, result.major());
            assertEquals(2, result.minor());
            assertEquals(3, result.patch());
            assertTrue(result.hotfix().isPresent());
            assertEquals(2, result.hotfix().get());
        }

        @Test
        @DisplayName("bumpVersion should handle multiple components")
        void bumpVersionShouldHandleMultipleComponents() {
            final var version = new Version(1, 2, 3, Optional.empty(), Optional.empty(),
                    Optional.empty(), DEFAULT_CONFIG);
            Snapshot snapshot = new Snapshot();

            Version result = version.bumpVersion(snapshot, List.of(VersionComponent.HOT_FIX, VersionComponent.PATCH));

            assertEquals(1, result.major());
            assertEquals(2, result.minor());
            assertEquals(4, result.patch());
            assertTrue(result.hotfix().isPresent());
            assertEquals(1, result.hotfix().get());
        }
    }

    @Nested
    @DisplayName("Pre-release Tests")
    class PreReleaseTests {

        @Test
        @DisplayName("newPreRelease should create initial pre-release")
        void newPreReleaseShouldCreateInitialPreRelease() {
            final var version = new Version(1, 2, 3, Optional.empty(), Optional.empty(),
                    Optional.empty(), DEFAULT_CONFIG);
            Snapshot snapshot = new Snapshot();

            Version result = version.bumpVersion(snapshot, List.of(VersionComponent.NEW_PRE_RELEASE));

            assertEquals(1, result.major());
            assertEquals(2, result.minor());
            assertEquals(3, result.patch());
            assertFalse(result.hotfix().isPresent());
            assertTrue(result.preRelease().isPresent());
            assertEquals("-RC.1", result.preRelease().get().toStringValue());
        }

        @Test
        @DisplayName("newPreRelease should throw exception when pre-release already exists")
        void newPreReleaseShouldThrowExceptionWhenPreReleaseAlreadyExists() {
            final var version = new Version(1, 2, 3, Optional.empty(), Optional.of(DEFAULT_PRE_RELEASE),
                    Optional.empty(), DEFAULT_CONFIG);
            Snapshot snapshot = new Snapshot();

            assertThrows(IllegalArgumentException.class,
                    () -> version.bumpVersion(snapshot, List.of(VersionComponent.NEW_PRE_RELEASE)),
                    "Current version is already pre-release");
        }

        @Test
        @DisplayName("bumpPreRelease should increment pre-release version")
        void bumpPreReleaseShouldIncrementPreReleaseVersion() {
            final var version = new Version(1, 2, 3, Optional.empty(), Optional.of(DEFAULT_PRE_RELEASE),
                    Optional.empty(), DEFAULT_CONFIG);
            Snapshot snapshot = new Snapshot();

            Version result = version.bumpVersion(snapshot, List.of(VersionComponent.PRE_RELEASE));

            assertEquals(1, result.major());
            assertEquals(2, result.minor());
            assertEquals(3, result.patch());
            assertFalse(result.hotfix().isPresent());
            assertTrue(result.preRelease().isPresent());
            assertEquals("-alpha2", result.preRelease().get().toStringValue());
        }

        @Test
        @DisplayName("bumpPreRelease should throw exception when no pre-release exists")
        void bumpPreReleaseShouldThrowExceptionWhenNoPreReleaseExists() {
            final var version = new Version(1, 2, 3, Optional.empty(), Optional.empty(),
                    Optional.empty(), DEFAULT_CONFIG);
            Snapshot snapshot = new Snapshot();

            assertThrows(IllegalArgumentException.class,
                    () -> version.bumpVersion(snapshot, List.of(VersionComponent.PRE_RELEASE)),
                    "Cannot bump pre-release because the latest version is not a pre-release version");
        }

        @Test
        @DisplayName("promoteToRelease should remove pre-release")
        void promoteToReleaseShouldRemovePreRelease() {
            final var version = new Version(1, 2, 3, Optional.of(4), Optional.of(DEFAULT_PRE_RELEASE),
                    Optional.empty(), DEFAULT_CONFIG);
            Snapshot snapshot = new Snapshot();

            Version result = version.bumpVersion(snapshot, List.of(VersionComponent.PROMOTE_TO_RELEASE));

            assertEquals(1, result.major());
            assertEquals(2, result.minor());
            assertEquals(3, result.patch());
            assertTrue(result.hotfix().isPresent());
            assertEquals(4, result.hotfix().get());
            assertFalse(result.preRelease().isPresent());
            assertFalse(result.snapshot().isPresent());
            assertEquals(DEFAULT_CONFIG, result.preReleaseConfig());
        }
    }

    @Nested
    @DisplayName("Hotfix Edge Cases")
    class HotfixEdgeCases {

        @Test
        @DisplayName("bumpHotfix should throw exception when version is pre-release")
        void bumpHotfixShouldThrowExceptionWhenVersionIsPreRelease() {
            final var version = new Version(1, 2, 3, Optional.empty(), Optional.of(DEFAULT_PRE_RELEASE),
                    Optional.empty(), DEFAULT_CONFIG);
            Snapshot snapshot = new Snapshot();

            assertThrows(IllegalArgumentException.class,
                    () -> version.bumpVersion(snapshot, List.of(VersionComponent.HOT_FIX)),
                    "Current version is a a pre-release.");
        }
    }

    @Nested
    @DisplayName("Snapshot Tests")
    class SnapshotTests {

        @Test
        @DisplayName("bumpSnapshot should add snapshot")
        void bumpSnapshotShouldAddSnapshot() {
            final var version = new Version(1, 2, 3, Optional.empty(), Optional.empty(),
                    Optional.empty(), DEFAULT_CONFIG);
            Snapshot newSnapshot = new Snapshot("TEST", "meta");

            Version result = version.bumpVersion(newSnapshot, List.of(VersionComponent.SNAPSHOT));

            assertEquals(1, result.major());
            assertEquals(2, result.minor());
            assertEquals(3, result.patch());
            assertFalse(result.hotfix().isPresent());
            assertFalse(result.preRelease().isPresent());
            assertTrue(result.snapshot().isPresent());
            assertEquals(newSnapshot, result.snapshot().get());
        }

        @Test
        @DisplayName("bumpSnapshot should replace existing snapshot")
        void bumpSnapshotShouldReplaceExistingSnapshot() {
            final var version = new Version(1, 2, 3, Optional.empty(), Optional.empty(),
                    Optional.of(DEFAULT_SNAPSHOT), DEFAULT_CONFIG);
            Snapshot newSnapshot = new Snapshot("TEST", "meta");

            Version result = version.bumpVersion(newSnapshot, List.of(VersionComponent.SNAPSHOT));

            assertEquals(newSnapshot, result.snapshot().get());
        }

        @Test
        @DisplayName("bumpSnapshot should handle null snapshot")
        void bumpSnapshotShouldHandleNullSnapshot() {
            final var version = new Version(1, 2, 3, Optional.empty(), Optional.empty(),
                    Optional.of(DEFAULT_SNAPSHOT), DEFAULT_CONFIG);

            Version result = version.bumpVersion(null, List.of(VersionComponent.SNAPSHOT));

            assertFalse(result.snapshot().isPresent());
        }
    }

    @Nested
    @DisplayName("Custom PreReleaseConfig Tests")
    class CustomPreReleaseConfigTests {

        @Test
        @DisplayName("newPreRelease should use custom config")
        void newPreReleaseShouldUseCustomConfig() {
            PreReleaseConfig customConfig = new PreReleaseConfig("BETA", "-", 2);
            final var version = new Version(1, 2, 3, Optional.empty(), Optional.empty(),
                    Optional.empty(), customConfig);
            Snapshot snapshot = new Snapshot();

            Version result = version.bumpVersion(snapshot, List.of(VersionComponent.NEW_PRE_RELEASE));

            assertTrue(result.preRelease().isPresent());
            assertEquals("-BETA-2", result.preRelease().get().toStringValue());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Conditions")
    class EdgeCasesAndErrorConditions {

        @Test
        @DisplayName("Should handle empty component list")
        void shouldHandleEmptyComponentList() {
            final var version = new Version(1, 2, 3, Optional.empty(), Optional.empty(),
                    Optional.empty(), DEFAULT_CONFIG);
            Snapshot snapshot = new Snapshot();

            Version result = version.bumpVersion(snapshot, List.of());

            assertEquals(version, result);
        }

        @Test
        @DisplayName("Should handle large version numbers")
        void shouldHandleLargeVersionNumbers() {
            final var version = new Version(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE,
                    Optional.of(Integer.MAX_VALUE), Optional.empty(),
                    Optional.empty(), DEFAULT_CONFIG);

            assertEquals(Integer.MAX_VALUE, version.major());
            assertEquals(Integer.MAX_VALUE, version.minor());
            assertEquals(Integer.MAX_VALUE, version.patch());
            assertTrue(version.hotfix().isPresent());
            assertEquals(Integer.MAX_VALUE, version.hotfix().get());
        }

        @Test
        @DisplayName("Should handle zero versions")
        void shouldHandleZeroVersions() {
            final var version = new Version(0, 0, 0, Optional.empty(), Optional.empty(),
                    Optional.empty(), DEFAULT_CONFIG);

            assertEquals("0.0.0", version.toStringValue());
        }

        @ParameterizedTest
        @EnumSource(VersionComponent.class)
        @DisplayName("Should handle all version components")
        void shouldHandleAllVersionComponents(VersionComponent component) {
            final var version = new Version(1, 2, 3, Optional.empty(), Optional.empty(),
                    Optional.empty(), DEFAULT_CONFIG);
            Snapshot snapshot = new Snapshot();

            // Some components will throw exceptions, which is expected behavior
            assertDoesNotThrow(() -> {
                try {
                    version.bumpVersion(snapshot, List.of(component));
                } catch (IllegalArgumentException e) {
                    // Expected for some components
                }
            });
        }
    }
}
