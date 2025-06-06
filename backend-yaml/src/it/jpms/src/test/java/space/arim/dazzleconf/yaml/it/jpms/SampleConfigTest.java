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

package space.arim.dazzleconf.backend.yaml.it.jpms;

import org.junit.jupiter.api.Test;
import space.arim.dazzleconf2.Configuration;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.StringRoot;
import space.arim.dazzleconf2.engine.liaison.SubSection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SampleConfigTest {

    @Test
    public void loadDefaults() {
        Configuration<SampleConfig> configuration = Configuration.defaultBuilder(SampleConfig.class)
                .build();
        SampleConfig defaults = configuration.loadDefaults();
        assertEquals("success", defaults.helloJpms());
        assertTrue(defaults.areWeVisible().shouldBe());
    }

    @Test
    public void writeToBackend() {
        StringRoot stringRoot = new StringRoot("");
        Backend backend = new space.arim.dazzleconf.backend.yaml.YamlBackend(stringRoot);
        DataTree.Mut outputTree = new DataTree.Mut();
        Configuration.defaultBuilder(SampleConfig.class).build().writeTo(new SampleConfig() {
            @Override
            public String helloJpms() {
                return "wrote helloJpms";
            }

            @Override
            public @SubSection Visible areWeVisible() {
                return () -> true;
            }
        }, outputTree);
        if (backend != null) {
            backend.write(Backend.Document.simple(outputTree));
            assertTrue(stringRoot.readString().contains("wrote helloJpms"));
        }
    }
}
