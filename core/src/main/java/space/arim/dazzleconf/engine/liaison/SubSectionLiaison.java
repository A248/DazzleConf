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

package space.arim.dazzleconf.engine.liaison;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.SideEffectFree;
import space.arim.dazzleconf.ConfigurationDefinition;
import space.arim.dazzleconf.LoadResult;
import space.arim.dazzleconf.backend.CommentData;
import space.arim.dazzleconf.backend.DataTree;
import space.arim.dazzleconf.backend.KeyMapper;
import space.arim.dazzleconf.backend.KeyPath;
import space.arim.dazzleconf.engine.DefaultValues;
import space.arim.dazzleconf.engine.DeserializeInput;
import space.arim.dazzleconf.engine.Interprocessor;
import space.arim.dazzleconf.engine.SerializeDeserialize;
import space.arim.dazzleconf.engine.SerializeOutput;
import space.arim.dazzleconf.engine.TypeLiaison;
import space.arim.dazzleconf.engine.UpdateReason;
import space.arim.dazzleconf.reflect.TypeToken;

/**
 * A type liaison that enables using subsection configurations.
 * <p>
 * This liaison covers all types whose usage is annotated with {@link SubSection}. Such types will be loaded as
 * configuration interfaces and fit seamlessly into the parent configuration.
 *
 */
public final class SubSectionLiaison implements TypeLiaison {

    /**
     * Creates
     */
    public SubSectionLiaison() {}

    @Override
    @SideEffectFree
    public <V> @Nullable Agent<V> makeAgent(@NonNull TypeToken<V> typeToken, @NonNull Handshake handshake) {
        if (typeToken.getReifiedType().annotations().hasAny(SubSection.class)) {
            return new SectionAgent<>(handshake.getConfiguration(typeToken));
        }
        return null;
    }

    private static class SectionAgent<V> implements Agent<V> {

        private final ConfigurationDefinition<V> configuration;

        SectionAgent(ConfigurationDefinition<V> configuration) {
            this.configuration = configuration;
        }

        @Override
        @SideEffectFree
        public @Nullable DefaultValues<V> loadDefaultValues(@NonNull DefaultInit<V> defaultInit) {
            DefaultValues<V> methodDefault = defaultInit.methodDefault();
            if (methodDefault != null) {
                return methodDefault;
            }
            return new DefaultValues<V>() {
                @Override
                public @NonNull V defaultValue() {
                    return configuration.loadDefaults();
                }

                @Override
                public @NonNull V ifMissing() {
                    return configuration.loadDefaults();
                }
            };
        }

        @Override
        public @NonNull CommentData loadComments(@NonNull CommentInit commentInit) {
            return configuration.getDefinedLayout().getComments()
                    .append(Agent.super.loadComments(commentInit));
        }

        @Override
        @SideEffectFree
        public @NonNull SerializeDeserialize<V> makeSerializer() {
            return new SerDer();
        }

        class SerDer implements SerializeDeserialize<V> {

            @Override
            public @NonNull LoadResult<@NonNull V> deserialize(@NonNull DeserializeInput deser) {
                // In order to reduce stack depth, avoid functions like LoadResult#flatMap
                LoadResult<DataTree> dataTreeResult = deser.requireDataTree();
                if (dataTreeResult.isFailure()) {
                    return LoadResult.failure(dataTreeResult.getErrorContexts());
                }
                DataTree dataTree = dataTreeResult.getOrThrow();
                return configuration.readFrom(dataTree, deser);
            }

            @Override
            public @NonNull LoadResult<@NonNull V> deserializeUpdate(@NonNull DeserializeInput deser,
                                                                     @NonNull SerializeOutput updateTo) {

                LoadResult<DataTree> requireDataTree = deser.requireDataTree();
                if (requireDataTree.isFailure()) {
                    return LoadResult.failure(requireDataTree.getErrorContexts());
                }
                DataTree originalTree = requireDataTree.getOrThrow();
                DataTree.Mut updatableTree = originalTree.intoMut();

                class RecordUpdates implements ConfigurationDefinition.ReadWithUpdateOptions {
                    private boolean updated;

                    @Override
                    public @NonNull KeyMapper keyMapper() {
                        return deser.keyMapper();
                    }

                    @Override
                    public @NonNull KeyPath keyPath() {
                        return deser.keyPath();
                    }

                    @Override
                    public @NonNull Interprocessor getInterprocessor() {
                        return deser.getInterprocessor();
                    }

                    @Override
                    public void notifyUpdate(@NonNull KeyPath entryPath, @NonNull UpdateReason updateReason) {
                        updated = true;
                        deser.notifyUpdate(entryPath, updateReason);
                    }
                }
                RecordUpdates recordUpdates = new RecordUpdates();
                LoadResult<V> result = configuration.readWithUpdate(updatableTree, recordUpdates);
                if (result.isSuccess() && (recordUpdates.updated || originalTree != updatableTree)) {
                    // No need to call deser.notifyUpdate(), since it will already have been called for child paths
                    updateTo.outDataTree(updatableTree);
                }
                return result;
            }

            @Override
            public void serialize(@NonNull V value, @NonNull SerializeOutput ser) {
                DataTree.Mut dataTreeMut = new DataTree.Mut();
                configuration.writeTo(value, dataTreeMut, ser);
                ser.outDataTree(dataTreeMut);
            }
        }
    }
}
