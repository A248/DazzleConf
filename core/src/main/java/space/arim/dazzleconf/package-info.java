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
 * DazzleConf, the library.
 * <p>
 * This is the base package for the library, and it contains most high level APIs. Excluding backend selection (YAML,
 * HOCON, etc.), using this package alone is technically sufficient to effectively and efficiently use this library.
 * <p>
 * We now present an overview of the library itself.
 * <h2>Walkthrough</h2>
 * <h3>Getting started</h3>
 * <p>
 * The default settings of a {@link space.arim.dazzleconf.Configuration} are appropriate to build, design, and handle
 * configuration interfaces using primitive types, strings, lists, and configuration sections. Check out
 * {@link space.arim.dazzleconf.Configuration#defaultBuilder(java.lang.Class)} to get running, then see that type's
 * documentation on how to use it.
 * <h3>Picking a data source</h3>
 * <p>
 * How configuration data is obtained will depend on the format you use. Each format will have its own artifact, plus
 * a {@link space.arim.dazzleconf.backend.Backend} implementation.
 * <p>
 * The backend represents the <i>format</i> itself. In turn, it depends on a data <i>root</i>, the raw data source.
 * Most backends require a {@link space.arim.dazzleconf.backend.ReadableRoot}, implying a human-readable textual format.
 * The root can be a file, for example, with {@link space.arim.dazzleconf.backend.PathRoot}.
 * <p>
 * Find your backend, combine it with the data root, then use it with the library's core interfaces.
 * <h3>Advanced usage: custom types and more</h3>
 * <p>
 * To use arbitrary types in a configuration, one must extend the library with their own <i>type liaisons</i>. A type
 * liaison covers one or more types and handles their serialization, deserialization, default values, and comment
 * handling. For a guide to type liaisons and more advanced usage, it is recommended to check out the documentation
 * pages located in the source repository:
 * <a href="https://github.com/A248/DazzleConf/tree/version2/docs">github.com/A248/DazzleConf/tree/version2/docs</a>
 * <h2>This package</h2>
 * The base package defines error handling and configuration definitions.
 * <h3>Configuration definitions</h3>
 * <p>
 * The general API-friendly type, {@link space.arim.dazzleconf.Configuration}, reflects a single configuration type,
 * ready to be used for all purposes. It is the entrypoint for the library as a whole, and it provides many convenient
 * and functional features. It is also a subtype of {@link space.arim.dazzleconf.ConfigurationDefinition}.
 * <p>
 * The difference between a {@code Configuration} and {@code ConfigurationDefinition} is that the latter contains only
 * the minimum structure needed for reading, writing, and reading with updates. As such, the latter is used by the
 * library for configuration <i>subsections</i>, such as those returned by
 * {@link space.arim.dazzleconf.engine.TypeLiaison.Handshake#getConfiguration(space.arim.dazzleconf.reflect.TypeToken)}.
 *
 * <h3>Error management</h3>
 * <p>
 * User-caused mistakes do not throw exceptions. Rather, they trigger {@link space.arim.dazzleconf.LoadResult}s to be
 * failures. If any user input causes an exception to be thrown, that is a bug and should be reported to the library
 * maintainers.
 * <p>
 * Developer-caused errors, however, do throw an exception. A {@link space.arim.dazzleconf.DeveloperMistakeException}
 * indicates a programming error related to how the library is used. For example, returning null from a configuration
 * interface's default method triggers this exception, because the library does not allow {@code null} as a default
 * value (or indeed, as a configuration value anywhere). The existence of this exception should almost always be caught
 * during unit tests.
 */
package space.arim.dazzleconf;

