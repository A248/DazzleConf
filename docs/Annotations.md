
# Annotations

DazzleConf supports several annotations on configuration entries and a couple on configuration interfaces.

## Entry Attributes

### @ConfKey

Overrides the key of the configuration entry. If unspecified, the key is the method name. The key in @ConfKey may contain '.', the special character denoting a nested path.

### Default annotations

@DefaultBoolean, @DefaultInteger, @DefaultLong, @DefaultDouble, and @DefaultString are self-explanatory. For custom types, or for numeric types other than integer, long, and double, DazzleConf will perform the same best-attempt type conversion as it would for user input.

@DefaultBooleans, @DefaultIntegers, @DefaultLongs, @DefaultDoubles, and @DefaultStrings all specify a collection of elements of the noted type.

### @ConfComments and @ConfHeader

@ConfComments adds comments to a configuration entry. @ConfHeader is placed on a configuration interface and sets the comment header of the entire configuration.

These annotations do not guarantee comments will be written to the underlying configuration format. JSON has no concept of a comment. YAML has comments but SnakeYAML does not write them. In these cases, DazzleConf has format-specific options, which are disabled by default, to hack comments into the configuration format.

### @SubSection

This annotation is required for nested configuration interfaces. Without it, DazzleConf cannot tell whether the object is supposed to be its own type, contained in a single key and value, or a nested config section.

## Other Annotations

### @ConfSerialisers and @ConfValidator

Both these annotations take a class or classes as an argument. DazzleConf will instantiate the target classes using either a static `getInstance()` method (if it exists) or the public no-arg constructor.

*ConfSerialiser* is placed on a configuration interface and specifies classes implementing *ValueSerialiser*. ValueSerialiser is to be used for implementing custom types; it is concerned with deserialisation and serialisation. URLValueSerialiser is one usable implementation serving as a good example. All the ValueSerialisers instantiated by *ConfSerialiser* will apply to the configuration entries in that configuration interface which the annotation is placed on. Note that nested config sections do not "inherit" this annotation.

*ConfValidator* is placed on a configuration entry and specifies a single class implementing *ValueValidator* applying to the entry. *ValueValidator* is designed to validate a value once it has been deserialised.

### Range annotations

@CollectionSize limits the size of a collection. Minimum and maximum can be specified.

@IntegerRange and @NumericRange are similar, limiting the range of a numeric type. Both annotations are conceptually the same, and both can be used for any numeric type. The only difference is @IntegerRange specifies the minimum and maximum as a `long`, whereas @NumericRange takes `double`.
