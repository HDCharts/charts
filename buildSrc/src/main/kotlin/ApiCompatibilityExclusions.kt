/** Exclusions applied consistently by JApiCmp and its XML finding parser. */
internal val apiCompatibilityPackageExcludes =
    listOf(
        "*.internal",
        "*.internal.*",
        // Compose Multiplatform generates the Res accessor classes and resource collectors into
        // *.generated.resources packages from whatever composeResources the module ships. That
        // package is build output, not hand-authored API, so a module dropping unused Compose
        // resources removes it from the published binary without a public API change.
        "*.generated.resources",
    )

/** Compose compiler implementation details whose generated members can change between Kotlin versions. */
internal val apiCompatibilityClassExcludes = listOf("*.ComposableSingletons\$*")

internal fun isExcludedApiClass(className: String): Boolean =
    apiCompatibilityPackageExcludes.any { it.matchesApiPackageName(className) } ||
        apiCompatibilityClassExcludes.any { it.matchesApiClassName(className) }

private fun String.matchesApiClassName(className: String): Boolean =
    japiCmpRegex(includeNestedClasses = true).matches(className)

private fun String.matchesApiPackageName(className: String): Boolean =
    japiCmpRegex(includeNestedClasses = false).matches(className.substringBeforeLast('.', missingDelimiterValue = ""))

private fun String.japiCmpRegex(includeNestedClasses: Boolean): Regex =
    Regex(
        buildString {
            append('^')
            this@japiCmpRegex.forEach { character ->
                if (character == '*') append(".*") else append(Regex.escape(character.toString()))
            }
            if (includeNestedClasses) append("(\\$.*)?")
            append('$')
        },
    )
