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

package space.arim.dazzleconf.reflect;

import space.arim.dazzleconf.internals.ImmutableCollections;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ProxyHandlerToValues<I> extends ProxyHandler<I> {

    // Methods returning fixed values with no parameters (except receiver)
    private final HashMap<String, Object> dataValues;
    // Methods based on handles - whether default implementations or fixed returns with unused parameters
    private final HashMap<Method, MethodHandle> implMethods;
    // Methods returning fixed values for methods with unused parameters. Used for equality exclusively
    private final List<Map.Entry<MethodId, Object>> multiParamFixedMethods;

    ProxyHandlerToValues(Class<I> iface, HashMap<String, Object> dataValues, HashMap<Method, MethodHandle> implMethods,
                         List<Map.Entry<MethodId, Object>> multiParamFixedMethods) {
        super(iface);
        /*
        Sort in a deterministic (but arbitrary) fashion for multiParamFixedMethods. This lets us implement equality.
        A list is a more compact data structure than a map or set, as we don't need lookup capabilities.
         */
        Comparator<MethodId> methodIdComparator = multiParamFixedMethodSorter();
        multiParamFixedMethods.sort((o1, o2) -> {
            return methodIdComparator.compare(o1.getKey(), o2.getKey());
        });
        this.dataValues = dataValues;
        this.implMethods = implMethods;
        this.multiParamFixedMethods = ImmutableCollections.listOf(multiParamFixedMethods);
    }

    private static int compareTypes(ReifiedType ty1, ReifiedType ty2) {
        /*
        We can use raw types (and their class names) to distinguish binary signatures. It's not possible for the same
        class to be loaded with a name, twice. Then it couldn't be used within a single interface.
         */
        Class<?> rawType1 = ty1.rawType();
        Class<?> rawType2 = ty2.rawType();
        if (rawType1.equals(rawType2)) {
            return 0;
        }
        int hashComp = rawType1.hashCode() - rawType2.hashCode();
        if (hashComp == 0) {
            hashComp = rawType1.getName().compareTo(rawType2.getName());
            assert hashComp != 0 : "class with same name can't exist in hierarchy of visible signatures";
        }
        return hashComp;
    }

    private static Comparator<MethodId> multiParamFixedMethodSorter() {
        return (id1, id2) -> {
            int compareName = id1.name().compareTo(id2.name());
            if (compareName != 0) {
                return compareName;
            }
            int retComp = compareTypes(id1.returnType(), id2.returnType());
            if (retComp != 0) {
                return retComp;
            }
            if (id1.parameterCount() != id2.parameterCount()) {
                return Integer.compare(id1.parameterCount(), id2.parameterCount());
            }
            ReifiedType[] id1Params = id1.parameters();
            ReifiedType[] id2Params = id2.parameters();
            for (int n = 0; n < id1Params.length; n++) {
                int paramComp = compareTypes(id1Params[n], id2Params[n]);
                if (paramComp != 0) {
                    return paramComp;
                }
            }
            return 0;
        };
    }

    @Override
    Object fastPathNoParams(String methodName) {
        return dataValues.get(methodName);
    }

    @Override
    Object implInvoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object dataValue;
        if (method.getParameterCount() == 0 && (dataValue = dataValues.get(method.getName())) != null) {
            return dataValue;
        }
        MethodHandle methodHandle = implMethods.get(method);
        // We shouldn't receive a null handle. If we do, it means the method yield we were provided was lacking
        // We could throw an exception, but that might slow down the compiled method. So just use an assert.
        assert methodHandle != null : "Bad proxy; incomplete data";
        return methodHandle.invokeExact(proxy, args);
    }

    @Override
    boolean implEquals(Object ourProxy, Object otherProxy, ProxyHandler<?> otherHandler) {
        if (otherHandler instanceof ProxyHandlerToValues) {
            ProxyHandlerToValues<?> that = (ProxyHandlerToValues<?>) otherHandler;
            // No need to add default method implementations. They are the mirror of what is left out here
            return dataValues.equals(that.dataValues) && multiParamFixedMethods.equals(that.multiParamFixedMethods);
        }
        if (otherHandler instanceof ProxyHandlerToDelegate) {
            // Invert direction => unwrap the delegate
            return otherHandler.implEquals(otherProxy, ourProxy, this);
        }
        if (otherHandler instanceof ProxyHandlerToEmpty) {
            // Only possibility is that all methods are default, see reflection service documentation
            return dataValues.isEmpty() && multiParamFixedMethods.isEmpty();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    int implHashCode() {
        // IMPORTANT - See equality contract
        // If dataValues.isEmpty() and multiParamFixedMethods.isEmpty(), make sure iface.hashCode() is the result
        int result = dataValues.hashCode();
        result = 31 * result + (multiParamFixedMethods.isEmpty() ? 0 : multiParamFixedMethods.hashCode());
        result = 31 * result + iface.hashCode();
        return result;
    }

    @Override
    void implToString(StringBuilder output) {
        String commaSep = ", ";
        output.append(commaSep)
                .append("dataValues").append('=').append(dataValues)
                .append(commaSep)
                .append("implMethods.size()").append('=').append(implMethods.size())
                .append(commaSep)
                .append("multiParamFixedMethods").append('=').append(multiParamFixedMethods);
    }
}
