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

package space.arim.dazzleconf.backend.mutmodel;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CombinatoricsTest {

    @Test
    public void exponential() {
        assertEqual2dArraySet(
                new int[][] {
                        new int[] {0}, new int[] {1}, new int[] {2}, new int[] {3}, new int[] {4}
                },
                Combinatorics.exponential(5, 1)
        );
        assertEqual2dArraySet(
                new int[][] {
                        new int[] {0, 0}, new int[] {0, 1}, new int[] {0, 2},
                        new int[] {1, 0}, new int[] {1, 1}, new int[] {1, 2},
                        new int[] {2, 0}, new int[] {2, 1}, new int[] {2, 2}
                },
                Combinatorics.exponential(3, 2)
        );
    }

    @Test
    public void calcCombination() {
        assertEquals(6, Combinatorics.calcCombination(6, 1));
        assertEquals(15, Combinatorics.calcCombination(6, 2));
        assertEquals(20, Combinatorics.calcCombination(6, 3));
        assertEquals(15, Combinatorics.calcCombination(6, 4));
        assertEquals(6, Combinatorics.calcCombination(6, 5));
        assertEquals(1, Combinatorics.calcCombination(6, 6));
    }

    @Test
    public void combination() {
        assertEqual2dArraySet(
                new int[][] {
                        new int[] {0}, new int[] {1}, new int[] {2}, new int[] {3}, new int[] {4}, new int[] {5}
                },
                Combinatorics.combination(6, 1)
        );
        assertEqual2dArraySet(
                new int[][] {
                        new int[] {0, 1}, new int[] {0, 2}, new int[] {0, 3}, new int[] {0, 4}, new int[] {0, 5},
                        new int[] {1, 2}, new int[] {1, 3}, new int[] {1, 4}, new int[] {1, 5},
                        new int[] {2, 3}, new int[] {2, 4}, new int[] {2, 5},
                        new int[] {3, 4}, new int[] {3, 5},
                        new int[] {4, 5}
                },
                Combinatorics.combination(6, 2)
        );
    }

    private static void assertEqual2dArraySet(int[][] arrExpect, Stream<int[]> arrStream) {
        Set<IntArray> expect = Stream.of(arrExpect).map(IntArray::new).collect(Collectors.toSet());
        Set<IntArray> actual = arrStream.map(IntArray::new).collect(Collectors.toSet());
        assertEquals(expect, actual);
    }

    static class IntArray {

        private final int[] value;

        IntArray(int[] value) {
            this.value = value;
        }

        @Override
        public final boolean equals(Object o) {
            if (!(o instanceof IntArray intArray)) return false;

            return Arrays.equals(value, intArray.value);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(value);
        }
    }
}
