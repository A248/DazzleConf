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

package space.arim.dazzleconf.engine.liaison;

import space.arim.dazzleconf.Configuration;
import space.arim.dazzleconf.reflect.TypeToken;

import java.lang.invoke.MethodHandles;

interface StringType {
    String value();

    static <V> Configuration<V> configuration(TypeToken<V> configType) {
        return Configuration.defaultBuilder(configType)
                .addTypeLiaisons(new StringTypeLiaison<>(TrimOnDeser.class, TrimOnDeser::new))
                .addTypeLiaisons(new StringTypeLiaison<>(ClipOnSer.class, ClipOnSer::new))
                .lookup(MethodHandles.lookup())
                .build();
    }
}
