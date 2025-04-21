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

import space.arim.dazzleconf2.ConfigurationReadWrite;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.DataTreeMut;
import space.arim.dazzleconf2.reflect.TypeToken;

public final class SubConfigTypeLiaison implements TypeLiaison {
    @Override
    public <V> Agent<V> makeAgent(TypeToken<V> typeToken, Handshake handshake) {
        if (typeToken.getReifiedType().getAnnotation(SubSection.class) != null) {
            return new AgentImpl<>(handshake.getConfiguration(typeToken));
        }
        return null;
    }

    static class AgentImpl<V> implements Agent<V> {

        private final ConfigurationReadWrite<V> configuration;

        AgentImpl(ConfigurationReadWrite<V> configuration) {
            this.configuration = configuration;
        }

        @Override
        public DefaultValues<V> loadDefaultValues() {
            return new DefaultValues<>() {
                @Override
                public V defaultValue() {
                    return configuration.loadDefaults();
                }

                @Override
                public V ifMissing() {
                    return configuration.loadDefaults();
                }
            };
        }

        @Override
        public SerializeDeserialize<V> makeSerializer() {
            return new SerDer();
        }

        class SerDer implements SerializeDeserialize<V> {

            @Override
            public LoadResult<V> deserialize(DeserializeInput deser) {
                return deser.requireDataTree().flatMap((dataTree -> {
                    return configuration.readWithKeyMapper(dataTree, deser::flagUpdate, deser.keyMapper());
                }));
            }

            @Override
            public void serialize(V value, SerializeOutput ser) {
                DataTreeMut dataTreeMut = new DataTreeMut();
                configuration.writeWithKeyMapper(value, dataTreeMut, ser.keyMapper());
                ser.outDataTree(dataTreeMut);
            }
        }
    }
}
