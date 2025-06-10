
# Annotations

DazzleConf supports several annotations on configuration entries and a couple on configuration interfaces.

## Type Annotations

These annotations qualify types, especially return types. You can even place them inside generic arguments, like this: `List<@IntegerRange(min = 1, max = 10) Integer>`

### @SubSection

This annotation is required for nested configuration interfaces. Without it, DazzleConf cannot tell whether the object is supposed to be its own type, contained in a single key and value, or a nested config section.

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

    default Map<String, @SubSection MySection> extraSections() {
        return Map.of("key", new MySection() {
            @Override
            public boolean hello() {
                return false;
            }
        });
    }
}
```

### @StringDefault

A default value-providing annotation. If you prefer, you can use this annotation as an alternative to `default` methods. The annotation also lets you differentiate between defaults-as-defaults and defaults-as-substitutes for missing values.

### @ByteDefault, @ShortDefault, @IntegerDefault, @LongDefault, @FloatDefault, @DoubleDefault

These annotations have a similar function to `StringDefault`. A default annotation exists for primitive numeric type, and currently, you have to use the annotation corresponding to the type you're using. Mixing is not allowed.

### @ByteRange, @ShortRange, @IntegerRange, @LongRange, @FloatRange, @DoubleRange

One range annotation exists for each numeric primitive. Currently, you have to use the range annotation corresponding to the type you're using.

## Method Annotations

### @CallableFn

This annotation tells the library *not* to treat a method as a configuration entry.

Default methods annotated with `@CallableFn` are left as their default implementation (the library won't implement them), and they can call other methods on the interface as usual.

### @Comments

Adds comments to a configuration entry. You can specify the annotation multiple times, either to add multiple comments, or to specify different locations.

```java
@Comments("I don't know")
@Comments("What this means")
@Comments({"What am I doing?", "Writing code..."}) // You can also use brackets to specify multiple lines
@Comments(value = "This comment is separate and will be placed below", location = CommentLocation.BELOW)
int numberOfHello();
```

These annotations do not guarantee comments will be written to the underlying configuration format. For example, HOCON doesn't support `CommentLocation.BELOW`, so if you use it, you won't see the comment appear in the file.

## Class Annotations

### @Comments

This annotation can also be used on the top-level configuration section. If so, it lets you define a comment header (for the whole document) or the footer likewis.

If used on a class other than the top-level configuration interface, `@Comments` will associate the comment data with all uses of that type as a return type.
