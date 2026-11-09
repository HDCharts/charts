import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class CiTestSelectionTest {
    private val deviceTestModules =
        listOf(":charts-line", ":charts-pie", ":charts-bar", ":charts-radar", ":charts-gauge")
    private val firstShard = listOf(":charts-line", ":charts-bar", ":charts-radar")

    @Test
    fun `absent or blank property selects every module`() {
        assertNull(CiTestSelection.parse(null))
        assertNull(CiTestSelection.parse(""))
        assertNull(CiTestSelection.parse(" , "))
        assertEquals(deviceTestModules, CiTestSelection.select(deviceTestModules, CiTestSelection.parse("")))
    }

    @Test
    fun `selection keeps the listed modules in task order`() {
        val selection = CiTestSelection.parse(":charts-pie, :charts-line,:androidApp")

        assertEquals(listOf(":charts-line", ":charts-pie"), CiTestSelection.select(deviceTestModules, selection))
    }

    @Test
    fun `shard without selected modules is empty`() {
        val selected = CiTestSelection.select(deviceTestModules, setOf(":charts-line", ":charts"))

        assertEquals(listOf(":charts-line"), CiTestSelection.androidShard(selected, "1", firstShard))
        assertEquals(emptyList(), CiTestSelection.androidShard(selected, "2", firstShard))
    }

    @Test
    fun `shards split every module when nothing is selected`() {
        assertEquals(firstShard, CiTestSelection.androidShard(deviceTestModules, "1", firstShard))
        assertEquals(
            listOf(":charts-pie", ":charts-gauge"),
            CiTestSelection.androidShard(deviceTestModules, "2", firstShard),
        )
        assertEquals(deviceTestModules, CiTestSelection.androidShard(deviceTestModules, null, firstShard))
    }

    @Test
    fun `unknown shard fails`() {
        assertFailsWith<IllegalStateException> {
            CiTestSelection.androidShard(deviceTestModules, "3", firstShard)
        }
    }
}
