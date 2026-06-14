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

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyPathAsSequenceIT {

    /*

    DYNAMIC TESTS

    These tests perform miscellaneous calls to the CharSequence implementation.

     */

    private record TestCandidate(String string, CharSequence keyPathSequence) {
        ReadyTest test(Function<CharSequence, ?> operation) {
            return testWith(operation.toString(), operation);
        }

        ReadyTest testWith(String name, Function<CharSequence, ?> operation) {
            return new ReadyTest(
                    name,
                    () -> new DoubleCall(
                            this,
                            CallResult.generate(string, operation),
                            CallResult.generate(keyPathSequence, operation)
                    )
            );
        }
    }

    record ReadyTest(String name, Supplier<DoubleCall> execute) {}

    record CallResult(Object returnVal, Throwable exception) {
        static CallResult generate(CharSequence subject, Function<CharSequence, ?> operation) {
            Object returnVal;
            try {
                returnVal = operation.apply(subject);
            } catch (Throwable ex) {
                return new CallResult(null, ex);
            }
            return new CallResult(returnVal, null);
        }
    }

    record DoubleCall(TestCandidate candidate, CallResult onString, CallResult onKeyPathSequence) {}

    private static final class ArrayEq {

        private final int[] arr;

        private ArrayEq(int[] arr) {
            this.arr = arr;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ArrayEq arrayEq)) return false;
            return Arrays.equals(arr, arrayEq.arr);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(arr);
        }

        @Override
        public String toString() {
            return "ArrayEq{" + Arrays.toString(arr) + '}';
        }
    }

    @TestFactory
    public Stream<DynamicNode> charSequenceCalls() {
        return Stream.of(
                new String[0], new String[] {"key"}, new String[] {"k", "path", "forever"},
                new String[] {"loooooong", "path"}, new String[] {"so", "many", "elements", "it", "is", "insanity"}
        ).flatMap(pathArray -> Stream.of(
                new KeyPath.Immut(pathArray.clone()), new KeyPath.Mut(pathArray)
        )).map(keyPath -> {
            return new TestCandidate(keyPath.printString(), keyPath.asCharSequence());
        }).flatMap(candidate -> {
            int startLen = -2;
            int endLen = candidate.string.length() + 2;
            Stream<ReadyTest> simpleTests = Stream.of(
                    candidate.test(CharSequence::isEmpty),
                    candidate.test(CharSequence::length),
                    candidate.test(CharSequence::toString),
                    candidate.test(seq -> new ArrayEq(seq.chars().toArray())),
                    candidate.test(seq -> new ArrayEq(seq.codePoints().toArray()))
            );
            Stream<ReadyTest> charAtTests = IntStream.range(startLen, endLen).mapToObj(charAtIdx -> {
                return candidate.test(seq -> seq.charAt(charAtIdx));
            });
            Stream<ReadyTest> subSequenceTests;
            {
                record Range(int start, int end) {}

                subSequenceTests = IntStream.range(startLen, endLen).boxed().flatMap(start -> {
                    return IntStream.range(startLen, endLen).mapToObj(end -> new Range(start, end));
                }).map((range) -> {
                    return candidate.test(seq -> {
                        CharSequence subSeq = seq.subSequence(range.start, range.end);
                        return subSeq.toString();
                    });
                });
            }
            return Stream.concat(Stream.concat(simpleTests, charAtTests), subSequenceTests);

        }).map(readyTest -> {
            return DynamicTest.dynamicTest(readyTest.name, () -> {
                DoubleCall doubleCall = readyTest.execute.get();
                Throwable stringException = doubleCall.onString.exception;
                Throwable ourException = doubleCall.onKeyPathSequence.exception;
                if (doubleCall.onString.exception == null) {
                    assertNull(ourException, "Expected no exception on " + doubleCall.candidate);
                    assertEquals(doubleCall.onString.returnVal, doubleCall.onKeyPathSequence.returnVal, "Expected on " + doubleCall.candidate);
                } else {
                    assertNotNull(ourException, () -> "Expected to see a " + doubleCall.onString.exception + " on " + doubleCall.candidate);
                    assertTrue(stringException.getClass().isInstance(ourException) || ourException.getClass().isInstance(stringException),
                            () -> "Expected similar exception type to " + stringException + ", but was " + ourException);
                }
            });
        });
    }
}
