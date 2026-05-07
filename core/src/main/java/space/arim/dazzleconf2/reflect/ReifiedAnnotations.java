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

package space.arim.dazzleconf2.reflect;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * All the reified annotations on the use of a type.
 * <p>
 * In the terminology of the {@link AnnotatedElement} API, this class stores <i>associated</i> annotations. It exposes
 * the capability to compute itself using {@link #computeFrom(java.lang.reflect.AnnotatedElement)}. Thereafter, this
 * class implements equality based on the annotations it extracted, with a bias toward the repeated value of repeatable
 * annotations (ignoring the container).
 * <p>
 * <b>Repeated annotations and single usage</b>
 * <p>
 * If a repeated annotation is used more than once, it will always have a container. However, if a repeated annotation
 * is used <i>just once</i>, it may or may not be used with its container. This class intentionally considers a single
 * usage without a container to be equal to a single usage with the container, according to the equality contract of
 * this class itself.
 * <p>
 * For example, {@code ReifiedAnnotation}s instances for the following two classes {@code A} and {@code B} are
 * considered equal:
 * <pre>
 *     {@code
 *         @Retention(RUNTIME)
 *         public @interface RepeatCont {
 *             Repeat[] value();
 *         }
 *         @Retention(RUNTIME)
 *         @Repeatable(RepeatCont.class)
 *         public @interface Repeat {}
 *
 *         @Repeat
 *         public class A {}
 *         @RepeatCont({@Repeat})
 *         public class B {}
 *     }
 * </pre>
 * Asserting equality:
 * <pre>
 *     {@code
 *         ReifiedAnnotations onA = ReifiedAnnotations.computeFrom(A.class);
 *         ReifiedAnnotations onB = ReifiedAnnotations.computeFrom(B.class);
 *         assert onA.equals(onB);
 *     }
 * </pre>
 */
public final class ReifiedAnnotations {

    /*
    We cannot, in fact, always know the contained type given an arbitrary container. To solve this, we store the
    container type as map key. The map is populated by checking each type and storing repeatable annotations according
    to their map key.

    Each entry follows one of three states:
    1. Non-repeatable annotation: container is not null and holds the instance
    2. Repeatable annotation: container is null but containedValue is not null
    3. Repeatable annotation, stored container: container is not null, containedValue may be not null if cached
     */

    private interface EntryLike extends Comparable<EntryLike> {
        Class<?> containerAnnote();
    }

    private static final class EntryKey implements EntryLike {

        private final Class<?> containerAnnote;

        private EntryKey(Class<?> containerAnnote) {
            this.containerAnnote = containerAnnote;
        }

        @Override
        public Class<?> containerAnnote() {
            return containerAnnote;
        }

        @Override
        public int compareTo(EntryLike o) {
            return Integer.compare(containerAnnote.hashCode(), o.containerAnnote().hashCode());
        }
    }

    private static final class Entry implements EntryLike {

        private final Class<? extends Annotation> containerAnnote;
        private final Annotation container;
        // Being a lazily computed value, this does not have to be volatile
        private Annotation[] containedValue;

        private Entry(Class<? extends Annotation> containerAnnote, Annotation container) {
            this.containerAnnote = containerAnnote;
            this.container = container;
        }

        private static boolean eqAnyOrder(Annotation[] annotes, Annotation[] otherAnnotes) {
            if (annotes.length != otherAnnotes.length) {
                return false;
            }
            for (Annotation annote : annotes) {
                boolean anyEq = false;
                for (Annotation otherAnnote : otherAnnotes) {
                    if (annote.equals(otherAnnote)) {
                        anyEq = true;
                        break;
                    }
                }
                if (!anyEq) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public Class<?> containerAnnote() {
            return containerAnnote;
        }

        // We implement comparable to obtain a stable sort for the lightweight map
        @Override
        public int compareTo(EntryLike o) {
            return Integer.compare(containerAnnote.hashCode(), o.containerAnnote().hashCode());
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Entry)) return false;

            Entry that = (Entry) o;
            if (!containerAnnote.equals(that.containerAnnote)) {
                return false;
            }
            if (container != null && that.container != null) {
                // Hooray! This is refreshing
                return container.equals(that.container);
            }
            return eqAnyOrder(getOrComputeContainedValue(), that.getOrComputeContainedValue());
        }

        @Override
        public int hashCode() {
            return containerAnnote.hashCode();
        }

        private Annotation[] getOrComputeContainedValue() {
            Annotation[] containedValue = this.containedValue;
            if (containedValue != null) {
                return containedValue;
            }
            try {
                Method valueMethod = containerAnnote.getMethod("value");
                valueMethod.setAccessible(true);
                containedValue = (Annotation[]) valueMethod.invoke(container);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                throw new IllegalStateException("Unable to access container annotation value()", ex);
            }
            this.containedValue = containedValue;
            return containedValue;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "containerAnnote=" + containerAnnote.getName() +
                    ", container=" + container +
                    ", containedValue=" + Arrays.toString(containedValue) +
                    '}';
        }
    }

    // Use a memory-lightweight binary map
    private final Entry[] containersToValues;

    private static final ReifiedAnnotations EMPTY = new ReifiedAnnotations(new Entry[0]);

    private ReifiedAnnotations(Entry[] containersToValues) {
        this.containersToValues = containersToValues;
    }

    static {
        // @Repeatable is not inherited (it messes up our getDeclaredAnnotation calls if it is)
        assert !Repeatable.class.isAnnotationPresent(Inherited.class) : "@Repeatable is not @Inherited";
    }

    /**
     * Returns an empty annotations instance
     *
     * @return an instance with no annotations
     */
    public static @NonNull ReifiedAnnotations empty() {
        return EMPTY;
    }

    /**
     * Computes all the annotations from the provided element.
     * <p>
     * <b>Inherited and repeated annotations</b>
     * <p>
     * All annotations whether repeatable or inherited will be considered. If the annotated element is a {@code Class},
     * then annotation types declared on super types will be included if the argument class does not declare them
     * itself.
     * <p>
     * If the element has repeatable annotations, then the returned {@code ReifiedAnnotations} instance will include
     * them. The container annotation will only be included if it is implicitly or explicitly declared; for example, a
     * single usage of a repeatable annotation typically does <i>not</i> cause an implicit container annotation.
     *
     * @param element the annotated element
     * @return its annotations
     */
    public static @NonNull ReifiedAnnotations computeFrom(@NonNull AnnotatedElement element) {
        // 'present' includes inheritable but excludes contained annotations
        Annotation[] allPresent = element.getAnnotations();
        Entry[] containersToValues = new Entry[allPresent.length];
        for (int n = 0; n < allPresent.length; n++) {
            Annotation present = allPresent[n];
            Class<? extends Annotation> annotationType = present.annotationType();
            Repeatable repeatable = annotationType.getDeclaredAnnotation(Repeatable.class);
            if (repeatable == null) {
                // Non-repeatable annotation. Store Annotation in map, simply
                containersToValues[n] = new Entry(annotationType, present);
            } else {
                // Key according to the container type (repeatable.value())
                Entry entry = new Entry(repeatable.value(), null);
                entry.containedValue = element.getAnnotationsByType(annotationType);;
                containersToValues[n] = entry;
            }
        }
        // Stable sort (used by equals() implementation and bin-map lookups)
        Arrays.sort(containersToValues);
        return new ReifiedAnnotations(containersToValues);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!(o instanceof ReifiedAnnotations)) return false;

        ReifiedAnnotations that = (ReifiedAnnotations) o;
        // Thanks to stable sorting in the previous method, we can rely on normal array equality
        return Arrays.equals(containersToValues, that.containersToValues);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(containersToValues);
    }

    /*
    Retrieval methods
     */

    void forEachKnownAnnote(Consumer<Annotation> action) {
        for (Entry entry : containersToValues) {
            if (entry.containedValue == null) {
                action.accept(entry.container);
            } else {
                for (Annotation contained : entry.containedValue) {
                    action.accept(contained);
                }
            }
        }
    }

    private Entry getMatchingEntry(Class<?> lookForContainerType) {
        int idx = Arrays.binarySearch(containersToValues, new EntryKey(lookForContainerType));
        return idx >= 0 ? containersToValues[idx] : null;
    }

    /**
     * Determines whether the given annotation is anywhere present
     *
     * @param annotationClass the annotation to search for
     * @return true if present
     */
    public boolean hasAny(@NonNull Class<? extends Annotation> annotationClass) {
        Repeatable repeatable = annotationClass.getDeclaredAnnotation(Repeatable.class);
        Class<?> lookFor = (repeatable == null) ? annotationClass : repeatable.value();
        Entry entry = getMatchingEntry(lookFor);
        if (entry == null) {
            return false;
        }
        // If non-repeatable, it definitely exists.
        // Otherwise, need to check that the container is non-empty
        return repeatable == null || entry.getOrComputeContainedValue().length != 0;
    }

    /**
     * Gets one instance of the annotation.
     * <p>
     * The rules differ depending on the relation of the annotation to repeatability:
     * <ul>
     *     <li>A regular, non-repeating annotation will be returned if it is present, or {@code null} if none exists.
     *     This is the main purpose for calling this method, and most callers will be interested in non-repeatable annotations.</li>
     *     <li>A repeatable annotation will return the one instance if there is one, {@code null} if there are none, and
     *     throw an exception if multiple exist. To avoid throwing an exception if multiple of the annotation are
     *     present, use {@link #getAll(Class)} instead.</li>
     *     <li>The container for a repeatable annotation may be implicitly declared if more than one of the repeatable
     *     target annotation is present. However, if only one of the repeatable annotation is present, then calling
     *     this method for the container annotation will probably return {@code null}. This method will only return
     *     non-null with respect to the container annotation itself.</li>
     * </ul>
     *
     * @param annotationClass the annotation type to look for
     * @return the annotation if found, or {@code null} if it is not declared / does not exist
     * @throws IllegalStateException if repeatable and more than one annotation of this type exists
     * @param <A> the annotation type
     */
    public <A extends Annotation> @Nullable A getOne(@NonNull Class<A> annotationClass) {
        Repeatable repeatable = annotationClass.getDeclaredAnnotation(Repeatable.class);
        Class<?> lookFor = (repeatable == null) ? annotationClass : repeatable.value();
        Entry entry = getMatchingEntry(lookFor);
        if (entry == null) {
            return null;
        }
        if (repeatable == null) {
            @SuppressWarnings("unchecked")
            A cast = (A) entry.container;
            return cast;
        }
        Annotation[] containedValue = entry.getOrComputeContainedValue();
        switch (containedValue.length) {
            case 0:
                return null;
            case 1:
                @SuppressWarnings("unchecked")
                A cast = (A) containedValue[0];
                return cast;
            default:
                throw new IllegalStateException("More than one contained value for repeated annotation: " + annotationClass);
        }
    }

    /**
     * Gets all the annotations of a certain type.
     * <p>
     * This will include all the <i>associated</i> annotations of this type, in the terminology of {@link AnnotatedElement}.
     * For repeatable annotations, this means an array of more than one element can be returned.
     *
     * @param annotationClass the annotation class
     * @return all such annotations of this type (inherited or repeatable), or an empty array if none exist
     * @param <A> the annotation type
     */
    public <A extends Annotation> @NonNull A @NonNull [] getAll(@NonNull Class<A> annotationClass) {
        @SuppressWarnings("unchecked")
        A[] cast = (A[]) getAllErased(annotationClass);
        return cast;
    }

    private Object[] getAllErased(Class<?> annotationClass) {
        Repeatable repeatable = annotationClass.getDeclaredAnnotation(Repeatable.class);
        Class<?> lookFor = (repeatable == null) ? annotationClass : repeatable.value();
        Entry entry = getMatchingEntry(lookFor);
        if (entry == null) {
            return (Object[]) Array.newInstance(annotationClass, 0);
        }
        if (repeatable == null) {
            Object[] ret = (Object[]) Array.newInstance(annotationClass, 1);
            ret[0] = entry.container;
            return ret;
        }
        return entry.getOrComputeContainedValue();
    }

    @Override
    public @NonNull String toString() {
        return "ReifiedAnnotations{" + Arrays.toString(containersToValues) + '}';
    }
}
