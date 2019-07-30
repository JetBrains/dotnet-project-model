package org.jetbrains.dotnet.test

import org.jetbrains.dotnet.discovery.ProjectType
import org.jetbrains.dotnet.discovery.ProjectTypeSelectorImpl
import org.jetbrains.dotnet.discovery.data.Project
import org.jetbrains.dotnet.discovery.data.Reference
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class ProjectTypeSelectorTest {
    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
            // Test
            arrayOf(create(false, "Microsoft.NET.Test.Sdk" to "1.0"), setOf(ProjectType.Test)),
            arrayOf(create(false, "microsofT.net.test.SDK" to "10.1.1"), setOf(ProjectType.Test)),
            arrayOf(create(false, "Microsoft.NET.Test.Sdk" to "1.0", "abc" to "*"), setOf(ProjectType.Test)),
            arrayOf(create(false, "abc.Microsoft.NET.Test.Sdk" to "*"), setOf(ProjectType.Unknown)),
            arrayOf(create(false, "abcMicrosoft.NET.Test.Sdk" to "1"), setOf(ProjectType.Unknown)),
            arrayOf(create(false, "Microsoft.NET.Test.Sdk.abc" to "123"), setOf(ProjectType.Unknown)),
            arrayOf(create(false, "Microsoft.NET.Test.Sdkabc" to "20"), setOf(ProjectType.Unknown)),
            arrayOf(create(false, "Microsoft.NET.Test.Sdkþ" to "4.0.4"), setOf(ProjectType.Unknown)),
            arrayOf(create(false, "abc.Microsoft.NET.Test.Sdk.abc" to "lol"), setOf(ProjectType.Unknown)),
            arrayOf(create(false, "abcMicrosoft.NET.Test.Sdkabc" to "kek"), setOf(ProjectType.Unknown)),
            arrayOf(create(false, ".Microsoft.NET.Test." to "*"), setOf(ProjectType.Unknown)),
            // Publish
            arrayOf(create(false, "Microsoft.aspnet.Abc" to "abc"), setOf(ProjectType.Publish)),
            arrayOf(create(false, "Microsoft.ASPNET.Abc" to "aaa"), setOf(ProjectType.Publish)),
            arrayOf(create(false, "Microsoft.aspnet.Abc" to "*", "abc" to "*"), setOf(ProjectType.Publish)),
            arrayOf(create(false, "Microsoft.aspnet." to "3.2.1"), setOf(ProjectType.Publish)),
            arrayOf(create(true, "Microsoft.aspnet.Abc" to "1010101"), setOf(ProjectType.Publish)),
            arrayOf(create(true), setOf(ProjectType.Publish)),
            arrayOf(create(false, "Microsoft.aspnet." to "*"), setOf(ProjectType.Publish)),
            arrayOf(create(false, ".Microsoft.aspnet.abc" to "*"), setOf(ProjectType.Unknown)),
            arrayOf(create(false, "abc.Microsoft.aspnet.abc" to "0.1"), setOf(ProjectType.Unknown)),
            arrayOf(create(false, "abcMicrosoft.aspnetabc" to "10"), setOf(ProjectType.Unknown)),
            // Mixed
            arrayOf(create(true, "Microsoft.NET.Test.Sdk" to "16.1.4", "abc" to "6.1"), setOf(ProjectType.Publish, ProjectType.Test)),
            // Empty
            arrayOf(create(false, "abc" to "*"), setOf(ProjectType.Unknown)),
            arrayOf(create(), setOf(ProjectType.Unknown))
        )
    }

    @Test(dataProvider = "testData")
    fun shouldSelectProjectTypes(project: Project, expectedProjectTypes: Set<ProjectType>) {
        // Given
        val projectTypeSelector = ProjectTypeSelectorImpl()

        // When
        val actualProjectTypes = projectTypeSelector.select(project)

        // Then
        Assert.assertEquals(actualProjectTypes, expectedProjectTypes)
    }

    private fun create(generatePackageOnBuild: Boolean = false, vararg references: Pair<String, String>): Project =
        Project(
            "abc.proj",
            emptyList(),
            emptyList(),
            emptyList(),
            references.map { Reference(it.first, it.second) },
            emptyList(),
            emptyList(),
            generatePackageOnBuild
        )
}