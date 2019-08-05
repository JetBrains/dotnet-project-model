package org.jetbrains.dotnet.common

import java.nio.file.Path

internal fun Path.toNormalizedUnixString(): String = this.normalize().toString().toSystem(true)

internal fun String.toSystem(isUnix: Boolean = !isWindows()): String {
    val separators = getSeparators(isUnix)
    return this.replace(separators.first, separators.second)
}

internal fun Path.toSystem(isUnix: Boolean = !isWindows()) =
    Path.of(this.toString().toSystem(isUnix))


private fun isWindows(): Boolean {
    return System.getProperty("os.name").toLowerCase().contains("win")
}

private fun getSeparators(isUnix: Boolean) =
    if (isUnix) {
        WINDOWS_SEPARATOR to UNIX_SEPARATOR
    } else {
        UNIX_SEPARATOR to WINDOWS_SEPARATOR
    }

private const val WINDOWS_SEPARATOR = "\\"
private const val UNIX_SEPARATOR = "/"