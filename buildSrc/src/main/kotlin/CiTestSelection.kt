/**
 * Narrows the `ciTest*` entry points to the modules and screenshot classes a pull request affects.
 *
 * `.github/scripts/ci-impact.py` chooses them, and the workflows pass them as [MODULES_PROPERTY] and
 * [SCREENSHOT_TESTS_PROPERTY]. An absent or blank property selects everything.
 */
object CiTestSelection {
    const val MODULES_PROPERTY = "ciTestModules"
    const val SCREENSHOT_TESTS_PROPERTY = "ciScreenshotTests"

    /** Splits a comma-separated property value. Returns null, which selects everything, when nothing is listed. */
    fun parse(value: String?): Set<String>? =
        value
            ?.split(',')
            ?.map(String::trim)
            ?.filter(String::isNotEmpty)
            ?.toSet()
            ?.takeIf { it.isNotEmpty() }

    fun select(
        modules: List<String>,
        selection: Set<String>?,
    ): List<String> = if (selection == null) modules else modules.filter { it in selection }

    /**
     * CI splits device tests across two emulator jobs. Shard 1 holds [firstShard], the slowest modules, and
     * shard 2 takes the rest, so new modules need no edit. A null shard keeps every module.
     */
    fun androidShard(
        modules: List<String>,
        shard: String?,
        firstShard: List<String>,
    ): List<String> =
        when (shard) {
            null -> modules
            "1" -> modules.filter { it in firstShard }
            "2" -> modules.filterNot { it in firstShard }
            else -> error("androidTestShard must be 1 or 2, was $shard")
        }
}
