package jetbrains.buildServer.dotnet.common

import java.io.InputStream

interface Deserializer<out T> {
    fun deserialize(inputStream: InputStream): T
}