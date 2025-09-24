/*
 * DazzleConf
 * Copyright © 2021 Anand Beh
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

package space.arim.dazzleconf.core.it.jpms.exported;

import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf.LoadResult;
import space.arim.dazzleconf.engine.DeserializeInput;
import space.arim.dazzleconf.engine.SerializeDeserialize;
import space.arim.dazzleconf.engine.SerializeOutput;

public final class CustomType {

    private final String value;

    public CustomType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static class Serializer implements SerializeDeserialize<CustomType> {

        @Override
        public @NonNull LoadResult<@NonNull CustomType> deserialize(@NonNull DeserializeInput deser) {
            return deser.requireString().map(CustomType::new);
        }

        @Override
        public void serialize(@NonNull CustomType value, @NonNull SerializeOutput ser) {
            ser.outString(value.value);
        }
    }
}
