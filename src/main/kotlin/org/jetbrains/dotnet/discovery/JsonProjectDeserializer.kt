package org.jetbrains.dotnet.discovery

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import org.jetbrains.dotnet.common.toNormalizedUnixString
import org.jetbrains.dotnet.discovery.data.*
import org.jetbrains.dotnet.discovery.data.Reference.Companion.DEFAULT_VERSION
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.regex.Pattern

class JsonProjectDeserializer(
    private val _readerFactory: ReaderFactory,
    private val sourceDiscoverer: NuGetConfigDiscoverer? = null
) : SolutionDeserializer {

    private val _gson: Gson

    init {
        val builder = GsonBuilder()
        _gson = builder.create()
    }

    override fun accept(path: Path): Boolean = PathPattern.matcher(path.toNormalizedUnixString()).find()

    override fun deserialize(path: Path, projectStreamFactory: ProjectStreamFactory): Solution =
        projectStreamFactory.tryCreate(path)?.use { stream ->
            _readerFactory.create(stream).use { reader ->
                val project = try {
                    _gson.fromJson(reader, JsonProjectDto::class.java)
                } catch (e: JsonSyntaxException) {
                    LOG.debug("$path contains invalid json")
                    return Solution(emptyList())
                }

                val configurations = project.configurations?.keys?.map {
                    Configuration(it)
                } ?: emptyList()

                val frameworks = project.frameworks?.keys?.map { Framework(it) } ?: emptyList()

                val runtimes = project.runtimes?.keys?.map { Runtime(it) } ?: emptyList()

                val references = project.dependencies?.mapNotNull { (name, info) ->
                    val version = when(info) {
                        is String -> info
                        is JsonObject -> {
                            info["version"]?.asString
                        }
                        else -> null
                    } ?: DEFAULT_VERSION
                    Reference(name, version, path.toNormalizedUnixString())
                } ?: emptyList()

                val sources = sourceDiscoverer?.discover(path, projectStreamFactory)?.toList() ?: emptyList()

                Solution(
                    listOf(
                        Project(
                            path.toNormalizedUnixString(),
                            configurations,
                            frameworks,
                            runtimes,
                            references,
                            sources = sources
                        )
                    )
                )
            }
        } ?: Solution(emptyList())

    private companion object {
        private val LOG: Logger = LoggerFactory.getLogger(JsonProjectDeserializer::class.java.name)
        private val PathPattern: Pattern = Pattern.compile("^(.+[^\\w\\d]|)project\\.json$", Pattern.CASE_INSENSITIVE)
    }
}