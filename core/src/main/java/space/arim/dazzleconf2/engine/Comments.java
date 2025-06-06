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

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Adds comments to a configuration entry or type.
 * <p>
 * This annotation can be repeated multiple times to specify comments in different locations. Please note that
 * writing this annotation multiple times for the same location will simply result in that location being overidden.
 * <p>
 * <b>Usage on a method</b>
 * <p>
 * This annotation is frequently applied to configuration methods. If applied, the comments will be attached to that
 * method where it is declared (due to the nature of Java inheritance, comments may need to be re-specified if the
 * method is overidden).
 * <p>
 * <b>Usage on a class</b>
 * <p>
 * This annotation can also be applied to a class. If so, the comments will be associated with that class, and they
 * will be added wherever a configuration method declares that class as its return type. If both the method-level
 * annotation and class-level annotation exist, the method-level annotation will take precedence.
 * <p>
 * Additionally, if the annotated class is a configuration interface itself, the comments specified will become
 * top-level comments in the configuration definition. Such top-level comments are passed directly to the backend.
 */
@Repeatable(Comments.Container.class)
@Retention(RUNTIME)
@Target({METHOD, TYPE})
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
     * This function will add the comments on the corresponding data entry via
     * {@link DataEntry#withComments(CommentLocation, List)} using the given location, merging with existing comments
     * at that location as necessary.
     * <p>
     * If no location is specified, the default is {@link CommentLocation#ABOVE}.
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
    @Retention(RUNTIME)
    @Target({METHOD, TYPE})
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
