/*
 * DazzleConf
 * Copyright © 2023 Anand Beh
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

package space.arim.dazzleconf.ext.snakeyaml;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.function.Supplier;

final class DefaultYaml {

	static final Supplier<Yaml> SUPPLIER;

	static {
		Supplier<Yaml> supplier;
		try {
			DumperOptions.class.getMethod("setProcessComments", boolean.class);
			supplier  = () -> {
				DumperOptions dumperOptions = new DumperOptions();
				dumperOptions.setProcessComments(true);
				dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
				return new Yaml(dumperOptions);
			};
		} catch (NoSuchMethodException | SecurityException ex) {
			supplier = () -> {
				DumperOptions dumperOptions = new DumperOptions();
				dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
				return new Yaml(dumperOptions);
			};
		}
		SUPPLIER = supplier;
	}

}
