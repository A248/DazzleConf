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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import space.arim.dazzleconf2.Configuration;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.engine.CallableFn;
import space.arim.dazzleconf2.reflect.MethodId;
import space.arim.dazzleconf2.reflect.MethodMirror;
import space.arim.dazzleconf2.reflect.TypeToken;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AmbiguousMethodTest {

    public interface Config {

        default int trouble() {
            return -1;
        }

        @CallableFn
        default int trouble(int ret) {
            return ret;
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void callTrouble(boolean fromData) {
        Configuration<Config> configuration = Configuration.defaultBuilder(Config.class).build();
        Config config;
        if (fromData) {
            DataTree.Mut dataTree = new DataTree.Mut();
            dataTree.set("trouble", new DataEntry(-2));
            config = configuration.readFrom(dataTree).getOrThrow();
        } else {
            config = configuration.loadDefaults();
        }
        assertEquals(fromData ? -2 : -1, config.trouble());
        assertEquals(2, config.trouble(2));
        assertEquals(3, config.trouble(3));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void invokeTrouble(boolean fromData) throws InvocationTargetException {
        Configuration<Config> configuration = Configuration.defaultBuilder(Config.class).build();
        Config config;
        if (fromData) {
            DataTree.Mut dataTree = new DataTree.Mut();
            dataTree.set("trouble", new DataEntry(-2));
            config = configuration.readFrom(dataTree).getOrThrow();
        } else {
            config = configuration.loadDefaults();
        }
        MethodMirror methodMirror = configuration.getLayout().getMethodMirror();
        MethodMirror.Invoker invoker = methodMirror.makeInvoker(config, Config.class);
        MethodMirror.TypeWalker configTypeWalk = methodMirror.typeWalker(new TypeToken<Config>() {}.getReifiedType());

        MethodId troublePlain = configTypeWalk.getViableMethods().filter(methodId -> methodId.parameterCount() == 0).findAny().orElseThrow();
        assertEquals(fromData ? -2 : -1, invoker.invokeMethod(troublePlain));
        MethodId troubleRet = configTypeWalk.getViableMethods().filter(methodId -> methodId.parameterCount() == 1).findAny().orElseThrow();
        assertEquals(2, invoker.invokeMethod(troubleRet, 2));
        assertEquals(3, invoker.invokeMethod(troubleRet, 3));
    }

    @Test
    public void invokeTroubleNonProxyInstance() throws InvocationTargetException {
        Configuration<Config> configuration = Configuration.defaultBuilder(Config.class).build();
        Config config = new Config() {
            @Override
            public int trouble() {
                return -4;
            }

            @Override
            public int trouble(int ret) {
                return ret * 2;
            }
        };
        MethodMirror methodMirror = configuration.getLayout().getMethodMirror();
        MethodMirror.Invoker invoker = methodMirror.makeInvoker(config, Config.class);
        MethodMirror.TypeWalker configTypeWalk = methodMirror.typeWalker(new TypeToken<Config>() {}.getReifiedType());

        MethodId troublePlain = configTypeWalk.getViableMethods().filter(methodId -> methodId.parameterCount() == 0).findAny().orElseThrow();
        assertEquals(-4, invoker.invokeMethod(troublePlain));
        MethodId troubleRet = configTypeWalk.getViableMethods().filter(methodId -> methodId.parameterCount() == 1).findAny().orElseThrow();
        assertEquals(4, invoker.invokeMethod(troubleRet, 2));
        assertEquals(6, invoker.invokeMethod(troubleRet, 3));
    }

}
