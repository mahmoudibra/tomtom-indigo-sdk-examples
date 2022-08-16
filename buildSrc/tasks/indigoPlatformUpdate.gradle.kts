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

import com.tomtom.ivi.buildsrc.environment.IndigoUpdateHelper

tasks.register<DefaultTask>("generateIndigoLibrariesVersionFile") {
    group = "Help"
    description = "Takes a `-PlatestIndigoVersion=x.y.z` Gradle input argument and, if " +
        "different from the version currently in use, creates a new version file."

    doLast {
        val versionLibraryFile = "libraries.versions.toml"
        val indigoUpdateHelper = IndigoUpdateHelper(rootProject)
        val currentVersionFile = File(rootProject.projectDir, "build-logic/$versionLibraryFile")
        val newVersionFile = File(rootProject.projectDir, versionLibraryFile)

        indigoUpdateHelper.generateNewVersionFile(currentVersionFile, newVersionFile)
    }
}
