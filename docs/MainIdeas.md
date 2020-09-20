
# Core ideas of a configuration

## Terminology

A *configuration interface* is the class which defines the configuration.

*Configuration entry* and *configuration method* both refer to one of the methods specified in a configuration interface.

## Nullability - Never

By design, all objects returned from configuration methods are non-null. Why was the decision made? Well, for two main reasons:

* Code which makes heavy use of `null` is far more prone to error and code quality issues.
* From a users' perspective, what is null? How does a user write 'null' in a configuration section? What does it mean in context?

These make a compelling justification for choosing to abolish null config values.

## Thread Safety - By design

Configuration interfaces are immutable once loaded. The only publicly exposed, mutable objects in DazzleConf are builder objects.

## Validation and Type Conversion

Other configuration libraries use methods like `getInteger`, `getString`, and `getBoolean`. However, this approach has the downside that the implementation does not know the desired type beforehand. This means it must retain the original type in memory until at the least the first invocation of one of these methods.

They may also have poor conversion techniques, leading to increased user error. Some of this is because such conversion cannot be too expensive in the first place. By taking advantage of its fundamentally different design, DazzleConf avoids both these problems.

### When does it happen?

In DazzleConf, configuration objects are validated and converted to their desired type when a configuration is created. This has the added guarantee that a configuration object is always valid. Calling a method in a configuration interface, assuming that interface is implemented by DazzleConf, is a guarantee the value already exists and is ready to be retrieved.

### How does it work?

* Numeric types:
    * All numeric types are interchangeable to any other. If a user writes '0.0', for instance, in a configuration, but the configuration method's return type is an `int`, DazzleConf will convert the `double` free of charge.
    * All can be parsed from a `String`.
* String types will never fail to validate. Object.toString() is used to ensure this.
* Boolean types are parsed from true/false and yes/no, ignoring case for all comparisons.
* Collections are supported:
    * Set, List, and Collection are supported. Collection is currently implemented as Set.
    * The generic parameter of the collection is determined and used for parsing and validation.
    * Each element in the collection is parsed and validated as it would be if the element was any other value.
* Maps are supported similarly to collections, but their key type must be String.
* Custom types require a ValueSerialiser to be specified, either in the `ConfigurationOptions`, or by `@ConfSerialiser` on the entry.

All serialisation can also be overridden per key using the `@ConfSerialiser` annotation. See the annotations page for more information.

## Default Methods

Default methods are supported. They are ignored during the serialisation process. They may be called as normally.

