import me.champeau.gradle.japicmp.JapicmpTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.register
import java.io.File

plugins {
    id("me.champeau.gradle.japicmp")
}

private val apiCompatibilityBaselineJarsDirProperty = "apiCompatibilityBaselineJarsDir"
private val apiCompatibilityBaselineRefProperty = "apiCompatibilityBaselineRef"
// Fallback for a repository that has no release tag yet; squash-safe, unlike a pinned pre-merge SHA.
private val apiCompatibilityFallbackBaselineRef = "origin/main"
private val apiCompatibilityAcknowledgedBreaksFilePath = "API-COMPATIBILITY-BREAKS.txt"
private val apiCompatibilitySemVerPattern = Regex("^[0-9]+\\.[0-9]+\\.[0-9]+$")

// Axion appends a sanitized branch name to the version on any non-release branch, so the pending
// release heading can only match a leading X.Y.Z prefix, not the whole resolved version string.
private val apiCompatibilitySemVerPrefixPattern = Regex("^[0-9]+\\.[0-9]+\\.[0-9]+")
private val apiCompatibilityDefaultBaselineJarsDir = "api-compatibility/baseline-jars"
// japicmp --exclude expects wildcard expressions, not regex.
private val apiCompatibilityInternalExcludePattern = "*.internal.*"
// Compose compiler implementation detail whose generated members can change between Kotlin versions.
private val apiCompatibilityComposeSingletonsExcludePattern = "*.ComposableSingletons\$*"

private fun Project.baselineJarsDir(): File =
    providers
        .gradleProperty(apiCompatibilityBaselineJarsDirProperty)
        .orNull
        ?.takeIf { it.isNotBlank() }
        ?.let(::File)
        ?.absoluteFile
        ?: layout.buildDirectory
            .dir(apiCompatibilityDefaultBaselineJarsDir)
            .get()
            .asFile

private fun Project.adHocBaselineRef(): String? =
    providers
        .gradleProperty(apiCompatibilityBaselineRefProperty)
        .orNull
        ?.takeIf { it.isNotBlank() }

private fun Project.latestReleaseTag(): String? =
    execAndGetStdout(listOf("git", "tag", "--list", "--sort=-version:refname"), ignoreExitCode = true)
        .lineSequence()
        .map { it.trim() }
        .firstOrNull { apiCompatibilitySemVerPattern.matches(it) }

// The gate always spans the current release cycle: the latest release tag up to the working tree.
private fun Project.resolveBaselineRef(): String =
    adHocBaselineRef() ?: latestReleaseTag() ?: apiCompatibilityFallbackBaselineRef

// apiCompatibilityCheck and apiCompatibilityAcknowledgeBreaks must agree on the span they describe.
private fun Project.requireGateBaseline() {
    val adHocRef = adHocBaselineRef() ?: return
    throw GradleException(
        "This task always compares against the current release cycle, so -P$apiCompatibilityBaselineRefProperty " +
            "($adHocRef) does not apply. Use 'apiCompatibilityCompare' to diff against an arbitrary ref.",
    )
}

// Axion's resolved version, which advances only when a tag lands, so it is stable across a cycle.
private fun Project.resolvePendingReleaseHeading(): String {
    val rawVersion = rootProject.version.toString()
    return apiCompatibilitySemVerPrefixPattern.find(rawVersion)?.value
        ?: throw GradleException(
            "Resolved project version '$rawVersion' does not start with a SemVer X.Y.Z; cannot derive an " +
                "$apiCompatibilityAcknowledgedBreaksFilePath section heading from it.",
        )
}

private fun Project.execAndGetStdout(
    args: List<String>,
    workingDir: File = rootProject.rootDir,
    ignoreExitCode: Boolean = false,
): String {
    val process =
        ProcessBuilder(args)
            .directory(workingDir)
            .redirectErrorStream(true)
            .start()
    val output = process.inputStream.bufferedReader().readText()
    val exitCode = process.waitFor()
    if (exitCode != 0 && !ignoreExitCode) {
        throw GradleException(
            "Command failed (${args.joinToString(" ")}): ${output.trim()}",
        )
    }
    return output.trim()
}

private fun String.toArtifactId(): String = removePrefix(":")

private fun String.toTaskSuffix(): String =
    split('-', '.')
        .filter { it.isNotBlank() }
        .joinToString("") { token ->
            token.replaceFirstChar { firstChar -> firstChar.uppercase() }
        }

private fun Project.apiCompatibilityXmlReportFile(artifactId: String): File =
    layout.buildDirectory
        .file("reports/api-compatibility/$artifactId.xml")
        .get()
        .asFile

private fun Project.currentApiCompatibilityFindings(): List<ApiFinding> =
    ChartsModules.library.flatMap { projectPath ->
        val artifactId = projectPath.toArtifactId()
        val xmlFile = apiCompatibilityXmlReportFile(artifactId)
        if (xmlFile.isFile) parseJapicmpXmlFindings(artifactId, xmlFile) else emptyList()
    }

tasks.register("prepareApiCompatibilityBaselineJars") {
    group = "verification"
    description =
        "Builds baseline jars for API compatibility checks from the latest release tag."

    doLast {
        // A skipped JapicmpTask leaves its old report behind, which would read as a current finding.
        project.delete(layout.buildDirectory.dir("reports/api-compatibility"))

        val baselineJarsDir = project.baselineJarsDir()

        project.delete(baselineJarsDir)
        baselineJarsDir.mkdirs()

        val baselineRef = project.resolveBaselineRef()
        logger.lifecycle("Building API compatibility baseline jars from $baselineRef")
        val baselineSha =
            project.execAndGetStdout(
                listOf("git", "rev-parse", "-q", "--verify", "$baselineRef^{commit}"),
            )
        val baselineWorktreeDir = File(temporaryDir, "baseline-src").absoluteFile
        project.execAndGetStdout(
            listOf("git", "worktree", "remove", "--force", baselineWorktreeDir.absolutePath),
            ignoreExitCode = true,
        )
        project.execAndGetStdout(
            listOf("git", "worktree", "prune"),
            ignoreExitCode = true,
        )
        project.delete(baselineWorktreeDir)
        baselineWorktreeDir.parentFile.mkdirs()

        project.execAndGetStdout(
            listOf("git", "worktree", "add", "--detach", baselineWorktreeDir.absolutePath, baselineSha),
        )

        try {
            val gradlewPath = File(baselineWorktreeDir, "gradlew").absolutePath
            val baselineProjects =
                ChartsModules.library.filter { projectPath ->
                    val moduleRelativePath =
                        project(projectPath).projectDir.relativeTo(rootProject.rootDir).path
                    File(baselineWorktreeDir, moduleRelativePath).isDirectory
                }
            val baselineJarTasks = baselineProjects.map { "$it:jvmJar" }

            if (baselineJarTasks.isNotEmpty()) {
                project.execAndGetStdout(
                    listOf(gradlewPath) + baselineJarTasks + listOf("--no-daemon"),
                    baselineWorktreeDir,
                )
            }

            baselineProjects.forEach { projectPath ->
                val artifactId = projectPath.toArtifactId()
                val moduleRelativePath =
                    project(projectPath).projectDir.relativeTo(rootProject.rootDir).path
                val libsDir = File(File(baselineWorktreeDir, moduleRelativePath), "build/libs")
                val baselineJars =
                    libsDir
                        .listFiles { file ->
                            file.isFile &&
                                file.extension == "jar" &&
                                file.name.startsWith("$artifactId-jvm")
                        }?.sortedBy { it.name }
                        .orEmpty()
                val baselineJar = baselineJars.singleOrNull()

                if (baselineJar == null) {
                    val foundBaselineJarNames = baselineJars.map { it.name }
                    throw GradleException(
                        "Expected exactly one baseline jar for $artifactId in ${libsDir.absolutePath}, " +
                            "found: $foundBaselineJarNames",
                    )
                }

                baselineJar.copyTo(File(baselineJarsDir, "$artifactId-jvm.jar"), overwrite = true)
            }
        } finally {
            project.execAndGetStdout(
                listOf("git", "worktree", "remove", "--force", baselineWorktreeDir.absolutePath),
            )
        }
    }
}

private val apiCompatibilityTasks =
    ChartsModules.library.map { projectPath ->
        val artifactId = projectPath.toArtifactId()
        tasks.register<JapicmpTask>("apiCompatibility${artifactId.toTaskSuffix()}") {
            group = "verification"
            description = "Checks binary/source API compatibility for $artifactId against baseline jars."

            dependsOn("$projectPath:jvmJar")
            dependsOn("prepareApiCompatibilityBaselineJars")
            accessModifier = "public"
            ignoreMissingClasses = true
            onlyModified = true
            // apiCompatibilityCheck decides pass/fail from this task's XML report instead.
            failOnModification = false
            onlyBinaryIncompatibleModified = true
            failOnSourceIncompatibility = false
            packageExcludes = listOf(apiCompatibilityInternalExcludePattern)
            classExcludes = listOf(apiCompatibilityComposeSingletonsExcludePattern)
            mdOutputFile.set(layout.buildDirectory.file("reports/api-compatibility/$artifactId.md"))
            xmlOutputFile.set(project.apiCompatibilityXmlReportFile(artifactId))

            val baselineJarsDirProvider = providers.provider { project.baselineJarsDir().absoluteFile }
            val oldJarProvider =
                baselineJarsDirProvider.map { resolvedBaselineJarsDir ->
                    File(resolvedBaselineJarsDir, "$artifactId-jvm.jar")
                }
            val newJarProvider =
                project(projectPath)
                    .tasks
                    .named<Jar>("jvmJar")
                    .flatMap { it.archiveFile }
                    .map { it.asFile }

            oldArchives.from(oldJarProvider)
            newArchives.from(newJarProvider)
            oldClasspath.from(oldJarProvider)
            newClasspath.from(newJarProvider)
            onlyIf {
                oldJarProvider.get().isFile
            }

            doFirst {
                logger.lifecycle(
                    "Running API compatibility for $artifactId-jvm against baseline ${oldJarProvider.get().absolutePath}",
                )
            }
        }
    }

tasks.register("apiCompatibilityCheck") {
    group = "verification"
    description =
        "Checks published JVM artifacts for breaking API changes made since the latest release tag, " +
        "failing only on findings not acknowledged in $apiCompatibilityAcknowledgedBreaksFilePath."
    dependsOn(apiCompatibilityTasks)
    doFirst { project.requireGateBaseline() }

    doLast {
        val findings = project.currentApiCompatibilityFindings()
        if (findings.isEmpty()) return@doLast

        val acknowledgedBreaksFile = rootProject.file(apiCompatibilityAcknowledgedBreaksFilePath)
        val acknowledged =
            if (acknowledgedBreaksFile.isFile) {
                parseAcknowledgedBreaksSection(
                    acknowledgedBreaksFile.readText(),
                    project.resolvePendingReleaseHeading(),
                ).toSet()
            } else {
                emptySet()
            }
        val unacknowledged = findings.filterNot { it in acknowledged }

        if (unacknowledged.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("Unacknowledged breaking API change(s) detected (${unacknowledged.size}):")
                    unacknowledged.forEach { finding ->
                        appendLine(
                            "  - ${finding.module} | ${finding.className} | ${finding.member} | ${finding.changeKind}",
                        )
                    }
                    appendLine()
                    append(
                        "If intentional, run './gradlew apiCompatibilityAcknowledgeBreaks' and commit the " +
                            "updated $apiCompatibilityAcknowledgedBreaksFilePath in this pull request.",
                    )
                },
            )
        }
    }
}

tasks.register("apiCompatibilityAcknowledgeBreaks") {
    group = "verification"
    description =
        "Appends the API changes detected for the current release cycle to " +
        "$apiCompatibilityAcknowledgedBreaksFilePath, under a heading for the pending release version " +
        "resolved from Axion. Commit the regenerated file alongside the breaking change."
    dependsOn(apiCompatibilityTasks)
    doFirst { project.requireGateBaseline() }

    doLast {
        val acknowledgedBreaksFile = rootProject.file(apiCompatibilityAcknowledgedBreaksFilePath)
        val sectionHeading = project.resolvePendingReleaseHeading()
        val acknowledged =
            if (acknowledgedBreaksFile.isFile) {
                parseAcknowledgedBreaksSection(acknowledgedBreaksFile.readText(), sectionHeading).toSet()
            } else {
                emptySet()
            }
        val newFindings = project.currentApiCompatibilityFindings().filterNot { it in acknowledged }

        if (newFindings.isEmpty()) {
            logger.lifecycle(
                "No new API compatibility findings; $apiCompatibilityAcknowledgedBreaksFilePath left unchanged.",
            )
            return@doLast
        }

        val addedCount = appendAcknowledgedBreaks(acknowledgedBreaksFile, newFindings, sectionHeading)
        logger.lifecycle(
            "Appended $addedCount acknowledged finding(s) to $apiCompatibilityAcknowledgedBreaksFilePath " +
                "under \"## $sectionHeading\".",
        )
    }
}

tasks.register("apiCompatibilityCompare") {
    group = "verification"
    description =
        "Reports the public API diff against -P$apiCompatibilityBaselineRefProperty=<ref> for investigation, " +
        "leaving $apiCompatibilityAcknowledgedBreaksFilePath and the compatibility gate untouched."
    dependsOn(apiCompatibilityTasks)

    doLast {
        val baselineRef =
            project.adHocBaselineRef()
                ?: throw GradleException(
                    "Pass -P$apiCompatibilityBaselineRefProperty=<ref> naming the ref to compare against.",
                )
        val findings = project.currentApiCompatibilityFindings()
        logger.lifecycle(
            buildString {
                appendLine("Public API diff against $baselineRef (${findings.size} finding(s)):")
                findings.forEach { finding ->
                    appendLine(
                        "  - ${finding.module} | ${finding.className} | ${finding.member} | ${finding.changeKind}",
                    )
                }
            },
        )
    }
}
