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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * An action is a stateless function operating in some way upon a subject
 *
 */
public abstract class Action<A> {

    private final String signature;
    final List<A> argumentUniverse;

    static final List<Void> NULL_ARG = Collections.singletonList(null);

    protected Action(String signature, List<A> argumentUniverse) {
        this.signature = Objects.requireNonNull(signature, "signature");
        this.argumentUniverse = argumentUniverse;
    }

    String signature() {
        return signature;
    }

    boolean isAggregate() {
        return argumentUniverse == Action.NULL_ARG;
    }

}
