import java.io.File

/**
 * One japicmp finding, and one line of API-COMPATIBILITY-BREAKS.txt once acknowledged. Why a break was
 * accepted lives in the git history of the line, not here.
 */
internal data class ApiFinding(
    val module: String,
    val className: String,
    val member: String,
    val changeKind: String,
)

private const val API_COMPATIBILITY_ENTRY_SEPARATOR = " | "

private fun ApiFinding.toLine(): String =
    listOf(module, className, member, changeKind).joinToString(API_COMPATIBILITY_ENTRY_SEPARATOR)

private fun parseAcknowledgedBreakLine(line: String): ApiFinding? {
    val parts = line.split(API_COMPATIBILITY_ENTRY_SEPARATOR)
    if (parts.size != 4) return null
    return ApiFinding(
        module = parts[0].trim(),
        className = parts[1].trim(),
        member = parts[2].trim(),
        changeKind = parts[3].trim(),
    )
}

/** Reads the acknowledged breaks listed under `## <heading>` in [fileText]; ignores every other section. */
internal fun parseAcknowledgedBreaksSection(
    fileText: String,
    heading: String,
): List<ApiFinding> {
    val headingLine = "## $heading"
    val lines = fileText.lines()
    val headingIndex = lines.indexOfFirst { it.trim() == headingLine }
    if (headingIndex == -1) return emptyList()

    return lines
        .asSequence()
        .drop(headingIndex + 1)
        .map { it.trim() }
        .takeWhile { !it.startsWith("## ") }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .mapNotNull(::parseAcknowledgedBreakLine)
        .toList()
}

/** Adds [newEntries] under `## <sectionHeading>`, skipping duplicates, and returns how many landed. */
internal fun appendAcknowledgedBreaks(
    file: File,
    newEntries: List<ApiFinding>,
    sectionHeading: String,
): Int {
    val headingLine = "## $sectionHeading"
    val header =
        listOf(
            "# Acknowledged breaking API changes, grouped by release.",
            "# Format per entry: <module> | <class> | <member> | <change-kind>",
            "#",
            "# Each section heading is the pending release version, resolved from Axion when an entry",
            "# is acknowledged via ./gradlew apiCompatibilityAcknowledgeBreaks. Once that version is",
            "# tagged, the next acknowledgment resolves the next version and opens its own section above.",
            "# Run 'git blame' on a line to see the pull request that accepted that break.",
            "",
        )

    val originalLines = if (file.isFile) file.readLines() else header
    val lines = originalLines.toMutableList()
    if (lines.none { it.trim() == headingLine }) {
        // A new cycle's section opens above the released ones, so the newest is always on top.
        val firstSection = lines.indexOfFirst { it.trim().startsWith("## ") }
        if (firstSection == -1) lines.add(headingLine) else lines.addAll(firstSection, listOf(headingLine, ""))
    }

    val insertAt = lines.indexOfFirst { it.trim() == headingLine } + 1
    val sectionEnd = (insertAt until lines.size).firstOrNull { lines[it].trim().startsWith("## ") } ?: lines.size
    val existingEntryLines =
        (insertAt until sectionEnd)
            .map { lines[it].trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .toSet()

    val linesToAdd = newEntries.map(ApiFinding::toLine).distinct().filterNot { it in existingEntryLines }
    if (linesToAdd.isEmpty()) return 0

    // Append after the section's last entry, keeping the blank line that separates it from the next.
    var insertPoint = sectionEnd
    while (insertPoint > insertAt && lines[insertPoint - 1].isBlank()) insertPoint--

    lines.addAll(insertPoint, linesToAdd)
    file.parentFile?.mkdirs()
    file.writeText(lines.joinToString(System.lineSeparator()) + System.lineSeparator())
    return linesToAdd.size
}
