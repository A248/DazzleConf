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

package space.arim.dazzleconf.ext.snakeyaml;

import java.util.IllegalFormatException;
import java.util.Objects;

/**
 * Determines the way in which comments will be written
 *
 */
public final class CommentMode {

	private final YamlWriter.Factory writerFactory;

	static final String DEFAULT_COMMENT_FORMAT = " # %s";

	CommentMode(YamlWriter.Factory writerFactory) {
		this.writerFactory = writerFactory;
	}

	YamlWriter.Factory writerFactory() {
		return writerFactory;
	}

	/**
	 * Whether this comment mode <i>fully</i> comments, including the comment header
	 * as well as comments on individual entries
	 *
	 * @return whether this mode fully supports comments
	 */
	public boolean supportsComments() {
		return writerFactory().supportsComments();
	}

	/**
	 * Whether this comment mode is the same as another
	 *
	 * @param o the object to determine equality with
	 * @return true if equal, false otherwise
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommentMode that = (CommentMode) o;
		return writerFactory.equals(that.writerFactory);
	}

	@Override
	public int hashCode() {
		return writerFactory.hashCode();
	}

	@Override
	public String toString() {
		return "CommentMode{" +
				"writerFactory=" + writerFactory +
				'}';
	}

	/**
	 * A mode which will write the comment header on the top level configuration,
	 * but which is unable to write any further comments. <br>
	 * <br>
	 * This is the default mode, because it is most compatible with old versions
	 * of snakeyaml, and is highly stable.
	 *
	 * @return a comment mode which writes the top comment header only
	 */
	public static CommentMode headerOnly() {
		return new CommentMode(BasicWriter.Factory.INSTANCE);
	}

	/**
	 * A mode which will use an alternative yaml writer (other than snakeyaml)
	 * to write both the comments of the configuration as well as the entries
	 * themselves. <br>
	 * <br>
	 * This implies full comment support, but note that the alternative yaml writer,
	 * while tested and workable, does not have the same level of stability as
	 * snakeyaml itself.
	 * <br>
	 * <b>The Comment Format</b> <br>
	 * The comment format is used for writing comments. It must be a {@code String.format}
	 * compatible format string. <br>
	 * <br>
	 * By default, comments are formatted as in the example:
	 * ' # Comment here'. This corresponds to the comment format {@code " # %s"} <br>
	 * <br>
	 * <b>Note well:</b> it is caller's responsibility to ensure the comment format results
	 * in valid YAML.
	 *
	 * @param commentFormat the comment format with which to write comments
	 * @return a comment mode using the alternative yaml writer
	 * @throws IllegalFormatException if the comment format is an illegal format string
	 */
	public static CommentMode alternativeWriter(String commentFormat) {
		Objects.requireNonNull(commentFormat, "commentFormat");
		String.format(commentFormat, "dummy comment");
		return new CommentMode(new CommentedWriter.Factory(commentFormat));
	}

	/**
	 * A mode which will use an alternative yaml writer (other than snakeyaml)
	 * to write both the comments of the configuration as well as the entries
	 * themselves. <br>
	 * <br>
	 * This implies full comment support, but note that the alternative yaml writer,
	 * while tested and workable, does not have the same level of stability as
	 * snakeyaml itself. <br>
	 * <br>
	 * Uses the default comment format, i.e. ' # Comment here'
	 *
	 * @return a comment mode using the alternative yaml writer
	 */
	public static CommentMode alternativeWriter() {
		return alternativeWriter(DEFAULT_COMMENT_FORMAT);
	}

	/**
	 * A mode which has full support for comments, taking advantage of functionality
	 * introduced in snakeyaml 1.28. If the version of snakeyaml in use is less
	 * than version 1.28, this option will fail at runtime (an undefined exception
	 * will be thrown at a later time)
	 *
	 * @return a mode which fully supports comments using snakeyaml 1.28 or a later version
	 */
	public static CommentMode fullComments() {
		return new CommentMode(FullWriter.Factory.INSTANCE);
	}

}
