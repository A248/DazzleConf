/**
 * Defines standard annotations to control the behaviour of configuration entries. <br>
 * <br>
 * The most common annotations used will be entry attributes: {@link ConfKey}, {@link ConfComments}, and annotations
 * in {@link ConfDefault} all describe a property of a configuration entry.  {@link SubSection} defines a config entry
 * as a nested configuration section. <br>
 * <br>
 * Next are validators. {@link NumericRange} and {@link IntegerRange} specify the range of a numeric type,
 * while {@link CollectionSize} controls the size of some collection. For finer grained validation, {@link ConfValidator}
 * may be specified. <br>
 * <br>
 * There are only 2 annotations applying to a configuration interface rather than individual entries: {@link ConfHeader}
 * and {@link ConfSerialisers}. The former adds a comment header, and the latter attaches additional {@link ValueSerialiser}
 * implementations used specifically for the target configuration interface.
 * 
 */
package space.arim.dazzleconf.annote;

import space.arim.dazzleconf.serialiser.ValueSerialiser;