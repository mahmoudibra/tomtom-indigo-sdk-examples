/*
 * Copyright © 2020 TomTom NV. All rights reserved.
 *
 * This software is the proprietary copyright of TomTom NV and its subsidiaries and may be
 * used for internal evaluation purposes or commercial use strictly subject to separate
 * license agreement between you and TomTom NV. If you are the licensee, you are only permitted
 * to use this software in accordance with the terms of your license agreement. If you are
 * not the licensee, you are not authorized to use this software in any manner and should
 * immediately return or destroy it.
 */

package com.tomtom.ivi.buildsrc.environment

import org.gradle.api.Project
import java.io.File

/**
 * Generates a versions file based on existing versions file and TomTom IndiGO version provided as a
 * Gradle parameter.
 *
 * The new version number must be given as `-PlatestIndigoVersion=x.y.z` command line argument to
 * Gradle.
 * A new TOML version file will be generated with whatever old version number replaced by the new
 * one.
 */
class IndigoUpdateHelper(project: Project) {

    private val latestVersion = project.property("latestIndigoVersion") as String

    fun generateNewVersionFile(oldFile: File, newFile: File) {
        val versionTag = "indigoPlatform = "
        val versionValue = Regex("""(\d+.\d+.\d+)""")
        oldFile.useLines { lines ->
            newFile.bufferedWriter().use { outFile ->
                lines.forEach { oldLine ->
                    val newLine = when {
                        oldLine.contains(versionTag) -> {
                            val currentVersion: String? = versionValue.find(oldLine)?.value
                            println("Replacing $currentVersion IndiGO version with $latestVersion")
                            oldLine.replace(versionValue, latestVersion)
                        }
                        else -> oldLine
                    }
                    @Suppress("DEPRECATION")
                    outFile.appendln(newLine)
                }
            }
        }
    }
}
