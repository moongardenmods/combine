# Combine

Lua Stub Documentation Generator for Java. Primarily used in [allium-example-script](https://github.com/moongardenmods/allium-example-script).

## Setup

This setup assumes you're using the [`fabric-loom`](https://github.com/FabricMC/fabric-loom/) plugin. If not, some slight reconfiguration of these run configs and
their arguments may be necessary.

After bringing it in as a dependency add the following to your `build.gradle.kts`:
```kotlin
register<JavaExec>("genLuaSources") {
    group = "fabric"

    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "dev.moongarden.combine.Combine"
    jvmArgs = listOf("-Dcombine.output=../docs")
    workingDir = file("run")
}

register<JavaExec>("genLuaSourcesAll") {
    group = "fabric"

    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "dev.moongarden.combine.Combine"
    jvmArgs = listOf("-Dcombine.output=../docs", "-Dcombine.ignoreAccess")
    workingDir = file("run")
}
```