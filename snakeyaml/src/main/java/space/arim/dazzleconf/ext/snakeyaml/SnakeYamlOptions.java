/* 
 * DazzleConf-snakeyaml
 * Copyright © 2020 Anand Beh <https://www.arim.space>
 * 
 * DazzleConf-snakeyaml is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DazzleConf-snakeyaml is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with DazzleConf-snakeyaml. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU Lesser General Public License.
 */
package space.arim.dazzleconf.ext.snakeyaml;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
	private final Charset charset;
	
	SnakeYamlOptions(Builder builder) {
		Supplier<Yaml> yamlSupplier = builder.yamlSupplier;
		this.yamlSupplier = (yamlSupplier != null) ? yamlSupplier : DefaultYamlSupplier.INSTANCE;

		this.useCommentingWriter = builder.useCommentingWriter;

		Charset charset = builder.charset;
		this.charset = (charset != null) ? charset : StandardCharsets.UTF_8;
	}

	private static class DefaultYamlSupplier implements Supplier<Yaml> {

		static final Supplier<Yaml> INSTANCE = new DefaultYamlSupplier();

		private DefaultYamlSupplier() {}

		@Override
		public Yaml get() {
			Representer representer = new Representer();
			representer.setDefaultFlowStyle(FlowStyle.BLOCK);
			return new Yaml(representer);
		}
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
				+ ", charset=" + charset + "]";
	}

	/**
	 * Builder of {@code SnakeYamlOptions}
	 * 
	 * @author A248
	 *
	 */
	public static class Builder {
		
		private Supplier<Yaml> yamlSupplier;
		private boolean useCommentingWriter;
		private Charset charset;
		
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
			this.yamlSupplier = yamlSupplier;
			return this;
		}
		
		/**
		 * SnakeYaml does not support writing comments. This option enables an alternative yaml writer
		 * implementation which has the ability to write comments. False by default.
		 * 
		 * @param useCommentingWriter true to use the commenting alternate writer, false otherwise
		 * @return this builder
		 */
		public Builder useCommentingWriter(boolean useCommentingWriter) {
			this.useCommentingWriter = useCommentingWriter;
			return this;
		}
		
		/**
		 * Builds the options. May be used repeatedly without side effects
		 * 
		 * @return the built options
		 */
		public SnakeYamlOptions build() {
			return new SnakeYamlOptions(this);
		}

		@Override
		public String toString() {
			return "SnakeYamlOptions.Builder [yamlSupplier=" + yamlSupplier + ", useCommentingWriter=" + useCommentingWriter
					+ ", charset=" + charset + "]";
		}
		
	}
	
}
