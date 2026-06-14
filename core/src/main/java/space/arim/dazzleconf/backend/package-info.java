/*
 * DazzleConf
 * Copyright © 2026 Anand Beh
 *
 * DazzleConf is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DazzleConf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with DazzleConf. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU Lesser General Public License.
 */

/**
 * This package defines a framework for interactions between the library and the configuration format. It defines the
 * structure of data passed between them.
 * <p>
 * The classes in this package each correspond to steps in the access and mutation of data, its in-memory representation,
 * and its usage by other parts of the library:
 * <ul>
 *     <li>Data comes from a <b>data root</b> as raw bytes or characters</li>
 *     <li>It is then parsed into a {@code DataTree} by the configuration format.</li>
 *     <li>A configuration format is represented by {@code Backend} which handles reading and writing data trees by interfacing with the data root.</li>
 *     <li>Some configuration formats will provide a {@code KeyMapper}, an interface for making sure that Java method names
 *     (as used with this library) map to strings in accordance with the configuration format's recommended practices.</li>
 * </ul>
 * <h2>Configuration data</h2>
 * <p>
 * Data starts as raw user input, then it is built into an in-memory structure of trees and lists. The step between
 * these two categories is governed by the configuration format.
 * <p>
 * <b>Roots and Backends</b>
 * <p>
 * Data comes from a root source. We call this root source a 'data root.' The data root is usually a file, but it can
 * also be a <code>String</code> or raw bytes. It is represented abstractly by {@link space.arim.dazzleconf.backend.DataRoot}.
 * Root data is just bytes or string data; it is unparsed and could be full of a bajillion errors.
 * <p>
 * The {@link space.arim.dazzleconf.backend.Backend} represents the configuration format itself. An implementation
 * might exist for YAML, HOCON, TOML, .ini, .properties, or any other configuration formats that are to be supported. A
 * backend takes a data root, and it allows reading or writing to that root in the form of a data tree.
 * <p>
 * Some backends require specific roots. For example, a backend for a human-readable configuration format might
 * expect a {@code ReadableRoot}. A backend for byte serialization, in contrast, might need to use {@code BinaryRoot}
 * if the deserialized bytes do not comprise a valid string. Backend implementations can declare which root is
 * necessary in their constructor.
 * <p>
 * <b>Trees, lists, and entries</b>
 * <p>
 * Data is held in memory using a {@link space.arim.dazzleconf.backend.DataTree}. A data tree is essentially an ordered
 * map of keys to values, but protected in a way that enables internal optimizations and narrows the possible key
 * objects to strings and primitives. The values themselves can be strings, primitives, {@code DataList}s, or other
 * {@code DataTree}s.
 * <p>
 * Each value can have metadata like comments or line number attached, provided through
 * {@link space.arim.dazzleconf.backend.DataEntry}. This class guards the data type of the actual value. Because it
 * can store other trees and lists, it uses a special {@code toString()} implementation to defend against infinite
 * recursion. In fact, all three of these types piggyback on this implementation for similar protection, as well as
 * pretty formatting.
 * <p>
 * A list, or {@link space.arim.dazzleconf.backend.DataList}, is an ordered sequence of entries. These three types
 * together constitute the in-memory structure of syntactically valid configuration data.
 * <h2>Mutability model</h2>
 * <p>
 * All of the {@code DataTree}, {@code DataList} and {@code KeyPath} types in this package rely on a mutability model
 * that follows similar patterns. This model is designed for efficient access, guarded mutation, and firm control over
 * ownership.
 * <h3>Structure and deep immutability</h3>
 * <p>
 * This model makes use of subclasses which denote the mutability of the data they contain. Because mutation methods
 * are only available in the mutable variant, modifying an immutable data tree becomes statically impossible. This is
 * implemented through a triangle relationship: an umbrella parent type, a mutable variant, and an immutable variant.
 * <p>
 * At runtime, the umbrella type is typically one of the named subtypes. For example, a {@code DataTree} could be either
 * {@code DataTree.Mut} or a {@code DataTree.Immut}. This type hierarchy is not exclusive, and it is possible that
 * other concrete subclasses could exist, even if they are not exposed. Callers should thus avoid instanceof checks on
 * the main type.
 * <p>
 * The immutable variant guarantees <i>deep immutability</i>. That is, not only is the data structure itself immutable,
 * but any objects stored within it are immutable as well. For a {@code DataTree} or {@code DataList}, this means
 * ensuring that any nested trees and lists are also immutable. For a {@code KeyPath}, it means baking the character
 * sequences into strings so they cannot be modified underneath.
 * <h3>Conversion</h3>
 * <p>
 * The API provides ways to convert between the mutable and immutable variants. For the immutable variant, this is the
 * only way of obtaining a non-empty instance. The {@code intoImmut()} and {@code intoMut()} methods define this
 * convention.
 * <p>
 * If the receiver is already an instance of the target variant, it is simply returned without changes. Otherwise, a
 * copy operation logically takes place. Importantly, because the <i>deep mutability</i> guarantees differ between
 * mutable and immutable variants, conversion adopts the following semantics:
 * <ul>
 *     <li>Converting to immutable </li>
 *     <li>Converting to</li>
 * </ul>
 * For data trees and lists, this ensures that "recycling" the data structure (such as through calling
 * {@code intoImmut().intoImmut()}) will produce an object that is equal to the original. For {@code KeyPath}
 *  * every time.
 * Let's use {@code DataTree} as an example.
 * <ul>
 *     <li>An existing {@code DataTree} can be made mutable with {@link space.arim.dazzleconf.backend.DataTree#intoMut()}.
 *     If the receiver data tree is not already mutable, a new object is created and the data is copied to it. For
 *     efficiency, the data is actually copied lazily upon first mutation. If already mutable, the data tree yields itself.</li>
 *     <li>An existing {@code DataTree} can be made immutable with {@link space.arim.dazzleconf.backend.DataTree#intoImmut()}.
 *     This function moves the data into a new immutable container and poisons the old {@code DataTree} instance. Thus, it
 *     mimics a Rust-like ownership model where the caller is expected to have exclusive access to the mutable data tree,
 *     and therefore the ability to move that data to a new place. If already immutable, the data tree yields itself.</li>
 *     <li>Thanks to lazy copying, repeated conversion using the aforementioned methods has low performance impact.
 *     Copying is performed upon first mutation of a mutable instance that was created from an immutable one.</li>
 * </ul>
 * <h3>Usage</h3>
 * <p>
 * The variant model makes it easier for APIs to do more, but promise less. By using the main type of unspecified
 * mutability in API signatures, different API users can pick the guarantees that suit them. Most APIs throughout this
 * library, namely those outside this package, follow this pattern.
 * <p>
 * Implementors of methods that return unspecified mutability need to be careful that, if they return mutable instances,
 * they are comfortable with callers modifying the value. This is a problem for methods which return fixed values, but
 * not an issue for methods that <i>produce</i> new values when called.
 * <h3>Mutability</h3>
 * <p>
 * The {@code Mut} variant is not thread safe, and this <b>cannot be solved</b> even with external synchronization.
 * Because they often reuse internal structures during conversion operations, mutable variants can track state outside
 * their immediate objects. Therefore, they must <b>never</b> be sent to other threads.
 * <p>
 * The {@code Immut} variants are guaranteed to be thread safe and should be used for intrathread communication.
 *
 */
package space.arim.dazzleconf.backend;