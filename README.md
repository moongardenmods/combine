# Combine

Lua Stub Documentation Generator for Java. Primarily used in [allium-example-script](https://github.com/moongardenmods/allium-example-script).

## Setup

This setup assumes you're using the [`fabric-loom`](https://github.com/FabricMC/fabric-loom/) plugin. If not, some slight reconfiguration of these run configs and
their arguments may be necessary.

After bringing it in as a dependency add the following to your `build.gradle.kts`:
```kotlin
register<JavaExec>("genLuaSources") {
    group = "allium"

    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "dev.moongarden.combine.Combine"
    jvmArgs = listOf("-Dcombine.output=../docs")
    workingDir = file("run")
}

register<JavaExec>("genLuaSourcesAll") {
    group = "allium"

    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "dev.moongarden.combine.Combine"
    jvmArgs = listOf("-Dcombine.output=../docs", "-Dcombine.ignoreAccess")
    workingDir = file("run")
}
```

### Extensions

Combine supports extensions that can rename methods, fields, and can even disable documentation for them outright.
Extension classes must implement the `CombineExtension` interface, providing a new ClassParserExtension every time the
`createClassParser` method is invoked.

To add extensions, supply the class reference path to `-Dcombine.extensions`. To add multiple, colon separate the class 
references. Example:
```
-Dcombine.extensions=dev.moongarden.combine.TestExtensionOne:dev.moongarden.combine.TestExtensionTwo
```