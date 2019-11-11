package org.jetbrains.dotnet.discovery

import org.jetbrains.dotnet.common.toNormalizedUnixString
import org.jetbrains.dotnet.common.toSystem
import org.jetbrains.dotnet.discovery.data.Solution
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern

class MSBuildSolutionDeserializer(
    private val _readerFactory: ReaderFactory,
    private val _msBuildProjectDeserializer: SolutionDeserializer
) : SolutionDeserializer {
    override fun accept(path: Path): Boolean =
        supportedConfigs.contains(path.toSystem().toFile().extension.toLowerCase())

    override fun deserialize(path: Path, projectStreamFactory: ProjectStreamFactory): Solution =
        projectStreamFactory.tryCreate(path)?.use {
            _readerFactory.create(it).use {
                val projects = it
                    .readLines()
                    .asSequence()
                    .mapNotNull { ProjectPathPattern.matcher(it) }
                    .filter { it.find() }
                    .flatMap {
                        val projectPath = getProjectPath(path, Paths.get(it.group(1)))
                        if (_msBuildProjectDeserializer.accept(projectPath)) {
                            _msBuildProjectDeserializer.deserialize(projectPath, projectStreamFactory)
                                .projects.asSequence()
                        } else {
                            emptySequence()
                        }
                    }
                    .distinctBy { it.project }
                    .toList()

                Solution(projects, path.toNormalizedUnixString())
            }
        } ?: Solution(emptyList())

    fun getProjectPath(basePath: Path, path: Path): Path {
        val baseParent = basePath.toSystem().parent ?: Paths.get("")
        return baseParent.resolve(path.toSystem())
    }

    private companion object {
        private val ProjectPathPattern = Pattern.compile(
            "^Project\\(.+\\)\\s*=\\s*\".+\"\\s*,\\s*\"(.+)\"\\s*,\\s*\".+\"\\s*\$",
            Pattern.CASE_INSENSITIVE
        )
        val supportedConfigs = listOf("sln")
    }
}