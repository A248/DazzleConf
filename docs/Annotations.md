
# Annotations

DazzleConf supports several annotations on configuration entries.

## Entry Attributes

### @ConfKey

Overrides the key of the configuration entry. If unspecified, the key is the method name. The key in @ConfKey may contain '.', the special character denoting a nested path.

### Default annotations

@DefaultBoolean, @DefaultInteger, @DefaultLong, @DefaultDouble, and @DefaultString are self-explanatory. For custom types, or for numeric types other than integer, long, and double, DazzleConf will perform the same best-attempt type conversion as it would for user input.

@DefaultBooleans, @DefaultIntegers, @DefaultLongs, @DefaultDoubles, and @DefaultStrings all specify a collection of elements of the noted type.

### @ConfComment

A repeatable annotation which adds a comment to the configuration entry.

Note that this does not guarantee a comment will be written to the underlying configuration format. Some formats do not support comments or do not support writing them. In these cases, DazzleConf has knobs, which are disabled by default, to hack comments into the configuration format.

### @SubSection

This annotation is required for nested configuration interfaces. Without it, DazzleConf cannot tell whether the object is supposed to be its own type, contained in a single key and value, or a nested config section.

## Other Annotations

### @ConfSerialiser and @ConfValidator

Both these annotations take a class as an argument. To instantiate it, DazzleConf will use either its static no-arg `getInstance()` method (if it exists) or its public no-arg constructor.

*ConfSerialiser* specifies a class implementing *ValueSerialiser*. ValueSerialiser is to be used for implementing custom types; it is concerned with deserialising and serialising from a string. URLValueSerialiser is one usable implementation serving as a good example.

*ConfValidator* specifies a *ValueValidator* applying to the configuration entry. *ValueValidator* is designed to validate a value once it has been deserialised.

### Range annotations

@CollectionSize limits the size of a collection. Minimum and maximum can be specified.

@IntegerRange and @NumericRange are similar, limiting the range of a numeric type. Both annotations are conceptually the same, and both can be used for any numeric type. The only difference is @IntegerRange specifies the minimum and maximum as a `long`, whereas @NumericRange takes `double`.
