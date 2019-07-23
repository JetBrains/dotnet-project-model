package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.common.DotnetCommandType
import jetbrains.buildServer.dotnet.discovery.DiscoveredTargetNameFactory

class DiscoveredTargetNameFactoryStub(private val _name: String) : DiscoveredTargetNameFactory {
    override fun createName(commandType: DotnetCommandType, path: String): String = _name
}