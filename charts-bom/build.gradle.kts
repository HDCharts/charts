plugins {
    `java-platform`
    `maven-publish`
    signing
    alias(libs.plugins.mavenPublish)
}

javaPlatform {
    allowDependencies()
}

dependencies {
    constraints {
        api("${Config.GROUP_ID}:${Config.ARTIFACT_CORE_ID}:${project.version}")
        api("${Config.GROUP_ID}:${Config.ARTIFACT_LINE_ID}:${project.version}")
        api("${Config.GROUP_ID}:${Config.ARTIFACT_PIE_ID}:${project.version}")
        api("${Config.GROUP_ID}:${Config.ARTIFACT_BAR_ID}:${project.version}")
        api("${Config.GROUP_ID}:${Config.ARTIFACT_HISTOGRAM_ID}:${project.version}")
        api("${Config.GROUP_ID}:${Config.ARTIFACT_STACKED_BAR_ID}:${project.version}")
        api("${Config.GROUP_ID}:${Config.ARTIFACT_STACKED_AREA_ID}:${project.version}")
        api("${Config.GROUP_ID}:${Config.ARTIFACT_RADAR_ID}:${project.version}")
        api("${Config.GROUP_ID}:${Config.ARTIFACT_ID}:${project.version}")
    }
}

mavenPublishing {
    coordinates(
        groupId = Config.GROUP_ID,
        artifactId = Config.ARTIFACT_BOM_ID,
        version = project.version.toString(),
    )

    pom {
        ChartsPublishing.configurePom(
            pom = this,
            moduleName = "Charts BOM",
            moduleDescription = "Bill of Materials (BOM) for aligning Charts module versions.",
        )
    }
}
