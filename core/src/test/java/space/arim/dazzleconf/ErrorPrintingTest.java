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
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf2.Configuration;
import space.arim.dazzleconf2.ConfigurationDefinition;
import space.arim.dazzleconf2.ErrorPrint;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.ReloadShell;
import space.arim.dazzleconf2.StandardErrorPrint;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.KeyMapper;
import space.arim.dazzleconf2.backend.Printable;
import space.arim.dazzleconf2.backend.SnakeCaseKeyMapper;
import space.arim.dazzleconf2.engine.LoadListener;
import space.arim.dazzleconf2.engine.liaison.SubSection;
import space.arim.dazzleconf2.reflect.DefaultInstantiator;
import space.arim.dazzleconf2.reflect.Instantiator;
import space.arim.dazzleconf2.reflect.MethodId;
import space.arim.dazzleconf2.reflect.MethodMirror;
import space.arim.dazzleconf2.reflect.MethodYield;
import space.arim.dazzleconf2.reflect.ReifiedType;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

public class ErrorPrintingTest {

    public interface Config {

        String opening();

        String hello();

        boolean enabled();

        @SubSection SubConfig subSection();

        interface SubConfig {

            int integral();

            Character character();

            List<Double> decimalList();

            Set<Set<Boolean>> superNestedBools();

        }

    }

    private void expectOutput(int maximumErrorCollect, DataTree dataTree, String expected) {
        List<Printable> printables = new ArrayList<>();
        ErrorPrint errorPrint = new StandardErrorPrint(printables::add);
        LoadResult<Config> loadResult = Configuration.defaultBuilder(Config.class)
                .locale(Locale.ENGLISH)
                // Necessary in order to enforce a stable sort order
                .instantiator(new SortedInstantiator(new DefaultInstantiator(getClass().getClassLoader())))
                .build()
                .readFrom(dataTree, new ConfigurationDefinition.ReadOptions() {
                    @Override
                    public @NonNull LoadListener loadListener() {
                        return (entryPath, updateReason) -> {};
                    }

                    @Override
                    public @NonNull KeyMapper keyMapper() {
                        return new SnakeCaseKeyMapper();
                    }

                    @Override
                    public int maximumErrorCollect() {
                        return maximumErrorCollect;
                    }
                });
        assumeFalse(loadResult.isSuccess());
        errorPrint.onError(loadResult.getErrorContexts());

        StringBuilder builder = new StringBuilder();
        printables.forEach(printable -> printable.printTo(builder));
        String actual = builder.toString().trim();

        assertEquals(expected, actual);
    }


    private void expectOutput(DataTree dataTree, String expected) {
        expectOutput(20, dataTree, expected);
    }

    @Test
    public void singleBadValue() {
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set("opening", new DataEntry("hi"));
        dataTree.set("hello", new DataEntry("goodbye"));
        dataTree.set("enabled", new DataEntry(true));
        dataTree.set("sub-section", new DataEntry(-1));

        expectOutput(dataTree, """
                We found problems loading the configuration file.
                Where or how the error happened:
                  sub-section: This value is not chosen correctly. The value < -1 > is of type integer, but it should be configuration section.""");
    }

    @Test
    public void twoBadValuesAndLineNumber() {
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set("opening", new DataEntry("hi"));
        dataTree.set("hello", new DataEntry("goodbye"));
        dataTree.set("enabled", new DataEntry("BROKEN").withLineNumber(2));
        dataTree.set("sub-section", new DataEntry(-1));
        expectOutput(dataTree, """
                We found problems loading the configuration file.
                Where or how the error happened:
                  enabled @ line 2: This value is not chosen correctly. The value < BROKEN > is of type text/string, but it should be true/false value.
                  sub-section: This value is not chosen correctly. The value < -1 > is of type integer, but it should be configuration section.""");
    }

    @Test
    public void oneBadValueBecauseCapped() {
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set("opening", new DataEntry("hi"));
        dataTree.set("hello", new DataEntry("goodbye"));
        dataTree.set("enabled", new DataEntry("BROKEN").withLineNumber(2));
        dataTree.set("sub-section", new DataEntry(-1));
        expectOutput(1, dataTree, """
                We found problems loading the configuration file.
                Where or how the error happened:
                  enabled @ line 2: This value is not chosen correctly. The value < BROKEN > is of type text/string, but it should be true/false value.""");
    }

    @Test
    public void sixMissingValues() {
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set("opening", new DataEntry("hi"));
        dataTree.set("sub-section", new DataEntry(new DataTree.Immut()));
        expectOutput(dataTree, """
                We found problems loading the configuration file.
                Where or how the error happened:
                  enabled: No value is configured here but it is required.
                  hello: No value is configured here but it is required.
                  sub-section.character: No value is configured here but it is required.
                  sub-section.decimal-list: No value is configured here but it is required.
                  (+2 more...)""");
    }

    @Test
    public void fiveMissingValuesBecauseCapped() {
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set("opening", new DataEntry("hi"));
        dataTree.set("sub-section", new DataEntry(new DataTree.Immut()));
        expectOutput(5, dataTree, """
                We found problems loading the configuration file.
                Where or how the error happened:
                  enabled: No value is configured here but it is required.
                  hello: No value is configured here but it is required.
                  sub-section.character: No value is configured here but it is required.
                  sub-section.decimal-list: No value is configured here but it is required.
                  (+1 more...)""");
    }

    @Test
    public void threeMissingValuesBecauseCapped() {
        expectOutput(3, new DataTree.Immut(), """
                We found problems loading the configuration file.
                Where or how the error happened:
                  enabled: No value is configured here but it is required.
                  hello: No value is configured here but it is required.
                  opening: No value is configured here but it is required.""");
    }

    @Test
    public void errorsInChildElements() {
        DataTree.Mut subSection = new DataTree.Mut();
        subSection.set("integral", new DataEntry(2));
        subSection.set("character", new DataEntry("not a character"));
        subSection.set("decimal-list", new DataEntry(List.of(new DataEntry(0.0), new DataEntry(1.1), new DataEntry(-4.9))));
        subSection.set("super-nested-bools", new DataEntry(List.of(
                new DataEntry(List.of()),
                new DataEntry(List.of(
                        new DataEntry("true"), new DataEntry("false"), new DataEntry("NA"), new DataEntry("NOPE")
                ))
        )));
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set("opening", new DataEntry("hi"));
        dataTree.set("hello", new DataEntry("goodbye"));
        dataTree.set("enabled", new DataEntry(false));
        dataTree.set("sub-section", new DataEntry(subSection));
        expectOutput(dataTree, """
                We found problems loading the configuration file.
                Where or how the error happened:
                  sub-section.character: This value is not chosen correctly. The value < not a character > is of type text/string, but it should be character.
                  sub-section.super-nested-bools.$1.$2: This value is not chosen correctly. The value < NA > is of type text/string, but it should be true/false value.""");
    }

    // Delegate to default Instantiator/MethodMirror, but sort returned methods

    record SortedInstantiator(Instantiator instantiator) implements Instantiator {

        @Override
        public MethodMirror getMethodMirror() {
            return new SortedMethodMirror(instantiator.getMethodMirror());
        }

        @Override
        public boolean hasProduced(@NonNull Object instance) {
            return instantiator.hasProduced(instance);
        }

        @Override
        public @NonNull Object generate(@NonNull Class<?> @NonNull [] targets, @NonNull MethodYield methodYield) {
            return instantiator.generate(targets, methodYield);
        }

        @Override
        public @NonNull <I> ReloadShell<I> generateShell(@NonNull Class<I> iface) {
            return instantiator.generateShell(iface);
        }

        @Override
        public <I> @NonNull I generateEmpty(@NonNull Class<I> iface) {
            return instantiator.generateEmpty(iface);
        }
    }

    record SortedMethodMirror(MethodMirror methodMirror) implements MethodMirror {

        @Override
        public @NonNull TypeWalker typeWalker(ReifiedType.@NonNull Annotated reifiedType) {
            return new SortedTypeWalker(methodMirror.typeWalker(reifiedType));
        }

        @Override
        public @NonNull Invoker makeInvoker(@NonNull Object receiver, @NonNull Class<?> enclosingType) {
            return methodMirror.makeInvoker(receiver, enclosingType);
        }
    }

    record SortedTypeWalker(MethodMirror.TypeWalker delegate) implements MethodMirror.TypeWalker {

        @Override
        public ReifiedType.@NonNull Annotated getEnclosingType() {
            return delegate.getEnclosingType();
        }

        @Override
        public @NonNull Stream<@NonNull MethodId> getViableMethods() {
            return delegate.getViableMethods().sorted(Comparator.comparing(MethodId::name));
        }

        @Override
        public @NonNull AnnotatedElement getAnnotations(@NonNull MethodId methodId) {
            return delegate.getAnnotations(methodId);
        }

        @Override
        public MethodMirror.@NonNull TypeWalker @NonNull [] getSuperTypes() {
            MethodMirror.TypeWalker[] superTypes = delegate.getSuperTypes();
            for (int n = 0; n < superTypes.length; n++) {
                superTypes[n] = new SortedTypeWalker(superTypes[n]);
            }
            return superTypes;
        }
    }
}
