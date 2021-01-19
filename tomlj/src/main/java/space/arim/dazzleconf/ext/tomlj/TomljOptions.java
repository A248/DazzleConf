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

package space.arim.dazzleconf.ext.tomlj;

import org.tomlj.TomlVersion;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Options for Tomlj configurations
 *
 */
public final class TomljOptions {

	private final TomlVersion tomlVersion;
	private final Charset charset;

	TomljOptions(TomlVersion tomlVersion, Charset charset) {
		this.tomlVersion = tomlVersion;
		this.charset = charset;
	}

	/**
	 * Gets the toml spec version
	 *
	 * @return the toml version
	 */
	public TomlVersion tomlVersion() {
		return tomlVersion;
	}

	/**
	 * Gets the charset used
	 *
	 * @return the charset
	 */
	public Charset charset() {
		return charset;
	}

	@Override
	public String toString() {
		return "TomljOptions{" +
				"tomlVersion=" + tomlVersion +
				", charset=" + charset +
				'}';
	}

	/**
	 * Builder of tomlj options
	 *
	 */
	public static final class Builder {

		private TomlVersion tomlVersion = TomlVersion.LATEST;
		private Charset charset = StandardCharsets.UTF_8;

		/**
		 * Creates the builder
		 */
		public Builder() {

		}

		/**
		 * Sets the toml spec version used. By default, this is {@code TomlVersion.LATEST}
		 *
		 * @param tomlVersion the toml version to use
		 * @return this builder
		 */
		public Builder tomlVersion(TomlVersion tomlVersion) {
			this.tomlVersion = Objects.requireNonNull(tomlVersion, "tomlVersion");
			return this;
		}

		/**
		 * Sets the character encoding used by the factory. Default is UTF 8
		 *
		 * @param charset the charset
		 * @return this builder
		 */
		public Builder charset(Charset charset) {
			this.charset = Objects.requireNonNull(charset, "charset");
			return this;
		}

		/**
		 * Builds the options. May be used repeatedly without side effects
		 *
		 * @return the built options
		 */
		public TomljOptions build() {
			return new TomljOptions(tomlVersion, charset);
		}

		@Override
		public String toString() {
			return "Builder{" +
					"tomlVersion=" + tomlVersion +
					", charset=" + charset +
					'}';
		}
	}
}
