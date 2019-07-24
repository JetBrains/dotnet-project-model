package org.jetbrains.dotnet.discovery

interface SolutionDeserializer {
    fun accept(path: String): Boolean

    fun deserialize(path: String, streamFactory: StreamFactory): Solution
}