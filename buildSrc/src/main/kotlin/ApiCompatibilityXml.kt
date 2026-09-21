import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

private fun NodeList.elements(): List<Element> = (0 until length).mapNotNull { item(it) as? Element }

private fun Element.directChildElements(tagName: String): List<Element> =
    childNodes.elements().filter { it.tagName == tagName }

/** Only the compatibility changes that actually broke binary or source compatibility, not additive ones. */
private fun Element.breakingCompatibilityChangeTypes(): List<String> =
    directChildElements("compatibilityChanges")
        .flatMap { it.directChildElements("compatibilityChange") }
        .filter { it.getAttribute("binaryCompatible") == "false" || it.getAttribute("sourceCompatible") == "false" }
        .map { it.getAttribute("type") }

// (wrapper tag, member tag) pairs a <class> element groups its members under.
private val memberTagsByWrapper = listOf("constructors" to "constructor", "methods" to "method", "fields" to "field")

// A field's member key is just its name; a constructor/method's includes parameter types to disambiguate overloads.
private fun Element.memberKey(memberTag: String): String {
    val name = getAttribute("name")
    if (memberTag == "field") return name
    val parameterTypes =
        directChildElements("parameters")
            .flatMap { it.directChildElements("parameter") }
            .map { it.getAttribute("type") }
    return "$name(${parameterTypes.joinToString(", ")})"
}

private fun Element.memberFindings(
    module: String,
    className: String,
    memberTag: String,
): List<ApiFinding> {
    val changeKinds = breakingCompatibilityChangeTypes()
    if (changeKinds.isEmpty()) return emptyList()
    val member = memberKey(memberTag)
    return changeKinds.map { ApiFinding(module, className, member, changeKind = it) }
}

private fun Element.classFindings(module: String): List<ApiFinding> {
    val className = getAttribute("fullyQualifiedName")
    val classLevel =
        breakingCompatibilityChangeTypes().map { ApiFinding(module, className, member = "", changeKind = it) }
    val memberLevel =
        memberTagsByWrapper.flatMap { (wrapperTag, memberTag) ->
            directChildElements(wrapperTag)
                .flatMap { it.directChildElements(memberTag) }
                .flatMap { it.memberFindings(module, className, memberTag) }
        }
    return classLevel + memberLevel
}

/** Parses a japicmp XML report (see JapicmpTask#xmlOutputFile) into the breaking findings it contains. */
internal fun parseJapicmpXmlFindings(
    module: String,
    xmlFile: File,
): List<ApiFinding> {
    val document =
        DocumentBuilderFactory
            .newInstance()
            .apply { isNamespaceAware = false }
            .newDocumentBuilder()
            .parse(xmlFile)
    return document.getElementsByTagName("class").elements().flatMap { it.classFindings(module) }
}
