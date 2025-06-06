

## Custom Types

### Type Liaison

For advanced handling, implement a `TypeLiaison`. These liaisons are far more powerful than single-type serializers. 

### TypeLiaison

A `TypeLiaison` matches a type `V` and returns a `TypeLiaison.Agent<V>` to handle it. It can do the following:

* Scanning for annotations on the requested type (*TYPE_USE* annotations)
* Accessing generic arguments, e.g. getting the `T` argument from a `MyType<T>`
* Performing complex type matching
* Obtaining dependent serializers
* Updating the source data if there is a more appropriate representation

All of these capabilities are available through various APIs, where suitable.

**Source code examples**

Example 1: `ListLiaison` captures any `List<T>`, accesses the `T` parameter, and handles elements of that list based on a serializer for `T`.

Example 2: `EnumLiaison` works with any enum type, so long as `Class#isEnum` returns true.

### Default type liaisons, and overriding

The default type liaisons cover strings, primitives, enums, and collections. If you use `Configuration.defaultBuilder`, these default type liaisons will be added automatically.

If you want to override these default type liaisons to provide your own handling, you have a few options:
* Use `new ConfigurationBuilder` directly.
  * By using the constructor, you get a `ConfigurationBuilder` which is empty (no liaisons are part of it).
  * After construction, you can add liaisons as you wish. The default liaisons are located in `space.arim.dazzleconf.engine.liaison` if you wish to pick and choose the ones you want.
* Adding more type liaisons.
  * By design, liaisons added *later* will override liaisons added *earlier*.
  * Thus, if you add a type liaison for type `T`, but there is already a liaison for `T`, your liaison will be called first.
  * Note that this behavior is dynamic and depends on whether the liaison returns a null `TypeLiaison.Agent`. This enables you to do things like override existing liaisons "partially." For example, maybe you want to override behavior for `@MyAnnote T` but not for all `T`; by arranging the liaisons in the correct order, this works.

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

### Migrations

The migrations API is an optional extension that lets you detect past configuration data and update it to the latest version. You can use different configuration interfaces in the same project to represent each version.

Before you use the migrations API, **please keep in mind**: your code must be able to correctly differentiate past configuration versions from the current version. If you don't, then every time the configuration is loaded, the migration will run - and the migration will never stop running. This bug can be extremely annoying and a source of confusion for developers and users.

Thus, your migrations must **NOT** detect the latest version as a previous version. To ensure this, you can use a `int configVersion()` method, and check it by adding a migration filter (`MigrateSource.Filter`).

Please read the package javadoc of `space.arim.dazzleconf.migration` for more information.

### Custom instantiation

Since version 2 of this library, it is possible to swap out the instantiatior used to make instances of the configuration interface and its subsections. The default implementation uses standard proxy reflection, but users can implement their own `Instantiator` and, for example, generate class files at runtime using ASM or ByteBuddy. An implementation for ASM would be a welcome addition and could be the subject of a future PR or feature addition.

Note that configuration objects created by different `Instantiator`s will never be equal (see "Configuration Objects > Equality"). Implementors are required to uphold this contract.

### Class binaries

Per the Java Language Specification, class file binaries are permitted to have methods with the same name and parameters, but different return types. For example:

```java
Player[] getPlayers();

Collection<Player> getPlayers();
```

The above code is "legal" if it exists in binary form (not source form). However, for use with this library, this pattern is rejected. Methods that have the same name and parameters - but different return types -- must be either synthetic or bridge methods. In other words, this library follows the same constraints as for source code.



