package org.jetbrains.dotnet.test

import org.jetbrains.dotnet.common.toNormalizedUnixString
import org.jetbrains.dotnet.discovery.MSBuildSolutionDeserializer
import org.jetbrains.dotnet.discovery.ReaderFactoryImpl
import org.jetbrains.dotnet.discovery.SolutionDeserializer
import org.jetbrains.dotnet.discovery.data.*
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.nio.file.Paths

class MSBuildSolutionDeserializerTest {
    @Test
    fun shouldDeserialize() {
        // Given
        val target = "/solution.sln"
        val path = Paths.get("projectPath/aaa.sln")
        val streamFactory = ProjectStreamFactoryStub().add(path, this::class.java.getResourceAsStream(target))
        val ctx = Mockery()
        val msBuildProjectDeserializer = ctx.mock(SolutionDeserializer::class.java)

        val solution1 = Solution(
            listOf(
                Project(
                    "projectPath1",
                    listOf(
                        Configuration("Core"),
                        Configuration("Release")
                    ),
                    listOf(
                        Framework("Netcoreapp2.0"),
                        Framework("netcoreapp1.0")
                    ),
                    listOf(
                        Runtime("win7-x64"),
                        Runtime("win-7x86"),
                        Runtime("ubuntu.16.10-x64")
                    ),
                    listOf(Reference("Microsoft.NET.Sdk", "*", "projectPath1"))
                )
            )
        )
        val solution2 = Solution(
            listOf(
                Project(
                    "projectPath2",
                    listOf(Configuration("core")),
                    listOf(Framework("netcoreapp2.0")),
                    listOf(
                        Runtime("win7-x64"),
                        Runtime("win-7x86")
                    ),
                    listOf(
                        Reference("Microsoft.NET.sdk", "*", "projectPath2"),
                        Reference("Microsoft.NET.test.sdk", "*", "projectPath2")
                    )
                )
            )
        )
        val expectedSolution =
            Solution(
                solution1.projects.plus(solution2.projects),
                path.toNormalizedUnixString()
            )

        ctx.checking(object : Expectations() {
            init {
                oneOf<SolutionDeserializer>(msBuildProjectDeserializer).accept(Paths.get("projectPath/proj1.csproj"))
                will(returnValue(true))

                oneOf<SolutionDeserializer>(msBuildProjectDeserializer).deserialize(
                    Paths.get("projectPath/proj1.csproj"),
                    streamFactory
                )
                will(returnValue(solution1))

                oneOf<SolutionDeserializer>(msBuildProjectDeserializer).accept(Paths.get("projectPath/dir2/proj2.csproj"))
                will(returnValue(true))

                oneOf<SolutionDeserializer>(msBuildProjectDeserializer).deserialize(
                    Paths.get("projectPath/dir2/proj2.csproj"),
                    streamFactory
                )
                will(returnValue(solution2))

                oneOf<SolutionDeserializer>(msBuildProjectDeserializer).accept(Paths.get("projectPath/Solution Items"))
                will(returnValue(false))
            }
        })

        val deserializer = MSBuildSolutionDeserializer(
            ReaderFactoryImpl(),
            msBuildProjectDeserializer
        )

        // When
        val actualSolution = deserializer.deserialize(path, streamFactory)

        // Then
        Assert.assertEquals(actualSolution, expectedSolution)
    }

    @DataProvider
    fun testAcceptData(): Array<Array<Any>> {
        return arrayOf(
            arrayOf("abc.sln", true),
            arrayOf("abc.sLn", true),
            arrayOf("abc122.sLn", true),
            arrayOf("ab c.sLn", true),
            arrayOf("abcPsln", false),
            arrayOf("abc.", false),
            arrayOf("abc", false),
            arrayOf("abc.proj", false),
            arrayOf(".sln", true),
            arrayOf("sln", false),
            arrayOf("", false)
        )
    }

    @Test(dataProvider = "testAcceptData")
    fun shouldAccept(path: String, expectedAccepted: Boolean) {
        // Given
        val ctx = Mockery()
        val msBuildProjectDeserializer = ctx.mock(SolutionDeserializer::class.java)
        val deserializer = MSBuildSolutionDeserializer(
            ReaderFactoryImpl(),
            msBuildProjectDeserializer
        )

        // When
        val actualAccepted = deserializer.accept(Paths.get(path))

        // Then
        Assert.assertEquals(actualAccepted, expectedAccepted)
    }

    @DataProvider
    fun testNormalizePathData(): Array<Array<String>> {
        return arrayOf(
            arrayOf("dir/abc.sln", "my.proj", "dir/my.proj"),
            arrayOf("Dir/abc.sln", "MY.proj", "Dir/MY.proj"),
            arrayOf("dir\\abc.sln", "my.proj", "dir/my.proj"),
            arrayOf("dir", "my.proj", "my.proj"),
            arrayOf("dir/abc.sln", "dir2/my.proj", "dir/dir2/my.proj"),
            arrayOf("dir\\abc.sln", "dir2\\my.proj", "dir/dir2/my.proj")
        )
    }

    @Test(dataProvider = "testNormalizePathData")
    fun shouldNormalizePath(basePath: String, path: String, expectedPath: String) {
        // Given
        val ctx = Mockery()
        val msBuildProjectDeserializer = ctx.mock(SolutionDeserializer::class.java)
        val deserializer = MSBuildSolutionDeserializer(
            ReaderFactoryImpl(),
            msBuildProjectDeserializer
        )

        // When
        val actualPath = deserializer.getProjectPath(Paths.get(basePath), Paths.get(path))

        // Then
        Assert.assertEquals(actualPath.toNormalizedUnixString(), expectedPath)
    }
}