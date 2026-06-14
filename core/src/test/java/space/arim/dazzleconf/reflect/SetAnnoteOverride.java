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

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

class SetAnnoteOverride<A extends Annotation> extends AnnotatedElementOverride {

    private final Class<A> clazz;

    SetAnnoteOverride(AnnotatedElement element, Class<A> clazz, A annote) {
        super(element, annote);
        this.clazz = clazz;
    }

    @Override
    boolean filterPrevious(Annotation previous) {
        Class<? extends Annotation> prevType = previous.annotationType();
        if (prevType.equals(clazz)) {
            return false;
        }
        Repeatable repeatable = prevType.getAnnotation(Repeatable.class);
        return repeatable == null || !repeatable.value().equals(clazz);
    }

    @Override
    String toStringContrib() {
        return ", clazz=" + clazz.getName();
    }

    static <A extends Annotation, B extends Annotation> SetAnnoteOverride<A> withContainedValue(
            AnnotatedElement element, Class<A> containerType, B[] contained
    ) {
        B[] containedCopy = contained.clone();
        A container = containerType.cast(Proxy.newProxyInstance(
                SetAnnoteOverride.class.getClassLoader(),
                new Class[]{containerType},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        return switch (method.getName()) {
                            case "equals" -> proxy == args[0];
                            case "hashCode" -> System.identityHashCode(proxy);
                            case "toString" ->
                                    "SingleAnnoteOverride#withContainedValue(" + containerType + ", " + Arrays.toString(contained) + ')';
                            case "value" -> containedCopy.clone();
                            case "annotationType" -> containerType;
                            default -> throw new UnsupportedOperationException();
                        };
                    }
                }
        ));
        return new SetAnnoteOverride<>(element, containerType, container);
    }
}
