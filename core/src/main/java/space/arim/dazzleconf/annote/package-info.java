/**
 * Defines standard annotations to control the behaviour of configuration entries. <br>
 * <br>
 * The most common annotations used will be entry attributes: {@link ConfKey}, {@link ConfComments}, and annotations
 * in {@link ConfDefault} all describe a property of a configuration entry.  {@link SubSection} defines a config entry
 * as a nested configuration section. <br>
 * <br>
 * Next are simple validators. {@link NumericRange} and {@link IntegerRange} specify the range of a numeric type,
 * while {@link CollectionSize} controls the size of some collection. <br>
 * <br>
 * For finer grained serialisation and validation, including that of custom types, {@link ConfSerialiser} and
 * {@link ConfValidator} may be specified. <br>
 * <br>
 * {@link ConfHeader} adds a header to a configuration interface. It is the only annotation applying to a type
 * rather than a method.
 * 
 */
package space.arim.dazzleconf.annote;