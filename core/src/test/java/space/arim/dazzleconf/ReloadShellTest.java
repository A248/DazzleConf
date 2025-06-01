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

import org.junit.jupiter.api.Test;
import space.arim.dazzleconf2.Configuration;
import space.arim.dazzleconf2.ReloadShell;
import space.arim.dazzleconf2.engine.CallableFn;

import static org.junit.jupiter.api.Assertions.*;

public class ReloadShellTest {

    public interface Config {

        int integral();

        @CallableFn
        default int compute(int value) {
            return value + 1;
        }
    }

    @Test
    public void nullDelegate() {
        ReloadShell<Config> reloadShell = Configuration.defaultBuilder(Config.class).build().makeReloadShell(null);
        assertNull(reloadShell.getCurrentDelegate());
        Config shell = reloadShell.getShell();
        assertThrows(NullPointerException.class, shell::integral);
        assertThrows(NullPointerException.class, () -> shell.compute(1));
    }

    @Test
    public void delegateTo() {
        Config delegate = new Config() {
            @Override
            public int integral() {
                return 4;
            }

            @Override
            public int compute(int value) {
                return 5 * value;
            }
        };
        ReloadShell<Config> reloadShell = Configuration.defaultBuilder(Config.class).build().makeReloadShell(delegate);
        assertEquals(delegate, reloadShell.getCurrentDelegate());
        Config shell = reloadShell.getShell();
        assertEquals(4, shell.integral());
        assertEquals(20, shell.compute(4));
    }

    @Test
    public void swap() {
        Config first = () -> 4;
        Config second = () -> 5;
        ReloadShell<Config> reloadShell = Configuration.defaultBuilder(Config.class).build().makeReloadShell(null);
        Config shell = reloadShell.getShell();

        reloadShell.setCurrentDelegate(first);
        assertDoesNotThrow(() -> reloadShell.setCurrentDelegate(first));
        assertSame(first, reloadShell.getCurrentDelegate());
        assertEquals(4, shell.integral());
        assertEquals(1, shell.compute(0));

        reloadShell.setCurrentDelegate(second);
        assertSame(second, reloadShell.getCurrentDelegate());
        assertEquals(5, shell.integral());
        assertEquals(1, shell.compute(0));
    }
}
