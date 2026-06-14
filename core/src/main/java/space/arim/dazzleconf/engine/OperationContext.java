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
import space.arim.dazzleconf.Configuration;
import space.arim.dazzleconf.ConfigurationDefinition;
import space.arim.dazzleconf.backend.Backend;
import space.arim.dazzleconf.backend.KeyMapper;
import space.arim.dazzleconf.backend.KeyPath;

/**
 * Parent context for serialization and deserialization
 *
 * @param <X> the type of the subinterface
 */
public interface OperationContext<X extends OperationContext<X>> extends ConfigurationDefinition.OperationOptions {

    /**
     * Gets the key mapper.
     * <p>
     * The key mapper is whichever key mapper is being used for the read or write operation.
     * <p>
     * If using {@link Configuration#configureWith(Backend)}, the key mapper may have been recommended by
     * {@link Backend#recommendKeyMapper()} even if no key mapper was set on the configuration. It is provided here
     * for purposes of handling configuration subsections.
     *
     * @return the key mapper
     */
    @Override
    @NonNull KeyMapper keyMapper();

    /**
     * Gets the absolute key path of the enclosing context.
     * <p>
     * This path will automatically include all key parts from the configuration root all the way until the current
     * entry. It is user displayable and may be included in error messages, as it is meant to reflect an actual
     * location within the document.
     *
     * @return the absolute key path
     */
    @Override
    @NonNull KeyPath keyPath();

    /**
     * Makes a child context at the given key, creating a new context taken from this one.
     * <p>
     * For example, suppose the current entry logically corresponds to a list. For each list element being deserialized
     * (or serialized), a context for that element should be used to handle the operation on it.
     * <p>
     * The {@code locIdentifier} argument provides a user recognizable string identifying where the context is located.
     * For example, list items can provide the index of the element, which will make the key path numbered.
     *
     * @param locIdentifier an identifier for the child context's location, based on {@code toString()}
     * @return the child context
     */
    @NonNull X deriveContext(@NonNull Object locIdentifier);

}
