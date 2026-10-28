import japicmp.filter.JavaDocLikeClassFilter
import japicmp.filter.JavadocLikePackageFilter
import javassist.ClassPool
import kotlin.test.Test
import kotlin.test.assertEquals

class ApiCompatibilityExclusionsTest {
    @Test
    fun `matches JApiCmp filtering for API classes`() {
        val cases =
            mapOf(
                "io.github.hdcharts.core.internal.ApiCompatibilityTestClass" to true,
                "io.github.hdcharts.core.internal.ApiCompatibilityTestClass\$Nested" to true,
                "io.github.hdcharts.core.internal.deep.ApiCompatibilityTestClass" to true,
                "io.github.hdcharts.core.ComposableSingletons\$ApiCompatibilityTestKt" to true,
                "io.github.hdcharts.core.internalUtils.ApiCompatibilityTestClass" to false,
                "io.github.hdcharts.core.model.ApiCompatibilityTestClass" to false,
                "io.github.hdcharts.core.model.ComposableSingletonsHelper" to false,
            )

        cases.forEach { (className, expectedExcluded) ->
            val japiCmpExcluded = isExcludedByJapiCmp(className)
            assertEquals(expectedExcluded, japiCmpExcluded, "JApiCmp: $className")
            assertEquals(japiCmpExcluded, isExcludedApiClass(className), "XML parser: $className")
        }
    }

    private fun isExcludedByJapiCmp(className: String): Boolean {
        val ctClass = ClassPool.getDefault().makeClass(className)
        return try {
            apiCompatibilityPackageExcludes.any { JavadocLikePackageFilter(it, true).matches(ctClass) } ||
                apiCompatibilityClassExcludes.any { JavaDocLikeClassFilter(it).matches(ctClass) }
        } finally {
            ctClass.detach()
        }
    }
}
