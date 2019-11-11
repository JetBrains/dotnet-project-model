package org.jetbrains.dotnet.discovery


import org.jetbrains.dotnet.common.XPathReader
import org.jetbrains.dotnet.common.XmlDocumentService
import org.jetbrains.dotnet.common.toNormalizedUnixString
import org.jetbrains.dotnet.common.toSystem
import org.jetbrains.dotnet.discovery.data.*
import org.jetbrains.dotnet.discovery.data.Reference.Companion.DEFAULT_VERSION
import org.jetbrains.dotnet.discovery.data.Target
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.nio.file.Path
import java.nio.file.Paths

class MSBuildProjectDeserializer(
    private val _xmlDocumentService: XmlDocumentService,
    private val sourceDiscoverer: NuGetConfigDiscoverer? = null
) : XPathReader(), SolutionDeserializer {
    override fun accept(path: Path): Boolean =
        supportedConfigs.contains(path.toSystem().toFile().extension.toLowerCase())

    override fun deserialize(path: Path, projectStreamFactory: ProjectStreamFactory): Solution =
        projectStreamFactory.tryCreate(path)?.use { stream ->
            val doc = _xmlDocumentService.deserialize(stream)

            val packagesConfigPath = Paths.get(path.parent?.toString() ?: "", "packages.config")

            val packagesConfig = projectStreamFactory.tryCreate(packagesConfigPath)?.use { packageConfigDoc ->
                loadPackagesConfig(_xmlDocumentService.deserialize(packageConfigDoc))
                    .map { Reference(it.first, it.second, packagesConfigPath.toNormalizedUnixString()) }
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
                .map { Reference(it.first, it.second, path.toNormalizedUnixString()) }
                .plus(
                    getAttributes(doc, "/Project/ItemGroup/Reference[@Include]", "Include")
                        .map {
                            val attributes = it.split(',')
                            val id = attributes.first()
                            val version = attributes
                                .mapNotNull { versionPattern.find(it)?.groupValues?.get(1) }
                                .firstOrNull() ?: DEFAULT_VERSION

                            Reference(id, version, path.toNormalizedUnixString())
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

            val sources = sourceDiscoverer?.discover(path, projectStreamFactory)?.toList() ?: emptyList()

            Solution(
                listOf(
                    Project(
                        path.toNormalizedUnixString(),
                        configurations,
                        frameworks,
                        runtimes,
                        references,
                        targets,
                        sources,
                        generatePackageOnBuild
                    )
                )
            )
        } ?: Solution(emptyList())

    private fun getPackageReferences(doc: Document, xpath: String): Sequence<Pair<String, String>> =
        getElements(doc, xpath)
            .map { Pair(it.getAttribute("Include") ?: "", getVersion(it)) }
            .filter { it.first.isNotEmpty() }

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

    private fun loadPackagesConfig(doc: Document): Sequence<Pair<String, String>> =
        getElements(doc, "/packages/package")
            .map {
                Pair(
                    it.getAttribute("id") ?: "",
                    it.getAttribute("version") ?: DEFAULT_VERSION
                )
            }
            .filter { it.first.isNotBlank() }

    companion object {
        private val ConditionPattern: Regex =
            Regex("'\\$\\(Configuration\\)([^']*)' == '([^|]*)([^']*)'", RegexOption.IGNORE_CASE)
        private val versionPattern: Regex = Regex("""^\s*Version\s*=\s*(.*)$""")
        val supportedConfigs = listOf(
            "csproj",
            "vbproj",
            "vcxproj",
            "dbproj",
            "fsproj",
            "pyproj",
            "rbproj",
            "wixproj",
            "vdproj",
            "isproj",
            "pssproj",
            "modelproj",
            "proj"
        )
    }
}