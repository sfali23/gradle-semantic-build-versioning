package gradlesemverrelease;

import org.eclipse.jgit.util.StringUtils;

import java.util.regex.Pattern;

/**
 * Configuration class representing pre-release settings for semantic versioning.
 *
 * This record encapsulates the configuration for generating and parsing
 * pre-release version components in semantic versioning.
 * It includes details such as a prefix, separator, and starting version for the pre-release versions.
 *
 * Key Characteristics:
 * - Immutable by design as a record.
 * - Validates the provided values during instantiation to ensure correctness.
 * - Supports default initialization with standard values.
 *
 * Fields:
 * - `prefix`: The prefix to denote the type of pre-release (e.g., "RC", "alpha").
 * - `separator`: The separator between the prefix and the numeric version.
 * - `startingVersion`: The initial version number for pre-release versions, which must be greater than zero.
 *
 * Key Behaviors:
 * - Validation of the prefix, separator, and starting version during instantiation.
 * - A default constructor that initializes the prefix to "RC", the separator to ".", and the starting version to 1.
 * - Provides a method to compile a regex pattern for matching and parsing pre-release version components based on the configuration.
 *
 * Usage Scenarios:
 * - Defining configurations for semantic version pre-release components.
 * - Parsing and validating pre-release versions based on configured patterns.
 *
 * Constraints:
 * - `prefix` must not be null or empty.
 * - `separator` must not be null or empty.
 * - `startingVersion` must be a positive integer greater than zero.
 */
public record PreReleaseConfig(String prefix, String separator, int startingVersion) {

    public PreReleaseConfig {
        validate(prefix, separator, startingVersion);
    }

    public PreReleaseConfig() {
        this("RC", ".", 1);
    }

    public Pattern preReleasePartPattern() {
        String escapedSeparator = separator.replace(".", "\\.");
        String pattern = "^(?i)(" + prefix + ")(" + escapedSeparator + ")([1-9]\\d*)$";
        return Pattern.compile(pattern);
    }

    private static void validate(String prefix, String separator, int startingVersion) {
        if (StringUtils.isEmptyOrNull(prefix)) {
            throw new IllegalArgumentException("prefix cannot be null or empty string");
        }
        if (StringUtils.isEmptyOrNull(separator)) {
            throw new IllegalArgumentException("separator cannot be null or empty string");
        }
        if (startingVersion <= 0) {
            throw new IllegalArgumentException("startingVersion must be positive integer greater than 0");
        }
    }
}
