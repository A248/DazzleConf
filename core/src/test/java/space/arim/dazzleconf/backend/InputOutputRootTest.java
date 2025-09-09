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

package space.arim.dazzleconf.backend;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;
import space.arim.dazzleconf2.backend.InputOutputRoot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD;

@TestInstance(PER_METHOD)
public class InputOutputRootTest extends PathBasedRootTest<InputOutputRoot> {

    public InputOutputRootTest(@TempDir Path target) {
        super(target);
    }

    @Override
    InputOutputRoot createRoot(Path file, Charset charset) {
        return new InputOutputRoot(charset) {
            @Override
            public <R> R openInputStream(@NonNull Operation<R, @NonNull InputStream> operation) throws IOException {
                try (InputStream inputStream = Files.newInputStream(file)) {
                    return operation.operateUsing(inputStream);
                }
            }

            @Override
            public <R> R openOutputStream(@NonNull Operation<R, @NonNull OutputStream> operation) throws IOException {
                try (OutputStream outputStream = Files.newOutputStream(file)) {
                    return operation.operateUsing(outputStream);
                }
            }

            @Override
            public boolean dataExists() throws IOException {
                return Files.exists(file);
            }
        };
    }

}
