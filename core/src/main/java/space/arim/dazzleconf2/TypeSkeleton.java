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

package space.arim.dazzleconf2;

import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.engine.*;
import space.arim.dazzleconf2.reflect.MethodId;
import space.arim.dazzleconf2.reflect.MethodMirror;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Defines a single interface supertype which has not yet been instantiated
 */
final class TypeSkeleton {

    /**
     * Functions annotated with @Callable
     */
    final MethodId[] callableDefaultMethods;
    /**
     * Functions whose return values are supplied by us
     */
    final MethodNode<?>[] methodNodes;

    TypeSkeleton(Collection<MethodId> callableDefaultMethods, List<MethodNode<?>> methodNodes) {
        this.callableDefaultMethods = callableDefaultMethods.toArray(new MethodId[0]);
        this.methodNodes = methodNodes.toArray(new MethodNode[0]);
    }

    static final class MethodNode<V> {

        final DataEntry.Comments comments;
        final boolean optional;
        final MethodId methodId;
        private final DefaultValues<V> defaultValues; // Can be null if optional, or if defaults unconfigured
        final SerializeDeserialize<V> serializer;

        MethodNode(DataEntry.Comments comments, boolean optional, MethodId methodId, DefaultValues<V> defaultValues,
                   SerializeDeserialize<V> serializer) {
            this.comments = comments;
            this.optional = optional;
            this.methodId = Objects.requireNonNull(methodId, "methodId");
            this.defaultValues = defaultValues;
            this.serializer = Objects.requireNonNull(serializer, "serializer");
        }

        /**
         * Makes the return value for this method, representing the "default value"
         *
         * @param inType the type enclosing this method
         * @return the default value
         * @throws DeveloperMistakeException if no default value was configured for this method, or
         * {@link DefaultValues#defaultValue()} is wrongly implemented
         */
        Object makeDefaultValue(Class<?> inType) {
            if (defaultValues == null) {
                if (optional) {
                    return Optional.empty();
                }
                throw new DeveloperMistakeException(
                        "No default values configured for " +  inType.getName() + '#' + methodId.name() + ". " +
                                "To use Configuration#loadDefaults, default values must be set for every option."
                );
            }
            V defaultVal;
            try {
                defaultVal = defaultValues.defaultValue();
            } catch (RuntimeException ex) {
                throw new DeveloperMistakeException("DefaultValues#defaultValue threw an exception", ex);
            }
            if (defaultVal == null) {
                throw new DeveloperMistakeException(
                        "DefaultValues#defaultValue returned null for " + inType.getName() + '#' + methodId.name()
                );
            }
            return optional ? Optional.of(defaultVal) : defaultVal;
        }

        /**
         * Makes the return value for this method, representing the "missing value"
         *
         * @param inType the type enclosing this method
         * @return the missing value, or null if no missing value can be made
         * @throws DeveloperMistakeException if {@link DefaultValues#ifMissing()} is wrongly implemented
         */
        Object makeMissingValue(Class<?> inType) {
            if (optional) {
                return Optional.empty();
            }
            if (defaultValues == null) {
                return null;
            }
            V defaultVal = defaultValues.ifMissing();
            if (defaultVal == null) {
                throw new DeveloperMistakeException(
                        "DefaultValues#missingValue returned null for " + inType.getName() + '#' + methodId.name()
                );
            }
            return defaultVal;
        }

        @Nullable DataEntry serialize(MethodMirror.Invoker invoker, SerializeOutput ser) {
            Object value;
            try {
                value = invoker.invokeMethod(methodId);
            } catch (InvocationTargetException e) {
                throw new DeveloperMistakeException("Configuration methods must not throw exceptions", e);
            }
            if (value == null) {
                throw new DeveloperMistakeException(
                        "Configuration method " + methodId + " must not return null"
                );
            }
            if (optional && (value = ((Optional<?>) value).orElse(null)) == null) {
                return null;
            }
            serializer.serialize((V) value, ser);

            Object output = ser.getAndClearLastOutput();
            if (output == null) {
                throw new DeveloperMistakeException(
                        "Serializer " + serializer + " did not produce any output for " + value
                );
            }
            return new DataEntry(output).withComments(comments);
        }
    }
}
