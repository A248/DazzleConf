
# Contributing

## Building from Source

You will need JDK 15 (or greater), Maven, and Git.

Steps:

1. Clone the repository with git
2. Make code changes 
3. Run `mvn package`

## Pull Requests

PRs are very welcome. Of course, they will need to meet some standards.

For new features, do consider whether the feature requires or is best served by a change to this library.
Tangential features might be better implemented by another software component.

With regards to code style, it is best to:
* Avoid use of null. If necessary, use Optional for API return types.
* Minimize code in constructors.
* Make objects immutable.
* Use object-oriented programming rather than static methods.
* Generally, follow good practices for clean code.

### A Note on Toolchains

Running `mvn verify` requires that you have a toolchain for JDK 8 installed. Setting up toolchains is not difficult. Use this:

```xml
<?xml version="1.0" encoding="UTF8"?>
<toolchains>
        <toolchain>
                <type>jdk</type>
                <provides>
                        <version>1.8</version>
                </provides>
                <configuration>
                        <jdkHome>/path/to/java8/home</jdkHome>
                </configuration>
        </toolchain>
</toolchains>
```
