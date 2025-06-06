
This page covers how a configuration interface is defined.

Among the components of a configuration interface, DazzleConf supports several annotations on method entries and a couple on the class itself. These annotations are documented here.

## Scanning the interface

### Inheritance

Methods inherited from parent interfaces are equally valid as those declared by the current type.

### Overriding methods

Per standard Java behavior, overriding a method *does NOT* preserve that method's annotations. You may need to re-declare annotations if you override methods in sub-classes.

### Skipping methods

If you don't want an interface method to become a configuration entry, you should place `@CallableFn` on it.

This annotation tells the library *not* to treat a method as a configuration entry.

Default methods annotated with `@CallableFn` are left as their default implementation (the library won't implement them), and they can call other methods on the interface as usual.

## Comments

Add comments to a configuration entry using `@Comments`. You can specify the annotation multiple times, either to add multiple comments, or to specify different locations.

```java
@Comments("I don't know")
@Comments("What this means")
@Comments({"What am I doing?", "Writing code..."}) // You can also use brackets to specify multiple lines
@Comments(value = "This comment is separate and will be placed below", location = CommentLocation.BELOW)
int numberOfHello();
```

### Header and footer

To specify the header and footer of a configuration interface, you can apply `@Comments` to the interface itself. The application of `@Comments` to the interface will define a comment header or footer (for the whole document). This only works for the root configuration.

### Notice on backend support

Using the `@Comments` annotation does not guarantee comments will be written to the underlying configuration format. For example, HOCON doesn't support `CommentLocation.BELOW`, so if you use it, you won't see the comment appear in the file.

# Entries

An *entry* refers to an interface method that has become a configuration entry. For each entry, we might expect some key/value in the file data.

Entries have the following properties in our library's model:

* A return value, called the *target value*
* A method name, called the *label*
* Default values:
  * A default value to use with `Configuration#loadDefaults`
  * A default value to use as a substitute, if the entry was missing in the user's file data.
  * While recommended, default values are technically not required, and you can use DazzleConf without the library being aware of default values (see below).
* Optionality
  * If the method returns `Optional<T>`, we will consider `T` the target value.
  * Optional entries can be omitted from the user perspective. An optional method will return `Optional.empty()` if the corresponding key/value pair didn't exist in the file data.

## Defaults through methods

If a `default` method exists, it can provide a default value for the configuration entry.

## Defaults through annotations

Instead of using `default` methods, you can use certain annotations to provide default values. These annotations are specific to their types, and they have to be supported by the type liaison (e.g., if you override the default type liaison for Integer, `@IntegerDefault` might not work).

If defaults through annotations are defined, they take precedence over default methods.

## Defaults from a resource file

It's possible to use this library without default values or any kind of defaults annotations. Calling one of the `Configuration#readFrom` methods will suffice, and an error result will be returned if configuration entries are missing.

Here's an example of what you can do:
* Store a resource called `config.yml` inside your jar.
* Use the backend (yaml, hocon, toml) to load the configuration data into memory.
* When loading the user's configuration, copy the resource file data into a new `DataTree`, and load the user data on top of it (overwriting the resource file data). Call `resoureDataCopy.copyFrom(userData)` where these variables are each a `DataTree`.
* Then, you can call `Configuration#readFrom`, and if entries were missing in the user data, the resource file data's backup value will be used instead. This happens because the resource file's key/value pair won't have been overwritten.

This can be helpful for users who still wish to keep a configuration file inside their jar. One use-case would be to preserve comments if the backend doesn't support writing comments. It would then be possible to write one's own handling of default values by loading that internal jar resource and keeping it around as needed.

### @StringDefault

A default value-providing annotation, which can be used as an alternative to `default` methods.

The annotation also lets you differentiate between defaults-as-defaults and defaults-as-substitutes for missing values.

### @ByteDefault, @ShortDefault, @IntegerDefault, @LongDefault, @FloatDefault, @DoubleDefault

These annotations have a similar function to `StringDefault`. A default annotation exists for primitive numeric type, and you have to use the annotation corresponding to the type you're using.

## Return types

Note that `TYPE_USE` annotations can be placed on type arguments, as well as the return type itself. For example, you can write `List<@IntegerRange(min = 1, max = 10) Integer>`, which says "give me a list of integers where each integer is between 1 and 10".

### @ByteRange, @ShortRange, @IntegerRange, @LongRange, @FloatRange, @DoubleRange

One range annotation exists for each numeric primitive. If this annotation qualifies a return type, the type liaison for this primitive will reject user input outside the specified range.

Currently, you have to use the range annotation corresponding to the type you're using. For example, `byte priority()` requires you to write `@ByteRange(min = 0, max = 20) byte priority()`.

### Sub sections

Using the `@SubSection` annotation lets you use nested configuration interfaces. Without it, DazzleConf cannot tell whether the object is supposed to be its own type, contained in a single key and value, or a nested config section.

Example of creating a sub-section:

```java
public interface MyConfig {

    @SubSection
    MySection mySubSection();

    interface MySection {
        default boolean hello() {
            return true;
        }
    }
}
```

Example using the annotation on a generic parameter:

```java
public interface MyConfig {

    interface MySection {
        default boolean hello() {
            return true;
        }
    }

    default List<@SubSection MySection> extraSections() {
        return List.of(new MySection() {
            @Override
            public boolean hello() {
                return false;
            }
        });
    }
}
```

## Labels
