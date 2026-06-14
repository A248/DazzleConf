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

record TransferStep<S, M>(int fromSlot, int toSlot, TransferAction<S, M> action) implements ActionStep<S, M, Void> {
    @Override
    public String details() {
        return "data into " + toSlot + " from " + fromSlot;
    }

    @Override
    public void execute(S[] subjects, M[] models) {
        action.transfer.accept(subjects[fromSlot], subjects[toSlot]);
        action.transferModel.accept(models[fromSlot], models[toSlot]);
    }

    @Override
    public @Nullable Void argument() {
        return null;
    }
}
