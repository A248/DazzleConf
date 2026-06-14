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
import space.arim.dazzleconf.backend.KeyPath;
import space.arim.dazzleconf.engine.Interprocessor;
import space.arim.dazzleconf.engine.NoOutput;
import space.arim.dazzleconf.engine.SerializeContext;
import space.arim.dazzleconf.engine.SerializeOutput;

import java.util.Objects;

abstract class SerContext implements SerializeContext {

    private final ConfigurationDefinition.WriteOptions writeOptions;

    SerContext(ConfigurationDefinition.WriteOptions writeOptions) {
        this.writeOptions = writeOptions;
    }

    abstract KeyPath.Mut getPathContribution();

    @Override
    public @NonNull KeyMapper keyMapper() {
        return writeOptions.keyMapper();
    }

    @Override
    public @NonNull KeyPath keyPath() {
        KeyPath.Mut path = writeOptions.keyPath().intoMut();
        path.addPath(KeyPath.SequenceBoundary.BACK, getPathContribution());
        return path;
    }

    @Override
    public @NonNull Interprocessor getInterprocessor() {
        return writeOptions.getInterprocessor();
    }

    @Override
    public @NonNull SerializeContext deriveContext(@NonNull Object locIdentifier) {
        return new ChildContext(locIdentifier);
    }

    @Override
    public @NonNull SerializeOutput newOutput() {
        return new AsOutput();
    }

    static final class Standalone extends SerContext {

        Standalone(ConfigurationDefinition.WriteOptions writeOptions) {
            super(writeOptions);
        }

        @Override
        KeyPath.Mut getPathContribution() {
            return new KeyPath.Mut();
        }
    }

    static final class AtKey extends SerContext {

        private final String mappedKey;

        AtKey(ConfigurationDefinition.WriteOptions writeOptions, String mappedKey) {
            super(writeOptions);
            this.mappedKey = mappedKey;
        }

        @Override
        KeyPath.Mut getPathContribution() {
            return new KeyPath.Mut(mappedKey);
        }
    }

    final class ChildContext extends SerContext {

        private final Object locIdentifier;

        ChildContext(Object locIdentifier) {
            super(SerContext.this.writeOptions);
            this.locIdentifier = Objects.requireNonNull(locIdentifier, "locIdentifier");
        }

        @Override
        KeyPath.Mut getPathContribution() {
            KeyPath.Mut path = SerContext.this.getPathContribution();
            path.addBack(locIdentifier.toString());
            return path;
        }
    }

    final class AsOutput extends SerContext implements SerializeOutput {

        private Object output;

        AsOutput() {
            super(SerContext.this.writeOptions);
        }

        @Override
        KeyPath.Mut getPathContribution() {
            return SerContext.this.getPathContribution();
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
}
