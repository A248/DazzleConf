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

import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.backend.Printable;
import space.arim.dazzleconf2.internals.lang.LibraryLang;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

/**
 * Usable implementation of {@link ErrorPrint} that formats the error message in a language-independent fashion.
 * <p>
 * Note this implementation requires at least one error context passed in the list {@link #onError(List)}.
 * If the list is empty, an {@code IllegalArgumentException} will be thrown.
 *
 */
public final class StandardErrorPrint implements ErrorPrint {

    private final ErrorPrint.Output output;

    /**
     * Creates from the given output
     *
     * @param output where to send messages
     */
    public StandardErrorPrint(@NonNull Output output) {
        this.output = Objects.requireNonNull(output);
    }

    @Override
    public void onError(@NonNull List<@NonNull ErrorContext> errorContexts) {
        if (errorContexts.isEmpty()) {
            // See class javadoc. We're not really prepared to handle this
            throw new IllegalArgumentException("Error context list is empty");
        }
        output.output(new Printable.Abstract() {
            @Override
            public void printTo(@NonNull Appendable output) throws IOException {

                LibraryLang lang = LibraryLang.Accessor.access(errorContexts.get(0), ErrorContext::getLocale);

                output.append('\n');
                output.append(lang.errorIntro());
                output.append('\n');
                output.append(lang.errorContext());

                int errorCount = errorContexts.size();
                int cap = Integer.min(4, errorCount);
                for (int n = 0; n < cap; n++) {
                    ErrorContext currentError = errorContexts.get(n);
                    output.append("\n  ");
                    // 1. Entry path
                    boolean pathOrLineNumber = false;
                    KeyPath path = currentError.query(ErrorContext.ENTRY_PATH);
                    if (path != null && !path.isEmpty()) {
                        path.printTo(output);
                        pathOrLineNumber = true;
                    }
                    // 2. Line number
                    Integer lineNumber = currentError.query(ErrorContext.LINE_NUMBER);
                    if (lineNumber != null) {
                        if (pathOrLineNumber) {
                            output.append(' ');
                            output.append('@');
                            output.append(' ');
                        }
                        output.append(lang.line());
                        output.append(' ');
                        output.append(Integer.toString(lineNumber));
                        pathOrLineNumber = true;
                    }
                    // 3. Main message
                    if (pathOrLineNumber) {
                        output.append(':');
                        output.append(' ');
                    }
                    currentError.mainMessage().printTo(output);
                    // 4. Backend message
                    Printable backendMessage = currentError.query(ErrorContext.BACKEND_MESSAGE);
                    if (backendMessage != null) {
                        output.append(':');
                        output.append(' ');
                        backendMessage.printTo(output);
                        // 5. Syntax linter
                        URL syntaxLinter = currentError.query(ErrorContext.SYNTAX_LINTER);
                        if (syntaxLinter != null) {
                            output.append("\n  ");
                            output.append(lang.syntaxInvalidPleaseTryAt(syntaxLinter));
                        }
                    }
                }
                if (cap != errorCount) {
                    output.append("\n  ");
                    output.append('(');
                    output.append('+');
                    output.append(lang.more(errorCount - cap));
                    output.append(')');
                }
                output.append('\n');
            }
        });
    }
}
