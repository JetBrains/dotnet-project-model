package org.jetbrains.dotnet.discovery

import java.util.regex.Pattern

class MSBuildSolutionDeserializer(
    private val _readerFactory: ReaderFactory,
    private val _msBuildProjectDeserializer: SolutionDeserializer
) : SolutionDeserializer {
    override fun accept(path: String): Boolean = PathPattern.matcher(path).find()

    override fun deserialize(path: String, streamFactory: StreamFactory): Solution =
        streamFactory.tryCreate(path)?.use {
            _readerFactory.create(it).use {
                val projects = it
                    .readLines()
                    .asSequence()
                    .mapNotNull { ProjectPathPattern.matcher(it) }
                    .filter { it.find() }
                    .flatMap {
                        val projectPath = normalizePath(path, it.group(1))
                        if (_msBuildProjectDeserializer.accept(projectPath)) {
                            _msBuildProjectDeserializer.deserialize(projectPath, streamFactory)
                                .projects.asSequence()
                        } else {
                            emptySequence()
                        }
                    }
                    .distinctBy { it.project }
                    .toList()

                Solution(projects, path)
            }
        } ?: Solution(emptyList())

    fun normalizePath(basePath: String, path: String): String {
        val baseParent = basePath.replace('\\', '/').split('/').reversed().drop(1).reversed().joinToString("/")
        val normalizedPath = path.replace('\\', '/')
        if (baseParent.isBlank()) {
            return normalizedPath
        }

        return "$baseParent/$normalizedPath"
    }

    private companion object {
        private val ProjectPathPattern = Pattern.compile(
            "^Project\\(.+\\)\\s*=\\s*\".+\"\\s*,\\s*\"(.+)\"\\s*,\\s*\".+\"\\s*\$",
            Pattern.CASE_INSENSITIVE
        )
        private val PathPattern: Pattern = Pattern.compile("^.+\\.sln$", Pattern.CASE_INSENSITIVE)
    }
}