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

import org.yaml.snakeyaml.Yaml;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Options for SnakeYAML configurations
 * 
 * @author A248
 *
 */
public final class SnakeYamlOptions {

	private final Supplier<Yaml> yamlSupplier;
	private final CommentMode commentMode;
	private final boolean useCommentingWriter;
	private final Charset charset;
	
	SnakeYamlOptions(Supplier<Yaml> yamlSupplier, CommentMode commentMode, boolean useCommentingWriter,
					 Charset charset) {
		this.yamlSupplier = yamlSupplier;
		this.commentMode = commentMode;
		this.useCommentingWriter = useCommentingWriter;
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
	 * Gets the comment mode used. The mode exposes no data but may be used
	 * for comparison with other comment modes via {@code equals}
	 *
	 * @return the comment mode
	 */
	public CommentMode commentMode() {
		return commentMode;
	}
	
	/**
	 * Whether the commenting alternative writer should be used. See {@link Builder#useCommentingWriter(boolean)}
	 * for more details.
	 * 
	 * @return whether the alternative yaml writer is enabled
	 * @deprecated The choice of comments is no longer limited to whether the alternate writer is used.
	 * Use {@link CommentMode} instead
	 */
	@Deprecated
	public boolean useCommentingWriter() {
		return useCommentingWriter;
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
		return "SnakeYamlOptions{" +
				"yamlSupplier=" + yamlSupplier +
				", commentMode=" + commentMode +
				", useCommentingWriter=" + useCommentingWriter +
				", charset=" + charset +
				'}';
	}

	/**
	 * Builder of {@code SnakeYamlOptions}
	 * 
	 * @author A248
	 *
	 */
	public static class Builder {
		
		private Supplier<Yaml> yamlSupplier;
		private CommentMode commentMode = CommentMode.headerOnly();
		private boolean useCommentingWriter;
		private Charset charset = StandardCharsets.UTF_8;
		
		public Builder() {
			
		}
		
		/**
		 * Sets the {@code Yaml} supplier for this builder to the specified one. The default is a
		 * supplier which returns a {@code Yaml} instance with block flow style, and comments enabled
		 * if comments are supported.
		 * 
		 * @param yamlSupplier the yaml supplier
		 * @return this builder
		 */
		public Builder yamlSupplier(Supplier<Yaml> yamlSupplier) {
			this.yamlSupplier = Objects.requireNonNull(yamlSupplier, "yamlSupplier");
			return this;
		}

		/**
		 * Sets the comment mode used for writing comments. The available modes
		 * are obtainable via static factory methods in {@link CommentMode}
		 *
		 * @param commentMode the comment mode to use
		 * @return this builder
		 */
		public Builder commentMode(CommentMode commentMode) {
			this.commentMode = Objects.requireNonNull(commentMode, "commentMode");
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
		 * @deprecated Comments are now handled via the comment mode. Use {@link #commentMode(CommentMode)}
		 * with {@code CommentMode.alternativeWriter()} to replace 'true' calls to this method.
		 */
		@Deprecated
		public Builder useCommentingWriter(boolean useCommentingWriter) {
			this.useCommentingWriter = useCommentingWriter;
			commentMode = (useCommentingWriter) ? CommentMode.alternativeWriter() : CommentMode.headerOnly();
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
		public SnakeYamlOptions build() {
			Supplier<Yaml> yamlSupplier = this.yamlSupplier;
			if (yamlSupplier == null) {
				yamlSupplier = DefaultYaml.SUPPLIER;
			}
			return new SnakeYamlOptions(yamlSupplier, commentMode, useCommentingWriter, charset);
		}

		@Override
		public String toString() {
			return "Builder{" +
					"yamlSupplier=" + yamlSupplier +
					", commentMode=" + commentMode +
					", useCommentingWriter=" + useCommentingWriter +
					", charset=" + charset +
					'}';
		}
	}
	
}
