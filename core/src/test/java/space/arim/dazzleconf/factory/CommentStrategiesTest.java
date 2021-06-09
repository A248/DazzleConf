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

package space.arim.dazzleconf.factory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.annote.ConfComments;
import space.arim.dazzleconf.annote.SubSection;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CommentStrategiesTest {

	private final MapReceiver mapReceiver;

	public CommentStrategiesTest(@Mock MapReceiver mapReceiver) {
		this.mapReceiver = mapReceiver;
	}

	@Test
	public void commentsThroughWrapper() throws IOException {
		var factory = new TransparentWriterFactory<>(Config.class, ConfigurationOptions.defaults(), mapReceiver) {
			@Override
			public boolean supportsCommentsThroughWrapper() {
				return true;
			}
		};
		String valueOne = "val1";
		String subValue = "subVal";
		factory.write(Config.withValues(valueOne, subValue), OutputStream.nullOutputStream());
		verify(mapReceiver).writeMap(Map.of(
				"value", new CommentedWrapper(List.of("top comment"), valueOne),
				"subConfig", Map.of(
						"subValue", new CommentedWrapper(List.of("sub comment", "another sub comment"), subValue))));
	}

	@Test
	public void pseudoComments() throws IOException {
		String suffix = "-comment";
		var factory = new TransparentWriterFactory<>(Config.class, ConfigurationOptions.defaults(), mapReceiver) {
			@Override
			public String pseudoCommentsSuffix() {
				return suffix;
			}
		};
		String valueOne = "val1";
		String subValue = "subVal";
		factory.write(Config.withValues(valueOne, subValue), OutputStream.nullOutputStream());
		verify(mapReceiver).writeMap(Map.of(
				"value" + suffix, "top comment",
				"value", valueOne,
				"subConfig", Map.of(
						"subValue" + suffix, "sub comment\nanother sub comment",
						"subValue", subValue)));
	}

	public interface Config {

		static Config withValues(String value, String subValue) {
			return new Config() {
				@Override
				public String value() { return value; }

				@Override
				public SubConfig subConfig() { return () -> subValue; }
			};
		}

		@ConfComments("top comment")
		String value();

		@SubSection
		SubConfig subConfig();

		interface SubConfig {

			@ConfComments({"sub comment", "another sub comment"})
			String subValue();
		}
	}
}
