
First, you'll need to add the dependency to your project.

### Dependency Information

The artifact you choose will depend on which configuration format you desire. In this example, I'll use YAML which relies on the SnakeYaml library.

**Maven Example**

```xml
<dependency>
	<groupId>space.arim.dazzleconf</groupId>
	<artifactId>dazzleconf-yaml</artifactId>
	<version>2.0.0-M3</version>
</dependency>
```

**Gradle Example**

```
dependencies {
    implementation 'space.arim.dazzleconf:dazzleconf-yaml:2.0.0-M3'
}
```

### Version

The latest version of DazzleConf may be slightly more up-to-date than the one on this page.

[![Maven Central](https://img.shields.io/maven-central/v/space.arim.dazzleconf/dazzleconf-parent?color=brightgreen&label=Latest%20Version)](https://mvnrepository.com/artifact/space.arim.dazzleconf/dazzleconf-core)

### Available formats

You only need to declare a dependency on the format you choose. There is a transitive dependency on dazzleconf-core.

For your information, the implementation library is listed here, but note that it is an implementation detail and can change. The implementation library is shaded and repackaged into the DazzleConf namspace (e.g. space.arim.dazzleconf.yaml.libs.snakeyaml_engine), so you do not have to worry about it.

| Format | Artifact                                 | Implementation library                                                                                 |
|--------|------------------------------------------|--------------------------------------------------------------------------------------------------------|
| HOCON  | `space.arim.dazzleconf:dazzleconf-hocon` | [lightbend/config](https://github.com/lightbend/config) with in-repo patches for ordering + efficiency |
| TOML   | `space.arim.dazzleconf:dazzleconf-toml`  | [JToml](https://github.com/WasabiThumb/jtoml)                                                          |
| YAML   | `space.arim.dazzleconf:dazzleconf-yaml`  | [SnakeYaml-Engine](https://bitbucket.org/snakeyaml/snakeyaml-engine)                                   |

### Snapshot dependencies

If you want to use a version which ends in `-SNAPSHOT`, you will need to add the OSSRH repository.

With Maven:

```xml
<repository>
  <id>ossrh</id>
  <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
</repository>
```

With Gradle:

```
repositories {
    maven {
        url "https://oss.sonatype.org/content/repositories/snapshots/"
    }
}
```

### Kotlin

If you're using Kotlin, you need to enable the `-Xemit-jvm-type-annotations` option in the Kotlin compiler.

This option ensures that the Kotlin compiler will correctly emit annotations! It really should be enabled by default. Here's an example of enabling it using `build.gradle.kts`:

```
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.set(
            listOf(
                "-Xemit-jvm-type-annotations"
            )
        )
    }
}
```

### Shading and Relocation

In order for your project to work at runtime, you'll need to shade this library using your build system.

With Maven, that would be the maven-shade-plugin. With Gradle, shadowJar.

I won't go over the specifics of shading because it is assumed you already know that.

**Relocation**

Relocating `space.arim.dazzleconf` is critical in some environments.

If you fail to relocate, your software can conflict with other software that also shades the same library. This is relevant for plugin environments (e.g. Bukkit plugins) where multiple Java programs are expected to work together in harmony.

**DazzleConf versions before 2.0**

If you're still using version 1.x of the library, there are additional transitive dependencies. They should also be shaded and/or relocated, and it is your responsibility to handle them. For example, `dazzleconf-ext-gson` depends on the Gson library, and it's your decision how to satisfy this dependency.
