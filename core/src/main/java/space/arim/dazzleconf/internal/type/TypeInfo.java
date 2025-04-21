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

package space.arim.dazzleconf.internal.type;

import space.arim.dazzleconf2.internals.AccessChecking;
import space.arim.dazzleconf2.internals.ImmutableCollections;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Objects;

public final class TypeInfo<T> {

	private final Class<T> rawType;
	private final List<Annotation> annotations;
	private final List<TypeInfo<?>> arguments;

	TypeInfo(Class<T> rawType, List<Annotation> annotations, List<TypeInfo<?>> arguments) {
		this.rawType = Objects.requireNonNull(rawType, "raw type");
		this.annotations = ImmutableCollections.listOf(annotations);
		this.arguments = ImmutableCollections.listOf(arguments);
	}

	public Class<T> rawType() {
		return rawType;
	}

	public boolean isTypeAccessible() {
		return AccessChecking.isAccessible(rawType);
	}

	public List<Annotation> annotations() {
		return annotations;
	}

	/**
	 * Gets whether an annotation is present, either on the use of this type, or on
	 * the declaration of this type in its class file. <br>
	 * <br>
	 * Does not include annotations which may be present on any type arguments.
	 *
	 * @param annotationClass the annotation type
	 * @return whether the annotation is directly present
	 */
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		if (rawType().isAnnotationPresent(annotationClass)) {
			return true;
		}
		for (Annotation annotation : annotations) {
			if (annotation.annotationType().equals(annotationClass)) {
				return true;
			}
		}
		return false;
	}

	public List<TypeInfo<?>> arguments() {
		return arguments;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TypeInfo<?> typeInfo = (TypeInfo<?>) o;
		return rawType.equals(typeInfo.rawType)
				&& annotations.equals(typeInfo.annotations)
				&& arguments.equals(typeInfo.arguments);
	}

	@Override
	public int hashCode() {
		int result = rawType.hashCode();
		result = 31 * result + annotations.hashCode();
		result = 31 * result + arguments.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "TypeInfo{" +
				"rawType=" + rawType +
				", annotations=" + annotations +
				", arguments=" + arguments +
				'}';
	}
}
