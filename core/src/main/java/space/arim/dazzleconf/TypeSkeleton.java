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
import space.arim.dazzleconf.backend.DataEntry;
import space.arim.dazzleconf.backend.DataTree;
import space.arim.dazzleconf.backend.KeyMapper;
import space.arim.dazzleconf.backend.KeyPath;
import space.arim.dazzleconf.backend.Printable;
import space.arim.dazzleconf.engine.DefinedLayout;
import space.arim.dazzleconf.engine.DefinedNode;
import space.arim.dazzleconf.engine.DeserializeContext;
import space.arim.dazzleconf.engine.DeserializeInput;
import space.arim.dazzleconf.engine.SerializeDeserialize;
import space.arim.dazzleconf.engine.SerializeOutput;
import space.arim.dazzleconf.engine.UpdateReason;
import space.arim.dazzleconf.reflect.MethodYield;
import space.arim.dazzleconf.reflect.ReflectionProvider;
import space.arim.dazzleconf.reflect.TypeToken;

import java.util.Arrays;
import java.util.List;

/**
 * Defines a single interface supertype which has not yet been instantiated
 */
final class TypeSkeleton<B> implements DefinedLayout.Branch<B> {

    private final TypeToken<B> typeToken;
    private final SkeletonNode.Val<?, B>[] valNodes;
    private final SkeletonNode.Callable<?, B>[] callableNodes;

    @SuppressWarnings("unchecked")
    TypeSkeleton(TypeToken<B> typeToken,
                 List<SkeletonNode.Val<?, B>> valNodes, List<SkeletonNode.Callable<?, B>> callableNodes) {
        this.typeToken = typeToken;
        this.valNodes = valNodes.toArray(new SkeletonNode.Val[0]);
        this.callableNodes = callableNodes.toArray(new SkeletonNode.Callable[0]);
    }

    void implLoadDefaults(MethodYield methodYield) {
        Class<B> currentType = typeToken.getRawType();
        try (MethodYield.ForImplementable yieldForType = methodYield.forImplementable(currentType)) {
            for (SkeletonNode.Val<?, B> valNode : valNodes) {
                Object defaultValue = valNode.makeDefaultValue(currentType);
                yieldForType.returnValue(valNode.methodId(), defaultValue);
            }
            for (SkeletonNode.Callable<?, B> callable : callableNodes) {
                yieldForType.callDefault(callable.methodId());
            }
        }
    }

    interface HowToUpdate<UPD, DT extends DataTree> {

        UPD makeUpdater(String mappedKey);

        <V, B> void insertMissingValue(DT dataTree, String mappedKey, SkeletonNode.Val<V, B> valNode, V missingValue,
                                       DefinedLayout.Branch<B> branchOfNode);

        <V> LoadResult<V> deserialize(SerializeDeserialize<V> serializeDeserialize, DeserializeInput deser, UPD updater);

        <B> void updateIfDesired(DT dataTree, String mappedKey, DataEntry sourceEntry, SkeletonNode.Val<?, B> valNode,
                                 DeserializeContext deserContext, DefinedLayout.Branch<B> branchOfNode, UPD updater);

    }

    final class ImplRead<UPD, DT extends DataTree> {

        private final @NonNull DT dataTree;
        private final @NonNull HowToUpdate<UPD, DT> howToUpdate;
        private final ConfigurationDefinition.@NonNull ReadOptions readOptions;
        private final Definition<?> definition;

        ErrorContext[] collectedErrors;
        int errorCount;

        ImplRead(@NonNull DT dataTree, @NonNull HowToUpdate<UPD, DT> howToUpdate,
                 ConfigurationDefinition.@NonNull ReadOptions readOptions, Definition<?> definition) {
            this.dataTree = dataTree;
            this.howToUpdate = howToUpdate;
            this.readOptions = readOptions;
            this.definition = definition;
        }

        void readNodes(MethodYield methodYield) {
            try (MethodYield.ForImplementable yieldForType = methodYield.forImplementable(typeToken.getRawType())) {
                // Add method values
                for (SkeletonNode.Val<?, B> valNode : valNodes) {
                    readNode(yieldForType, valNode);
                    if (collectedErrors != null && errorCount == collectedErrors.length) {
                        return;
                    }
                }
                if (collectedErrors == null) {
                    // Add callable default methods
                    for (SkeletonNode.Callable<?, B> callable : callableNodes) {
                        yieldForType.callDefault(callable.methodId());
                    }
                }
            }
        }

        private <V> void readNode(
                MethodYield.ForImplementable yieldForType, SkeletonNode.@NonNull Val<V, B> valNode
        ) {
            V value;
            String mappedKey = readOptions.keyMapper().labelToKey(valNode.label()).toString();
            DataEntry dataEntry = dataTree.get(mappedKey);
            if (dataEntry == null) {

                // Absent entry. Three possibilities for the method:
                // 1. Absence acceptable -> get absent value
                // 2. Mandatory, so fill in the missing value -> okay, signal updated path
                // 3. Mandatory, and no missing value -> error

                DeserContext deserContext = new DeserContext.AtKey(definition.libraryLang, readOptions, mappedKey);
                V absentValue = valNode.serializeDeserialize().deserializeAbsent(deserContext);
                V missingValue;
                if (absentValue != null) {
                    // 1.
                    value = absentValue;
                } else if ((missingValue = valNode.makeMissingValue(typeToken.getRawType())) != null) {
                    // 2.
                    /*
                    Note that if the value serializes to null in #insertMissingValue, we still signal an update here.
                    If so, we are dealing with a rogue liaison that cannot accept absent values but nonetheless outputs
                    absent values. To maintain symmetry across read/read-update operations,
                    notifyUpdate(..., UpdateReason.MISSING) is always called.
                     */
                    readOptions.notifyUpdate(new KeyPath.Mut(mappedKey), UpdateReason.MISSING);
                    howToUpdate.insertMissingValue(dataTree, mappedKey, valNode, missingValue, TypeSkeleton.this);
                    value = missingValue;
                } else {
                    // 3.
                    ErrorContext errorContext = deserContext.buildError(Printable.preBuilt(definition.libraryLang.missingValue()));
                    // Append this error
                    if (collectedErrors == null) {
                        collectedErrors = new ErrorContext[readOptions.maximumErrorCollect()];
                    }
                    collectedErrors[errorCount++] = errorContext;
                    return;
                }
            } else {
                //
                // Main deserialization route - most cases go here
                //

                // Deserialization
                UPD updater = howToUpdate.makeUpdater(mappedKey);
                DeserContext deserContext = new DeserContext.AtKey(definition.libraryLang, readOptions, mappedKey);
                LoadResult<V> valueResult = howToUpdate.deserialize(
                        valNode.serializeDeserialize(), deserContext.newInputHere(dataEntry), updater
                );
                // Error handling
                if (valueResult.isFailure()) {
                    if (collectedErrors == null) {
                        collectedErrors = new ErrorContext[readOptions.maximumErrorCollect()];
                    }
                    for (ErrorContext errorToAppend : valueResult.getErrorContexts()) {
                        // Append this error
                        collectedErrors[errorCount++] = errorToAppend;
                        // Check if maxed out
                        if (errorCount == collectedErrors.length) {
                            break;
                        }
                    }
                    return;
                }
                // Update if desired
                howToUpdate.updateIfDesired(
                        dataTree, mappedKey, dataEntry, valNode, deserContext, TypeSkeleton.this, updater
                );
                // No errors - all good
                value = valueResult.getOrThrow();
            }
            yieldForType.returnValue(valNode.methodId(), value);
        }
    }

    <C extends B> void implWriteNodes(
            ReflectionProvider<C> reflectionProvider, C config, DefinedLayout<C> definedLayout,
            ConfigurationDefinition.WriteOptions writeOptions, DataTree.Mut dataTree
    ) {
        ReflectionProvider.Invoker<B> invoker = reflectionProvider.makeInvoker(config, typeToken);
        KeyMapper keyMapper = writeOptions.keyMapper();
        DefinedLayout.OnWriteStructuredEntry onWriteStructuredEntry = writeOptions.getInterprocessor()
                .getHook(DefinedLayout.WRITE_STRUCTURED_ENTRY);
        for (SkeletonNode.Val<?, B> valNode : valNodes) {
            String mappedKey = keyMapper.labelToKey(valNode.label()).toString();
            SerializeOutput serOutput = new SerContext.AtKey(writeOptions, mappedKey).newOutput();
            DataEntry entry = valNode.serialize(invoker, serOutput);
            entry = onWriteStructuredEntry.writeNew(entry, serOutput, definedLayout, valNode, this);
            if (entry == null) {
                dataTree.remove(mappedKey); // Empty optional value
            } else {
                dataTree.put(mappedKey, entry);
            }
        }
    }

    @Override
    public @NonNull TypeToken<B> getType() {
        return typeToken;
    }

    @Override
    public @NonNull List<@NonNull ? extends DefinedNode<?, B>> getNodes() {
        @SuppressWarnings("unchecked")
        DefinedNode<?, B>[] array = new DefinedNode[callableNodes.length + valNodes.length];
        System.arraycopy(valNodes, 0, array, 0, valNodes.length);
        System.arraycopy(callableNodes, 0, array, valNodes.length, callableNodes.length);
        return Arrays.asList(array);
    }

    @Override
    public String toString() {
        return "TypeSkeleton{" +
                "typeToken=" + typeToken +
                ", valNodes=" + Arrays.toString(valNodes) +
                ", callableNodes=" + Arrays.toString(callableNodes) +
                '}';
    }
}
