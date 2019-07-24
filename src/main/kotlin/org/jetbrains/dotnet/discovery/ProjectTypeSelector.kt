package org.jetbrains.dotnet.discovery

interface ProjectTypeSelector {
    fun select(project: Project): Set<ProjectType>
}