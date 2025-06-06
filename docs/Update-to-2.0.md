
This guide is your one-stop shop for all matters related to version 2 and upgrading from version 1.

### Introduction

When I first wrote DazzleConf, I was amazed at the way it came to life. I'd rounded off a few hard edges, covered up the gross internals, and wrote a fun, usable API. The result is what you see: DazzleConf 1.0, a cute little library that did its job. However, the API was mainly designed around simple loading, with primitives and strings in mind. Custom types were additional but not central, and the library had a couple quirks like over-using annotations.

DazzleConf 2.0 is a complete rewrite, and it allowed me to re-organize and build an API-driven model. The outcome is far more powerful, beautiful, and expansive. It is, I hope, both conceptually simpler and ergonomically superior.

Like any API, however, DazzleConf 2.0 requires some time to become acquainted with it. This is a guide to help you migrate.

## Transitioning

Here, we describe some releases that make moving from the 1.x to 2.x APIs somewhat smoother.

### Version 1.3.0

This is a normal DazzleConf 1.x release. It has little relation to version 2.0, but it deprecates three features (value validators, @ConfSerialisers, and dotted paths in @ConfKey) in advance of their removal.

### Version 1.4.0

This special version forks the main artifact, and it provides 1.x and 2.x APIs side-by-side. It splits packages into the following:

* `space.arim.dazzleconf` - 1.x APIs
* `space.arim.dazzleconf2` - 2.x APIs, with the package name changed.

Pros:
* Lets you use both versions at the same time.
  * Can be helpful if you want to use version 2's *migrate* feature to upgrade existing config files on disk.
  * Can be helpful if you want the same configuration interface to work with both APIs.

Cons:
* Leaves you with the technical debt of having to rewrite package references from `space.arim.dazzleconf2` to `space.arim.dazzleconf` when you do finally upgrade to 2.x
* Can be confusing if you are developing with this version, since importing class names will pull in both 1.x and 2.x APIs.
  * These APIs are NOT compatible with each other, so annotations from one API may silently fail in the other.

Using the migrate feature is a surefire way to overcome library upgrade difficulties without hidden bugs (rather than having to ensure that your code works exactly the same). That's the main purpose of using this version. Other than migrations, however, using this special version is not encouraged, and you might as well update to version 2.0 (fully) anyway.

### Version 2.0

This is DazzleConf 2.0. Once you are using this artifact successfully, you have fully transitioned to DazzleConf 2.

# Migrating library usage

Each section below describes a changed library feature and how to adapt to it.

## Type handling

In 1.x, the `ValueSerialiser` interface handles serialization for a single type, without generic parameters. Basic types were built-in, like primitives, String, enums, and collections.

2.0 introduces `TypeLiaison`, which is a fully capable handler for a type. Every type has a liaison. Not only does `TypeLiaison` support wildcards and generics, it can also provide default values, depend on other liaisons, and load configuration subsections. Liaisons can check annotations, too. Please see the [API guide](API-guide.md) for more information.

This has many implications:

* Serialization is just *one* part of a liaison's responsibilities. A liaison can do other things like provide default values.
* There are no built-in types. Every type uses a liaison.
* Default methods can provide default values.

We cover each of these below.

### Serialization and deserialization

Serialization features several differences:
 * Version 2.0 uses the interface `SerializeDeserialize`. This is the closest equivalent to 1.x's `ValueSerialiser`.
 * A serializer must load its dependencies up-front. A serializer is only able to request other serializers at construction time, and *not* during the serialization process itself. This differs from FlexibleType, which let you request *any type* at any time.

*Quickest resolution:* If you want an easy path with the least changes, you can use `ConfigurationBuilder#addSimpleSerializer` which is the closest 1.x-equivalent in the 2.0 API.

### No more built-in types

In version 2.0, using `Configuration.defaultBuilder` will automatically add the default type liaisons: strings, primitives, enums, and collections are covered. Version 2.0 also  lets you override these default liaisons: see the [API guide on overidding](API-guide.md#default-type-liaisons-and-overriding).

Because of this, you can no longer use methods like `FlexibleType#getInt` or `#getBoolean`. You have to depend on the other serializer by using `handshake.getOtherSerializer` inside your TypeLiaison implementation.

### Default methods

In 1.x, default methods were ignored in configuration interfaces. Calling a default method on an instance would use the default implementation.

In 2.x, if you want default methods to be called as-is (and not treated like configuration entries), you should use `@CallableFn`. Otherwise, default methods will provide default values.

*Quickest resolution:* Annotate all default methods with `@CallableFn`.

## High-Level

### No more `ConfigurationOptions`

Here are replacements for each setting:
* `validators` - value validation should be folded into the deserialization process itself.
* `strictParseEnums` - enums are no longer built-in types; instead, you can override the default enum liaison. See the javadoc of `ConfigurationBuilder` for more details.
* `createSingleElementCollections` - no replacement, but you can implement this logic by writing your own type liaison.
* `sorter` - no easy replacement, but can be replicated. See below.

### ConfigurationHelper class

Congrats, this functionality is now baked into the core API with `configureOrFallback`. It makes everything more usable.

### Annotation changes

The following annotations have changed name:
* `@ConfComments` -> `@Comments`
* `ConfDefault.@DefaultXXX` -> `@XXXDefault` where XXX is Integer, Boolean, etc. Note that the new annotations also have to be paired exactly with the type requested.

The following annotations have been removed without replacement:

* `@ConfKey`
  * Outmoded in version 2.0. This mainly existed to help map method names to format keys, e.g. `boolean myMethod() -> my-method: true` for YAML.
  * Instead of using this annotation, rename the interface method or use key-mapping where appropriate.
* `@ConfSerialisers`
* `@ConfValidator`
* `@CollectionSize`

## Design overhauls

### Errors

Instead of the silly mess with `BadValueException`, 2.x uses `LoadResult` which is a result-container object. It's more suitable to this library than throwing a checked exception, and it lets us attach context details more ergonomically.

### Format backends

Version 1.0 used factories like `SnakeYamlConfigurationFactory` as the main entry point. This API was tightly coupled and suffered from mis-organization of concerns. Separation of API concepts did not occur in the right places.

In version 2.0, adding support for a new configuration format is as simple as implementing `Backend` and plugging it in. See `TomlBackend` for an easy-to-understand example.

### Sorting

The `ConfigurationSorter` interface has been removed. 1.x used this interface to sort configuration methods after they were identified.

In 2.x, we are aiming to do something better: maintain order at every step of the way, so that sorting later is unnecessary. However, this is a difficult effort. Thus, order is still something that version 2.0 is working on:

* The backend (YAML, HOCON, TOML) needs to provide consistent order.
  * So far, only YAML preserves order both ways (reading + writing).
  * HOCON preserves order while writing, but when reading, the data is loaded in random order. We are working to solve this and looking for a maintainable solution.
* The reflection mechanism needs to provide consistent order. By default, this is `Class.getDeclaredMethods()`, which has undefined order. However, we have made the reflection mechanism swappable (ConfigurationBuilder#instantiator), so you could theoretically override this to provide order for returned methods.

Thus, while `ConfigurationSorter` doesn't exist anymore, you could provide your own ordering by ensuring 1) you use YAML 2) you override the `Instantiator` used to scan configuration interfaces.

Please see the class javadoc of 2.0's `Configuration` for more details on ordering.
