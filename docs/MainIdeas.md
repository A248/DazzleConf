
# Core ideas of a configuration

## Terminology

A *configuration interface* is the class which defines the configuration.

*Configuration entry* and *configuration method* both refer to a method in a configuration interface.

## Nullability - Never

By design, all objects returned from configuration methods are non-null. Why was this decision made? Well, for two main reasons:

* Code which makes heavy use of `null` is far more prone to error and code quality issues.
* From a users' perspective, what is null? How does a user write 'null' in a configuration section? What does it mean in context?

These make a compelling justification for choosing to abolish null config values.

## Thread Safety - By Design of Immutability

Configuration interfaces are immutable once loaded. They are thread safe by this virtue.

The only publicly exposed, mutable objects in DazzleConf are builder-style objects, which are documented as such.

## Validation and Type Conversion

Other configuration libraries use methods like `getInteger`, `getString`, and `getBoolean`. However, this approach has the downside that the implementation does not know the desired type beforehand. This means it must retain the original type in memory until at the least the first invocation of one of these methods. 

By taking advantage of its fundamentally different design, DazzleConf avoids this problem. Because it knows in advance what type a configuration entry should be, It performs type conversion when values are loaded, not when they are retrieved. This also means it can implement more aggressive type conversion, since any conversion operations will run once and therefore cannot be performance-sensitive.

### When does it happen?

Configuration objects are validated and converted to their desired type when a configuration is created. This has the added guarantee that a configuration object is always valid. Calling a method in a configuration interface, assuming that interface is implemented by DazzleConf, is a guarantee the value already exists and is ready to be retrieved.

### How does it work?

* Numeric types: all numeric types are interchangeable to any other. Suppose a configuration method's return type is an `int`. The user may write 0, 0.0, '0', or '0.0'. Any is acceptable, as numbers are parsed from strings, and numeric types are casted to each other.
* String types will never fail to validate. Object.toString() is used to ensure this.
* Boolean types are parsed from true/false and yes/no, ignoring case for all comparisons.
* Enums are parsed ignoring case by default. They can be parsed case-sensitively if desired.
* Collections are supported:
    * Set, List, and Collection are supported. Collection is currently implemented as Set.
    * The generic parameter of the collection is determined and used for parsing and validation.
    * Each element in the collection is parsed and validated as it would be if the element was a single value.
* Maps are supported similarly to collections. Their keys and values are processed.
* Nested configuration sections are abundantly supported via @SubSection, especially since version 1.2.0.
* Custom types require a ValueSerialiser to be specified, either in the `ConfigurationOptions`, or by `@ConfSerialisers` on the configuration interface.

### Other Info

### Default Methods

Default methods are supported. They are ignored during the serialisation process. They may be called as normally.

### Static Methods

Since version 1.2.0, static methods in config interfaces are properly ignored during the serialisation process.

