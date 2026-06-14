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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.backend.DataEntry;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class InterprocessorTest {

    private static final DefinedLayout.OnWriteStructuredEntry hook1 = new DefinedLayout.OnWriteStructuredEntry() {
        @Override
        public @Nullable <R, B> DataEntry writeNew(@Nullable DataEntry entry, @NonNull SerializeContext serializeContext,
                                                   @NonNull DefinedLayout<?> definedLayout,
                                                   DefinedNode.@NonNull Value<R, B> valueNode,
                                                   DefinedLayout.@NonNull Branch<B> branchOfNode) {
            System.out.println("Found key path " + serializeContext.keyPath());
            return entry;
        }
    };
    private static final DefinedLayout.OnUpdateStructuredEntry hook2 = new DefinedLayout.OnUpdateStructuredEntry() {
        @Override
        public @Nullable <R, B> DataEntry insertUpdate(@Nullable DataEntry entry, @NonNull DataEntry oldEntry,
                                                       @NonNull DeserializeContext deserializeContext,
                                                       @NonNull SerializeContext serializeContext,
                                                       @NonNull DefinedLayout<?> definedLayout,
                                                       DefinedNode.@NonNull Value<R, B> valueNode, DefinedLayout.@NonNull Branch<B> branchOfNode) {
            System.out.println("Hooray!");
            return entry;
        }
    };

    @Test
    public void defaultHooks() {
        Interprocessor interprocessor = new Interprocessor.Builder().build();
        assertNull(interprocessor.getHookIfPresent(DefinedLayout.WRITE_STRUCTURED_TREE));
        assertNull(interprocessor.getHookIfPresent(DefinedLayout.WRITE_STRUCTURED_ENTRY));
        assertNull(interprocessor.getHookIfPresent(DefinedLayout.UPDATE_STRUCTURED_ENTRY));
    }

    @Test
    public void oneHookSet() {
        Interprocessor interprocessor = new Interprocessor.Builder()
                .addHook(DefinedLayout.WRITE_STRUCTURED_ENTRY, hook1)
                .build();
        assertNull(interprocessor.getHookIfPresent(DefinedLayout.WRITE_STRUCTURED_TREE));
        assertSame(hook1, interprocessor.getHookIfPresent(DefinedLayout.WRITE_STRUCTURED_ENTRY));
        assertNull(interprocessor.getHookIfPresent(DefinedLayout.UPDATE_STRUCTURED_ENTRY));
    }

    @Test
    public void multipleHooksSet() {
        Interprocessor interprocessor = new Interprocessor.Builder()
                .addHook(DefinedLayout.WRITE_STRUCTURED_ENTRY, hook1)
                .addHook(DefinedLayout.UPDATE_STRUCTURED_ENTRY, hook2)
                .build();
        assertNull(interprocessor.getHookIfPresent(DefinedLayout.WRITE_STRUCTURED_TREE));
        assertSame(hook1, interprocessor.getHookIfPresent(DefinedLayout.WRITE_STRUCTURED_ENTRY));
        assertSame(hook2, interprocessor.getHookIfPresent(DefinedLayout.UPDATE_STRUCTURED_ENTRY));
    }

    @Test
    public void removeHookNewProcessor() {
        Interprocessor interprocessor1 = new Interprocessor.Builder()
                .addHook(DefinedLayout.WRITE_STRUCTURED_ENTRY, hook1)
                .addHook(DefinedLayout.UPDATE_STRUCTURED_ENTRY, hook2)
                .build();
        assertNull(interprocessor1.getHookIfPresent(DefinedLayout.WRITE_STRUCTURED_TREE));
        assertSame(hook1, interprocessor1.getHookIfPresent(DefinedLayout.WRITE_STRUCTURED_ENTRY));
        assertSame(hook2, interprocessor1.getHookIfPresent(DefinedLayout.UPDATE_STRUCTURED_ENTRY));

        Interprocessor interprocessor2 = new Interprocessor.Builder()
                .addHook(DefinedLayout.WRITE_STRUCTURED_ENTRY, hook1)
                .build();
        assertNull(interprocessor2.getHookIfPresent(DefinedLayout.WRITE_STRUCTURED_TREE));
        assertSame(hook1, interprocessor2.getHookIfPresent(DefinedLayout.WRITE_STRUCTURED_ENTRY));
        assertNull(interprocessor2.getHookIfPresent(DefinedLayout.UPDATE_STRUCTURED_ENTRY));
    }

    @Test
    public void addHookNewProcessor() {
        Interprocessor interprocessor1 = new Interprocessor.Builder()
                .addHook(DefinedLayout.WRITE_STRUCTURED_ENTRY, hook1)
                .build();
        assertNull(interprocessor1.getHookIfPresent(DefinedLayout.WRITE_STRUCTURED_TREE));
        assertSame(hook1, interprocessor1.getHookIfPresent(DefinedLayout.WRITE_STRUCTURED_ENTRY));
        assertNull(interprocessor1.getHookIfPresent(DefinedLayout.UPDATE_STRUCTURED_ENTRY));

        Interprocessor interprocessor2 = new Interprocessor.Builder()
                .addHook(DefinedLayout.WRITE_STRUCTURED_ENTRY, hook1)
                .addHook(DefinedLayout.UPDATE_STRUCTURED_ENTRY, hook2)
                .build();
        assertNull(interprocessor2.getHookIfPresent(DefinedLayout.WRITE_STRUCTURED_TREE));
        assertSame(hook1, interprocessor2.getHookIfPresent(DefinedLayout.WRITE_STRUCTURED_ENTRY));
        assertSame(hook2, interprocessor2.getHookIfPresent(DefinedLayout.UPDATE_STRUCTURED_ENTRY));
    }

    @AfterEach
    public void resetStaticState() {
        DefinedLayout.WRITE_STRUCTURED_TREE.unsafeDeregister();
        DefinedLayout.WRITE_STRUCTURED_ENTRY.unsafeDeregister();
        DefinedLayout.UPDATE_STRUCTURED_ENTRY.unsafeDeregister();
        Interprocessor.HookKey.unsafeFinishDeregisterAll();
    }
}
