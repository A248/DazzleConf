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

import java.util.List;
import java.util.function.Function;

public final class GenerateAction<S, M, A> extends Action<A> {

    final Function<A, S> generate;
    final Function<A, M> generateModel;
    final boolean outputIsMutable;

    public GenerateAction(String signature, List<A> argumentUniverse, Function<A, S> generate, Function<A, M> generateModel, boolean outputIsMutable) {
        super(signature, argumentUniverse);
        this.generate = generate;
        this.generateModel = generateModel;
        this.outputIsMutable = outputIsMutable;
    }
}
