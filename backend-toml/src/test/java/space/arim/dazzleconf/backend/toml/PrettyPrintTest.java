/*
 * DazzleConf
 * Copyright © 2026 Anand Beh
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

package space.arim.dazzleconf.backend.toml;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.Configuration;
import space.arim.dazzleconf.backend.Backend;
import space.arim.dazzleconf.backend.DataTree;
import space.arim.dazzleconf.backend.StringRoot;
import space.arim.dazzleconf.engine.Comments;
import space.arim.dazzleconf.engine.liaison.SubSection;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrettyPrintTest {

    public interface Config {

        default List<@SubSection Sorter> sorters() {
            return List.of(new Sorter() {});
        }

        interface Sorter {

            default Criteria criteria() {
                return Criteria.PREFIX_WEIGHT;
            }

            default Order order() {
                return Order.ASCENDING;
            }

            default Type type() {
                return Type.LUCKPERMS;
            }

        }

        @Comments({
                "Groups allow you to apply same options to multiple worlds",
                "Highest group gets applied first and lowest group gets applied last"
        })
        default Map<String, @SubSection Group> groups() {
            return Map.of("example", new Group() {});
        }

        interface Group {

            default String playerListName() {
                return "<luckperms_prefix><player_name>";
            }

            default String regex() {
                return ".*";
            }

            default List<String> footer() {
                return List.of("<rainbow>This is a footer");
            }

            default List<String> header() {
                return List.of("<blue>This is an example", "<red>header with 2 lines");
            }
        }
    }

    private enum Criteria {
        PREFIX_WEIGHT
    }
    private enum Order {
        ASCENDING
    }
    private enum Type {
        LUCKPERMS
    }

    @Test
    @Disabled("This test exists to show the format, but TOML's order is not reliable and thus cannot be tested")
    public void printDefaults() {
        StringRoot stringRoot = new StringRoot("");
        Backend backend = new TomlBackend(stringRoot);
        Configuration<Config> configuration = Configuration.defaultBuilder(Config.class).build();
        Config defaults = configuration.loadDefaults();
        DataTree.Mut dataTree = new DataTree.Mut();
        configuration.writeTo(defaults, dataTree);
        backend.write(Backend.Document.simple(dataTree));
        assertEquals("", stringRoot.readString());
    }
}
