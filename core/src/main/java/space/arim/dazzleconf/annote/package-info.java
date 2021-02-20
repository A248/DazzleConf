/**
 * Defines annotations which control the behaviour of configuration entries. <br>
 * <br>
 * The most common annotations used will be entry attributes: {@link space.arim.dazzleconf.annote.ConfKey},
 * {@link space.arim.dazzleconf.annote.ConfComments}, and annotations in {@link space.arim.dazzleconf.annote.ConfDefault}
 * all describe a property of a configuration entry.  {@link space.arim.dazzleconf.annote.SubSection} defines a config entry
 * as a nested configuration section. <br>
 * <br>
 * Next are validators. {@link space.arim.dazzleconf.annote.NumericRange} and
 * {@link space.arim.dazzleconf.annote.IntegerRange} specify the range of a numeric type, while
 * {@link space.arim.dazzleconf.annote.CollectionSize} controls the size of some collection. For finer grained
 * validation, {@link space.arim.dazzleconf.annote.ConfValidator} may be specified. <br>
 * <br>
 * There are only 2 annotations applying to a configuration interface rather than individual entries:
 * {@link space.arim.dazzleconf.annote.ConfHeader} and {@link space.arim.dazzleconf.annote.ConfSerialisers}. The former
 * adds a comment header, and the latter attaches additional {@link space.arim.dazzleconf.serialiser.ValueSerialiser}
 * implementations used specifically for the target configuration interface.
 * 
 */
package space.arim.dazzleconf.annote;