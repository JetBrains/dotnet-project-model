package org.jetbrains.dotnet.discovery

data class Solution(val projects: List<Project>, val solution: String = "") {
    val isSimple: Boolean = solution.isBlank()
}