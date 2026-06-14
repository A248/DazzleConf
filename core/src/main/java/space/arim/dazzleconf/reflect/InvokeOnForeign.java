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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

final class InvokeOnForeign<I> extends InvokeBase<I> {

    private final ReflectHandleCache.ForType handleCache;

    InvokeOnForeign(I receiver, Class<I> declaringType, ReflectHandleCache.ForType handleCache) {
        super(receiver, declaringType);
        this.handleCache = handleCache;
    }

    @Override
    public @Nullable Object invokeMethod(@NonNull MethodId methodId, @Nullable Object @Nullable ... arguments) throws Throwable {
        Method method = getCheckedMethod(methodId);
        MethodHandle methodHandle = handleCache.getOrComputeHandle(method);
        return methodHandle.invokeExact((Object) receiver, arguments);
    }
}
