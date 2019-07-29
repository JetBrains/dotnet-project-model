package org.jetbrains.dotnet.discovery

import com.google.gson.annotations.SerializedName

class JsonAssetsProjectDto(
    @SerializedName("targets") val targets: Map<String, Map<String, PackageDto>>?,
    @SerializedName("project") val project: ProjectDto?
)

class PackageDto(
    @SerializedName("dependencies") val dependencies: Map<String, String>?
)

class ProjectDto(
    @SerializedName("restore") val restore : RestoreDto?,
    @SerializedName("frameworks") val frameworks: Map<String, DependenciesDto>?
)

class DependenciesDto (
    @SerializedName("dependencies") val dependencies:  Map<String, Any>?
)

class RestoreDto(
    @SerializedName("projectPath") val projectPath : String?,
    @SerializedName("sources") val sources : Map<String, Any>?
)
