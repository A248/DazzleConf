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

package space.arim.dazzleconf.backend;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public final class KeyPathVerify {

    private final BiConsumer<KeyPath, String[]> verify;

    KeyPathVerify(BiConsumer<KeyPath, String[]> verify) {
        this.verify = verify;
    }

    public void assertEq(KeyPath actual, String...expected) {
        verify.accept(actual, expected);
    }

    public static class Provider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) throws Exception {
            return Stream.of(
                    (BiConsumer<KeyPath, String[]>) (keyPath, content) -> assertEquals(String.join(".", content), keyPath.toString()),
                    (keyPath, content) -> assertArrayEquals(content, keyPath.intoParts()),
                    (keyPath, content) -> assertEquals(List.of(content), keyPath.intoPartsList()),
                    (keyPath, content) -> {
                        CharSequence leading = keyPath.getLeading(KeyPath.SequenceBoundary.FRONT);
                        if (content.length == 0) {
                            assertNull(leading);
                        } else {
                            assertEquals(content[0], leading);
                        }
                    },
                    (keyPath, content) -> {
                        CharSequence trailing = keyPath.getLeading(KeyPath.SequenceBoundary.BACK);
                        if (content.length == 0) {
                            assertNull(trailing);
                        } else {
                            assertEquals(content[content.length - 1], trailing);
                        }
                    },
                    (keyPath, content) -> {
                        List<CharSequence> output = new ArrayList<>();
                        keyPath.forEach(output::add);
                        assertEquals(List.of(content), output);
                    },
                    (keyPath, content) -> {
                        List<CharSequence> output = new ArrayList<>();
                        keyPath.iterateFrom(KeyPath.SequenceBoundary.FRONT, output::add);
                        assertEquals(List.of(content), output);
                    },
                    (keyPath, content) -> {
                        List<CharSequence> output = new ArrayList<>();
                        keyPath.iterateFrom(KeyPath.SequenceBoundary.BACK, output::add);
                        Collections.reverse(output);
                        assertEquals(List.of(content), output);
                    }
            ).map(KeyPathVerify::new).map(Arguments::of);
        }
    }
}
