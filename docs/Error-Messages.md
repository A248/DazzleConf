
There are two kinds of errors: mistakes by the user configuring the file, and mistakes by the developer writing code.

DazzleConf seeks to minimize user AND developer errors, while smoothening them where they occur.

# User Errors

DazzleConf attempts to be maximally user friendly. To that end, error messages are supposed to be human readable -- even by non-developers.

The emphasis on user friendliness means that server administrators, IT managers, and new/onboarding developers can all fix mistakes in the configuration.

## Error List

Not all errors are listed here, and we might add/remove some in the future. Also, any software that uses DazzleConf  can provide additional error messages -- which can't be listed here.

### Wrong type

*Work in progress: More documentation will be added here later.*

### Missing value

This happens if the configuration doesn't have default values attached to it, but the user deleted a key/value pair that was supposed to exist.

The only way to fix this is to re-add the key-value pair.

## Translation

Error messages are automatically translated based on the library language.

### Language selection

By default, the library language is based on the JVM's language setting, `Locale.getDefault()`. This means that most software using DazzleConf will automatically detect the host language.

The host language reflects the system's `LANG` environment variable. To use a different error message locale, you can change that variable:

```java
export LANG=fr_FR.UTF-8
java -jar myprogram.jar
```

As a developer, you can also override the locale used to generate error messages. Call `ConfigurationBuilder#locale` with the locale of your choice.

### Unsupported locales

If a locale is not supported, you will see English error messages.

### Contributing locales

If your locale isn't supported, you can open a PR to add a translation for it.

Translation PRs are highly appreciated, and they benefit everyone who uses the language. Your fellow speakers will thank you for it, including users of *other software* that uses DazzleConf.

To contribute a locale, please open `core/src/main/java/space/arim/dazzleconf/internals/lang`. There you will find existing language files. You should copy one of them, change the messages, and follow the instructions in `ReadMe.java` to add your translation to the library.

# Developer Errors

These errors are related to the usage of DazzleConf in source code. They're thrown as plain exceptions.

Fortunately, the library design makes it easy to automatically test for developer errors, then correct them.

## Error List

Our configuration library doesn't perfectly map to the Java type system, which means that you can sometimes do silly things by accident. We try to list comon error messages here, in alphabetical order of the message itself.

### Configuration method cannot have parameters

DazzleConf only handles functions with no arguments. You can only use functions with arguments if you want to use their default implementation, in which case `@CallableFn` is required.

### Configuration method is marked with @CallableFn, but it is not a default method

If you use `@CallableFn` on an interface method without a default exception, an error will be thrown.

### Cycle detected

The library detects circular dependencies caused by type liaisons depending on each other.

The library detects circular type hierarchies caused by return values. For example, if you return the top-level configuration interface from an method inside a sub-section.

### Default method returned null

Null is not an allowed value to return. If you want to use optional configuration entries, please use `Optional<YourType>` and return an empty optional.

### Failed to resolve agent. Please add a TypeLiaison or serializer for this type

Happens if you used a custom type as a return value in your configuration interface, but forgot to register a type liaison.

A frequent mistake is to forget `@SubSection` on configuration sub-sections.

Solution: register an appropriate type liaison for your custom type.

## An error-free life

Here is the recommend way to solve developer errors. Don't catch `DeveloperException` at runtime, which is pointless and won't help you.

Instead, see below for how to test your configuration, by incorporating automatic testing into your build.

## Prevent developer errors, through automatic testing

It's easy to prevent mistakes, provided you are familiar with a test framework.

Here, we outline a couple tests that you can easily implement. In practice, these tests catch >90% of developer mistakes.

### Load the configuration definition

Incorporate a test into your build that loads the configuration. This will check that all return types have type liaisons registered, for example.

```java
public class ConfigTest {
    
    @Test
    public void loadDefinition() {
        // Assume you have a configuration interface `MyConfig`
        // Assume you have a static method that returns a `Configuration<MyConfig>` from your code
        assertDoesNotThrow(() -> ConfigFactory.defineConfig());
    }
}

// Let's assume this class is your regular code
public class ConfigFactory {
    public static Configuration<MyConfig> defineConfig() {
        // ...
    }
}
```

### Load and write the default values

Unless you're using DazzleConf without default values (which is rare), you should add this test. It will ensure you've provided default values baked into your configuration interface.

Building off the previous example, we can load the default values, serialize them, and reload them.

```java
public class ConfigTest {
    
    @Test
    public void loadDefinition() {
        // Assume you have a configuration interface `MyConfig`
        // Assume you have a static method that returns a `Configuration<MyConfig>` from your code
        assertDoesNotThrow(() -> ConfigFactory.defineConfig());
    }
    
    @Test
    public void reloadDefaults() {
        Configuration<MyConfig> configuration = ConfigFactory.defineConfig();
        MyConfig defaultValues = assertDoesNotThrow(configuration::loadDefaults);
        DataTree.Mut dataTree = new DataTree.Mut();
        assertDoesNotThrow(() -> configuration.writeTo(defaultValues, dataTree));
    }
}
```

This ensures that your default values are still valid even after reading/writing from the file system. It also helps test your type liaisons, if you use any custom types.

### Manually testing

Manually testing should basically never be necessary. However, if you discover a bug in DazzleConf through your testing, please report it to us, and we will fix it promptly.
