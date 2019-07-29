package org.jetbrains.dotnet.discovery

data class Project(
    val project: String,
    var configurations: List<Configuration> = emptyList(),
    var frameworks: List<Framework> = emptyList(),
    var runtimes: List<Runtime> = emptyList(),
    val references: List<Reference> = emptyList(),
    val targets: List<Target> = emptyList(),
    val sources: List<Source> = emptyList(),
    val generatePackageOnBuild: Boolean = false
)