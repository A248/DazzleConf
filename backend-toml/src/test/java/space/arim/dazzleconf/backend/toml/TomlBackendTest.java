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

package space.arim.dazzleconf.backend.toml;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.dazzleconf.backend.BackendTest;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.StringRoot;

@ExtendWith(MockitoExtension.class)
public class TomlBackendTest extends BackendTest {
    @Override
    protected Backend createBackend() {
        return new TomlBackend(new StringRoot(""));
    }

    @Override
    protected boolean nonStringKeys() {
        return false;
    }
}
