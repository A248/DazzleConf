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

record SingleAnnote<A extends Annotation>(Class<A> clazz, A annote) {
    ReifiedAnnotations setOne(ReifiedAnnotations annotations) {
        return annotations.setOne(clazz, annote);
    }

    AnnotatedElement setOne(AnnotatedElement element) {
        if (clazz.getAnnotation(Repeatable.class) != null) {
            @SuppressWarnings("unchecked")
            A[] multi = (A[]) Array.newInstance(annote.annotationType(), 1);
            multi[0] = annote;
            return new MultiAnnote<>(clazz, multi).setAll(element);
        }
        if (annote.equals(element.getAnnotation(clazz))) {
            return element;
        }
        return new SetAnnoteOverride<>(element, clazz, annote);
    }
}
