package org.jetbrains.dotnet.common

import org.apache.commons.io.FilenameUtils
import java.nio.file.Path

internal fun Path.toUnixString(): String = this.toString().normalizeSystem(true)

internal fun String.normalizeSystem(isUnix: Boolean = !isWindows()) =
    FilenameUtils.normalize(this, isUnix)

internal fun Path.normalizeSystem(isUnix: Boolean = !isWindows()) =
    Path.of(this.toString().normalizeSystem(isUnix))


private fun isWindows(): Boolean {
    return System.getProperty("os.name").toLowerCase().contains("win")
}