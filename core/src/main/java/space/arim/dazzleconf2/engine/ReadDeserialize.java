/*
 * DazzleConf
 * Copyright © 2025 Anand Beh
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

package space.arim.dazzleconf2.engine;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf2.ErrorContext;

public interface ReadDeserialize<V> {

    @NonNull Scalar scalar();

    @NonNull Array array();

    @NonNull Tree tree();

    @Nullable V deserialize(ErrorContext.@NonNull Sink errorSink);

    @Nullable V deserializeUpdate(ErrorContext.@NonNull Sink errorSink, @NonNull SerializeOutput updateTo);

    interface Scalar {

        void onScalar(@NonNull Object value);

    }

    interface Array {

        @NonNull ReadDeserialize<Void> onElement(int index);

    }

    interface Tree {

        @NonNull ReadDeserialize<Void> onEntry(@NonNull Object key);

    }
}
