package org.jetbrains.dotnet.discovery

import org.jetbrains.dotnet.common.XPathReader
import org.jetbrains.dotnet.common.XmlDocumentService
import org.jetbrains.dotnet.common.toNormalizedUnixString
import org.jetbrains.dotnet.discovery.data.Source
import java.nio.file.Path
import java.util.regex.Pattern

class NuGetConfigDeserializer(private val xmlDocumentService: XmlDocumentService) : XPathReader() {

     fun accept(path: Path) = PathPattern.matcher(path.toNormalizedUnixString()).find()

     fun deserialize(path: Path, projectStreamFactory: ProjectStreamFactory): Sequence<Source> =
        projectStreamFactory.tryCreate(path)?.use { inputStream ->
            val doc = xmlDocumentService.deserialize(inputStream)
            getElements(doc, "/configuration/packageSources/add").map {
                val name = it.getAttribute("key")
                val url = it.getAttribute("value")
                Source(name, url, path.toNormalizedUnixString())
            }
        } ?: emptySequence()

    private companion object {
        private val PathPattern: Pattern = Pattern.compile("^(.+[^\\w\\d]|)nuget\\.config$", Pattern.CASE_INSENSITIVE)
    }
}