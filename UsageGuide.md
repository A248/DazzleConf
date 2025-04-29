

## Advanced

### Custom instantiation

Since version 2 of this library, it is possible to swap out the instantiatior used to make instances of the configuration interface and its subsections. The default implementation uses standard proxy reflection, but users can implement their own `Instantiator` and, for example, generate class files at runtime using ASM or ByteBuddy. An implementation for ASM would be a welcome addition to DazzleConf and could be the subject of a future PR or feature addition.

### Reading without defaults

It's possible to use this library without default values or any kind of defaults annotations. Calling one of the `Configuration#readFrom` methods will suffice, and an error result will be returned if non-optional configuration entries are missing.

This can be helpful for users who still wish to keep a configuration file inside their jar. One use-case would be to preserve comments if the backend doesn't support writing comments. It would then be possible to write one's own handling of default values by loading that internal jar resource and keeping it around as needed.

### Class binaries

Per the Java Language Specification, class file binaries are permitted to have methods with the same name and parameters. However, for use with this library, they must be synthetic or bridge methods and uncallable. In other words, this library follows the same constraints are for source code.



