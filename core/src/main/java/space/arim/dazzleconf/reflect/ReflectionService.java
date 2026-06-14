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

import java.lang.invoke.MethodHandles;

/**
 * A low-level service responsible for reflective access to arbitrary types.
 * <p>
 * This interface is responsible to create the {@link ReflectionProvider} for a given type. Both this and that interface
 * will together be referred to as the "reflection implementor" in their documentation, or simply "implementor."
 * <p>
 * <b>Type support</b>
 * <p>
 * Note that not all types must be supported by a particular implementation; for example, a reflection service
 * could limit itself to interface types, or to record types. One implementation could be combined with others to
 * support a broader range of types.
 * <p>
 * <b>API status</b>
 * <p>
 * Because of this interface's role as a service provider, implementors may require updating to newer minor versions of
 * the library. New minor versions might leverage the reflection implementor in new ways, to support newer library
 * features. If implementors are not updated, they will still be compatible with the library in a strict sense, as
 * existing code will not break. However, newer features may be disabled or refuse to work.
 */
public interface ReflectionService {

    /**
     * Creates a provider for the given type, accessed with the provided lookup.
     * <p>
     * <b>Lookup</b>
     * <p>
     * A reflection implementor uses the lookup to enable privileged reflection, where needed. For all intents and
     * purposes, implementors can rely on this lookup having full privileged access. For example, the implementor may
     * use it to see the type, <i>invokespecial</i> on one of its methods, or loading or defining classes implementing
     * the type as an interface.
     * <p>
     * By default, the library will pass a lookup representing its own {@code space.arim.dazzleconf} module. If this is
     * not sufficient, the library user is responsible with specifying a custom lookup. A custom lookup will be passed
     * through to this method call by the library.
     * <p>
     * <b>Idempotence</b>
     * <p>
     * Calls to this method with the same {@code type} and {@code lookup} must be functionally idempotent. Furthermore,
     * calls with lookups that are all sufficiently privileged, must also be functionally idempotent.
     * <p>
     * Idempotence holds created providers must behave identically with respect to their methods, including through the
     * equality semantics of instances they generate. This idempotence property means that the reflection provider must
     * be implemented equivalently regardless of the type's generic arguments and the unused privileges of the lookup.
     * <p>
     * To understand an example, consider an empty interface like {@link java.util.RandomAccess}. All lookups have
     * access to this type (it is public and in an exposed module). The reflection provider returned from this method
     * must, for all calls to {@link ReflectionProvider#generateEmpty()}, generate instances which are equal according
     * to {@code equals}, including for instances from providers returned by repeated calls to this method.
     * <pre>
     *     {@code
     *         ReflectionService service = new DefaultReflectionService();
     *         ReflectionProvider<RandomAccess> provider1 = service.newProvider(RandomAccess.class, MethodHandles.lookup());
     *         ReflectionProvider<RandomAccess> provider2 = service.newProvider(RandomAccess.class, MethodHandles.lookup());
     *         ReflectionProvider<RandomAccess> provider3 = service.newProvider(RandomAccess.class, MethodHandles.publicLookup());
     *         RandomAccess instance = provider1.generateEmpty();
     *         assert instance.equals(provider2.generateEmpty());
     *         assert instance.equals(provider3.generateEmpty());
     *         // For an empty interface, using an empty method yield is the same as generateEmpty()
     *         try (MethodYield yield = provider1.newMethodYield()) {
     *             assert instance.equals(provider1.generate(yield));
     *         }
     *     }
     * </pre>
     *
     * @param type the type on which to reflect. Note that unlike other areas of the API, only the raw type is passed.
     *             This ensures that service implementors generate instances based on erased types.
     * @param lookup the lookup to use for privileged access
     * @return the reflection provider
     * @throws UnsupportedOperationException if this reflection service does not support the type
     */
    <I> @NonNull ReflectionProvider<I> newProvider(Class<I> type, MethodHandles.@NonNull Lookup lookup);

    /**
     * Checks whether this reflection implementor produced the specified object.
     * <p>
     * Reflection implementors are encouraged to mark the instances they generate using some unique method. This could
     * be as simple as implementing a local interface as a marker.
     * <p>
     * Note that the <i>instance</i> of this reflection service is not considered, which means that different instances
     * of the same reflection service may return {@code true} for objects produced by each other. It is the
     * implementation <i>mechanism</i> that is relevant to this method.
     *
     * @param instance the object
     * @return true if the reflection implementor produced it, false otherwise
     * @throws UnsupportedOperationException if the reflection implementor does not track its generation
     */
    boolean hasProduced(@NonNull Object instance);

}
