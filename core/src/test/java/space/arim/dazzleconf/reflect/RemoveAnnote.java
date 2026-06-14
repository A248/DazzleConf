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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

record RemoveAnnote<A extends Annotation>(Class<A> clazz, Predicate<A> removeIf) {

    ReifiedAnnotations removeIf(ReifiedAnnotations annotations) {
        return annotations.removeIf(clazz, removeIf);
    }

    AnnotatedElement removeIf(AnnotatedElement element) {
        Repeatable repeatable = clazz.getAnnotation(Repeatable.class);
        if (repeatable == null) {
            // Non-repeatable. Remove if the predicate matches
            A original = element.getAnnotation(clazz);
            if (original == null || !removeIf.test(original)) {
                return element;
            }
            return new RemoveAnnoteOverride(element, clazz);
        }
        // Repeatable annotation. May need to construct new container annotation
        Class<? extends Annotation> container = repeatable.value();
        A[] original = element.getAnnotationsByType(clazz);
        List<A> filtered = new ArrayList<>(original.length);
        for (A orig : original) {
            if (!removeIf.test(orig)) {
                filtered.add(orig);
            }
        }
        if (filtered.size() == original.length) {
            return element;
        }
        if (filtered.isEmpty()) {
            // Remove both the annotation and the container
            return new RemoveAnnoteOverride(new RemoveAnnoteOverride(element, container), clazz);
        }
        @SuppressWarnings("unchecked")
        A[] intoArr = (A[]) Array.newInstance(clazz, 0);
        A[] newContainedValue = filtered.toArray(intoArr);
        return SetAnnoteOverride.withContainedValue(element, container, newContainedValue);
    }

}
