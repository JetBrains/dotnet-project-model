# .NET project model parser

Provides project model deserializer for .NET projects: csproj files, project.json, packages.config

## Example

```kt
val solutionDiscover = SolutionDiscoverImpl(...)
val projectStreamFactory = FileProjectStreamFactory(Path.of("./project"))
val solutions = solution.discover(projectStreamFactory, paths)
```

