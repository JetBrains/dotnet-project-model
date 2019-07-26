package org.jetbrains.dotnet.discovery

import java.nio.file.Path

interface SolutionDiscover {
    fun discover(streamFactory: StreamFactory, paths: Sequence<Path>): Sequence<Solution>
}