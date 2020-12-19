/**
 * Base package containing classes which most usage will involve. <br>
 * <br>
 * The following advice applies to all classes in this package and sub packages,
 * as well as the format extensions provided by DazzleConf. <br>
 * <br>
 * <b>Null</b> <br>
 * Unless specified otherwise, methods do not accept null values.
 * Passing a null value may be rejected with {@code NullPointerException}. <br>
 * <br>
 * Moreover, there are no methods returning null, except a couple deprecated methods.
 * <br>
 * <br>
 * <b>Thread Safety</b> <br>
 * By virtue of immutability, most classes and interface implementations are thread safe
 * through no extra effort. However, "builder" style objects, which are mutable, have
 * no similar guarantee.
 * 
 */
package space.arim.dazzleconf;