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

package space.arim.dazzleconf;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf.backend.CommentData;
import space.arim.dazzleconf.backend.DataEntry;
import space.arim.dazzleconf.engine.DefaultValues;
import space.arim.dazzleconf.engine.DefinedNode;
import space.arim.dazzleconf.engine.NoOutput;
import space.arim.dazzleconf.engine.SerializeDeserialize;
import space.arim.dazzleconf.engine.SerializeOutput;
import space.arim.dazzleconf.reflect.MethodId;
import space.arim.dazzleconf.reflect.ReflectionProvider;

import java.util.Objects;

class SkeletonNode<R, B> implements DefinedNode<R, B> {

    final MethodId methodId;

    SkeletonNode(MethodId methodId) {
        this.methodId = Objects.requireNonNull(methodId, "methodId");
    }

    @Override
    public @NonNull MethodId methodId() {
        return methodId;
    }

    static final class Val<V, B> extends SkeletonNode<V, B> implements Value<V, B> {

        private final CommentData comments;
        private final @Nullable DefaultValues<V> defaultValues; // Can be null if defaults unconfigured
        private final SerializeDeserialize<V> serializer;

        Val(MethodId methodId, CommentData comments, @Nullable DefaultValues<V> defaultValues, SerializeDeserialize<V> serializer) {
            super(methodId);
            this.comments = comments;
            this.defaultValues = defaultValues;
            this.serializer = Objects.requireNonNull(serializer, "serializer");
        }

        @Override
        public @NonNull CommentData comments() {
            return comments;
        }

        @Override
        public @NonNull SerializeDeserialize<V> serializeDeserialize() {
            return serializer;
        }

        @Override
        public @Nullable DefaultValues<V> defaultValues() {
            return defaultValues;
        }

        @Override
        public @NonNull String label() {
            return methodId.name();
        }

        private @Nullable V getMissingOrDefault(Class<B> inType, boolean missing) {
            if (defaultValues == null) {
                return null;
            }
            V val;
            try {
                val = missing ? defaultValues.ifMissing() : defaultValues.defaultValue();
            } catch (RuntimeException ex) {
                throw new DeveloperMistakeException(
                        "DefaultValues threw an exception for " + inType.getName() + '#' + methodId.name(),
                        ex
                );
            }
            if (val == null) {
                String methodName = missing ? "ifMissing" : "defaultValue";
                throw new DeveloperMistakeException(
                        "DefaultValues#" + methodName + " returned null for " + inType.getName() + '#' + methodId.name()
                );
            }
            return val;
        }

        /**
         * Makes the return value for this method, representing the "default value"
         *
         * @param inType the type enclosing this method
         * @return the default value
         * @throws DeveloperMistakeException if no default value was configured for this method, or
         *                                   {@link DefaultValues#defaultValue()} is wrongly implemented
         */
        V makeDefaultValue(Class<B> inType) {
            V defaultVal = getMissingOrDefault(inType, false);
            if (defaultVal == null) {
                throw new DeveloperMistakeException(
                        "No default values configured for " + inType.getName() + '#' + methodId.name() + ". " +
                                "To use Configuration#loadDefaults, default values must be set for every option."
                );
            }
            return defaultVal;
        }

        /**
         * Makes the return value for this method, representing the "missing value"
         *
         * @param inType the type enclosing this method
         * @return the missing value, or null if no missing value exists
         * @throws DeveloperMistakeException if {@link DefaultValues#ifMissing()} is wrongly implemented
         */
        @Nullable V makeMissingValue(Class<B> inType) {
            return getMissingOrDefault(inType, true);
        }

        @Nullable DataEntry serialize(ReflectionProvider.Invoker<B> invoker, SerializeOutput ser) {
            Object value;
            try {
                value = invoker.invokeMethod(methodId, (Object[]) null);
            } catch (Error ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new DeveloperMistakeException("Configuration methods must not throw exceptions", ex);
            }
            if (value == null) {
                throw new DeveloperMistakeException(
                        "Configuration method " + methodId + " must not return null"
                );
            }
            @SuppressWarnings("unchecked")
            V castValue = (V) value;
            return serialize(castValue, ser);
        }

        @Nullable DataEntry serialize(V value, SerializeOutput ser) {
            serializer.serialize(value, ser);

            Object output = ser.getAndClearLastOutput();
            if (output == null) {
                throw new DeveloperMistakeException(
                        "Serializer " + serializer + " did not produce any output for " + value
                );
            }
            if (output == NoOutput.INSTANCE) {
                return null;
            }
            return new DataEntry(output);
        }
    }

    static final class Callable<V, B> extends SkeletonNode<V, B> implements DefinedNode.Callable<V, B> {

        Callable(MethodId methodId) {
            super(methodId);
        }
    }

}
