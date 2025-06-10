

## Custom Types

### Simple serializers

### Type Liaison

For advanced handling, implement a `TypeLiaison`. These liaisons are far more powerful than single-type serializers. 

**TypeLiaison capabilities**

A `TypeLiaison` matches a type `V` and returns a `TypeLiaison.Agent<V>` to handle it. It can do the following:

* Scanning for annotations on the requested type (*TYPE_USE* annotations)
* Accessing generic arguments, e.g. getting the `T` argument from a `MyType<T>`
* Performing complex type matching
* Obtaining dependent serializers, such as for sub-types
* Updating the source data if there is a more appropriate representation

All of these capabilities are available through various APIs, where suitable.

**Source code examples**

Example 1: `ListLiaison` captures any `List<T>`, accesses the `T` parameter, and handles elements of that list based on a serializer for `T`.

Example 2: `EnumLiaison` works with any enum type, so long as `Class#isEnum` returns true.

## Error Handling

The library is designed to let the caller handle errors, while still providing user-friendly error messages. There are a few error handling methods to be aware of:

* `LoadResult<T>` is the core error API of the library.
  * This type is kind of like `Optional`, but for error handling.
  * Using `LoadResult#getOrThrow` will throw an exception, with a tidy error message, if you want a quick error-handling method.
* `Configuration.configureOrFallback(Backend, ErrorPrint)` is a method to automatically load/reload the configuration, and if it fails, print an error message while using default values as a backup.
* Providing custom error handling is easy, and because the library does not use exceptions, you will never pay the performance cost of catch + rethrow operations.
## Configuration Objects

When the library has loaded and parsed configuration values, it instantiates the interface defining the configuration. That instantiated interface is the *configuration object*. It has the following properties.

### Immutability and Thread Safety

Configuration objects are immutable and therefore also thread safe.

The sole exception to immutability is the `ReloadShell` feature, which manages a hot-swappable configuration interface. Using `ReloadShelll` does not break thread safety, however, since swapping the delegate configuration is performed with an atomic write.

Otherwise, the only way to break immutability and thread safety

### Equality

Configuration objects are equal to one another if they implement the same interface types and yield the same values.

Because of this, it is strongly recommended that all user-defined types implement `equals` and `hashCode` properly. The equality of the configuration object reflects the equality behavior of its return types.

## Advanced

### Custom instantiation

Since version 2 of this library, it is possible to swap out the instantiatior used to make instances of the configuration interface and its subsections. The default implementation uses standard proxy reflection, but users can implement their own `Instantiator` and, for example, generate class files at runtime using ASM or ByteBuddy. An implementation for ASM would be a welcome addition and could be the subject of a future PR or feature addition.

Note that configuration objects created by different `Instantiator`s will never be equal (see "Configuration Objects > Equality"). Implementors are required to uphold this contract.

### Reading without defaults

It's possible to use this library without default values or any kind of defaults annotations. Calling one of the `Configuration#readFrom` methods will suffice, and an error result will be returned if non-optional configuration entries are missing.

This can be helpful for users who still wish to keep a configuration file inside their jar. One use-case would be to preserve comments if the backend doesn't support writing comments. It would then be possible to write one's own handling of default values by loading that internal jar resource and keeping it around as needed.

### Class binaries

Per the Java Language Specification, class file binaries are permitted to have methods with the same name and parameters. However, for use with this library, they must be synthetic or bridge methods and uncallable. In other words, this library follows the same constraints as for source code.



