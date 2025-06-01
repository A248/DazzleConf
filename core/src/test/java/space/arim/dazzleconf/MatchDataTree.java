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

import org.mockito.ArgumentMatcher;
import space.arim.dazzleconf2.backend.DataStreamable;
import space.arim.dazzleconf2.backend.DataTree;

import java.util.Objects;

public final class MatchDataTree implements ArgumentMatcher<DataStreamable> {

    private final DataTree expected;

    public MatchDataTree(DataTree expected) {
        this.expected = expected;
    }

    @Override
    public boolean matches(DataStreamable argument) {
        return Objects.equals(expected, argument.getAsTree());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' + expected + '}';
    }
}
