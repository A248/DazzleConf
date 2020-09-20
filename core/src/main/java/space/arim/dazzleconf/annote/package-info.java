/**
 * Defines standard annotations to control the behaviour of configuration entries. <br>
 * <br>
 * The most common annotations used will be entry attributes: {@link ConfKey}, {@link ConfComment}, and annotations
 * in {@link ConfDefault} all describe a property of a configuration entry. {@link SubSection} defines the config value
 * as a nested configuration section. <br>
 * <br>
 * Next are simple validators. {@link NumericRange} and {@link IntegerRange} specify the range of a numeric type,
 * while {@link CollectionSize} controls the size of some collection. <br>
 * <br>
 * For finer grained serialisation and validation, including that of custom types, {@link ConfSerialiser} and
 * {@link ConfValidator} may be specified.
 * 
 */
package space.arim.dazzleconf.annote;