
First, you'll need to add the dependency to your project.

### Dependency Information

The artifact you choose will depend on which configuration format you desire. In this example, I'll use YAML which relies on the SnakeYaml library.

**Maven Example**

```xml
<dependency>
	<groupId>space.arim.dazzleconf</groupId>
	<artifactId>dazzleconf-yaml/artifactId>
	<version>2.0.0-M1</version>
</dependency>
```

**Gradle Example**

```
dependencies {
    implementation 'space.arim.dazzleconf:dazzleconf-yaml:2.0.0-M1'
}
```

### Version

The latest version of DazzleConf may be slightly more up-to-date than the one in this wiki.

[![Maven Central](https://img.shields.io/maven-central/v/space.arim.dazzleconf/dazzleconf-parent?color=brightgreen&label=Latest%20Version)](https://mvnrepository.com/artifact/space.arim.dazzleconf/dazzleconf-core)

### Available formats

You only need to declare a dependency on the format you choose. There is a transitive dependency on dazzleconf-core.

**Hocon**

Dependency: `space.arim.dazzleconf:dazzleconf-hocon`

**Toml**

Dependency: `space.arim.dazzleconf:dazzleconf-toml`

**Yaml**

Dependency: `space.arim.dazzleconf:dazzleconf-yaml`

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

### Shading and Relocation

In order for your project to work at runtime, you'll need to shade this library using your build system.

With Maven, that would be the maven-shade-plugin. With Gradle, shadowJar.

I won't go over the specifics of shading because it is assumed you already know that.

**Relocation**

Relocating `space.arim.dazzleconf` is important. Always relocate shaded dependencies when there is the possibility of a conflict.

**Transitive Dependencies**

If you're still using version 1.x of the library, there may be additional transitive dependencies. It is your responsibility to handle them.

However, since version 2.0, DazzleConf artifacts no longer expose you to third-party dependencies. It is recommended that you upgrade to 2.x so you can enjoy the full benefits of the new version.
