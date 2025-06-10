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
 * Version 2 of the library, provided as a separate package.
 * <p>
 * This package bundles all the libraries from version 2, along with sub-packages. Thus, it allows you to depend on
 * version 1 and version 2 side-by-side, which can be helpful for migration purposes.
 * <p>
 * However, note that version 2 itself (the version 2 artifact) will <b>NOT</b> use this package prefix. Instead,
 * the version 2 artifact uses the regular namespace (<code>space.arim.dazzleconf2</code>) and you'll need to perform
 * a refactor at a later time. Uusing an IDE, it's possible to change all usages of {@code space.arim.dazzleconf2}
 * to {@code space.arim.dazzleconf}.
 *
 */
package space.arim.dazzleconf2;

