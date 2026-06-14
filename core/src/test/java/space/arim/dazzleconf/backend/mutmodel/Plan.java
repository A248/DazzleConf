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

import org.opentest4j.AssertionFailedError;

import java.util.Arrays;
import java.util.Objects;

final class Plan<S, M> {

    private final int subjectCount;
    private final ActionStep<S, M, ?>[] steps;
    private final ObserveAction<S, M, Void> finalObserve;

    private Plan(int subjectCount, ActionStep<S, M, ?>[] steps, ObserveAction<S, M, Void> finalObserve) {
        this.subjectCount = subjectCount;
        this.steps = steps;
        this.finalObserve = finalObserve;
    }

    boolean checkInterdependent() {
        boolean[][] islands = new boolean[subjectCount][];
        for (ActionStep<S, M, ?> step : steps) {
            if (step instanceof GenerateStep<?, ?, ?> generateStep) {
                int subject = generateStep.slot();
                boolean[] island = islands[subject];
                if (island == null) {
                    island = new boolean[subjectCount];
                    island[subject] = true;
                    islands[subject] = island;
                }
            }
            if (step instanceof ProduceStep<?, ?, ?> produceStep) {
                int from = produceStep.inputSlot();
                int to = produceStep.outputSlot();
                boolean[] island = islands[from];
                assert island[from] : "a subject's island contains itself";
                island[to] = true;
                islands[to] = island;
            }
            if (step instanceof TransferStep<?, ?> transferStep) {
                int from = transferStep.fromSlot();
                int to = transferStep.toSlot();
                // Merge existing sets
                boolean[] islandFrom = islands[from];
                boolean[] islandTo = islands[to];
                assert islandTo != null : "Null island for " + to + " in " + this;
                assert islandFrom[from] : "a subject's island contains itself";
                assert islandTo[to] : "a subject's island contains itself";
                for (int subj = 0; subj < subjectCount; subj++) {
                    islandFrom[subj] |= islandTo[subj];
                }
                islands[to] = islandFrom;
            }
        }
        boolean[] oneIsland = null;
        for (boolean[] island : islands) {
            if (oneIsland == null) {
                oneIsland = island;
            } else if (oneIsland != island) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private static <V> V[] arrayGeneric(int count) {
        return (V[]) new Object[count];
    }

    void execute() {
        S[] subjects = arrayGeneric(subjectCount);
        M[] models = arrayGeneric(subjectCount);
        int step = 0;
        try {
            while (step < steps.length) {
                steps[step].execute(subjects, models);
                step++;
            }
        } catch (AssertionFailedError ex) {
            throw new AssertionFailedError("Failed to execute step " + step + " of " + this, ex);
        } catch (NullPointerException ex) {
            System.out.println("subjects = " + Arrays.toString(subjects));
            System.out.println("models = " + Arrays.toString(models));
            System.out.println("" + this);
            throw ex;
        }
        int subject = 0;
        try {
            while (subject < subjects.length) {
                finalObserve.observe.observe(subjects[subject], models[subject], null);
                subject++;
            }
        } catch (AssertionFailedError ex) {
            throw new AssertionFailedError(
                    "Failed final observation " + finalObserve.signature() + " for subject " + subject + " of " + this, ex
            );
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("action plan; ");
        builder.append(steps.length);
        builder.append(" steps.");
        builder.append("\n================================================================");
        for (int n = 0; n < steps.length; n++) {
            var step = steps[n];
            builder.append('\n');
            builder.append("Step ");
            builder.append(n);
            builder.append(": ");
            builder.append(step.action().signature());
            builder.append("; ");
            builder.append(step.details());
            Object argument = step.argument();
            if (argument != null) {
                builder.append("; ");
                builder.append("with argument ");
                builder.append(step.argumentIndex());
                builder.append(' ');
                builder.append(argument);
            }
        }
        builder.append("\n================================================================");
        return builder.toString();
    }

    static final class Draft<S, M> {

        private final ActionStep<S, M, ?>[] steps;
        private int firstUnset = 1;

        private Draft(Draft<S, M> forkFrom) {
            steps = forkFrom.steps.clone();
            firstUnset = forkFrom.firstUnset;
        }

        <A> Draft(GenerateAction<S, M, A> initialGenAction, int initialGenArgIdx, int stepCount) {
            @SuppressWarnings("unchecked")
            ActionStep<S, M, ?>[] steps = new ActionStep[stepCount + 1];
            steps[0] = new GenerateStep<>(0, initialGenAction, initialGenAction.argumentUniverse.get(initialGenArgIdx));
            this.steps = steps;
        }

        // Map of slot -> subject -> boolean
        boolean[][] calcMutableSubjectsPerStep(int subjectCount, int upToStep) {
            boolean[][] map = new boolean[upToStep + 1][];
            boolean[] currentPerSubject = new boolean[subjectCount];
            Arrays.fill(currentPerSubject, true);

            for (int stepIdx = 0; stepIdx <= upToStep; stepIdx++) {
                ActionStep<S, M, ?> step = steps[stepIdx];
                if (step instanceof GenerateStep<?, ?, ?> generateStep) {
                    int affectedSubject = generateStep.slot();
                    boolean outputIsMutable = generateStep.action().outputIsMutable;
                    if (currentPerSubject[affectedSubject] != outputIsMutable) {
                        currentPerSubject = currentPerSubject.clone();
                        currentPerSubject[affectedSubject] = outputIsMutable;
                    }

                } else if (step instanceof ProduceStep<?, ?, ?> produceStep) {
                    int affectedSubject = produceStep.outputSlot();
                    boolean outputIsMutable = produceStep.action().outputIsMutable;
                    if (currentPerSubject[affectedSubject] != outputIsMutable) {
                        currentPerSubject = currentPerSubject.clone();
                        currentPerSubject[affectedSubject] = outputIsMutable;
                    }
                }
                map[stepIdx] = currentPerSubject;
            }
            return map;
        }

        private void set(int stepSlot, ActionStep<S, M, ?> step) {
            steps[stepSlot] = step;
            if (stepSlot == firstUnset) {
                int nextUnset = firstUnset;
                do {
                    nextUnset++;
                    if (nextUnset == steps.length) {
                        nextUnset = -1;
                        break;
                    }
                } while (steps[nextUnset] != null);
                firstUnset = nextUnset;
            }
        }

        int getFirstUnset() {
            return firstUnset;
        }

        Draft<S, M> forkAndSet(int stepSlot, ActionStep<S, M, ?> step) {
            if (stepSlot == 0) {
                throw new IllegalStateException("First slot overidden");
            }
            Draft<S, M> forked = new Draft<>(this);
            forked.set(stepSlot, step);
            return forked;
        }

        Plan<S, M> build(int subjectCount, ObserveAction<S, M, Void> finalObserveAction) {
            if (subjectCount <= 0) {
                throw new IllegalArgumentException("subject count <= 0");
            }
            for (var step : steps) {
                Objects.requireNonNull(step, "draft incomplete: some steps empty");
            }
            return new Plan<>(subjectCount, steps, finalObserveAction);
        }
    }
}
