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

package space.arim.dazzleconf.internal.error;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import space.arim.dazzleconf.serialiser.URLValueSerialiser;

import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.stream.Stream;

public class StandardErrorArgumentsProvider implements ArgumentsProvider {

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
		var developerErrors = Stream.of(
				DeveloperError.expectedMap(Errors.When.LOAD_CONFIG, "key", new Object()),
				DeveloperError.replacedObject("key", "replaced", "replacement"),
				DeveloperError.noSerializerFound(Errors.When.WRITE_CONFIG, "key", ArgumentsProvider.class),
				DeveloperError.serializerReturnedNull(Errors.When.LOAD_CONFIG, "key", URLValueSerialiser.getInstance())
		);
		var userErrors = Stream.concat(
				Stream.of(
						UserError.sizeTooBig(3, 2),
						UserError.sizeTooSmall(2, 3)),
				Stream.concat(
						Arrays.stream(ElementaryType.values()),
						Stream.of(new EnumType(StandardCopyOption.class))
				).map((type) -> UserError.wrongType(type, new Object()))
		);
		return Stream.concat(developerErrors, userErrors).map(Arguments::of);
	}
}
