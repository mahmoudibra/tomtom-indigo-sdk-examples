/*
 * Copyright © 2022 TomTom NV. All rights reserved.
 *
 * This software is the proprietary copyright of TomTom NV and its subsidiaries and may be
 * used for internal evaluation purposes or commercial use strictly subject to separate
 * license agreement between you and TomTom NV. If you are the licensee, you are only permitted
 * to use this software in accordance with the terms of your license agreement. If you are
 * not the licensee, you are not authorized to use this software in any manner and should
 * immediately return or destroy it.
 */

import com.tomtom.ivi.platform.gradle.api.common.iviapplication.config.IviServiceHostConfig
import com.tomtom.ivi.platform.gradle.api.framework.config.ivi

plugins {
    id("com.tomtom.ivi.product.defaults.core")
}

android {
    buildFeatures {
        dataBinding = true
    }
}

apply(from = rootProject.file("examples/vpa/iviservicehosts.gradle.kts"))
val exampleVpaServiceHost: IviServiceHostConfig by project.extra

ivi {
    application {
        enabled = true
        services {
            addHost(exampleVpaServiceHost)
        }
    }
}

dependencies {
    implementation(project(":examples_common"))
    implementation(project(":examples_vpa_service"))
}
