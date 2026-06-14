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
import java.util.Arrays;
import java.util.Objects;

final class MultiAnnote<A extends Annotation> {

    private final Class<A> clazz;
    private final Class<? extends Annotation> container;
    private final A[] annotes;

    @SafeVarargs
    MultiAnnote(Class<A> clazz, A...annotes) {
        this.clazz = clazz;
        this.annotes = annotes;
        Repeatable repeatable = Objects.requireNonNull(clazz.getAnnotation(Repeatable.class), "non-repeatable annotations not allowed");
        container = repeatable.value();
    }

    ReifiedAnnotations setAll(ReifiedAnnotations annotations) {
        return annotations.setAll(clazz, annotes);
    }

    AnnotatedElement setAll(AnnotatedElement element) {
        if (Arrays.equals(element.getAnnotationsByType(clazz), annotes)) {
            return element;
        }
        return SetAnnoteOverride.withContainedValue(element, container, annotes);
    }

    @Override
    public String toString() {
        return "MultiAnnote{" +
                "clazz=" + clazz.getName() +
                ", container=" + container.getName() +
                ", annotes=" + Arrays.toString(annotes) +
                '}';
    }
}
