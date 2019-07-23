package jetbrains.buildServer.dotnet.discovery

import jetbrains.buildServer.dotnet.common.DotnetCommandType

class DefaultDiscoveredTargetNameFactory : DiscoveredTargetNameFactory {
    override fun createName(commandType: DotnetCommandType, path: String): String =
        "${commandType.id.replace('-', ' ')} ${getPathName(path)}"

    private fun getPathName(path: String) = if (path.contains(' ')) "\"$path\"" else path
}