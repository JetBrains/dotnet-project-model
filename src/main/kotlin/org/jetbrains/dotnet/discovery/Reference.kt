package org.jetbrains.dotnet.discovery

data class Reference(val id: String,
                     val version: String,
                     val dependencies: List<Reference> = emptyList(),
                     val isRoot: Boolean = true) {

    constructor(pair: Pair<String, String>) : this(pair.first, pair.second)

    companion object {
        const val DEFAULT_VERSION = "*"
    }
}