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

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf.ConfigurationDefinition;
import space.arim.dazzleconf.backend.CommentData;
import space.arim.dazzleconf.backend.DataEntry;
import space.arim.dazzleconf.backend.DataTree;
import space.arim.dazzleconf.reflect.ReflectionProvider;
import space.arim.dazzleconf.reflect.TypeToken;

import java.util.Collection;
import java.util.List;

/**
 * The low level layout of a configuration interface.
 * <p>
 * This layout includes all of a scanned interface's components, as well as reflective services used to access
 * those components.
 * <p>
 * <b>Collections and structure</b>
 * <p>
 * This whole layout, as a data structure, is immutable. Some of its methods or methods on associated data types return
 * collections, for example {@link #getBranches()}. The mutability of these collections is not defined. Callers should
 * treat them as immutable and not call any of their mutating methods; trying to mutate them may throw an exception, or
 * it may manifest itself in changes to temporary objects returned to callers.
 * <p>
 * <b>Implementing</b>
 * <p>
 * This interface is mainly implemented by the library. New abstract methods may be added in the future, depending upon
 * library needs. So, if library users wish to implement this themselves, they should be prepared to stay up-to-date
 * with library versions, otherwise linkage errors may occur when other code expects an up-to-date implementor.
 *
 * @param <C> the type of the configuration interface
 */
@API(status = API.Status.MAINTAINED)
public interface DefinedLayout<C> {

    /**
     * Interprocessor key for {@link OnWriteStructuredTree}.
     * <p>
     * The default value of this hook is a no-op.
     */
    Interprocessor.@NonNull HookKey<OnWriteStructuredTree> WRITE_STRUCTURED_TREE = new Interprocessor.HookKey<OnWriteStructuredTree>() {
        @Override
        public @NonNull OnWriteStructuredTree defaultValue() {
            return new OnWriteStructuredTree() {
                @Override
                public void writeTree(DataTree.@NonNull Mut dataTree, @NonNull SerializeContext treeContext,
                                      @NonNull DefinedLayout<?> definedLayout) {}
            };
        }
    };
    /**
     * Interprocessor key for {@link OnWriteStructuredEntry}.
     * <p>
     * The default value of this hook attaches comments to entries by copying the node's comments.
     */
    Interprocessor.@NonNull HookKey<OnWriteStructuredEntry> WRITE_STRUCTURED_ENTRY = new Interprocessor.HookKey<OnWriteStructuredEntry>() {
        @Override
        public @NonNull OnWriteStructuredEntry defaultValue() {
            return new OnWriteStructuredEntry() {
                @Override
                public @Nullable <R, B> DataEntry writeNew(@Nullable DataEntry entry,
                                                           @NonNull SerializeContext serializeContext,
                                                           @NonNull DefinedLayout<?> definedLayout,
                                                           DefinedNode.@NonNull Value<R, B> valueNode,
                                                           @NonNull Branch<B> branchOfNode) {
                    if (entry != null) {
                        entry = entry.withComments(valueNode.comments());
                    }
                    return entry;
                }
            };
        }
    };
    /**
     * Interprocessor key for {@link OnUpdateStructuredEntry}.
     * <p>
     * The default value of this hook is a no-op.
     */
    Interprocessor.@NonNull HookKey<OnUpdateStructuredEntry> UPDATE_STRUCTURED_ENTRY = new Interprocessor.HookKey<OnUpdateStructuredEntry>() {
        @Override
        public @NonNull OnUpdateStructuredEntry defaultValue() {
            return new OnUpdateStructuredEntry() {
                @Override
                public @Nullable <R, B> DataEntry insertUpdate(@Nullable DataEntry entry, @NonNull DataEntry oldEntry,
                                                               @NonNull DeserializeContext deserializeContext,
                                                               @NonNull SerializeContext serializeContext,
                                                               @NonNull DefinedLayout<?> definedLayout,
                                                               DefinedNode.@NonNull Value<R, B> valueNode,
                                                               @NonNull Branch<B> branchOfNode) {
                    return entry;
                }
            };
        }
    };

    /**
     * Gets the reified type of the configuration interface.
     * <p>
     * This is always the same type as {@link ConfigurationDefinition#getType()}.
     *
     * @return the type of the configuration interface
     */
    @NonNull TypeToken<C> getType();

    /**
     * Gets the comments on the configuration interface itself.
     * <p>
     * These comments are not tied to any specific entry, but rather the interface itself. For the top level
     * configuration (the one used with {@code Configuration}), they will become the document header and footer.
     *
     * @return the top level comments, which may be empty if not set
     */
    @NonNull CommentData getComments();

    /**
     * Gets the reflection provider used to generate and call implementations of the configuration type.
     *
     * @return the reflection provider
     */
    @NonNull ReflectionProvider<C> getReflectionProvider();

    /**
     * Gets all the branches in this layout.
     * <p>
     * Using this collection allows traversing the interface definition according to its type hiearchy. See
     * {@link Branch} for more details.
     *
     * @return the branches, which may or may not be immutable
     */
    @NonNull Collection<@NonNull ? extends Branch<?>> getBranches();

    /**
     * Performs an operation for each branch.
     * <p>
     * Unlike iterating over the collection of branches ({@link #getBranches()}), this method allows the callback to
     * bind the generic type of each branch. It is otherwise equivalent.
     *
     * @param branchCallback the operation to run
     */
    void forEachBranch(@NonNull BranchCallback<C> branchCallback);

    /**
     * A fragment of the layout that corresponds to a particular interface supertype.
     * <p>
     * Every superinterface of the configuration interface will be represented by a branch, including the configuration
     * interface itself. The methods which specifically belong to that branch are exposed as method nodes.
     *
     * @param <B> the interface supertype this branch corresponds to
     */
    interface Branch<B> {

        /**
         * Gets the type represented by this branch.
         * <p>
         * Note that even though this type exists in the hierarchy, not all of its methods may be directly available
         * as method nodes on this {@code Branch}. In particular, overridden methods will not exist as nodes in this
         * branch (they will exist in some other branch, corresponding to where they were overridden).
         *
         * @return the type this branch represents
         */
        @NonNull TypeToken<B> getType();

        /**
         * Gets all the method nodes for this branch.
         * <p>
         * Each method node represents one of the methods declared in this branch's type. Note that if a method was
         * overridden by another type (a subinterface), its node will not be located here.
         * <p>
         * The nodes will be ordered according to how the reflection service returned methods for this branch's type.
         *
         * @return the nodes for this branch, which may or may not be immutable
         */
        @NonNull List<? extends @NonNull DefinedNode<?, B>> getNodes();

    }

    /**
     * An operation that can be run per branch
     *
     * @param <C> the type of the main configuration interface
     */
    @FunctionalInterface
    interface BranchCallback<C> {

        /**
         * Runs the operation
         *
         * @param branch the branch
         */
        void runFor(@NonNull Branch<? super C> branch);

    }

    /**
     * A hook typically added to an {@link Interprocessor} that allows customizing writing and updating data trees.
     * <p>
     * If the data tree is logically associated with a defined layout, the writer for that data tree will likely want
     * to call this hook.
     */
    interface OnWriteStructuredTree {

        /**
         * Called when a data tree is about to be written, or has just been updated
         *
         * @param dataTree the data tree, for mutation purposes
         * @param treeContext the context in which tree is written
         * @param definedLayout the layout used to model the data tree
         */
        void writeTree(DataTree.@NonNull Mut dataTree, @NonNull SerializeContext treeContext, @NonNull DefinedLayout<?> definedLayout);
    }

    /**
     * A hook typically added to an {@link Interprocessor} that allows customizing writing data entries.
     * <p>
     * If the data entry is logically associated with a node in a defined layout, the writer for that data entry will
     * likely want to call this hook.
     */
    interface OnWriteStructuredEntry {

        /**
         * Called when a new data entry is about to be written to, or removed from, a data tree.
         *
         * @param entry the data entry candidate, or {@code null} if the entry will be removed
         * @param serializeContext the serialize context
         * @param definedLayout the layout used to model the data tree
         * @param valueNode the value node corresponding to the entry
         * @param branchOfNode the branch the node was taken from
         * @return the actual data entry that will be written, or {@code null} to remove the entry here
         * @param <R> the return value of the node
         * @param <B> the branch type
         */
        <R, B> @Nullable DataEntry writeNew(@Nullable DataEntry entry, @NonNull SerializeContext serializeContext,
                                            @NonNull DefinedLayout<?> definedLayout,
                                            DefinedNode.@NonNull Value<R, B> valueNode,
                                            @NonNull Branch<B> branchOfNode);

    }

    /**
     * A hook typically added to an {@link Interprocessor} that allows customizing updating data entries.
     * <p>
     * If the data entry is logically associated with a node in a defined layout, the updater for that data entry will
     * likely want to call this hook.
     */
    interface OnUpdateStructuredEntry {

        /**
         * Called when a data entry from a tree is about to be updated.
         *
         * @param entry the data entry candidate to update with, or {@code null} if the entry will be removed
         * @param oldEntry the existing data entry
         * @param deserializeContext the deserialize context
         * @param serializeContext the serialize context
         * @param definedLayout the layout used to model the data tree
         * @param valueNode the value node corresponding to the entry
         * @param branchOfNode the branch the node was taken from
         * @return the actual data entry that will be written, or {@code null} to remove the entry here
         * @param <R> the return value of the node
         * @param <B> the branch type
         */
        <R, B> @Nullable DataEntry insertUpdate(@Nullable DataEntry entry, @NonNull DataEntry oldEntry,
                                                @NonNull DeserializeContext deserializeContext,
                                                @NonNull SerializeContext serializeContext,
                                                @NonNull DefinedLayout<?> definedLayout,
                                                DefinedNode.@NonNull Value<R, B> valueNode,
                                                @NonNull Branch<B> branchOfNode);

    }
}
