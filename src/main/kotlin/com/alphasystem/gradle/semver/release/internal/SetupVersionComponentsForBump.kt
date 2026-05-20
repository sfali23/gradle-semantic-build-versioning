package com.alphasystem.gradle.semver.release.internal

import com.alphasystem.gradle.semver.release.VersionComponent
import com.alphasystem.gradle.semver.release.VersionComponent.*
import gradlesemverrelease.AutoBump

class SetupVersionComponentsForBump {

    private var result: Int = NONE.index

    fun parseMessage(commitMessage: String, autoBump: AutoBump): SetupVersionComponentsForBump {
        if (autoBump.major(commitMessage)) addMajor()
        if (autoBump.minor(commitMessage)) addMinor()
        if (autoBump.patch(commitMessage)) addPatch()
        if (autoBump.newPreRelease(commitMessage)) addNewPreRelease()
        if (autoBump.promoteToRelease(commitMessage)) addPromoteToRelease()
        return this
    }

    private fun addMajor(): SetupVersionComponentsForBump = addComponent(MAJOR)
    fun removeMajor(): SetupVersionComponentsForBump = removeComponent(MAJOR)

    private fun addMinor(): SetupVersionComponentsForBump = addComponent(MINOR)
    fun removeMinor(): SetupVersionComponentsForBump = removeComponent(MINOR)

    fun addPatch(): SetupVersionComponentsForBump = addComponent(PATCH)
    fun removePatch(): SetupVersionComponentsForBump = removeComponent(PATCH)

    fun addHotFix(): SetupVersionComponentsForBump = addComponent(HOT_FIX)
    fun removeHotFix(): SetupVersionComponentsForBump = removeComponent(HOT_FIX)

    private fun addNewPreRelease(): SetupVersionComponentsForBump = addComponent(NEW_PRE_RELEASE)
    fun removeNewPreRelease(): SetupVersionComponentsForBump = removeComponent(NEW_PRE_RELEASE)

    fun addPreRelease(): SetupVersionComponentsForBump = addComponent(PRE_RELEASE)
    fun removePreRelease(): SetupVersionComponentsForBump = removeComponent(PRE_RELEASE)

    fun addPromoteToRelease(): SetupVersionComponentsForBump = addComponent(PROMOTE_TO_RELEASE)
    fun removePromoteToRelease(): SetupVersionComponentsForBump = removeComponent(PROMOTE_TO_RELEASE)

    fun addSnapshot(): SetupVersionComponentsForBump = addComponent(SNAPSHOT)

    fun hasMajor(): Boolean = hasGivenComponent(MAJOR)
    fun hasMinor(): Boolean = hasGivenComponent(MINOR)
    fun hasPatch(): Boolean = hasGivenComponent(PATCH)
    fun hasPromoteToRelease(): Boolean = hasGivenComponent(PROMOTE_TO_RELEASE)
    fun hasPreRelease(): Boolean = hasGivenComponent(PRE_RELEASE)
    fun hasMandatoryComponents(): Boolean = hasMajor() || hasMinor() || hasPatch()
    fun hasEssentialComponents(): Boolean = hasMajor() || hasMinor() || hasPatch() || hasPromoteToRelease() ||
            hasPreRelease() || hasGivenComponent(HOT_FIX)

    fun reset(): SetupVersionComponentsForBump {
        result = NONE.index
        return this
    }

    fun addComponentIfRequired(versionComponent: VersionComponent, condition: () -> Boolean): SetupVersionComponentsForBump =
        if (condition()) addComponent(versionComponent) else this

    fun getVersionComponents(): List<VersionComponent> =
        setOf(
            VersionComponent.fromIndex(result and MAJOR.index),
            VersionComponent.fromIndex(result and MINOR.index),
            VersionComponent.fromIndex(result and PATCH.index),
            VersionComponent.fromIndex(result and HOT_FIX.index),
            VersionComponent.fromIndex(result and NEW_PRE_RELEASE.index),
            VersionComponent.fromIndex(result and PRE_RELEASE.index),
            VersionComponent.fromIndex(result and PROMOTE_TO_RELEASE.index),
            VersionComponent.fromIndex(result and SNAPSHOT.index)
        ).toList().filterNotNull()

    private fun addComponent(versionComponent: VersionComponent): SetupVersionComponentsForBump {
        result = result or (versionComponent.index or NONE.index)
        return this
    }

    private fun removeComponent(versionComponent: VersionComponent): SetupVersionComponentsForBump {
        result = result and (versionComponent.index or NONE.index).inv()
        return this
    }

    private fun hasGivenComponent(versionComponent: VersionComponent) =
        (result and versionComponent.index) == versionComponent.index

    companion object {
        fun apply(): SetupVersionComponentsForBump = SetupVersionComponentsForBump()
    }
}
