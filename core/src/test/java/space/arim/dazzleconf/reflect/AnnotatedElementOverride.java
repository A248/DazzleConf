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
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("NullableProblems")
abstract class AnnotatedElementOverride implements AnnotatedElement {

    private final AnnotatedElement inner;
    private final Annotation[] additions;

    AnnotatedElementOverride(AnnotatedElement inner, Annotation...additions) {
        this.inner = inner;
        this.additions = additions;
    }

    abstract boolean filterPrevious(Annotation previous);

    private <A extends Annotation> A[] modifyOriginalArray(A[] original, Consumer<List<A>> addMore) {
        List<A> source = new ArrayList<>(Arrays.asList(original));
        List<A> output = new ArrayList<>();
        for (A elem : source) {
            if (filterPrevious(elem)) {
                output.add(elem);
            }
        }
        addMore.accept(output);
        @SuppressWarnings("unchecked")
        A[] outputArray = (A[]) Array.newInstance(original.getClass().getComponentType(), 0);
        return output.toArray(outputArray);
    }

    private Annotation[] fromBulkArray(Annotation[] original) {
        return modifyOriginalArray(original, (output) -> output.addAll(Arrays.asList(additions)));
    }

    @Override
    public final Annotation[] getAnnotations() {
        return fromBulkArray(inner.getAnnotations());
    }

    @Override
    public final Annotation[] getDeclaredAnnotations() {
        return fromBulkArray(inner.getDeclaredAnnotations());
    }

    private <A extends Annotation> A[] fromAnnoteArrayByType(A[] original, Class<A> queryType) {
        Class<?> queryContainer;
        {
            Repeatable queryTypeRepeatable = queryType.getAnnotation(Repeatable.class);
            queryContainer = queryTypeRepeatable == null ? null : queryTypeRepeatable.value();
        }
        return modifyOriginalArray(original, (output) -> {
            List<Annotation> buffer = new ArrayList<>(Arrays.asList(additions));
            for (Annotation candidate : buffer) {
                if (queryType.isInstance(candidate)) {
                    output.add(queryType.cast(candidate));
                } else if (queryContainer != null && queryContainer.isInstance(candidate)) {
                    Object[] candidateValue;
                    try {
                        Method valueMethod = queryContainer.getMethod("value");
                        candidateValue = (Object[]) valueMethod.invoke(candidate);
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                        throw new IllegalStateException("Unable to access container annotation value()", ex);
                    }
                    for (Object candidateElem : candidateValue) {
                        output.add(queryType.cast(candidateElem));
                    }
                }
            }
        });
    }

    @Override
    public final <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return fromAnnoteArrayByType(inner.getAnnotationsByType(annotationClass), annotationClass);
    }

    @Override
    public final <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
        return fromAnnoteArrayByType(inner.getDeclaredAnnotationsByType(annotationClass), annotationClass);
    }

    @Override
    public final <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        for (Annotation addition : additions) {
            if (addition.annotationType().equals(annotationClass)) {
                return annotationClass.cast(addition);
            }
        }
        T fromOriginal = inner.getAnnotation(annotationClass);
        if (fromOriginal != null && filterPrevious(fromOriginal)) {
            return fromOriginal;
        }
        return null;
    }

    @Override
    public final <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        for (Annotation addition : additions) {
            if (addition.annotationType().equals(annotationClass)) {
                return annotationClass.cast(addition);
            }
        }
        T fromOriginal = inner.getDeclaredAnnotation(annotationClass);
        if (fromOriginal != null && filterPrevious(fromOriginal)) {
            return fromOriginal;
        }
        return null;
    }

    @Override
    public final boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return getAnnotation(annotationClass) != null;
    }

    abstract String toStringContrib();

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' +
                "inner=" + inner +
                ", additions=" + Arrays.toString(additions) +
                toStringContrib() +
                '}';
    }
}
