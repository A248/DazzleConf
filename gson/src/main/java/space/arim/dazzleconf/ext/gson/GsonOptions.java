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
	private final boolean pseudoComments;
	private final Charset charset;
	
	GsonOptions(Builder builder) {
		Gson gson = builder.gson;
		this.gson = (gson != null) ? gson : new GsonBuilder().setPrettyPrinting().create();

		this.pseudoComments = builder.pseudoComments;

		Charset charset = builder.charset;
		this.charset = (charset != null) ? charset : StandardCharsets.UTF_8;
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
	 * Gets whether pseudo comments are enabled. See {@link Builder#pseudoComments(boolean)}
	 * for a finer description of pseudo comments.
	 * 
	 * @return true if enabled, false otherwise
	 */
	public boolean pseudoComments() {
		return pseudoComments;
	}
	
	/**
	 * Gets the charset used
	 * 
	 * @return the charset
	 */
	public Charset charset() {
		return charset;
	}
	
	/**
	 * Builder of {@code GsonOptions}
	 * 
	 * @author A248
	 *
	 */
	public static class Builder {
		
		private Gson gson;
		private boolean pseudoComments;
		private Charset charset;
		
		public Builder() {
			
		}
		
		/**
		 * Sets the Gson instance to be used. By default this is an instance with pretty printing enabled
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
		 */
		public Builder pseudoComments(boolean pseudoComments) {
			this.pseudoComments = pseudoComments;
			return this;
		}
		
		/**
		 * Sets the charset used by the factory. Default is UTF 8
		 * 
		 * @param charset the charset
		 * @return this builder
		 */
		public Builder charset(Charset charset) {
			this.charset = charset;
			return this;
		}
		
		/**
		 * Builds the options. May be used repeatedly without side effects
		 * 
		 * @return the built options
		 */
		public GsonOptions build() {
			return new GsonOptions(this);
		}
		
	}
	
}
