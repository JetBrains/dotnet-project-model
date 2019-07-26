package org.jetbrains.dotnet.discovery

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import org.jetbrains.dotnet.common.toUnixString
import org.jetbrains.dotnet.discovery.Reference.Companion.DEFAULT_VERSION
import java.nio.file.Path
import java.util.regex.Pattern

class JsonProjectDeserializer(
    private val _readerFactory: ReaderFactory
) : SolutionDeserializer {

    private val _gson: Gson

    init {
        val builder = GsonBuilder()
        _gson = builder.create()
    }

    override fun accept(path: Path): Boolean = PathPattern.matcher(path.toUnixString()).find()

    override fun deserialize(path: Path, streamFactory: StreamFactory): Solution =
        streamFactory.tryCreate(path)?.use {
            _readerFactory.create(it).use {
                val project = _gson.fromJson(it, JsonProjectDto::class.java)
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
                    Reference(name, version)
                } ?: emptyList()

                Solution(
                    listOf(
                        Project(
                            path.toUnixString(),
                            configurations,
                            frameworks,
                            runtimes,
                            references
                        )
                    )
                )
            }
        } ?: Solution(emptyList())

    private companion object {
        private val PathPattern: Pattern = Pattern.compile("^(.+[^\\w\\d]|)project\\.json$", Pattern.CASE_INSENSITIVE)
    }
}