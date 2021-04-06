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

package space.arim.dazzleconf.ext.hocon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.error.MissingKeyException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class HoconConfigurationFactoryTest {

    private ConfigurationFactory<Config> factory;
    private Config defaults;
    private ByteArrayOutputStream defaultsOutput;

    @BeforeEach
    public void setup() throws IOException {
        factory = HoconConfigurationFactory.create(Config.class, ConfigurationOptions.defaults());
        defaults = factory.loadDefaults();
        defaultsOutput = new ByteArrayOutputStream();
        factory.write(defaults, defaultsOutput);
    }

    @Test
    public void writeConfig() throws IOException {
        String content = defaultsOutput.toString(StandardCharsets.UTF_8);
        List<String> contentLines = content.lines().collect(Collectors.toUnmodifiableList());
        /*
        Hocon guarantees no order; so either entry in Config may come first
         */
        List<String> entry1 = List.of(
                "# Comment using Hocon",
                "# Another line of comments",
                "some-option=option-value");
        List<String> entry2 = List.of(
                "# Section header",
                "section-one {",
                "    some-flag=false",
                "}");
        if (!contentLines.equals(combineLines(entry1, entry2))
            && contentLines.equals(combineLines(entry2, entry1))) {
            fail("Received unexpected content:\n" + content);
        }
    }

    private static List<String> combineLines(List<String> str1, List<String> str2) {
        List<String> combined = new ArrayList<>(str1.size() + str2.size());
        combined.addAll(str1);
        combined.addAll(str2);
        combined.add(""); // empty line at the end
        return List.copyOf(combined);
    }

    @Test
    public void reloadConfig() throws IOException, InvalidConfigException {
        Config reloaded = assertDoesNotThrow(() -> {
            return factory.load(new ByteArrayInputStream(defaultsOutput.toByteArray()));
        });
        assertEquals(defaults.someOption(), reloaded.someOption());
        assertEquals(defaults.sectionOne().someFlag(), reloaded.sectionOne().someFlag());
    }

    private InputStream streamFor(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "    ", " \n "})
    public void loadEmptyDocument(String emptyString) {
        var stream = streamFor(emptyString);
        assertThrows(MissingKeyException.class, () -> factory.load(stream));
    }

    @Test
    public void loadMissingKeys() {
        var stream = streamFor("some-option: 'some-value'");
        assertThrows(MissingKeyException.class, () -> factory.load(stream));
    }

}
