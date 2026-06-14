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

package space.arim.dazzleconf.engine;

import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf.ConfigurationDefinition;

/**
 * A context for serialization.
 * <p>
 * <b>Library implementor</b>
 * <p>
 * Instances of this type are implemented by the library and supplied where relevant. It must not be implemented by
 * library consumers, as new methods may be added in the future, and user implementations might expose themselves to
 * {@code NoSuchMethodError}s if they interoperate with more up-to-date code.
 */
public interface SerializeContext extends ConfigurationDefinition.WriteOptions, OperationContext<SerializeContext> {

    /**
     * Creates a new output serializer.
     * <p>
     * Typically, only one output serializer is required and may be re-used. Multiple instances may be needed for
     * advanced situations like parallel processing.
     *
     * @return the output
     */
    @NonNull SerializeOutput newOutput();

}
