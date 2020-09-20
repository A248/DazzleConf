
## Configuring a Configuration library

Because the wants of users are entirely different, DazzleConf uses a `ConfigurationOptions` bean to allow customisation. ConfigurationOptions.Builder follows the builder pattern.

### ConfigurationSorter

Sorting is one of the available options. In the current implementation, config entries are serialised in order of the returned array from `clazz.getMethods()`. This may be suitable for most users and in my testing it returned the methods in declaration order in the source file. However, this is not a guaranteed detail of the spec.

An example `ConfigurationSorter` implementation, AnnotationBasedSorter, uses an `@Order` annotation to determine sorting. Other implementations are imaginable, including using a bytecode library to sort methods in declaration order. However, that it outside the scope of DazzleConf.

### Enum Parsing

By default, enums are parsed using valueOf and by uppercasing all values. However, some may want to require the user to write in uppercase.

### ValueSerialiser implementations

Custom types can be supported by using the map of types to ValueSerialisers. `URLValueSerialiser` is a simple example implementation.

### ValueValidator implementations

Validators accept the deserialised configuration value at an entry. They are specified per key in `ConfigurationOptions`, but may also be annotated on a configuration entry with `@ConfValidator`.
