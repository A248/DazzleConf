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

package space.arim.dazzleconf;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.dazzleconf.engine.DefaultValues;
import space.arim.dazzleconf.engine.SerializeDeserialize;
import space.arim.dazzleconf.engine.TypeLiaison;
import space.arim.dazzleconf.engine.liaison.IntegerLiaison;
import space.arim.dazzleconf.reflect.TypeToken;

import java.lang.invoke.MethodHandles;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InterfaceMethodDependentTest {

    private interface Config {

        int customMethod();
    }

    private interface ConfigExtend extends Config { }

    private interface LoadDefaults {

        DefaultValues<Integer> loadDefaultValues(TypeLiaison.DefaultInit defaultInit);

    }

    private record LiaisonImpl(LoadDefaults loadDefaults, IntegerLiaison integerLiaison) implements TypeLiaison {

        @Override
        public @Nullable <V> Agent<V> makeAgent(@NonNull TypeToken<V> typeToken, @NonNull Handshake handshake) {
            return Agent.matchOnToken(typeToken, int.class, () -> {
                @SuppressWarnings("unchecked")
                Agent<Integer> innerAgent = (Agent<Integer>) integerLiaison.makeAgent(typeToken, handshake);
                return new AgentImpl(innerAgent);
            });
        }

        private final class AgentImpl implements Agent<Integer> {

            private final Agent<Integer> innerAgent;

            private AgentImpl(Agent<Integer> innerAgent) {
                this.innerAgent = innerAgent;
            }

            @Override
            public @Nullable DefaultValues<Integer> loadDefaultValues(@NonNull DefaultInit defaultInit) {
                return loadDefaults.loadDefaultValues(defaultInit);
            }

            @Override
            public @NonNull SerializeDeserialize<Integer> makeSerializer() {
                return innerAgent.makeSerializer();
            }
        }
    }

    private static void loadConfigType(TypeToken<?> configType, LoadDefaults loadDefaults) {
        new ConfigurationBuilder<>(configType)
                .addTypeLiaisons(new LiaisonImpl(loadDefaults, new IntegerLiaison()))
                .lookup(MethodHandles.lookup())
                .build();
    }

    @Test
    public void enclosingTypeMatch(@Mock LoadDefaults loadDefaults) {
        when(loadDefaults.loadDefaultValues(any())).thenReturn(null);
        TypeToken<Config> configType = new TypeToken<>() {};
        loadConfigType(configType, loadDefaults);
        verify(loadDefaults).loadDefaultValues(argThat(argument -> argument.enclosingType().equals(configType)));
    }

    @Test
    public void enclosingTypeMatchExtended(@Mock LoadDefaults loadDefaults) {
        when(loadDefaults.loadDefaultValues(any())).thenReturn(null);
        TypeToken<ConfigExtend> configType = new TypeToken<>() {};
        loadConfigType(configType, loadDefaults);
        verify(loadDefaults).loadDefaultValues(argThat(argument -> argument.enclosingType().equals(configType)));
    }

    @Test
    public void labelMatch(@Mock LoadDefaults loadDefaults) {
        when(loadDefaults.loadDefaultValues(any())).thenReturn(null);
        loadConfigType(new TypeToken<Config>() {}, loadDefaults);
        verify(loadDefaults).loadDefaultValues(argThat(argument -> argument.label().equals("customMethod")));
    }
}
