/* 
 * DazzleConf-gson
 * Copyright © 2020 Anand Beh <https://www.arim.space>
 * 
 * DazzleConf-gson is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DazzleConf-gson is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with DazzleConf-gson. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU Lesser General Public License.
 */
package space.arim.dazzleconf.ext.gson;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Options for GSON configurations
 * 
 * @author A248
 *
 */
public final class GsonOptions {

	private final Gson gson;
	private final String pseudoCommentsSuffix;
	private final Charset charset;
	
	GsonOptions(Gson gson, String pseudoCommentsSuffix, Charset charset) {
		this.gson = gson;
		this.pseudoCommentsSuffix = pseudoCommentsSuffix;
		this.charset = charset;
	}
	
	/**
	 * Gets the {@code Gson} instance used
	 * 
	 * @return the {@code Gson} instance
	 */
	public Gson gson() {
		return gson;
	}

	/**
	 * Gets the pseudo comments suffix. See {@link Builder#pseudoCommentsSuffix(String)} 
	 * for a finer description of pseudo comments.
	 *
	 * @return the pseudo comment suffix if enabled, an empty string otherwise
	 */
	public String pseudoCommentsSuffix() {
		return pseudoCommentsSuffix;
	}

	/**
	 * Gets whether pseudo comments are enabled. See {@link Builder#pseudoComments(boolean)}
	 * for a finer description of pseudo comments.
	 * 
	 * @return true if enabled, false otherwise
	 * @deprecated Use {@link #pseudoCommentsSuffix()} which takes into account more than
	 * whether pseudo comments are merely enabled.
	 */
	@Deprecated
	public boolean pseudoComments() {
		return !pseudoCommentsSuffix().isEmpty();
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
		return "GsonOptions [gson=" + gson + ", pseudoCommentsSuffix=" + pseudoCommentsSuffix
				+ ", charset=" + charset + "]";
	}

	/**
	 * Builder of {@code GsonOptions}
	 * 
	 * @author A248
	 *
	 */
	public static class Builder {

		/**
		 * Null for the default gson instance
		 */
		private Gson gson;
		private String pseudoCommentsSuffix = "";
		private Charset charset = StandardCharsets.UTF_8;
		
		public Builder() {
			
		}
		
		/**
		 * Sets the Gson instance to be used. By default pretty printing and lenient mode are enabled
		 * 
		 * @param gson the Gson instance
		 * @return this builder
		 */
		public Builder gson(Gson gson) {
			this.gson = Objects.requireNonNull(gson, "gson");
			return this;
		}
		
		/**
		 * JSON does not support comments normally. This option controls whether to enable "pseudo comments"
		 * to hack comments into generated json. False by default. <br>
		 * <br>
		 * If enabled, a "comment" is added as a string value before the configuration entry which is to be commented.
		 * The key of the "comment" will be that of the key it is supposed to comment on, with "{@literal -comment}"
		 * appended. For example: <br>
		 * <br>
		 * <pre>
		 *   retries-comment: "determines the amount of retries"
		 *   retries: 3
		 * </pre>
		 * 
		 * @param pseudoComments true to enable pseudo comments
		 * @return this builder
		 * @deprecated Use {@link #pseudoCommentsSuffix(String)} which allows customizing the comment suffix.
		 */
		@Deprecated
		public Builder pseudoComments(boolean pseudoComments) {
			return pseudoCommentsSuffix((pseudoComments) ? "-comment" : "");
		}

		/**
		 * JSON does not support comments normally. This option controls whether to enable "pseudo comments"
		 * to hack comments into generated json. False by default. <br>
		 * <br>
		 * If enabled, a "comment" is added as a string value before the configuration entry which is to be commented.
		 * The key of the "comment" will be that of the key it is supposed to comment on, with the comments suffix
		 * appended. For example, using "{@literal -comment}" as the suffix: <br>
		 * <br>
		 * <pre>
		 *   retries-comment: "determines the amount of retries"
		 *   retries: 3
		 * </pre>
		 *
		 * @param pseudoCommentsSuffix the pseudo comments suffix, or an empty string to disable
		 * @return this builder
		 */
		public Builder pseudoCommentsSuffix(String pseudoCommentsSuffix) {
			this.pseudoCommentsSuffix = Objects.requireNonNull(pseudoCommentsSuffix, "pseudoCommentsSuffix");
			return this;
		}

		/**
		 * Sets the charset used by the factory. Default is UTF 8
		 * 
		 * @param charset the charset
		 * @return this builder
		 * @deprecated This method leaked an internal null value
		 * from past versions. The null hostile {@link #encoding(Charset)}
		 * should be used instead.
		 */
		@Deprecated
		public Builder charset(Charset charset) {
			// In a previous version, null defaulted to UTF-8
			return encoding((charset == null) ? StandardCharsets.UTF_8 : charset);
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
		public GsonOptions build() {
			Gson gson = this.gson;
			if (gson == null) {
				// Default Gson
				gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
			}
			return new GsonOptions(gson, pseudoCommentsSuffix, charset);
		}

		@Override
		public String toString() {
			return "GsonOptions.Builder [gson=" + gson + ", pseudoCommentsSuffix=" + pseudoCommentsSuffix
					+ ", charset=" + charset + "]";
		}

	}
	
}
