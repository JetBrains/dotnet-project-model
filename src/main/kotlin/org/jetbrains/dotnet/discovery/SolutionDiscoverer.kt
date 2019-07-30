package org.jetbrains.dotnet.discovery

import org.jetbrains.dotnet.discovery.data.Solution
import java.nio.file.Path

interface SolutionDiscover {
    fun discover(projectStreamFactory: ProjectStreamFactory, paths: Sequence<Path>): Sequence<Solution>
}