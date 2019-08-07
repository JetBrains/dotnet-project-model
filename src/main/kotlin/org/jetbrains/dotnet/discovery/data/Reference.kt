package org.jetbrains.dotnet.discovery.data

data class Reference(
    val id: String,
    val version: String,
    val pathToFile: String,
    val dependencies: List<Reference> = emptyList(),
    val isRoot: Boolean = true) {

    companion object {
        const val DEFAULT_VERSION = "*"
    }
}