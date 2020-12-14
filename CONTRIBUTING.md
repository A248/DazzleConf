
# Contributing

## Building from Source

You will need JDK 11 (or greater), Maven, and Git.

Steps:

1. Clone the repository with git
2. Make code changes 
3. Run `mvn clean verify`

## Pull Requests

PRs are very welcome. Of course, they will need to meet some standards.

For new features, do consider whether the feature requires or is best served by or a change to this library.
Tangential features might be better implemented by another software component.

With regards to code style, it is best to:
* Avoid use of null. If necessary, use Optional for API return types.
* Minimize code in constructors.
* Make objects immutable.
* Use object-oriented programming rather than static methods.
* Generally, follow good practices for clean code.
