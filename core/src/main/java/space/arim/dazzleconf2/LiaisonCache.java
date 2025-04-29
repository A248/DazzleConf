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

import space.arim.dazzleconf2.engine.SerializeDeserialize;
import space.arim.dazzleconf2.engine.TypeLiaison;
import space.arim.dazzleconf2.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class LiaisonCache {

    private final Map<TypeToken<?>, Cache<?>> cachedAgents = new HashMap<>();
    private final List<TypeLiaison> typeLiaisons;

    LiaisonCache(List<TypeLiaison> typeLiaisons) {
        this.typeLiaisons = typeLiaisons;
    }

    <V> Cache<V> requestInfo(TypeToken<V> typeToken, TypeLiaison.Handshake handshake) {
        // Don't use computeIfAbsent - reduce stack depth
        @SuppressWarnings("unchecked")
        Cache<V> cached = (Cache<V>) cachedAgents.get(typeToken);
        if (cached != null) {
            return cached;
        }
        // None found, so query the type liaisons
        for (TypeLiaison liaison : typeLiaisons) {
            TypeLiaison.Agent<V> agent = liaison.makeAgent(typeToken, handshake);
            if (agent != null) {
                cached = new Cache<>(agent, agent.makeSerializer());
                cachedAgents.put(typeToken, cached);
                return cached;
            }
        }
        throw new DeveloperMistakeException(
                "Failed to resolve agent for " + typeToken + ". Please add a TypeLiaison or serializer for this type."
        );
    }

    static final class Cache<V> {

        final TypeLiaison.Agent<V> agent;
        final SerializeDeserialize<V> serializer;

        private Cache(TypeLiaison.Agent<V> agent, SerializeDeserialize<V> serializer) {
            this.agent = agent;
            this.serializer = serializer;
        }
    }
}
