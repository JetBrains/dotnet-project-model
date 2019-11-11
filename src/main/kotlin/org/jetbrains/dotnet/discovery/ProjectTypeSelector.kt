package org.jetbrains.dotnet.discovery

import org.jetbrains.dotnet.discovery.data.Project

interface ProjectTypeSelector {
    fun select(project: Project): Set<ProjectType>
}