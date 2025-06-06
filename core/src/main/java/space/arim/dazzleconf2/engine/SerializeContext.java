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

package space.arim.dazzleconf2.engine;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf2.ConfigurationDefinition;
import space.arim.dazzleconf2.backend.KeyMapper;

/**
 * A context for serialization.
 *
 */
public interface SerializeContext extends ConfigurationDefinition.WriteOptions {

    /**
     * Gets the key mapper being used
     *
     * @return the key mapper
     */
    @Override
    @NonNull KeyMapper keyMapper();

    /**
     * Checks whether comments are being written on entries, and if so, where.
     * <p>
     * This method helps the serializer decide to attach comments to entries in written data trees. The result
     * of this method is a hint, and it does not have to be followed.
     * <p>
     * This function is analogous to {@link ConfigurationDefinition.WriteOptions#writeEntryComments(CommentLocation)}.
     * However, this function may not necessarily call that one (responses may be cached, or other settings might
     * influence behavior).
     *
     * @param location the location of the entry comments in question
     * @return whether comments at this location are being written
     */
    @Override
    @API(status = API.Status.EXPERIMENTAL)
    boolean writeEntryComments(@NonNull CommentLocation location);

}
