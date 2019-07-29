package org.jetbrains.dotnet.discovery

import java.nio.file.Path

interface SolutionDiscover {
    fun discover(projectStreamFactory: ProjectStreamFactory, paths: Sequence<Path>): Sequence<Solution>
}