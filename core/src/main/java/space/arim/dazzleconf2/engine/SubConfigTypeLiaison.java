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
    public <V> Agent<V> makeAgent(TypeToken<V> typeToken, Handshake<V> handshake) {
        if (typeToken.getReifiedType().getAnnotation(SubSection.class) != null) {
            return new AgentImpl<>(handshake.childConfiguration());
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
            V defaults = configuration.loadDefaults();
            return new DefaultValues<>() {
                @Override
                public V defaultValue() {
                    return defaults;
                }

                @Override
                public V ifMissing() {
                    return defaults;
                }
            };
        }

        @Override
        public SerializeDeserialize<V> makeSerializer() {
            return new SerDer();
        }

        class SerDer implements SerializeDeserialize<V> {

            @Override
            public LoadResult<V> deserialize(OperableObject object) {
                return object.requireDataTree().flatMap((dataTree -> {
                    return configuration.readWithKeyMapper(dataTree, object::flagUpdate, object.keyMapper());
                }));
            }

            @Override
            public void serialize(V value, SerializeOutput output) {
                DataTreeMut dataTreeMut = new DataTreeMut();
                configuration.writeWithKeyMapper(value, dataTreeMut, output.keyMapper());
                output.outDataTree(dataTreeMut);
            }
        }
    }
}
