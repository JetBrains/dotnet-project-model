package org.jetbrains.dotnet.discovery

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.jetbrains.dotnet.common.normalizeSystem
import org.jetbrains.dotnet.common.toUnixString
import org.jetbrains.dotnet.discovery.Reference.Companion.DEFAULT_VERSION
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.regex.Pattern

class JsonAssetsProjectDeserializer(
    private val readerFactory: ReaderFactory
) : SolutionDeserializer {

    private val gson: Gson = Gson()

    override fun accept(path: Path): Boolean = PathPattern.matcher(path.toUnixString()).find()

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
                        val splitedId = id.split("/")
                        val name = splitedId[0]
                        val version = splitedId.getOrNull(1) ?: DEFAULT_VERSION
                        val dependencies = pkg.dependencies?.entries
                            ?.map { (name, ver) -> Reference(name, ver) } ?: emptyList()

                        val isRoot = roots.contains(name)
                        Reference(name, version, dependencies, isRoot)
                     } ?: emptyList()

                val frameworks = doc.project?.frameworks?.keys?.map { Framework(it) } ?: emptyList()

                val fullPathToConfig = Path.of(doc.project?.restore?.projectPath?.normalizeSystem()) ?: path
                val pathToConfig = fullPathToConfig
                    .toFile()
                    .relativeToOrSelf(projectStreamFactory.baseDirectory.absoluteFile)

                val sources = doc.project?.restore?.sources?.keys?.map { Source(it) } ?: emptyList()

                Solution(
                    listOf(
                        Project(
                            pathToConfig.path,
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