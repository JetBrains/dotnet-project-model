package org.jetbrains.dotnet.discovery

import com.google.gson.Gson
import java.nio.file.Path

class JsonAssetsProjectDeserializer : SolutionDeserializer {

    private val gson: Gson = Gson()

    override fun accept(path: Path): Boolean =
        path.endsWith("project.assets.json")


    override fun deserialize(path: Path, streamFactory: StreamFactory): Solution {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}