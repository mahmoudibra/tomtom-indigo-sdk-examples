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

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.VersionedVariant
import com.tomtom.ivi.buildsrc.environment.ProjectAbis
import groovy.lang.GroovyObject
import org.jfrog.gradle.plugin.artifactory.ArtifactoryPluginUtil
import org.jfrog.gradle.plugin.artifactory.dsl.ArtifactoryPluginConvention
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig

plugins.apply("com.jfrog.artifactory")
plugins.apply("maven-publish")

val isApplicationProject: Boolean by extra

// TODO(IVI-4701): Remove Artifactory reference.
val artifactoryRepoBaseUrl = "https://artifactory.navkit-pipeline.tt3.com/artifactory"

fun convertModuleNameToApkBuildpath(moduleName: String): String {
    val hierarchy = moduleName.split("_")
    val module = hierarchy.joinToString("/")
    return "../builds/$module/build/outputs/apk/"
}

group = "com.tomtom.ivi"

// Register publications
extensions.getByType(PublishingExtension::class.java).apply {
    publications {
        if (isApplicationProject) {
            project.buildVariants.configureEach {
                val variantName = name
                val versionName = (this as VersionedVariant).versionName

                if (variantName != "debug" || project.name != "template_app") {
                    // We only want to publish the debug version of the template app, 
                    // the published binaries are provided in the SDK as a possibility to try 
                    // the example app out, without the need to setup an environment and build 
                    // from source.
                    return@configureEach
                }

                val publication = findByName("apk") as MavenPublication? ?: create(
                    "apk",
                    MavenPublication::class.java
                ) {
                    groupId = project.group.toString()
                    artifactId = "products_indigo_examples"
                    version = versionName
                }

                ProjectAbis.enabledAbis.forEach { abi ->
                    val output = File(
                        rootProject.file(
                            convertModuleNameToApkBuildpath("${project.name}") + "${variantName}"
                        ),
                        "${project.name}-${abi}-${variantName}.apk"
                    )

                    publication.artifact(output) {
                        extension = "${abi}.apk"
                        classifier = variantName
                    }
                }
            }
        }
    }
}

val Project.buildVariants: DomainObjectSet<out BaseVariant>
    get() {
        val plugins = project.plugins
        val extensions = project.extensions
        return when {
            plugins.hasPlugin(AppPlugin::class.java) -> extensions.getByType(AppExtension::class.java).applicationVariants
            else -> throw GradleException("could not retrieve variants for project ${project.name} - android plugin not applied ?")
        }
    }

fun Project.artifactory(configure: ArtifactoryPluginConvention.() -> Unit): Unit =
    configure(project.convention.getPluginByName("artifactory"))

artifactory {
    setContextUrl(artifactoryRepoBaseUrl)
    val artifactoryRepo = findProperty("artifactoryRepo") as String? ?: "ivi-maven"

    publish(delegateClosureOf<PublisherConfig> {
        repository(delegateClosureOf<GroovyObject> {
            setProperty("repoKey", artifactoryRepo)
            if (project.hasProperty("publishUsername")) {
                setProperty("username", properties["publishUsername"].toString())
                setProperty("password", properties["publishPassword"].toString())
            }
        })
        defaults(delegateClosureOf<GroovyObject> {
            invokeMethod("publications", arrayOf("apk", "exampleAppDocs"))
            setProperty("publishArtifacts", true)
            setProperty("publishPom", true)
        })
    })
    if (project.hasProperty("proxyHost") &&
        project.hasProperty("proxyPort")
    ) {
        val rootClientConfig =
            ArtifactoryPluginUtil.getArtifactoryConvention(project).clientConfig
        rootClientConfig.proxy.host = properties["proxyHost"].toString()
        rootClientConfig.proxy.port = properties["proxyPort"].toString().toInt()
        if (project.hasProperty("proxyUsername") &&
            project.hasProperty("proxyPassword")
        ) {
            rootClientConfig.proxy.username = properties["proxyUsername"].toString()
            rootClientConfig.proxy.password = properties["proxyPassword"].toString()
        }
    }
}
