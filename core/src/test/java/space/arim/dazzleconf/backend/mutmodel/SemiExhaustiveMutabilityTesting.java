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

import org.junit.jupiter.api.DynamicTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.IntSupplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class SemiExhaustiveMutabilityTesting<S, M> {

    private final Random random;
    private final ActionBag<S, M> actions;

    protected SemiExhaustiveMutabilityTesting(long seed, ActionBag<S, M> actions) {
        random = new Random(seed);
        this.actions = actions;
    }

    protected Stream<DynamicTest> testAll(int subjectCount, int stepCount) {
        int genActionCount = actions.generateActions.size();
        // 3 int's, of: 1) initial gen action 2) initial gen arg 3) final observe action
        Stream<int[]> framings;
        {
            List<Integer> finalObserveActions = new ArrayList<>();
            for (int observeAction = 0; observeAction < actions.observeActions.size(); observeAction++) {
                if (actions.observeActions.get(observeAction).isAggregate()) {
                    finalObserveActions.add(observeAction);
                }
            }
            framings = IntStream.range(0, genActionCount)
                    .mapToObj(genActionIdx -> {
                        int argumentCount = actions.generateActions.get(genActionIdx).argumentUniverse.size();
                        return IntStream.range(0, argumentCount).mapToObj(argumentIdx -> {
                            return finalObserveActions.stream().map(observeActionIdx -> {
                                return new int[] {genActionIdx, argumentIdx, observeActionIdx};
                            });
                        });
                    })
                    .flatMap(s -> s)
                    .flatMap(s -> s);
        }
        Stream<Plan<S, M>> plans;
        {
            int produceActionCount = actions.produceActions.size();
            int genAndProduceActionCount = genActionCount + produceActionCount;
            plans = framings.flatMap(framing -> {
                // Warning: the values in these int arrays are offset (need to use + 1)
                Stream<int[]> genRemainingSubjectsWhere = Combinatorics.combination(stepCount, subjectCount - 1);
                return genRemainingSubjectsWhere.flatMap(genRemSubjectsWhere -> {
                    Stream<int[]> genRemainingSubjectsHow = Combinatorics.exponential(genAndProduceActionCount, subjectCount - 1);
                    return genRemainingSubjectsHow.flatMap(genRemSubjectsHow -> {
                        return genPlans(
                                subjectCount, stepCount, framing, genRemSubjectsWhere, genRemSubjectsHow
                        );
                    });
                });
            });
        }
        return plans.map(plan -> DynamicTest.dynamicTest(plan.toString(), plan::execute));
    }

    private Stream<Plan<S, M>> genPlans(int subjectCount, int stepCount, int[] framing,
                                        int[] genRemSubjectsWhere, int[] genRemSubjectsHow) {
        class Calc {

            @SuppressWarnings("unchecked")
            private Plan<S, M>[] output = new Plan[256];
            private int outputIdx;

            private Plan.Draft<S, M> current;
            private final int finalObserveAction;

            Calc(int[] framing, int stepCount) {
                current = new Plan.Draft<>(actions.generateActions.get(framing[0]), framing[1], stepCount);
                finalObserveAction = framing[2];
            }

            <A> void addOutput(ObserveAction<S, M, Void> finalObserveAction) {
                Plan<S, M> plan = current.build(subjectCount, finalObserveAction);
                int outputIdx = this.outputIdx++;
                if (outputIdx == output.length) {
                    output = Arrays.copyOf(output, output.length * 2);
                }
                output[outputIdx] = plan;
            }

            void compute() {
                computeGenSubject(1);
            }

            private void withForked(Plan.Draft<S, M> forked, Runnable what) {
                Plan.Draft<S, M> previous = current;
                try {
                    current = forked;
                    what.run();
                } finally {
                    current = previous;
                }
            }

            private void computeGenSubject(int subjSlot) {
                if (subjSlot > genRemSubjectsWhere.length) {
                    computeExtraSteps();
                    return;
                }
                int genWhere = genRemSubjectsWhere[subjSlot - 1] + 1;
                int genHow = genRemSubjectsHow[subjSlot - 1];
                if (genHow < actions.generateActions.size()) {
                    pickOneArg(actions.generateActions.get(genHow), new WithGenerateActionArg<S, M>() {
                        @Override
                        public <A> void withArg(GenerateAction<S, M, A> action, A arg) {
                            GenerateStep<S, M, ?> genStep = new GenerateStep<>(subjSlot, action, arg);
                            withForked(current.forkAndSet(genWhere, genStep), () -> {
                                computeGenSubject(subjSlot + 1);
                            });
                        }
                    });
                } else {
                    ProduceAction<S, M, ?> produceAction = actions.produceActions.get(genHow - actions.generateActions.size());
                    pickOneArg(produceAction, new WithProduceActionArg<S, M>() {
                        @Override
                        public <A> void withArg(ProduceAction<S, M, A> action, A arg) {
                            for (int prevSlot = 0; prevSlot < subjSlot; prevSlot++) {
                                ProduceStep<S, M, A> produceStep = new ProduceStep<>(prevSlot, subjSlot, action, arg);
                                withForked(current.forkAndSet(genWhere, produceStep), () -> {
                                    computeGenSubject(subjSlot + 1);
                                });
                            }
                        }
                    });
                }
            }

            private void computeExtraSteps() {
                int unsetStep = current.getFirstUnset();
                if (unsetStep == -1) {
                    // Finished!
                    @SuppressWarnings("unchecked")
                    ObserveAction<S, M, Void> finalObserveAction = (ObserveAction<S, M, Void>) actions.observeActions.get(this.finalObserveAction);
                    addOutput(finalObserveAction);
                    return;
                }
                int maxAvailableSubjectForOps = maxAvailableSubjectForOperationsIn(unsetStep);
                IntSupplier getRandomSubject = () -> {
                    return random.nextInt(maxAvailableSubjectForOps + 1);
                };
                boolean[] mutableSubjects = current.calcMutableSubjectsPerStep(subjectCount, unsetStep)[unsetStep];

                int kindHere = random.nextInt(3);
                switch (kindHere) {
                    case 0:
                        for (ModifyAction<S, M, ?> modifyAction : actions.modifyActions) {
                            pickOneArg(modifyAction, new WithModifyActionArg<S, M>() {
                                @Override
                                public <A> void withArg(ModifyAction<S, M, A> action, A arg) {
                                    int subject = getRandomSubject.getAsInt();
                                    if (mutableSubjects[subject]) {
                                        ModifyStep<S, M, ?> modifyStep = new ModifyStep<>(subject, action, arg);
                                        withForked(current.forkAndSet(unsetStep, modifyStep), Calc.this::computeExtraSteps);
                                    }
                                }
                            });
                        }
                        break;
                    case 1:
                        for (ObserveAction<S, M, ?> observeAction : actions.observeActions) {
                            pickOneArg(observeAction, new WithObserveActionArg<S, M>() {
                                @Override
                                public <A> void withArg(ObserveAction<S, M, A> action, A arg) {
                                    int subject = getRandomSubject.getAsInt();
                                    withForked(
                                            current.forkAndSet(unsetStep, new ObserveStep<>(subject, action, arg)),
                                            Calc.this::computeExtraSteps
                                    );
                                }
                            });
                        }
                        break;
                    case 2:
                        for (TransferAction<S, M> transferAction : actions.transferActions) {
                            int subjectTo = getRandomSubject.getAsInt();
                            if (!mutableSubjects[subjectTo]) {
                                continue;
                            }
                            int subjectFrom = getRandomSubject.getAsInt();
                            withForked(
                                    current.forkAndSet(unsetStep, new TransferStep<>(subjectFrom, subjectTo, transferAction)),
                                    this::computeExtraSteps
                            );
                        }
                        break;
                }
            }

            private int maxAvailableSubjectForOperationsIn(int stepSlot) {
                int maxAvail = 0;  // initial subject always available
                for (int subjExtra = 1; subjExtra < subjectCount; subjExtra++) {
                    if (genRemSubjectsWhere[subjExtra - 1] + 1 > stepSlot) {
                        // genRemSubjectsWhere is a combination, so strictly increasing, meaning we can exit early
                        break;
                    }
                    maxAvail = subjExtra;
                }
                return maxAvail;
            }
        }
        Calc calc = new Calc(framing, stepCount);
        calc.compute();
        return Arrays.stream(calc.output, 0, calc.outputIdx).filter(Plan::checkInterdependent);
    }

    private <A> A pickOneOf(List<A> argumentUniverse) {
        return argumentUniverse.get(random.nextInt(argumentUniverse.size()));
    }

    private <A> void pickOneArg(GenerateAction<S, M, A> action, WithGenerateActionArg<S, M> withArg) {
        withArg.withArg(action, pickOneOf(action.argumentUniverse));
    }

    private <A> void pickOneArg(ModifyAction<S, M, A> action, WithModifyActionArg<S, M> withArg) {
        withArg.withArg(action, pickOneOf(action.argumentUniverse));
    }

    private <A> void pickOneArg(ObserveAction<S, M, A> action, WithObserveActionArg<S, M> withArg) {
        withArg.withArg(action, pickOneOf(action.argumentUniverse));
    }

    private <A> void pickOneArg(ProduceAction<S, M, A> action, WithProduceActionArg<S, M> withArg) {
        withArg.withArg(action, pickOneOf(action.argumentUniverse));
    }

    private interface WithGenerateActionArg<S, M> {
        <A> void withArg(GenerateAction<S, M, A> action, A arg);
    }

    private interface WithModifyActionArg<S, M> {
        <A> void withArg(ModifyAction<S, M, A> action, A arg);
    }

    private interface WithObserveActionArg<S, M> {
        <A> void withArg(ObserveAction<S, M, A> action, A arg);
    }

    private interface WithProduceActionArg<S, M> {
        <A> void withArg(ProduceAction<S, M, A> action, A arg);
    }
}
