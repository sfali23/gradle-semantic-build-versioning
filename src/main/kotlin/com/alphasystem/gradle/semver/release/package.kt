package com.alphasystem.gradle.semver.release

val DefaultBooleanValue = false
val DefaultStartingVersion: String = "0.1.0"
val DefaultTagPrefix: String = "v"
val DefaultSnapshotPrefix: String = "SNAPSHOT"
val DefaultBumpLevel: VersionComponent = VersionComponent.PATCH
val DefaultComponentToBump: VersionComponent = VersionComponent.NONE
val DefaultHotfixBranchPattern: Regex = initializeHotfixBranchPattern()
val DefaultReleaseBranches: List<String> = listOf("main", "master")

fun initializeHotfixBranchPattern(tagPrefix: String = DefaultTagPrefix): Regex =
    "^$tagPrefix(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\+$".toRegex()

fun Regex.nonEmpty(input: String) = this.find(input) != null
