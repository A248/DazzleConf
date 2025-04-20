/* 
 * DazzleConf-core
 * Copyright © 2020 Anand Beh <https://www.arim.space>
 * 
 * DazzleConf-core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DazzleConf-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with DazzleConf-core. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU Lesser General Public License.
 */
package space.arim.dazzleconf.internal;

import space.arim.dazzleconf.annote.ConfComments;
import space.arim.dazzleconf.annote.ConfKey;
import space.arim.dazzleconf.annote.ConfValidator;
import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.internal.type.ReturnType;
import space.arim.dazzleconf.internal.type.ReturnTypeCreation;
import space.arim.dazzleconf.internal.type.SimpleSubSectionReturnType;
import space.arim.dazzleconf.internal.type.TypeInfo;
import space.arim.dazzleconf.internal.type.TypeInfoCreation;
import space.arim.dazzleconf.internal.util.ImmutableCollections;
import space.arim.dazzleconf.internal.util.MethodUtil;
import space.arim.dazzleconf.validator.ValueValidator;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;

class ConfEntryCreation {

	private final DefinitionReader<?> reader;
	private final Method method;
	private final TypeInfoCreation typeInfoCreation;

	ConfEntryCreation(DefinitionReader<?> reader, Method method, TypeInfoCreation typeInfoCreation) {
		this.reader = reader;
		this.method = method;
		this.typeInfoCreation = typeInfoCreation;
	}

	private String getQualifiedMethodName() {
		return MethodUtil.getQualifiedName(method);
	}

	ConfEntry create() {
		if (method.getParameterCount() > 0) {
			throw new IllDefinedConfigException(getQualifiedMethodName() + " should not have parameters");
		}
		TypeInfo<?> returnTypeInfo = typeInfoCreation.create(method.getReturnType());
		if (!returnTypeInfo.isTypeAccessible()) {
			throw new IllDefinedConfigException(
					getQualifiedMethodName() + " has inaccessible return type " + returnTypeInfo.rawType());
		}
		ReturnType<?> returnType = new ReturnTypeCreation(reader, returnTypeInfo).create(method);
		List<String> comments;
		if (returnType instanceof SimpleSubSectionReturnType) {
			// Simple sub-section; use comment header for entry comments
			ConfigurationDefinition<?> configDefinition = ((SimpleSubSectionReturnType<?>) returnType).configDefinition();
			comments = findComments(configDefinition::getHeader);
		} else {
			// Everything else
			comments = findComments(ImmutableCollections::emptyList);
		}
		return new ConfEntry(method, findKey(), comments, returnType, getValidator());
	}

	private ValueValidator getValidator() {
		ConfValidator chosenValidator = method.getAnnotation(ConfValidator.class);
		return (chosenValidator == null) ? null : reader.instantiate(ValueValidator.class, chosenValidator.value());
	}

	private String findKey() {
		ConfKey confKey = method.getAnnotation(ConfKey.class);
		if (confKey != null) {
			String confKeyValue = confKey.value();
			if (!reader.options.dottedPathInConfKey() && confKeyValue.contains(".")) {
				throw new IllDefinedConfigException(
						"Using dotted key paths in @ConfKey is deprecated disabled by defalt in DazzleConf 1.3.0." +
								"Please see the setting ConfigurationOptions.Builder#setDottedPathInConfKey if you" +
								"need to restore compatibility."
				);
			}
			return confKeyValue;
		}
		return method.getName();
	}

	private List<String> findComments(Supplier<List<String>> backupSupplier) {
		ConfComments commentsAnnotation = method.getAnnotation(ConfComments.class);
		if (commentsAnnotation != null) {
			return ImmutableCollections.listOf(commentsAnnotation.value());
		}
		return backupSupplier.get();
	}

}
