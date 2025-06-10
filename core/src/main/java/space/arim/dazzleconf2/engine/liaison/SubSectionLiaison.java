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

package space.arim.dazzleconf2.engine.liaison;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.SideEffectFree;
import space.arim.dazzleconf2.ConfigurationDefinition;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.KeyMapper;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.engine.CommentLocation;
import space.arim.dazzleconf2.engine.DefaultValues;
import space.arim.dazzleconf2.engine.DeserializeInput;
import space.arim.dazzleconf2.engine.LoadListener;
import space.arim.dazzleconf2.engine.SerializeDeserialize;
import space.arim.dazzleconf2.engine.SerializeOutput;
import space.arim.dazzleconf2.engine.TypeLiaison;
import space.arim.dazzleconf2.engine.UpdateReason;
import space.arim.dazzleconf2.reflect.TypeToken;

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
        if (typeToken.getReifiedType().getAnnotation(SubSection.class) != null) {
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
        public @Nullable DefaultValues<V> loadDefaultValues(@NonNull DefaultInit defaultInit) {
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
                return configuration.readFrom(dataTree, new ConfigurationDefinition.ReadOptions() {
                    @Override
                    public @NonNull LoadListener loadListener() {
                        return deser::flagUpdate;
                    }

                    @Override
                    public @NonNull KeyMapper keyMapper() {
                        return deser.keyMapper();
                    }

                    @Override
                    public int maximumErrorCollect() {
                        // TODO: Override this with the same value as the parent context
                        return ConfigurationDefinition.ReadOptions.super.maximumErrorCollect();
                    }
                });
            }

            @Override
            public @NonNull LoadResult<@NonNull V> deserializeUpdate(@NonNull DeserializeInput deser,
                                                                     @NonNull SerializeOutput updateTo) {
                class RecordUpdates implements LoadListener {

                    private boolean updated;

                    @Override
                    public void updatedPath(@NonNull KeyPath entryPath, @NonNull UpdateReason updateReason) {
                        updated = true;
                        deser.flagUpdate(entryPath, updateReason);
                    }
                }
                LoadResult<DataTree> requireDataTree = deser.requireDataTree();
                if (requireDataTree.isFailure()) {
                    return LoadResult.failure(requireDataTree.getErrorContexts());
                }
                DataTree.Mut updatableTree = requireDataTree.getOrThrow().intoMut();
                RecordUpdates recordUpdates = new RecordUpdates();

                LoadResult<V> result = configuration.readWithUpdate(updatableTree, new ConfigurationDefinition.ReadWithUpdateOptions() {
                    @Override
                    public @NonNull LoadListener loadListener() {
                        return recordUpdates;
                    }

                    @Override
                    public @NonNull KeyMapper keyMapper() {
                        return deser.keyMapper();
                    }

                    @Override
                    public int maximumErrorCollect() {
                        // TODO: Override this with the same value as the parent context
                        return ConfigurationDefinition.ReadWithUpdateOptions.super.maximumErrorCollect();
                    }

                    @Override
                    public boolean writeEntryComments(@NonNull CommentLocation location) {
                        return updateTo.writeEntryComments(location);
                    }
                });
                if (result.isSuccess() && recordUpdates.updated) {
                    // No need to call deser.flagUpdate(), since it will already have been called for child paths
                    updateTo.outDataTree(updatableTree);
                }
                return result;
            }

            @Override
            public void serialize(@NonNull V value, @NonNull SerializeOutput ser) {
                DataTree.Mut dataTreeMut = new DataTree.Mut();
                configuration.writeTo(value, dataTreeMut, new ConfigurationDefinition.WriteOptions() {

                    @Override
                    public @NonNull KeyMapper keyMapper() {
                        return ser.keyMapper();
                    }

                    @Override
                    public boolean writeEntryComments(@NonNull CommentLocation location) {
                        // TODO: Improve this API, maybe return an object instead of having this function here
                        return ser.writeEntryComments(location);
                    }
                });
                ser.outDataTree(dataTreeMut);
            }
        }
    }
}
