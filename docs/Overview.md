
# Core ideas of a configuration

## Ideas and Terminology

A *configuration interface* is the class which defines the configuration.

*Configuration entry* and *configuration method* both refer to a method in a configuration interface.

### Nullability - Never

By design, all objects returned from configuration methods are non-null. Why was this decision made? Well, for two main reasons:

* Code which makes heavy use of `null` is far more prone to error and code quality issues.
* From a users' perspective, what is null? How does a user write 'null' in a configuration section? What does it mean in context?

These make a compelling justification for choosing to abolish null config values.

### Types and Type Safety

Configuration methods should return the same types that are used in your code. This library encourages developers to fully specify the types they need, including by adding custom types if needed.

By moving

### Immutability and Thread Safety

Configuration interfaces are immutable once loaded, and they are thread safe by this virtue.

### Validation and Type Conversion

Other configuration libraries use methods like `getInteger`, `getString`, and `getBoolean`. However, this approach has the downside that the implementation does not know the desired type beforehand. This means it must retain the original type in memory until at the least the first invocation of one of these methods. 

By taking advantage of its fundamentally different design, DazzleConf avoids this problem. Because it knows in advance what type a configuration entry should be, it performs type conversion when values are loaded, not when they are retrieved. This also means it can implement more aggressive type conversion, since any conversion operations will run once and therefore won't be performance-heavy.

## Configuration objects and their definitions

### Loading a configuration instance

A configuration interface is defined by the entries you put inside it and their properties (methods, annotations, default values).

Using the definition, a configuration is loaded. Configuration instances are validated and converted to their desired type when they are created. This has the added guarantee that a configuration object is always valid. Calling a method in a configuration interface, assuming that interface is implemented by DazzleConf, is a guarantee that the value already exists and is ready to be retrieved.

### Reading the configuration definition

The configuration interface is scanned, and type liaisons are fetched for all the return types. Some type liaisons will in turn rely on others, e.g. `List<String>` will request a liaison for `List`, and another for `String`.

A type liaison is the handler which provides serialization, deserialization, updating-in-place, and default values for the given type.

The default type liaisons cover:
* Numeric types. All primitive and boxed types.
  * From the user perspective, numeric types are interchangeable to any other, where appropriate.
    * Automatic casting from integers to decimals. Writing `4` if the return type is `double` is fully accepted.
    * Automatic casting from bigger types to smaller types, if within range. Let's say `10` got loaded as a `long`, but only a `byte` is requested: this is accepted.
  * Writing a string, by accident, is also supported. E.g., `number-of-tries: "3"` will load 3 correctly.
* Enums are parsed ignoring case. If you don't want this, you can override enum handling, just as you can override all type liaisons.
* Collections:
    * Collection, List, and Set are the usable types.
    * The generic parameter of the collection is determined and used to find a serializer for the element type.
    * Each element in the collection is loaded as it would be if the element was a single value.
* Maps work similarly to collections. They detect the generic arguments for the key and the value, get a serializer for each type, and rely on the serializers as dependencies.
* Configuration interfaces annotated with @SubSection. These are loaded like the rest of the configuration interface, and they automatically inherit liaisons from the parent configuration.

Changes from version 1.x: Maps are no longer supported in the default liaisons.

## Skipped Methods

### Default Methods - @CallableFn

If you want default methods that use their implementation -- and don't represent a configuration entry -- you should annotate the method with `@CallableFn`. This will instruct the library not to provide its own implementation, but to transparently call your default method.

Methods annotated with `@CallableFn` are left as the default implementation, and they can call other methods on the interface as usual.

### Static Methods

Static methods in config interfaces are properly ignored by the library.

