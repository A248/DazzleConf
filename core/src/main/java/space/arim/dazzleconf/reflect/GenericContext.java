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

package space.arim.dazzleconf.reflect;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.TypeVariable;

/**
 * A concrete context where type variables are used. Provides the argument values of those type variables.
 *
 */
public interface GenericContext {

    /**
     * Finds the argument value of a type variable with the given name
     *
     * @param varName the type variable name
     * @return the type argument
     * @throws RuntimeException any exceptions thrown by this method are thrown by the outer method that uses
     * this type, for example {@link ReifiedType#computeFrom(AnnotatedType, GenericContext)}
     */
    @NonNull ReifiedType resolveTypeVariable(@NonNull String varName);

    /**
     * A type-level generic context.
     * <p>
     * This represents the generic variable space of a Java class, interface, enum, record, etc. It will resolve
     * input type variables to the type's generic arguments.
     *
     */
    final class OfType implements GenericContext {

        private final ReifiedType type;
        private final TypeVariable<?>[] typeVars;
        private final GenericContext fallback;

        /**
         * Creates from a type.
         * <p>
         * Uses the provided fallback context if the input type variable doesn't match any of the type variables on the provided type.
         *
         * @param type the type, with its arguments
         * @param fallback the fallback generic context
         */
        public OfType(@NonNull ReifiedType type, @NonNull GenericContext fallback) {
            this.fallback = fallback;
            this.type = type;
            TypeVariable<?>[] typeVars = type.rawType().getTypeParameters();
            if (typeVars.length != type.argumentCount()) {
                // ReifiedType.of() prevents this by blocking arbitrary construction
                throw new IllegalStateException("Malformed input type. Wrong number of arguments on " + type);
            }
            this.typeVars = typeVars;
        }

        @Override
        public @NonNull ReifiedType resolveTypeVariable(@NonNull String varName) {
            for (int n = 0; n < typeVars.length; n++) {
                if (typeVars[n].getName().equals(varName)) {
                    return type.argumentAt(n);
                }
            }
            return fallback.resolveTypeVariable(varName);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + '{' + type + '}';
        }
    }
}
