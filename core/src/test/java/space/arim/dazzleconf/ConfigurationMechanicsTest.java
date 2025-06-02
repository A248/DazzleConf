/*
 * DazzleConf
 * Copyright © 2025 Anand Beh
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

package space.arim.dazzleconf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf2.Configuration;

import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Covers:
// 1. Key mapping and labels
// 2. Error counts
// 3. Interfaces without defaults
public class ConfigurationMechanicsTest {

    private Configuration<Config> configuration;

    public interface Config {

        boolean method1();

        char methodTwo();

        String method3();

        List<List<String>> methodFour();

        Set<Float> method5();

        StandardCopyOption method6();

    }

    @BeforeEach
    public void setup() {
        configuration = Configuration.defaultBuilder(Config.class).build();
    }

    @Test
    public void getLabels() {
        assertEquals(
                Set.of("method1", "methodTwo", "method3", "methodFour", "method5", "method6"),
                new HashSet<>(configuration.getLayout().getLabels())
        );
    }

    @Test
    public void getLabelsAsStream() {
        assertEquals(
                Set.of("method1", "methodTwo", "method3", "methodFour", "method5", "method6"),
                configuration.getLayout().getLabelsAsStream().collect(Collectors.toSet())
        );
    }
}
