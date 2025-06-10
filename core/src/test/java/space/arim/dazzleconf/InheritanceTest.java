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

package space.arim.dazzleconf;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.dazzleconf2.Configuration;
import space.arim.dazzleconf2.ConfigurationDefinition;
import space.arim.dazzleconf2.DeveloperMistakeException;
import space.arim.dazzleconf2.backend.*;
import space.arim.dazzleconf2.engine.CallableFn;
import space.arim.dazzleconf2.engine.CommentLocation;
import space.arim.dazzleconf2.engine.LoadListener;
import space.arim.dazzleconf2.engine.UpdateReason;
import space.arim.dazzleconf2.engine.liaison.IntegerDefault;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class InheritanceTest {

    public interface Grandparent {

        boolean trueDefault();

        Object usableWhenOveridden();

        default Collection<String> setWhenOveridden() {
            return List.of("hi", "there");
        }

        default int reabstractMe() {
            return -1;
        }

        default void addAnnotation(String ignored) {}

        default String addAnnotation2() {
            return "not added";
        }

        @CallableFn
        default char removeAnnotation() {
            return 'n';
        }
    }

    public interface Parent extends Grandparent {

        @Override
        default boolean trueDefault() {
            return true;
        }

        @Override
        default String usableWhenOveridden() {
            return "usable";
        }

        @Override
        default Set<String> setWhenOveridden() {
            return Set.copyOf(Grandparent.super.setWhenOveridden());
        }

        @Override
        @IntegerDefault(value = 1, ifMissing = 2)
        int reabstractMe();

        @Override
        @CallableFn
        default void addAnnotation(String ignored) {}

        @Override
        @CallableFn
        default String addAnnotation2() {
            return "yes added";
        }

        @Override
        default char removeAnnotation() {
            return 'y';
        }
    }

    public interface Auntie extends Grandparent {

        default String unrelated() {
            return "potential gotcha";
        }
    }

    public interface Child extends Parent {}

    // Test the following interfaces

    public interface InheritParent extends Parent { }

    public interface InheritParentThenGrandparent extends Parent, Grandparent { }

    public interface InheritGrandparentThenParent extends Grandparent, Parent { }

    public interface InheritParentThenAuntie extends Parent, Auntie { }

    public interface InheritAuntieThenParent extends Auntie, Parent { }

    public interface InheritChildThenAuntie extends Child, Auntie { }

    public interface InheritAuntieThenChild extends Auntie, Child { }

    public static class ClassProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                    InheritParent.class,
                    InheritParentThenGrandparent.class, InheritGrandparentThenParent.class,
                    InheritParentThenAuntie.class, InheritAuntieThenParent.class,
                    InheritChildThenAuntie.class, InheritAuntieThenChild.class
            ).map(Arguments::of);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(ClassProvider.class)
    public <C extends Grandparent> void inheritance(Class<C> type) {
        // Make the configuration
        Configuration<C> configuration;
        try {
            configuration = Configuration.defaultBuilder(type).build();
        } catch (DeveloperMistakeException ex) {
            throw new AssertionError("Failed to load definition for " + type, ex);
        }

        // Load defaults successfully
        C defaults = assertDoesNotThrow(configuration::loadDefaults);
        assertInstanceOf(type, defaults);

        // Check that it worked fully
        assertTrue(defaults.trueDefault());
        assertEquals("usable", defaults.usableWhenOveridden());
        assertEquals(Set.of("hi", "there"), defaults.setWhenOveridden());
        assertEquals(1, defaults.reabstractMe());
        assertDoesNotThrow(() -> defaults.addAnnotation("data"));
        assertEquals("yes added", defaults.addAnnotation2());
        assertEquals('y', defaults.removeAnnotation());
        if (defaults instanceof Auntie) {
            assertEquals("potential gotcha", ((Auntie) defaults).unrelated());
        }

        // Try loading a Set with duplicates, check that they're removed
        // This ensures that setWhenOveridden() is using the set liaison
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set("usable", new DataEntry("msg"));
        dataTree.set("setWhenOveridden", new DataEntry(List.of(new DataEntry("hi"), new DataEntry("hi"), new DataEntry("hi"))));

        C readFrom = configuration.readFrom(dataTree.intoImmut()).getOrThrow();
        assertEquals("usable", readFrom.usableWhenOveridden());
        assertEquals(Set.of("hi"), readFrom.setWhenOveridden());
        assertEquals(2, readFrom.reabstractMe());

        LoadListener loadListener = mock(LoadListener.class);
        C readWithUpdate = configuration.readWithUpdate(dataTree, new ConfigurationDefinition.ReadWithUpdateOptions() {

            @Override
            public @NonNull LoadListener loadListener() {
                return loadListener;
            }

            @Override
            public @NonNull KeyMapper keyMapper() {
                return new DefaultKeyMapper();
            }

            @Override
            public boolean writeEntryComments(@NonNull CommentLocation location) {
                return true;
            }
        }).getOrThrow();
        assertEquals("usable", readFrom.usableWhenOveridden());
        assertEquals(Set.of("hi"), readFrom.setWhenOveridden());
        assertEquals(new DataEntry(List.of(new DataEntry("hi"))), dataTree.get("setWhenOveridden"));
        assertEquals(new DataEntry(2), dataTree.get("reabstractMe"));
        verify(loadListener).updatedPath(new KeyPath.Mut("setWhenOveridden"), UpdateReason.OTHER);
    }

}
