package jetbrains.buildServer.dotnet.discovery

import jetbrains.buildServer.dotnet.common.DotnetCommandType

interface DiscoveredTargetNameFactory {
    fun createName(commandType: DotnetCommandType, path: String): String
}