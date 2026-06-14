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
import java.util.function.BiConsumer;

public final class ModifyAction<S, M, A> extends Action<A> {

    final BiConsumer<S, A> modify;
    final BiConsumer<M, A> modifyModel;

    public ModifyAction(String signature, List<A> argumentUniverse, BiConsumer<S, A> modify, BiConsumer<M, A> modifyModel) {
        super(signature, argumentUniverse);
        this.modify = modify;
        this.modifyModel = modifyModel;
    }

}
