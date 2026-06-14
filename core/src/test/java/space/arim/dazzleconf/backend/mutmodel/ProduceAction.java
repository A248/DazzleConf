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
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

public final class ProduceAction<S, M, A> extends Action<A> {

    final BiFunction<S, A, S> produce;
    final BiFunction<M, A, M> produceModel;
    final boolean outputIsMutable;

    public ProduceAction(String signature, List<A> argumentUniverse, BiFunction<S, A, S> produce, BiFunction<M, A, M> produceModel, boolean outputIsMutable) {
        super(signature, argumentUniverse);
        this.produce = produce;
        this.produceModel = produceModel;
        this.outputIsMutable = outputIsMutable;
    }

    public static <S, M> ProduceAction<S, M, Void> aggregate(
            String signature, UnaryOperator<S> produce, UnaryOperator<M> produceModel, boolean outputIsMutable
    ) {
        return new ProduceAction<>(
                signature, Action.NULL_ARG, (s, arg) -> produce.apply(s),
                (m, arg) -> produceModel.apply(m), outputIsMutable
        );
    }
}
