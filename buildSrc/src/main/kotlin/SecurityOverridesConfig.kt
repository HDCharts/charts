import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.VersionCatalog

fun ConfigurationContainer.configureBuildscriptSecurityOverrides(versionCatalog: VersionCatalog) {
    val protobufSecurityVersion = versionCatalog.requiredVersion("protobuf-security")
    val jdomSecurityVersion = versionCatalog.requiredVersion("jdom-security")
    val commonsLang3SecurityVersion = versionCatalog.requiredVersion("commons-lang3-security")
    val httpClientSecurityVersion = versionCatalog.requiredVersion("httpclient-security")
    val jose4jSecurityVersion = versionCatalog.requiredVersion("jose4j-security")
    val jacksonCoreSecurityVersion = versionCatalog.requiredVersion("jackson-core-security")
    val protobufOverride = SecurityOverrideRule(protobufSecurityVersion, SecurityOverrides.PROTOBUF_REASON)
    val jdomOverride = SecurityOverrideRule(jdomSecurityVersion, SecurityOverrides.JDOM_REASON)
    val commonsLangOverride = SecurityOverrideRule(commonsLang3SecurityVersion, SecurityOverrides.COMMONS_LANG3_REASON)
    val httpClientOverride = SecurityOverrideRule(httpClientSecurityVersion, SecurityOverrides.HTTP_CLIENT_REASON)
    val jose4jOverride = SecurityOverrideRule(jose4jSecurityVersion, SecurityOverrides.JOSE4J_REASON)
    val jacksonOverride = SecurityOverrideRule(jacksonCoreSecurityVersion, SecurityOverrides.JACKSON_CORE_REASON)
    val buildscriptOverrides =
        buildMap<DependencyCoordinate, SecurityOverrideRule> {
            putAll(
                SecurityOverrides.PROTOBUF_ARTIFACTS.associate { artifact ->
                    DependencyCoordinate(SecurityOverrides.PROTOBUF_GROUP, artifact) to protobufOverride
                },
            )
            put(DependencyCoordinate(SecurityOverrides.JDOM_GROUP, SecurityOverrides.JDOM_ARTIFACT), jdomOverride)
            put(
                DependencyCoordinate(SecurityOverrides.COMMONS_LANG_GROUP, SecurityOverrides.COMMONS_LANG3_ARTIFACT),
                commonsLangOverride,
            )
            put(
                DependencyCoordinate(SecurityOverrides.HTTP_COMPONENTS_GROUP, SecurityOverrides.HTTP_CLIENT_ARTIFACT),
                httpClientOverride,
            )
            put(DependencyCoordinate(SecurityOverrides.JOSE4J_GROUP, SecurityOverrides.JOSE4J_ARTIFACT), jose4jOverride)
            put(
                DependencyCoordinate(SecurityOverrides.JACKSON_CORE_GROUP, SecurityOverrides.JACKSON_CORE_ARTIFACT),
                jacksonOverride,
            )
        }

    configureEach {
        if (name == "classpath") {
            resolutionStrategy.eachDependency {
                val requestedGroup = requested.group ?: return@eachDependency
                val overrideRule =
                    buildscriptOverrides[DependencyCoordinate(requestedGroup, requested.name)] ?: return@eachDependency
                useVersion(overrideRule.version)
                because(overrideRule.reason)
            }
        }
    }
}

fun ConfigurationContainer.configureProjectSecurityOverrides(
    versionCatalog: VersionCatalog,
    includeCommonsLang: Boolean = false,
) {
    val commonsLang3SecurityVersion = versionCatalog.requiredVersion("commons-lang3-security")
    val logbackSecurityVersion = versionCatalog.requiredVersion("logback-core-security")

    configureEach {
        if (includeCommonsLang) {
            resolutionStrategy.eachDependency {
                if (requested.group == SecurityOverrides.COMMONS_LANG_GROUP &&
                    requested.name == SecurityOverrides.COMMONS_LANG3_ARTIFACT
                ) {
                    useVersion(commonsLang3SecurityVersion)
                    because(SecurityOverrides.COMMONS_LANG3_REASON)
                }
            }
        }

        if (name == "ktlint") {
            resolutionStrategy.eachDependency {
                if (requested.group == SecurityOverrides.LOGBACK_GROUP &&
                    requested.name in SecurityOverrides.LOGBACK_ARTIFACTS
                ) {
                    useVersion(logbackSecurityVersion)
                    because(SecurityOverrides.LOGBACK_REASON)
                }
            }
        }
    }
}

private fun VersionCatalog.requiredVersion(alias: String): String =
    findVersion(alias)
        .get()
        .requiredVersion

private data class DependencyCoordinate(
    val group: String,
    val artifact: String,
)

private data class SecurityOverrideRule(
    val version: String,
    val reason: String,
)

fun Project.configureJsSecurityOverrides(versionCatalog: VersionCatalog) {
    val serializeJavascriptSecurityVersion = versionCatalog.requiredVersion("serialize-javascript-security")
    val uuidSecurityVersion = versionCatalog.requiredVersion("uuid-security")
    val yarnRootExtension = resolveYarnRootExtension()

    // Keep Kotlin/JS transitive dependencies patched in kotlin-js-store/yarn.lock.
    yarnRootExtension.applyResolution("serialize-javascript", serializeJavascriptSecurityVersion)
    yarnRootExtension.applyResolution("uuid", uuidSecurityVersion)
}

private fun Project.resolveYarnRootExtension(): Any {
    val yarnRootClass =
        sequenceOf(
            Thread.currentThread().contextClassLoader,
            javaClass.classLoader,
            this::class.java.classLoader,
        ).filterNotNull()
            .mapNotNull { classLoader ->
                runCatching {
                    Class.forName(
                        "org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension",
                        true,
                        classLoader,
                    )
                }.getOrNull()
            }.firstOrNull()
            ?: error("Unable to load YarnRootExtension class from available classloaders")
    return try {
        val getMethod = yarnRootClass.getMethod("get", Project::class.java)
        getMethod.invoke(null, this)
    } catch (_: NoSuchMethodException) {
        val companion = yarnRootClass.getDeclaredField("Companion").get(null)
        val getMethod =
            companion.javaClass.methods.firstOrNull { method ->
                method.name == "get" &&
                    method.parameterTypes.contentEquals(
                        arrayOf(Project::class.java),
                    )
            } ?: error("Unable to locate YarnRootExtension#get(Project) method")
        getMethod.invoke(companion, this)
    }
}

private fun Any.applyResolution(
    dependencyName: String,
    version: String,
) {
    val resolutionMethod =
        javaClass.methods.firstOrNull { method ->
            method.name == "resolution" &&
                method.parameterTypes.contentEquals(
                    arrayOf(String::class.java, String::class.java),
                )
        } ?: error("Unable to locate YarnRootExtension#resolution(String, String) method")
    resolutionMethod.invoke(this, dependencyName, version)
}
