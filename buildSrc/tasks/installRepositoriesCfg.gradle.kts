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

import java.util.Properties

tasks.create("installRepositoriesCfg") {
    doFirst {
        val androidDirectoryName = "${project.gradle.gradleUserHomeDir.parent}/.android"
        val repositoriesCfgFileName = "repositories.cfg"
        val fileHeader = "## User Sources for Android Repository"
        val countKey = "count"
        val srcKey = "src%02d"
        val enabledKey = "enabled%02d"
        val dispKey = "disp%02d"
        val dispTitle = "TomTom IndiGO Update Site - Android 11 Emulators"
        val versionEmulator = project.properties["versionEmulator"] as String?
        val srcUrl = if (project.hasProperty("nexusUsername")) {
            // TODO(IVI-3803): Upload the repo-sys-img.xml file to the developer portal
            "https://aaos.blob.core.windows.net/indigo-automotive/repo-sys-img.xml"
        } else {
            "https://artifactory.navkit-pipeline.tt3.com/artifactory/ivi-maven/com/tomtom/ivi/ivi-automotive-sdk/android-11/repo-sys-img.xml"
        }

        val androidDirectory = File(androidDirectoryName).apply { mkdirs() }
        val repositoriesCfgFile = File(androidDirectory, repositoriesCfgFileName)

        val properties = repositoriesCfgFile
            .takeIf { it.exists() }
            ?.let { Properties().apply { load(repositoriesCfgFile.reader()) } } ?: Properties()

        properties.takeUnless { it.values.contains(srcUrl) }
            ?.apply {
                logger.info("`$repositoriesCfgFile` will be updated.")
                val repositoryCount = getProperty(countKey)?.toIntOrNull() ?: 0
                setProperty(countKey, "${repositoryCount + 1}")
                setProperty(srcKey.format(repositoryCount), srcUrl)
                setProperty(enabledKey.format(repositoryCount), "true")
                setProperty(dispKey.format(repositoryCount), dispTitle)
                store(repositoriesCfgFile.writer(), fileHeader)
            } ?: run { logger.info("`$repositoriesCfgFile` is already up to date.") }
    }
}
