/*
 * DazzleConf
 * Copyright © 2020 Anand Beh
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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.IllegalFormatException;
import java.util.Objects;
import java.util.function.Supplier;

import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

/**
 * Options for SnakeYAML configurations
 * 
 * @author A248
 *
 */
public final class SnakeYamlOptions {

	private final Supplier<Yaml> yamlSupplier;
	private final boolean useCommentingWriter;
	private final String commentFormat;
	private final Charset charset;
	
	SnakeYamlOptions(Supplier<Yaml> yamlSupplier, boolean useCommentingWriter, String commentFormat,
					 Charset charset) {
		this.yamlSupplier = yamlSupplier;
		this.useCommentingWriter = useCommentingWriter;
		this.commentFormat = commentFormat;
		this.charset = charset;
	}

	/**
	 * Gets the yaml supplier used
	 * 
	 * @return the yaml supplier
	 */
	public Supplier<Yaml> yamlSupplier() {
		return yamlSupplier;
	}
	
	/**
	 * Whether the commenting alternative writer should be used. See {@link Builder#useCommentingWriter(boolean)}
	 * for more details.
	 * 
	 * @return whether the alternative yaml writer is enabled
	 */
	public boolean useCommentingWriter() {
		return useCommentingWriter;
	}

	/**
	 * Gets the format of written comments. See {@link Builder#commentFormat(String)} for details.
	 *
	 * @return the comment format
	 */
	public String commentFormat() {
		return commentFormat;
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
		return "SnakeYamlOptions [yamlSupplier=" + yamlSupplier + ", useCommentingWriter=" + useCommentingWriter
				 + ", commentFormat" + commentFormat + ", charset=" + charset + "]";
	}

	/**
	 * Builder of {@code SnakeYamlOptions}
	 * 
	 * @author A248
	 *
	 */
	public static class Builder {
		
		private Supplier<Yaml> yamlSupplier = () -> {
			Representer representer = new Representer();
			representer.setDefaultFlowStyle(FlowStyle.BLOCK);
			return new Yaml(representer);
		};
		private boolean useCommentingWriter;
		private Charset charset = StandardCharsets.UTF_8;
		private String commentFormat = " # %s";
		
		public Builder() {
			
		}
		
		/**
		 * Sets the {@code Yaml} supplier for this builder to the specified one. The default is a
		 * supplier which returns a {@code Yaml} instance with block flow style.
		 * 
		 * @param yamlSupplier the yaml supplier
		 * @return this builder
		 */
		public Builder yamlSupplier(Supplier<Yaml> yamlSupplier) {
			this.yamlSupplier = Objects.requireNonNull(yamlSupplier, "yamlSupplier");
			return this;
		}
		
		/**
		 * SnakeYaml does not support writing comments. This option enables an alternative yaml writer
		 * implementation which has the ability to write comments before each entry. False by default. <br>
		 * <br>
		 * With this option disabled, only the comment header on the top level configuration is written.
		 * 
		 * @param useCommentingWriter true to use the commenting alternate writer, false otherwise
		 * @return this builder
		 */
		public Builder useCommentingWriter(boolean useCommentingWriter) {
			this.useCommentingWriter = useCommentingWriter;
			return this;
		}

		/**
		 * Sets the format used for writing comments. Must be a {@code String.format}
		 * compatible format string. <br>
		 * <br>
		 * By default, comments are formatted as in the example:
		 * ' # Comment here'. This corresponds to the comment format {@code " # %s"} <br>
		 * <br>
		 * <b>Note well,</b> it is caller's responsibility to ensure the comment format results
		 * in valid YAML.
		 *
		 * @param commentFormat the comment format
		 * @return this builder
		 * @throws IllegalFormatException if the comment format is an illegal format string
		 */
		public Builder commentFormat(String commentFormat) {
			Objects.requireNonNull(commentFormat, "commentFormat");
			String.format(commentFormat, "dummy comment");
			this.commentFormat = commentFormat;
			return this;
		}

		/**
		 * Sets the character encoding used by the factory. Default is UTF 8
		 *
		 * @param charset the charset
		 * @return this builder
		 */
		public Builder encoding(Charset charset) {
			this.charset = Objects.requireNonNull(charset, "charset");
			return this;
		}

		/**
		 * Builds the options. May be used repeatedly without side effects
		 * 
		 * @return the built options
		 */
		public SnakeYamlOptions build() {
			return new SnakeYamlOptions(yamlSupplier, useCommentingWriter, commentFormat, charset);
		}

		@Override
		public String toString() {
			return "SnakeYamlOptions.Builder [yamlSupplier=" + yamlSupplier
					+ ", useCommentingWriter=" + useCommentingWriter + ", charset=" + charset + "]";
		}
		
	}
	
}
