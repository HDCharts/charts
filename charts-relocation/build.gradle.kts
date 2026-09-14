// Publishes relocation POMs only for 3.0.0 and the snapshot leading up to it.
// This module can be removed after the 3.0.0 release.

import org.gradle.api.publish.maven.MavenPublication

plugins {
    `maven-publish`
    signing
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.ktlint)
}

val relocationOldGroupId = "io.github.dautovicharis"
val relocationNewGroupId = Config.GROUP_ID

val relocationArtifactIds =
    listOf(
        Config.ARTIFACT_ID,
        Config.ARTIFACT_CORE_ID,
        Config.ARTIFACT_LINE_ID,
        Config.ARTIFACT_PIE_ID,
        Config.ARTIFACT_BAR_ID,
        Config.ARTIFACT_HISTOGRAM_ID,
        Config.ARTIFACT_STACKED_BAR_ID,
        Config.ARTIFACT_STACKED_AREA_ID,
        Config.ARTIFACT_RADAR_ID,
        Config.ARTIFACT_BOM_ID,
    )

val rawVersion = project.version.toString()
val isRelocationRelease =
    rawVersion == "3.0.0" || rawVersion == "3.0.0-SNAPSHOT"

publishing {
    publications {
        relocationArtifactIds.forEach { artifactId ->
            create<MavenPublication>("relocation-$artifactId") {
                groupId = relocationOldGroupId
                this.artifactId = artifactId
                version = rawVersion
                pom {
                    packaging = "pom"
                    withXml {
                        val root = asNode()
                        val distributionManagement = root.appendNode("distributionManagement")
                        val relocation = distributionManagement.appendNode("relocation")
                        relocation.appendNode("groupId", relocationNewGroupId)
                        relocation.appendNode("artifactId", artifactId)
                        relocation.appendNode("version", rawVersion)
                        relocation.appendNode("message", "HDCharts has moved to $relocationNewGroupId")
                    }
                }
            }
        }
    }
}

tasks.withType<PublishToMavenRepository>().configureEach {
    onlyIf("Relocation POMs are only published at the first release of the new namespace.") {
        isRelocationRelease
    }
}
