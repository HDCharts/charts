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
