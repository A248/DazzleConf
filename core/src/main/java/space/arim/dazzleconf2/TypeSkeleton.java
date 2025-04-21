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

import space.arim.dazzleconf.internal.util.ImmutableCollections;
import space.arim.dazzleconf2.engine.DefaultValues;
import space.arim.dazzleconf2.engine.SerializeDeserialize;
import space.arim.dazzleconf2.reflect.MethodId;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Defines a single interface supertype which has not yet been instantiated
 */
final class TypeSkeleton {

    /**
     * Functions annotated with @Callable
     */
    final Set<MethodId> callableDefaultMethods;
    /**
     * Functions whose return values are supplied by us
     */
    final Set<MethodNode> methodNodes;

    TypeSkeleton(Set<MethodId> callableDefaultMethods, Set<MethodNode> methodNodes) {
        this.callableDefaultMethods = ImmutableCollections.setOf(callableDefaultMethods);
        this.methodNodes = ImmutableCollections.setOf(methodNodes);
    }

    static final class MethodNode {

        final boolean optional;
        final MethodId methodId;
        private final DefaultValues<?> defaultValues; // Can be null if optional
        final SerializeDeserialize<?> serializer;

        MethodNode(boolean optional, MethodId methodId, DefaultValues<?> defaultValues,
                   SerializeDeserialize<?> serializer) {
            if (!optional && defaultValues == null) {
                throw new NullPointerException("defaultValues cannot be null if non-optional");
            }
            this.optional = optional;
            this.methodId = Objects.requireNonNull(methodId, "methodId");
            this.defaultValues = defaultValues;
            this.serializer = Objects.requireNonNull(serializer, "serializer");
        }

        Object makeDefaultValue(Class<?> inType) {
            if (optional && defaultValues == null) {
                return Optional.empty();
            }
            Object defaultVal = defaultValues.defaultValue();
            if (defaultVal == null) {
                throw new DeveloperMistakeException(
                        "DefaultValues#defaultValue returned null for " + inType.getName() + '#' + methodId.name()
                );
            }
            return optional ? Optional.of(defaultVal) : defaultVal;
        }

        Object makeMissingValue(Class<?> inType) {
            if (optional) {
                return Optional.empty();
            }
            Object defaultVal = defaultValues.ifMissing();
            if (defaultVal == null) {
                throw new DeveloperMistakeException(
                        "DefaultValues#missingValue returned null for " + inType.getName() + '#' + methodId.name()
                );
            }
            return defaultVal;
        }
    }
}
