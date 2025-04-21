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

package space.arim.dazzleconf2;

import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.engine.KeyMapper;
import space.arim.dazzleconf2.engine.SerializeDeserialize;
import space.arim.dazzleconf2.engine.SerializeOutput;

import java.util.List;
import java.util.Objects;

final class SerOutput implements SerializeOutput {

    Object output;
    private final KeyMapper keyMapper;

    SerOutput(KeyMapper keyMapper) {
        this.keyMapper = keyMapper;
    }

    @SuppressWarnings("unchecked")
    <V> void forceFeed(SerializeDeserialize<V> serializer, Object value) {
        serializer.serialize((V) value, this);
    }

    @Override
    public KeyMapper keyMapper() {
        return keyMapper;
    }

    @Override
    public void outString(String value) {
        output = Objects.requireNonNull(value);
    }

    @Override
    public void outBoolean(boolean value) {
        output = value;
    }

    @Override
    public void outByte(byte value) {
        output = value;
    }

    @Override
    public void outChar(char value) {
        output = value;
    }

    @Override
    public void outShort(short value) {
        output = value;
    }

    @Override
    public void outInt(int value) {
        output = value;
    }

    @Override
    public void outLong(long value) {
        output = value;
    }

    @Override
    public void outFloat(float value) {
        output = value;
    }

    @Override
    public void outDouble(double value) {
        output = value;
    }

    @Override
    public void outList(List<?> value) {
        // Canonical check is performed in Entry constructor
        output = Objects.requireNonNull(value);
    }

    @Override
    public void outDataTree(DataTree value) {
        output = Objects.requireNonNull(value);
    }
}
