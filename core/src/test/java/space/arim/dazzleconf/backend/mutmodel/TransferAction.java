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

import java.util.function.BiConsumer;

public final class TransferAction<S, M> extends Action<Void> {

    final BiConsumer<S, S> transfer;
    final BiConsumer<M, M> transferModel;

    public TransferAction(String signature, BiConsumer<S, S> transfer, BiConsumer<M, M> transferModel) {
        super(signature, Action.NULL_ARG);
        this.transfer = transfer;
        this.transferModel = transferModel;
    }

}
