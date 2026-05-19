package com.alphasystem.gradle.semver.release.internal;

import gradlesemverrelease.PreReleaseConfig;

public class Utils {

    // do not let anyone instantiate this class
    private Utils() {
    }

    public static PreReleaseVersion toInitialPreReleaseVersion(PreReleaseConfig src) {
        return new PreReleaseVersion(String.format("%s%s", src.prefix(), src.separator()), src.startingVersion(), null);
    }
}
