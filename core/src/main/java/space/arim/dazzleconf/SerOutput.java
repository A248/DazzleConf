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

package space.arim.dazzleconf;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf.backend.DataList;
import space.arim.dazzleconf.backend.DataTree;
import space.arim.dazzleconf.backend.KeyMapper;
import space.arim.dazzleconf.engine.CommentLocation;
import space.arim.dazzleconf.engine.NoOutput;
import space.arim.dazzleconf.engine.SerializeOutput;

import java.util.Objects;

final class SerOutput implements SerializeOutput {

    private Object output;
    private final KeyMapper keyMapper;
    private final ModifyComments modifyComments;

    SerOutput(KeyMapper keyMapper, ModifyComments modifyComments) {
        this.keyMapper = Objects.requireNonNull(keyMapper, "key mapper");
        this.modifyComments = modifyComments;
    }

    @Override
    public @NonNull KeyMapper keyMapper() {
        return keyMapper;
    }

    @Override
    public boolean writeEntryComments(@NonNull CommentLocation location) {
        return modifyComments.writeEntryComments(location);
    }

    @Override
    public void outString(@NonNull String value) {
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
    public void outDataTree(@NonNull DataTree value) {
        output = Objects.requireNonNull(value);
    }

    @Override
    public void outDataList(@NonNull DataList value) {
        output = Objects.requireNonNull(value);
    }

    @Override
    public void outNone() {
        output = NoOutput.INSTANCE;
    }

    @Override
    public void outObjectUnchecked(@NonNull Object value) {
        output = Objects.requireNonNull(value);
    }

    @Override
    public @Nullable Object getAndClearLastOutput() {
        Object output = this.output;
        this.output = null;
        return output;
    }
}
