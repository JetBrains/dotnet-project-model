package org.jetbrains.dotnet.test

import org.jetbrains.dotnet.common.XmlDocumentServiceImpl
import org.jetbrains.dotnet.discovery.*
import org.jetbrains.dotnet.discovery.Target
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class MSBuildProjectDeserializerTest {
    @DataProvider
    fun testDeserializeData(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(
                "/project-runtime.csproj",
                Solution(
                    listOf(
                        Project(
                            "projectPath",
                            emptyList(),
                            emptyList(),
                            listOf(
                                Runtime("win7-x64"),
                                Runtime("win-7x86"),
                                Runtime("ubuntu.16.10-x64")
                            ),
                            emptyList()
                        )
                    )
                )
            ),
            arrayOf(
                "/GeneratePackageOnBuild.csproj",
                Solution(
                    listOf(
                        Project(
                            "projectPath",
                            emptyList(),
                            listOf(Framework("netstandard2.0")),
                            emptyList(),
                            emptyList(),
                            emptyList(),
                            true
                        )
                    )
                )
            ),
            arrayOf(
                "/project14.csproj",
                Solution(
                    listOf(
                        Project(
                            "projectPath",
                            listOf(
                                Configuration("Debug"),
                                Configuration("Release")
                            ),
                            emptyList(),
                            emptyList(),
                            listOf(
                                Reference("nunit.engine.api", "3.0.0.0"),
                                Reference("System", "*"),
                                Reference("System.Data", "*"),
                                Reference("System.Xml", "*")
                            )
                        )
                    )
                )
            ),
            arrayOf(
                "/project.csproj",
                Solution(
                    listOf(
                        Project(
                            "projectPath",
                            listOf(Configuration("Core")),
                            listOf(Framework("netcoreapp1.0")),
                            emptyList(),
                            listOf(
                                Reference("Microsoft.NET.Sdk", "1.0.0-alpha-20161104-2"),
                                Reference("Microsoft.NET.Test.Sdk", "15.0.0-preview-20161024-02")
                            )
                        )
                    )
                )
            ),
            arrayOf(
                "/build.proj",
                Solution(
                    listOf(
                        Project(
                            "projectPath",
                            listOf(Configuration("Release")),
                            emptyList(),
                            emptyList(),
                            emptyList(),
                            listOf(
                                Target("GetNuGet"),
                                Target("Build"),
                                Target("Test")
                            )
                        )
                    )
                )
            ),
            arrayOf(
                "/project-simplified.csproj",
                Solution(
                    listOf(
                        Project(
                            "projectPath",
                            listOf(Configuration("Core")),
                            listOf(Framework("netcoreapp1.0")),
                            emptyList(),
                            listOf(
                                Reference("Microsoft.NET.Sdk", "1.0.0-alpha-20161104-2"),
                                Reference("Microsoft.NET.Test.Sdk", "15.0.0-preview-20161024-02")
                            )
                        )
                    )
                )
            ),
            arrayOf(
                "/project-frameworks.csproj",
                Solution(
                    listOf(
                        Project(
                            "projectPath",
                            emptyList(),
                            listOf(
                                Framework("net45"),
                                Framework("netstandard1.3")
                            ),
                            emptyList(),
                            listOf(Reference("Newtonsoft.Json", "10.0.3"))
                        )
                    )
                )
            )
        )
    }

    @Test(dataProvider = "testDeserializeData")
    fun shouldDeserialize(target: String, expectedSolution: Solution) {
        // Given
        val path = "projectPath"
        val streamFactory = StreamFactoryStub().add(path, this::class.java.getResourceAsStream(target))
        val deserializer =
            MSBuildProjectDeserializer(XmlDocumentServiceImpl())

        // When
        val actualSolution = deserializer.deserialize(path, streamFactory)

        // Then
        Assert.assertEquals(actualSolution, expectedSolution)
    }

    @DataProvider
    fun testAcceptData(): Array<Array<Any>> {
        return arrayOf(
            arrayOf("abc.proj", true),
            arrayOf("abcPproj", false),
            arrayOf("abc.csproj", true),
            arrayOf("abc.vbproj", true),
            arrayOf("abc3232.vbproj", true),
            arrayOf("abc.Proj", true),
            arrayOf("abc.CSproj", true),
            arrayOf("abc.VBproj", true),
            arrayOf("ab c.VBproj", true),
            arrayOf("dd/ff/abc.VBproj", true),
            arrayOf("c:\\dd\\ff\\abc.VBproj", true),
            arrayOf("abc.sln", false),
            arrayOf("abc.", false),
            arrayOf("abc", false),
            arrayOf("abc.projddd", false),
            arrayOf(".proj", false),
            arrayOf("proj", false),
            arrayOf("csproj", false),
            arrayOf("VBproj", false),
            arrayOf("   ", false),
            arrayOf("", false)
        )
    }

    @Test(dataProvider = "testAcceptData")
    fun shouldAccept(path: String, expectedAccepted: Boolean) {
        // Given
        val deserializer =
            MSBuildProjectDeserializer(XmlDocumentServiceImpl())

        // When
        val actualAccepted = deserializer.accept(path)

        // Then
        Assert.assertEquals(actualAccepted, expectedAccepted)
    }
}