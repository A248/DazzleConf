/*
 * DazzleConf
 * Copyright © 2021 Anand Beh
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

import space.arim.dazzleconf.annote.SubSection;
import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.internal.DefinitionReader;
import space.arim.dazzleconf.internal.util.MethodUtil;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ReturnTypeCreation {

	private final DefinitionReader<?> reader;
	private final TypeInfo<?> returnTypeInfo;

	public ReturnTypeCreation(DefinitionReader<?> reader, TypeInfo<?> returnTypeInfo) {
		this.reader = reader;
		this.returnTypeInfo = returnTypeInfo;
	}

	public ReturnType<?> create(Method method) {
		try {
			return createUsingFakeGenerics(method);
		} catch (IllDefinedConfigException ex) {
			// Rewrap with additional context
			throw new IllDefinedConfigException(
					"Unable to analyze return type of " + MethodUtil.getQualifiedName(method), ex);
		}
	}
	
	private <F1, F2 extends Collection<F1>> ReturnType<?> createUsingFakeGenerics(Method method) {
		if (CollectionKind.isCollectionOrSubclass(returnTypeInfo)) {
			@SuppressWarnings("unchecked")
			TypeInfo<F2> casted = (TypeInfo<F2>) returnTypeInfo;
			return makeCollectionReturnType(casted);
		}
		if (returnTypeInfo.rawType().equals(Map.class)) {
			@SuppressWarnings("unchecked")
			TypeInfo<Map<F1, F2>> casted = (TypeInfo<Map<F1, F2>>) returnTypeInfo;
			return makeMapReturnType(casted);
		}
		return makeSimpleReturnType(returnTypeInfo, method.isAnnotationPresent(SubSection.class));
	}

	private void checkCollectionTypeItselfNotSubSectionAnnotated() {
		if (returnTypeInfo.isAnnotationPresent(SubSection.class)) {
			throw new IllDefinedConfigException("@SubSection can be placed on collection elements or map values, " +
					"but it cannot be placed on Collection, List, Set, or Map directly");
		}
	}

	private List<TypeInfo<?>> getNonEmptyGenericParameters() {
		List<TypeInfo<?>> typeArguments = returnTypeInfo.arguments();
		if (typeArguments.isEmpty()) {
			String typeName = returnTypeInfo.rawType().getSimpleName();
			throw new IllDefinedConfigException(typeName + " must have generic parameters. Raw types cannot be used");
		}
		return typeArguments;
	}

	private <E, R extends Collection<E>> CollectionReturnType<E, ?> makeCollectionReturnType(TypeInfo<R> returnTypeInfo) {
		checkCollectionTypeItselfNotSubSectionAnnotated();
		List<TypeInfo<?>> typeArguments = getNonEmptyGenericParameters();
		@SuppressWarnings("unchecked")
		TypeInfo<E> elementTypeInfo = (TypeInfo<E>) typeArguments.get(0);
		if (elementTypeInfo.isAnnotationPresent(SubSection.class)) {
			return new SubSectionCollectionReturnType<>(returnTypeInfo, reader.createChildDefinition(elementTypeInfo));
		} else {
			return new SimpleCollectionReturnType<>(returnTypeInfo);
		}
	}

	private <K, V> MapReturnType<K, V> makeMapReturnType(TypeInfo<Map<K, V>> returnTypeInfo) {
		checkCollectionTypeItselfNotSubSectionAnnotated();
		List<TypeInfo<?>> typeArguments = getNonEmptyGenericParameters();
		if (typeArguments.get(0).isAnnotationPresent(SubSection.class)) {
			throw new IllDefinedConfigException("@SubSection can be placed on map values, but not map keys");
		}
		@SuppressWarnings("unchecked")
		TypeInfo<V> valueTypeInfo = (TypeInfo<V>) typeArguments.get(1);
		if (valueTypeInfo.isAnnotationPresent(SubSection.class)) {
			return new SubSectionMapReturnType<>(returnTypeInfo, reader.createChildDefinition(valueTypeInfo));
		} else {
			return new SimpleMapReturnType<>(returnTypeInfo);
		}
	}

	private <T> ReturnType<T> makeSimpleReturnType(TypeInfo<T> returnTypeInfo, boolean forceSubSection) {
		if (forceSubSection || returnTypeInfo.isAnnotationPresent(SubSection.class)) {
			return new SimpleSubSectionReturnType<>(returnTypeInfo, reader.createChildDefinition(returnTypeInfo));
		} else {
			return new SimpleTypeReturnType<>(returnTypeInfo);
		}
	}
}
