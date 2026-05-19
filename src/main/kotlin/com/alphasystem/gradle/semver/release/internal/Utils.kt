package com.alphasystem.gradle.semver.release.internal

import gradlesemverrelease.PreReleaseConfig

fun toInitialPreReleaseVersion(src: PreReleaseConfig): PreReleaseVersion {
    return PreReleaseVersion(String.format("%s%s", src.prefix, src.separator), src.startingVersion)
}

fun toPreReleaseVersion(version: String, src: PreReleaseConfig): PreReleaseVersion? {
    val matcher = src.preReleasePartPattern().matcher(version)

    var result: PreReleaseVersion? = null
    if (matcher.matches()) {
        result = PreReleaseVersion(null, -1)
        // skip group 0; since it contains entire matched string
        // group 1 is the prefix, group 2 is the separator, group 3 is the version
        for (i in 1..matcher.groupCount()) {
            val value = matcher.group(i)

            if (result?.version!! < 0 && isNumeric(value)) {
                result = result.updateVersion(value.toInt())
            } else if (value == src.separator) {
                // Skip the separator
                continue
            } else {
                result = result.updatePrefix(value)
            }
        }
    }

    return result
}

private fun isNumeric(str: String?): Boolean {
    return str != null && str.matches("-?\\d+".toRegex())
}
