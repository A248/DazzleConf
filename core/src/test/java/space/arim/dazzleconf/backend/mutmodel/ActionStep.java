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

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Objects;

interface ActionStep<S, M, A> {

    Action<A> action();

    String details();

    void execute(S[] subjects, M[] models);

    default void insertSubj(S[] subjects, int slot, S subject) {
        Objects.requireNonNull(subject, "subject");
        subjects[slot] = subject;
    }

    default void insertModel(M[] models, int slot, M model) {
        Objects.requireNonNull(model, "model");
        models[slot] = model;
    }

    @Nullable A argument();

    default int argumentIndex() {
        A argument = argument();
        List<A> argUniverse = action().argumentUniverse;
        for (int n = 0; n < argUniverse.size(); n++) {
            if (argument == argUniverse.get(n)) {
                return n;
            }
        }
        throw new IllegalStateException();
    }
}
