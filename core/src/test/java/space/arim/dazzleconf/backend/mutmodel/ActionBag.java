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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ActionBag<S, M> {

    final List<GenerateAction<S, M, ?>> generateActions;
    final List<ModifyAction<S, M, ?>> modifyActions;
    final List<ObserveAction<S, M, ?>> observeActions;
    final List<ProduceAction<S, M, ?>> produceActions;
    final List<TransferAction<S, M>> transferActions;

    ActionBag(Builder<S, M> builder) {
        generateActions = List.copyOf(builder.generateActions);
        modifyActions = List.copyOf(builder.modifyActions);
        observeActions = List.copyOf(builder.observeActions);
        produceActions = List.copyOf(builder.produceActions);
        transferActions = List.copyOf(builder.transferActions);
    }

    public static final class Builder<S, M> {

        private final List<GenerateAction<S, M, ?>> generateActions = new ArrayList<>();
        private final List<ModifyAction<S, M, ?>> modifyActions = new ArrayList<>();
        private final List<ObserveAction<S, M, ?>> observeActions = new ArrayList<>();
        private final List<ProduceAction<S, M, ?>> produceActions = new ArrayList<>();
        private final List<TransferAction<S, M>> transferActions = new ArrayList<>();

        @SafeVarargs
        public final Builder<S, M> generate(GenerateAction<S, M, ?>... actions) {
            generateActions.addAll(Arrays.asList(actions));
            return this;
        }

        @SafeVarargs
        public final Builder<S, M> modify(ModifyAction<S, M, ?>... actions) {
            modifyActions.addAll(Arrays.asList(actions));
            return this;
        }

        @SafeVarargs
        public final Builder<S, M> observe(ObserveAction<S, M, ?>... actions) {
            observeActions.addAll(Arrays.asList(actions));
            return this;
        }

        @SafeVarargs
        public final Builder<S, M> produce(ProduceAction<S, M, ?>... actions) {
            produceActions.addAll(Arrays.asList(actions));
            return this;
        }

        @SafeVarargs
        public final Builder<S, M> transfer(TransferAction<S, M>... actions) {
            transferActions.addAll(Arrays.asList(actions));
            return this;
        }

        public ActionBag<S, M> build() {
            return new ActionBag<>(this);
        }
    }
}
