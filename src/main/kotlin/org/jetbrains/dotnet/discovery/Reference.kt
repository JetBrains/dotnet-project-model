package org.jetbrains.dotnet.discovery

data class Reference(val id: String, val version: String) {
    constructor(pair: Pair<String, String>) : this(pair.first, pair.second)

    companion object {
        const val DEFAULT_VERSION = "*"
    }
}