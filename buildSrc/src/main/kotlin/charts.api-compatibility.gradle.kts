import me.champeau.gradle.japicmp.JapicmpTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.register
import java.io.File

plugins {
    id("me.champeau.gradle.japicmp")
}

val apiCompatibilityBaselineJarsDirProperty = "apiCompatibilityBaselineJarsDir"
val apiCompatibilityBaselineFilePath = ".github/api-compatibility-baseline.txt"
val apiCompatibilityDefaultBaselineJarsDir = "api-compatibility/baseline-jars"
// japicmp --exclude expects wildcard expressions, not regex.
val apiCompatibilityInternalExcludePattern = "*.internal.*"

fun Project.baselineJarsDir(): File =
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

fun Project.resolveBaselineRefFromFile(): String {
    val baselineFile = rootProject.file(apiCompatibilityBaselineFilePath)
    if (!baselineFile.isFile) {
        throw GradleException(
            "Missing baseline file at ${baselineFile.absolutePath}.",
        )
    }

    return baselineFile
        .readLines()
        .map { it.trim() }
        .firstOrNull { it.isNotBlank() && !it.startsWith("#") }
        ?: throw GradleException(
            "No baseline ref found in ${baselineFile.absolutePath}.",
        )
}

fun Project.execAndGetStdout(
    args: List<String>,
    workingDir: File = rootProject.rootDir,
): String {
    val process =
        ProcessBuilder(args)
            .directory(workingDir)
            .redirectErrorStream(true)
            .start()
    val output = process.inputStream.bufferedReader().readText()
    val exitCode = process.waitFor()
    if (exitCode != 0) {
        throw GradleException(
            "Command failed (${args.joinToString(" ")}): ${output.trim()}",
        )
    }
    return output.trim()
}

fun String.toArtifactId(): String = removePrefix(":")

fun String.toTaskSuffix(): String =
    split('-', '.')
        .filter { it.isNotBlank() }
        .joinToString("") { token ->
            token.replaceFirstChar { firstChar -> firstChar.uppercase() }
        }

tasks.register("prepareApiCompatibilityBaselineJars") {
    group = "verification"
    description =
        "Builds baseline jars for API compatibility checks. Default source is .github/api-compatibility-baseline.txt."

    doLast {
        val baselineJarsDir = project.baselineJarsDir()

        project.delete(baselineJarsDir)
        baselineJarsDir.mkdirs()

        val baselineRef = project.resolveBaselineRefFromFile()
        val baselineSha =
            project.execAndGetStdout(
                listOf("git", "rev-parse", "-q", "--verify", "$baselineRef^{commit}"),
            )
        val baselineWorktreeDir = File(temporaryDir, "baseline-src").absoluteFile
        project.delete(baselineWorktreeDir)
        baselineWorktreeDir.parentFile.mkdirs()

        project.execAndGetStdout(
            listOf("git", "worktree", "add", "--detach", baselineWorktreeDir.absolutePath, baselineSha),
        )

        try {
            val gradlewPath = File(baselineWorktreeDir, "gradlew").absolutePath
            val baselineJarTasks = ChartsModules.library.map { "$it:jvmJar" }
            project.execAndGetStdout(
                listOf(gradlewPath) + baselineJarTasks + listOf("--no-daemon"),
                baselineWorktreeDir,
            )

            ChartsModules.library.forEach { projectPath ->
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
                    throw GradleException(
                        "Expected exactly one baseline jar for $artifactId in ${libsDir.absolutePath}, found: ${baselineJars.map { it.name }}",
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

val apiCompatibilityTasks =
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
            failOnModification = true
            onlyBinaryIncompatibleModified = true
            failOnSourceIncompatibility = true
            packageExcludes = listOf(apiCompatibilityInternalExcludePattern)
            mdOutputFile.set(layout.buildDirectory.file("reports/api-compatibility/$artifactId.md"))

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
        "Checks published JVM artifacts for breaking API changes using baseline ref from .github/api-compatibility-baseline.txt (or -P$apiCompatibilityBaselineJarsDirProperty override)."
    dependsOn(apiCompatibilityTasks)
}
