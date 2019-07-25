package org.jetbrains.dotnet.discovery


import org.jetbrains.dotnet.common.XmlDocumentService
import org.jetbrains.dotnet.discovery.Reference.Companion.DEFAULT_VERSION
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern
import java.util.regex.Pattern.CASE_INSENSITIVE
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

class MSBuildProjectDeserializer(
    private val _xmlDocumentService: XmlDocumentService
) : SolutionDeserializer {
    override fun accept(path: String): Boolean = PathPattern.matcher(path).find()

    override fun deserialize(path: String, streamFactory: StreamFactory): Solution =
        streamFactory.tryCreate(path)?.use {
            val doc = _xmlDocumentService.deserialize(it)

            val packagesConfigPath = Paths.get(Path.of(path).parent?.toString() ?: ".", "packages.config").toString()

            val packagesConfig = streamFactory.tryCreate(packagesConfigPath)?.use {
                loadPackagesConfig(_xmlDocumentService.deserialize(it))
            } ?: emptySequence()

            val configurations = getAttributes(doc, "/Project/*[@Condition]", "Condition")
                .mapNotNull { ConditionPattern.find(it)?.groupValues?.get(2) }
                .filter { it.isNotBlank() }
                .plus(getContents(doc, "/Project/PropertyGroup/Configuration"))
                .distinct()
                .map { Configuration(it) }
                .toList()

            val frameworks = getContents(doc, "/Project/PropertyGroup/TargetFrameworks")
                .flatMap { it.split(';').asSequence() }
                .plus(getContents(doc, "/Project/PropertyGroup/TargetFramework"))
                .distinct()
                .map { Framework(it) }
                .toList()

            val runtimes = getContents(doc, "/Project/PropertyGroup/RuntimeIdentifiers")
                .flatMap { it.split(';').asSequence() }
                .plus(getContents(doc, "/Project/PropertyGroup/RuntimeIdentifier"))
                .distinct()
                .map { Runtime(it) }
                .toList()

            val references = getPackageReferences(doc, "/Project/ItemGroup/PackageReference[@Include]")
                .plus(
                    getAttributes(doc, "/Project/ItemGroup/Reference[@Include]", "Include")
                        .map {
                            val attributes = it.split(',')
                            val id = attributes.first()
                            val version = attributes
                                .mapNotNull { versionPattern.find(it)?.groupValues?.get(1) }
                                .firstOrNull() ?: DEFAULT_VERSION

                            Reference(id, version)
                        }
                )
                .plus(packagesConfig)
                .filter { it.id.isNotBlank() }
                .distinct()
                .toList()

            val targets = getAttributes(doc, "/Project/Target[@Name]", "Name")
                .distinct()
                .map { Target(it) }
                .toList()

            val generatePackageOnBuild = getContents(doc, "/Project/PropertyGroup/GeneratePackageOnBuild")
                .filter { "true".equals(it.trim(), true) }
                .any()

            Solution(
                listOf(
                    Project(
                        path,
                        configurations,
                        frameworks,
                        runtimes,
                        references,
                        targets,
                        generatePackageOnBuild
                    )
                )
            )
        } ?: Solution(emptyList())

    private fun getElements(doc: Document, xpath: String): Sequence<Element> = sequence {
        val nodes = xPath.evaluate(xpath, doc, XPathConstants.NODESET) as NodeList
        for (i in 0 until nodes.length) {
            val element = nodes.item(i) as Element
            yield(element)
        }
    }

    private fun getContents(doc: Document, xpath: String): Sequence<String> =
        getElements(doc, xpath).map { it.textContent }.filter { !it.isNullOrBlank() }

    private fun getAttributes(doc: Document, xpath: String, attributeName: String): Sequence<String> =
        getElements(doc, xpath).map { it.getAttribute(attributeName) }.filter { !it.isNullOrBlank() }

    private fun getPackageReferences(doc: Document, xpath: String): Sequence<Reference> =
        getElements(doc, xpath)
            .map { Reference(it.getAttribute("Include") ?: "", getVersion(it))}
            .filter { it.id.isNotEmpty() }

    private fun getVersion(element: Element): String {
        val rawVersion = when {
            element.hasAttribute("Version") -> element.getAttribute("Version")
            else -> element.getElementsByTagName("Version")?.item(0)?.textContent
        }
        return if (rawVersion.isNullOrBlank()) {
            DEFAULT_VERSION
        } else {
            rawVersion
        }
    }

    private fun loadPackagesConfig(doc: Document): Sequence<Reference> =
        getElements(doc, "/packages/package")
            .map { Reference(it.getAttribute("id") ?: "", it.getAttribute("version") ?: DEFAULT_VERSION) }
            .filter { it.id.isNotBlank() }

    private val xPath = XPathFactory.newInstance().newXPath()

    companion object {
        private val ConditionPattern: Regex =
            Regex("'\\$\\(Configuration\\)([^']*)' == '([^|]*)([^']*)'", RegexOption.IGNORE_CASE)
        private val PathPattern: Pattern = Pattern.compile("^.+\\.(proj|csproj|vbproj)$", CASE_INSENSITIVE)
        private val versionPattern: Regex = Regex("""^\s*Version\s*=\s*(.*)$""")
    }
}