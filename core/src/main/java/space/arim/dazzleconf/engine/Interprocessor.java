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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.SideEffectFree;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A middleman that can provide various hooks, where the kinds of hooks are extensible.
 * <p>
 * This class provides configuration-related hooks that are global with respect to a single read, write, or
 * read-with-update operation. That is, these hooks are not intended to be modified as they are passed around; even
 * deeply nested configuration subsections would use the same {@code Interprocessor}.
 */
public final class Interprocessor {

    /*
    We implement what is effectively a dynamic EnumMap. The number of hooks determines the size of the array, on a
    best-effort basis. Each hook key corresponds to a unique integer that becomes the hook value's array slot.

    This algorithm should yield optimal space/time tradeoffs for low numbers of hooks and frequent construction.
     */
    private final Object[] hookMap;

    private Interprocessor(Object[] hookMap) {
        this.hookMap = hookMap;
    }

    /**
     * An interprocessor with default settings (no hooks explicitly set)
     */
    public static final Interprocessor DEFAULT = new Interprocessor(new Object[0]);

    /**
     * Gets the value of a hook that was set when this interprocessor was created
     *
     * @param hookKey the hook key
     * @return the hook, which may be the default hook value if none was explicitly set
     * @param <H> the hook type
     */
    @SideEffectFree
    public <H> @NonNull H getHook(@NonNull HookKey<H> hookKey) {
        H hook = getHookIfPresent(hookKey);
        return hook == null ? hookKey.defaultValue() : hook;
    }

    /**
     * Gets the value of the hook if it is set, otherwise {@code null}
     *
     * @param hookKey the hook key
     * @return the hook, or null if not set
     * @param <H> the hook type
     */
    @SideEffectFree
    public <H> @Nullable H getHookIfPresent(@NonNull HookKey<H> hookKey) {
        return hookKey.getFromMap(hookMap);
    }

    /**
     * A key that corresponds to a kind of hook attached to an interprocessor.
     * <p>
     * For discoverability, constants of this type are typically located in the same place as their hook class. Hooks
     * are almost always interfaces.
     * <p>
     * <b>Implementor Contract</b>
     * <p>
     * Instances of this class must be declared as constant static fields. Failure to meet this condition may result
     * in exceptions or poor performance later on (but not bugs that affect other keys).
     *
     * @param <H> the hook kind
     */
    public static abstract class HookKey<H> {

        private int idx = IDX_UNINIT;
        private static final int IDX_UNINIT = -1;

        /**
         * Maximum index in existence (exclusive). Always zero or positive
         */
        private static volatile int keyIndexGen;
        // Need to account for applications that load and unload classes that use this library
        private static final Map<ClassKey, ClassKey.WeakIndex> INDEXES_USED = new HashMap<>();
        private static final List<Integer> INDEXES_FREE_LIST = new ArrayList<>();
        private static final ReferenceQueue<Class<?>> CLASS_GC_QUEUE = new ReferenceQueue<>();
        private static final int MAX_INDEXES = 256;

        // Synchronization: protect the above statics
        private static synchronized ClassKey.WeakIndex genKeyIndex(Class<?> hookKeyClass) {
            // Housekeeping: recycle indexes for GC'd classes
            for (Reference<?> gced; (gced = CLASS_GC_QUEUE.poll()) != null; ) {
                ClassKey.WeakIndex gcedWithIndex = (ClassKey.WeakIndex) gced;
                INDEXES_USED.remove(gcedWithIndex);
                INDEXES_FREE_LIST.add(gcedWithIndex.keyIndex);
            }
            ClassKey.WeakIndex existing = INDEXES_USED.get(new ClassKey.StrongKey(hookKeyClass));
            if (existing != null) {
                return existing;
            }
            int chosenKey;
            int freeListSize = INDEXES_FREE_LIST.size();
            if (freeListSize == 0) {
                chosenKey = keyIndexGen;
                if (chosenKey == MAX_INDEXES) {
                    throw new IllegalStateException("Too many hook keys (" + MAX_INDEXES + ") in application");
                }
                keyIndexGen = chosenKey + 1;
            } else {
                int popFree = INDEXES_FREE_LIST.get(freeListSize - 1);
                INDEXES_FREE_LIST.remove(freeListSize - 1);
                chosenKey = popFree;
            }
            assert chosenKey != IDX_UNINIT : "wrongly implemented";
            ClassKey.WeakIndex computed = new ClassKey.WeakIndex(hookKeyClass, CLASS_GC_QUEUE, chosenKey);
            INDEXES_USED.put(computed, computed);
            return computed;
        }

        private interface ClassKey {
            @Nullable Class<?> clazz();

            class WeakIndex extends WeakReference<Class<?>> implements ClassKey {

                private final int hashCode;
                private final int keyIndex;

                WeakIndex(Class<?> referent, ReferenceQueue<? super Class<?>> q, int keyIndex) {
                    super(referent, q);
                    this.hashCode = referent.hashCode();
                    this.keyIndex = keyIndex;
                }

                @Override
                public boolean equals(Object obj) {
                    if (obj == this) {
                        return true;
                    }
                    return obj instanceof ClassKey && Objects.equals(get(), ((ClassKey) obj).clazz());
                }

                @Override
                public int hashCode() {
                    return hashCode;
                }

                @Override
                public Class<?> clazz() {
                    return get();
                }
            }

            class StrongKey implements ClassKey {

                final Class<?> clazz;

                StrongKey(Class<?> clazz) {
                    this.clazz = Objects.requireNonNull(clazz);
                }

                @Override
                public boolean equals(Object obj) {
                    return obj instanceof ClassKey && clazz.equals(((ClassKey) obj).clazz());
                }

                @Override
                public int hashCode() {
                    return clazz.hashCode();
                }

                @Override
                public Class<?> clazz() {
                    return clazz;
                }
            }
        }

        // Used ONLY for testing purposes
        void unsafeDeregister() {
            int deregisterIdx = idx;
            if (deregisterIdx == IDX_UNINIT) {
                return;
            }
            idx = IDX_UNINIT;
            INDEXES_USED.remove(new ClassKey.StrongKey(getClass()));
            INDEXES_FREE_LIST.add(deregisterIdx);
        }

        // Used ONLY for testing purposes
        static void unsafeFinishDeregisterAll() {
            assert INDEXES_USED.isEmpty() : "must call unsafeDeregister() first";
            assert keyIndexGen == INDEXES_FREE_LIST.size() : "called at improper time";
            INDEXES_FREE_LIST.clear();
            keyIndexGen = 0;
        }

        /**
         * Privileged constructor.
         * <p>
         * Concrete subclasses are expected to guard construction and store themselves in static final fields.
         */
        protected HookKey() {}

        /**
         * The default value of the hook if none is explicitly set.
         * <p>
         * This should be set to a reasonable value for default logic.
         *
         * @return the default value of the hook
         */
        public abstract @NonNull H defaultValue();

        private int getOrComputeIdx() {
            // Non-volatile read / non-synchronized store are both fine
            // int is 32bit and atomically read, and key generation is idempotent
            int idx = this.idx;
            if (idx == IDX_UNINIT) {
                this.idx = idx = genKeyIndex(getClass()).keyIndex;
            }
            return idx;
        }

        @Nullable H getFromMap(Object[] hookMap) {
            int idx = this.idx;
            if (idx == IDX_UNINIT || idx >= hookMap.length) {
                return null;
            }
            @SuppressWarnings("unchecked")
            H cast = (H) hookMap[idx];
            return cast;
        }
    }

    /**
     * A builder for one or more {@link Interprocessor}s
     *
     */
    public static final class Builder {

        private Object[] hookMap;
        private int hookMapLen;

        private Builder(Object[] hookMap) {
            this.hookMap = hookMap;
            hookMapLen = hookMap.length;
        }

        /**
         * Creates the builder with default settings
         */
        public Builder() {
            this.hookMap = new Object[Integer.min(HookKey.keyIndexGen, 16)];
        }

        /**
         * Adds the specified hook to this builder. If the hook is already set, it will be overridden.
         *
         * @param hookKey the hook key
         * @param value the hook value
         * @return this builder
         * @param <H> the hook type
         */
        public <H> @NonNull Builder addHook(@NonNull HookKey<H> hookKey, @NonNull H value) {
            Objects.requireNonNull(value, "value");
            int idx = hookKey.getOrComputeIdx();
            if (idx >= hookMap.length) {
                hookMap = Arrays.copyOf(hookMap, Integer.max(idx + 1, hookMap.length * 2));
            }
            hookMap[idx] = value;
            hookMapLen = Integer.max(hookMapLen, idx + 1);
            return this;
        }

        /**
         * Builds into an interprocessor. This method can be called as many times as needed.
         *
         * @return the built interprocessor
         */
        @SideEffectFree
        public @NonNull Interprocessor build() {
            return new Interprocessor(Arrays.copyOf(hookMap, hookMapLen));
        }
    }

    /**
     * Turns the interprocessor back into a builder.
     * <p>
     * This object itself ({@code this}) will stay immutable, but the builder will inherit the same hooks as this
     * object, unless overridden.
     *
     * @return the builder
     */
    @SideEffectFree
    public @NonNull Builder toBuilder() {
        return new Builder(hookMap.clone());
    }

}
