package org.jetbrains.dotnet.discovery

import org.jetbrains.dotnet.discovery.data.Source
import java.nio.file.Path
import java.nio.file.Paths

class NuGetConfigDiscoverer(
     val deserializer: NuGetConfigDeserializer
) {
     fun discover(path: Path, projectStreamFactory: ProjectStreamFactory): Sequence<Source> {
        var result = emptySequence<Source>()

        var currPath: Path? = Paths.get(".").resolve(path)
        while (currPath != null) {
            val possiblePath = currPath.resolve("nuget.config")
            result += deserializer.deserialize(possiblePath, projectStreamFactory)
            currPath = currPath.parent
        }
        return result.distinct()
    }

}
