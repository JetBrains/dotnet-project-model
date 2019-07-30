package org.jetbrains.dotnet.discovery

import org.jetbrains.dotnet.discovery.data.Solution
import java.nio.file.Path

interface SolutionDeserializer {
    fun accept(path: Path): Boolean

    fun deserialize(path: Path, projectStreamFactory: ProjectStreamFactory): Solution
}