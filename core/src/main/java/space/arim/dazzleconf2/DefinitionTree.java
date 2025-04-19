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

package space.arim.dazzleconf2;

import space.arim.dazzleconf2.engine.DefaultValues;
import space.arim.dazzleconf2.engine.SerializeDeserialize;
import space.arim.dazzleconf2.engine.TypeLiaison;
import space.arim.dazzleconf2.reflect.MethodId;

final class DefinitionTree {

    static final class Node {

        private final boolean optional;
        private final MethodId methodId;
        private final DefaultValues<?> agent; // null if node is optional
        private final SerializeDeserialize<?> serializer;

        Node(boolean optional, MethodId methodId, DefaultValues<?> agent, SerializeDeserialize<?> serializer) {
            this.optional = optional;
            this.methodId = methodId;
            this.agent = agent;
            this.serializer = serializer;
        }
    }
}
