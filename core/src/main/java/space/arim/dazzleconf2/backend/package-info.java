/*
 * DazzleConf
 * Copyright © 2025 Anand Beh
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
 * <p>
 * <b>Roots and Backends</b>
 * <p>
 * Data comes from a root source. We call this root source a 'data root.' The data root is usually a file, but it can
 * also be a <code>String</code> or raw bytes. It is represented abstractly by {@link space.arim.dazzleconf2.backend.DataRoot}.
 * Root data is just bytes or string data; it is unparsed and could be full of a bajillion errors.
 * <p>
 * The {@link space.arim.dazzleconf2.backend.Backend} represents the configuration format itself. An implementation
 * might exist for YAML, HOCON, TOML, .ini, .properties, or any other configuration formats that are to be supported. A
 * backend takes a data root, and it allows reading or writing to that root in the form of a data tree.
 * <p>
 * Some backends require specific roots. For example, a backend for a human-readable configuration format might
 * expect a {@code ReadableRoot}. A backend for byte serialization, in contrast, might need to use {@code BinaryRoot}
 * if the deserialized bytes do not comprise a valid string. Backend implementations can declare which root is
 * necessary in their constructor.
 * <p>
 * <b>Data trees</b>
 * <p>
 * Data is held in memory using a {@link space.arim.dazzleconf2.backend.DataTree}. A data tree is essentially a map of
 * keys to values, where the values themselves can be other {@code DataTree}s. Additionally, each value can have
 * metadata like comments or line number attached, see {@link space.arim.dazzleconf2.backend.DataEntry}.
 * <p>
 * <b>Mutability models</b>
 * <p>
 * Both {@code DataTree} and {@code KeyPath} rely on a type-level mutability model that is designed for efficient data
 * access, guarded mutation, and firm control over ownership.
 * <p>
 * This model makes use of subclasses which denote the mutability of the data they contain. Because mutation methods
 * are only available in the mutable variant, modifying an immutable data tree becomes statically impossible. This is
 * implemented through a triangle relationship: an umbrella parent type, a mutable variant, and an immutable variant.
 * <p>
 * At runtime, a value of the parent type can be one of either sub-type. For example, a {@code DataTree} could be either
 * {@code DataTree.Mut} or a {@code DataTree.Immut}. The API, however, is intended to elide instanceof checks by making
 * conversions between these types logical and efficient. Let's use {@code DataTree} as an example.
 * <ul>
 *     <li>An existing {@code DataTree} can be made mutable with {@link space.arim.dazzleconf2.backend.DataTree#intoMut()}.
 *     If the receiver data tree is not already mutable, a new object is created and the data is copied to it. For
 *     efficiency, the data is actually copied lazily upon first mutation. If already mutable, the data tree yields itself.</li>
 *     <li>An existing {@code DataTree} can be made immutable with {@link space.arim.dazzleconf2.backend.DataTree#intoImmut()}.
 *     This function moves the data into a new immutable container and poisons the old {@code DataTree} instance. Thus, it
 *     mimics a Rust-like ownership model where the caller is expected to have exclusive access to the mutable data tree,
 *     and therefore the ability to move that data to a new place. If already immutable, the data tree yields itself.</li>
 *     <li>Thanks to lazy copying, repeated conversion using the aforementioned methods has low performance impact.
 *     Copying is performed upon first mutation of a mutable instance that was created from an immutable one.</li>
 * </ul>
 * <p>
 * As expected, the {@code Mut} variant is not thread safe without the presence of external synchronization.
 *
 */
package space.arim.dazzleconf2.backend;