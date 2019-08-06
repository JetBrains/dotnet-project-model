package org.jetbrains.dotnet.discovery

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.jetbrains.dotnet.common.toNormalizedUnixString
import org.jetbrains.dotnet.common.toSystem
import org.jetbrains.dotnet.discovery.data.*
import org.jetbrains.dotnet.discovery.data.Reference.Companion.DEFAULT_VERSION
import org.jetbrains.dotnet.discovery.data.Target
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern

class JsonAssetsProjectDeserializer(
    private val readerFactory: ReaderFactory,
    private val sourceDiscoverer: NuGetConfigDiscoverer? = null
) : SolutionDeserializer {

    private val gson: Gson = Gson()

    override fun accept(path: Path): Boolean = PathPattern.matcher(path.toNormalizedUnixString()).find()

    override fun deserialize(path: Path, projectStreamFactory: ProjectStreamFactory): Solution =
        projectStreamFactory.tryCreate(path)?.use { inputStream ->
            readerFactory.create(inputStream).use { reader ->
                val doc = try {
                    gson.fromJson(reader, JsonAssetsProjectDto::class.java)
                } catch (e : JsonSyntaxException) {
                    LOG.debug("$path contains invalid json")
                    return Solution(emptyList())
                }

                val targets = doc.targets?.keys?.map { Target(it) } ?: emptyList()
                val roots : Set<String> = doc.project?.frameworks?.values
                    ?.flatMap {
                        it.dependencies?.keys ?: emptySet()
                    }
                    ?.toHashSet() ?: emptySet()

                val references = doc.targets?.values
                    ?.flatMap { it.entries }
                    ?.map { (id, pkg) ->
                        val splinteredId = id.split("/")
                        val name = splinteredId[0]
                        val version = splinteredId.getOrNull(1) ?: DEFAULT_VERSION
                        val dependencies = pkg.dependencies?.entries
                            ?.map { (name, ver) -> Reference(name, ver) } ?: emptyList()

                        val isRoot = roots.contains(name)
                        Reference(name, version, dependencies, isRoot)
                     } ?: emptyList()

                val frameworks = doc.project?.frameworks?.keys?.map { Framework(it) } ?: emptyList()

                val fullPathToConfig = doc.project?.restore?.projectPath ?: path.toNormalizedUnixString()

                val configs = doc.project?.restore?.configs


                val sources = sourceDiscoverer?.let { discoverer ->
                    configs?.asSequence()?.flatMap { discoverer.deserializer.deserialize(Paths.get(it.toSystem()), projectStreamFactory) }?.toList()
                    ?: discoverer.discover(path, projectStreamFactory).toList()
                } ?: emptyList()

                Solution(
                    listOf(
                        Project(
                            fullPathToConfig,
                            targets = targets,
                            references = references,
                            frameworks = frameworks,
                            sources = sources
                        )
                    )
                )
            }
        } ?: Solution(emptyList())


    private companion object {
        private val LOG: Logger = LoggerFactory.getLogger(JsonAssetsProjectDeserializer::class.java.name)
        private val PathPattern: Pattern = Pattern.compile("^(.+[^\\w\\d]|)project\\.assets\\.json$", Pattern.CASE_INSENSITIVE)
    }
}