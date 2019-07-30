package org.jetbrains.dotnet.discovery

import org.jetbrains.dotnet.common.normalizeSystem
import org.jetbrains.dotnet.common.toUnixString
import org.jetbrains.dotnet.discovery.data.Solution
import java.nio.file.Path
import java.util.regex.Pattern

class MSBuildSolutionDeserializer(
    private val _readerFactory: ReaderFactory,
    private val _msBuildProjectDeserializer: SolutionDeserializer
) : SolutionDeserializer {
    override fun accept(path: Path): Boolean = PathPattern.matcher(path.toUnixString()).find()

    override fun deserialize(path: Path, projectStreamFactory: ProjectStreamFactory): Solution =
        projectStreamFactory.tryCreate(path)?.use {
            _readerFactory.create(it).use {
                val projects = it
                    .readLines()
                    .asSequence()
                    .mapNotNull { ProjectPathPattern.matcher(it) }
                    .filter { it.find() }
                    .flatMap {
                        val projectPath = getProjectPath(path, Path.of(it.group(1)))
                        if (_msBuildProjectDeserializer.accept(projectPath)) {
                            _msBuildProjectDeserializer.deserialize(projectPath, projectStreamFactory)
                                .projects.asSequence()
                        } else {
                            emptySequence()
                        }
                    }
                    .distinctBy { it.project }
                    .toList()

                Solution(projects, path.toUnixString())
            }
        } ?: Solution(emptyList())

    fun getProjectPath(basePath: Path, path: Path): Path {
        val baseParent = basePath.normalizeSystem().parent ?: Path.of("")
        return baseParent.resolve(path.normalizeSystem())
    }

    private companion object {
        private val ProjectPathPattern = Pattern.compile(
            "^Project\\(.+\\)\\s*=\\s*\".+\"\\s*,\\s*\"(.+)\"\\s*,\\s*\".+\"\\s*\$",
            Pattern.CASE_INSENSITIVE
        )
        private val PathPattern: Pattern = Pattern.compile("^.+\\.sln$", Pattern.CASE_INSENSITIVE)
    }
}