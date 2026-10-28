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
                "com.example.internal.Foo" to true,
                "com.example.internal.Foo\$Nested" to true,
                "com.example.internal.deep.Foo" to true,
                "com.example.ComposableSingletons\$FooKt" to true,
                "com.example.internalUtils.Foo" to false,
                "com.example.api.Foo" to false,
                "com.example.api.ComposableSingletonsHelper" to false,
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
