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

import java.util.Arrays;
import java.util.stream.Stream;

final class Combinatorics {

    private Combinatorics() {}

    private static long calcFactorial(int integer) {
        if (integer == 0) {
            return 1;
        }
        int calc = 1;
        for (int curr = integer; curr > 0; curr--) {
            calc *= curr;
        }
        return calc;
    }

    static Stream<int[]> exponential(int selection, int pickHowMany) {
        if (pickHowMany == 0) {
            return Stream.empty();
        }
        class Calculator {

            private final int[] progress = new int[pickHowMany];
            private final int[][] output = new int[(int) Math.pow(selection, pickHowMany)][];
            private int outputIdx;

            void gather(int currentDepth) {
                if (currentDepth == 0) {
                    output[outputIdx++] = progress.clone();
                    return;
                }
                currentDepth -= 1;
                for (int curr = 0; curr < selection; curr++) {
                    progress[currentDepth] = curr;
                    gather(currentDepth);
                }
            }
        }
        Calculator calculator = new Calculator();
        calculator.gather(pickHowMany);
        return Arrays.stream(calculator.output);
    }

    static Stream<int[]> combination(int selection, int pickHowMany) {
        /*
        Algorithm: Simulate a nested for loop of variable depth:
        for (int choice1 = 0; choice1 < selection; choice1++) {
          for (int choice2 = choice1 + 1; choice2 < selection; choice2++) {
            ...
          }
        }
         */
        int sizeRequired =  calcCombination(selection, pickHowMany);
        CombinationPick combinationPick = new CombinationPick(selection, pickHowMany, sizeRequired);
        combinationPick.gather(0, 0);
        return Arrays.stream(combinationPick.output);
    }

    static int calcCombination(int selection, int pickHowMany) {
        long value = calcFactorial(selection);
        value /= calcFactorial(selection - pickHowMany);
        value /= calcFactorial(pickHowMany);
        if (value >= 0 && value <= Integer.MAX_VALUE) {
            return (int) value;
        }
        throw new IllegalStateException("Combination too big");
    }

    private static final class CombinationPick {

        private final int selection;
        private final int pickHowMany;

        private final int[][] output;
        private int outputIdx;
        private final int[] gatheredSoFar;

        private CombinationPick(int selection, int pickHowMany, int sizeRequired) {
            this.selection = selection;
            this.pickHowMany = pickHowMany;

            output = new int[sizeRequired][];
            gatheredSoFar = new int[pickHowMany];
        }

        void gather(int currentDepth, int startHereFrom) {
            if (currentDepth == pickHowMany) {
                output[outputIdx++] = gatheredSoFar.clone();
                return;
            }
            for (int curr = startHereFrom; curr < selection; curr++) {
                gatheredSoFar[currentDepth] = curr;
                gather(currentDepth + 1, curr + 1);
            }
        }
    }

}
