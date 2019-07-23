package jetbrains.buildServer.dotnet.common

import java.io.OutputStream

interface Serializer<in T> {
    fun serialize(obj: T, outputStream: OutputStream)
}