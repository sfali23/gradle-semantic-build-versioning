package com.alphasystem.gradle.semver.release.internal;

import gradlesemverrelease.PreReleaseConfig;

public class Utils {

    // do not let anyone instantiate this class
    private Utils() {
    }

    public static PreReleaseVersion toInitialPreReleaseVersion(PreReleaseConfig src) {
        return new PreReleaseVersion(String.format("%s%s", src.prefix(), src.separator()), src.startingVersion());
    }

    public static PreReleaseVersion toPreReleaseVersion(String version, PreReleaseConfig src) {
        final var matcher = src.preReleasePartPattern().matcher(version);

        PreReleaseVersion result = null;
        if (matcher.matches()) {
            result = new PreReleaseVersion(null, -1);
            // skip group 0; since it contains entire matched string
            // group 1 is the prefix, group 2 is the separator, group 3 is the version
            for (int i = 1; i <= matcher.groupCount(); i++) {
                final var value = matcher.group(i);

                if (result.version() < 0 && isNumeric(value)) {
                    result = result.updateVersion(Integer.parseInt(value));
                } else if (result.prefix() != null && value.equals(src.separator())) {
                    // Skip the separator
                    continue;
                } else {
                    result = result.updatePrefix(value);
                }
            }
        }

        return result;
    }

    private static boolean isNumeric(String str) {
        return str != null && str.matches("-?\\d+");
    }

}
