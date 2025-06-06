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

package space.arim.dazzleconf2.backend;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;

/// Inverse of String#translateEscapes
final class EscapeString extends Printable.Abstract {

    private final String val;

    EscapeString(String val) {
        this.val = val;
    }

    @Override
    public void printTo(@NonNull Appendable output) throws IOException {
        output.append('"');
        class ErrorHold {
            IOException error;
        }
        ErrorHold errorHold = new ErrorHold();
        val.codePoints().forEach(character -> {
            if (errorHold.error != null) {
                return;
            }
            try {
                switch (character) {
                    case '"':
                        output.append("\\\"");
                        break;
                    case '\\':
                        output.append("\\\\");
                        break;
                    case '\b':
                        output.append("\\b");
                        break;
                    case '\f':
                        output.append("\\f");
                        break;
                    case '\n':
                        output.append("\\n");
                        break;
                    case '\r':
                        output.append("\\r");
                        break;
                    case '\t':
                        output.append("\\t");
                        break;
                    default:
                        if (Character.isISOControl(character)) {
                            output.append("\\u");
                            String code = Integer.toHexString(character);
                            // Ensure size of 4
                            for (int n = code.length(); n < 4; n++) {
                                output.append('0');
                            }
                            output.append(code);
                        } else if (Character.isBmpCodePoint(character)) {
                            output.append((char) character);
                        } else {
                            for (char ch : Character.toChars(character)) {
                                output.append(ch);
                            }
                        }
                        break;
                }
            } catch (IOException ex) {
                errorHold.error = ex;
            }
        });
        if (errorHold.error != null) {
            throw errorHold.error;
        }
        output.append('"');
    }
}
