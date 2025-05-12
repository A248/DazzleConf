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

import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf2.backend.DataEntry;

import java.lang.annotation.*;
import java.util.List;

/**
 * Adds comments to a configuration entry.
 * <p>
 * This annotation can be repeated multiple times to specify comments in different locations. Please note that
 * writing this annotation multiple times for the same location will simply result in that location being overidden.
 *
 */
@Repeatable(Comments.Container.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Comments {

    /**
     * The comments themselves. Each value in the array will be placed on a new line.
     *
     * @return the comments to add
     */
    @NonNull String @NonNull [] value();

    /**
     * Where the comments should be located.
     * <p>
     * This function will set the comments on the corresponding data entry via
     * {@link DataEntry#withComments(CommentLocation, List)} using the given location. If no location
     * is specified, the default is {@link CommentLocation#ABOVE}
     *
     * @return where to place the comments
     */
    CommentLocation location() default CommentLocation.ABOVE;

    /**
     * The repeatable annotations container for adding multiple comments.
     * <p>
     * Users do not need this directly and relying on the repeatability of {@link Comments} is far more ergonomic.
     *
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Container {

        /**
         * The values in this container. You shouldn't need to use this at all because {@link Comments} is a
         * repeatable annotation.
         *
         * @return the comments contained within this container
         */
        @NonNull Comments @NonNull [] value();

    }

}
