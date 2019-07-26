package org.jetbrains.dotnet.discovery

import java.nio.file.Path

interface SolutionDeserializer {
    fun accept(path: Path): Boolean

    fun deserialize(path: Path, streamFactory: StreamFactory): Solution
}